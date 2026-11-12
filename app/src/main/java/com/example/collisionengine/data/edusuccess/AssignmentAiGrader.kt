package com.example.collisionengine.data.edusuccess

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.util.Base64
import com.example.collisionengine.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class StepVerdict(
    val step: String,
    val isCorrect: Boolean,
    val explanation: String
)

data class AiSubmissionAnalysis(
    val ocrTranscription: String,
    val ocrObservations: String,
    val score: Float,
    val maxMarks: Float,
    val verdict: String,
    val strongPoints: List<String>,
    val weakPoints: List<String>,
    val stepVerdicts: List<StepVerdict>,
    val summaryFeedback: String,
    val ocrModelUsed: String = "meta/llama-3.2-11b-vision-instruct",
    val gradingModelUsed: String = "gpt-4o"
)

/**
 * Two-stage Student Assignment OCR & Grading pipeline:
 * 1) Llama 3.2 Vision Instruct (NVIDIA NIM) extracts handwritten work and OCR corrections.
 * 2) OpenAI GPT evaluates correctness against the assignment question, rubric, and reference answer,
 *    producing score, correct/wrong step breakdown, strong points, and weak points.
 */
object AssignmentAiGrader {

    private fun nvidiaKey(): String =
        BuildConfig.NVIDIA_API_KEY.ifBlank { System.getenv("NVIDIA_API_KEY") ?: "" }

    private fun openAiKey(): String =
        BuildConfig.OPENAI_API_KEY.ifBlank { System.getenv("OPENAI_API_KEY") ?: "" }

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Generates a realistic handwritten-style lined-paper bitmap for testing OCR end-to-end. */
    fun createSampleHandwrittenSheetBitmap(assignment: AssignmentRow?): Bitmap {
        val width = 960
        val height = 620
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#FCFCFA"))

        val linePaint = Paint().apply {
            color = Color.parseColor("#DCE6F2")
            strokeWidth = 2f
        }
        for (y in 90..height step 68) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), linePaint)
        }
        val marginPaint = Paint().apply {
            color = Color.parseColor("#F5B7B1")
            strokeWidth = 3f
        }
        canvas.drawLine(85f, 0f, 85f, height.toFloat(), marginPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        }
        val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 34f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }

        val id = assignment?.assignmentId ?: "A01"
        canvas.drawText("Assignment $id - Student Work", 110f, 65f, titlePaint)

        val lines = when (assignment?.conceptId) {
            "C01" -> listOf(
                "Q: Solve 3x + 7 = 25",
                "Step 1: 3x = 25 - 7 = 18",
                "Step 2: x = 18 / 3 = 6",
                "Check: 3(6) + 7 = 25 (Done)"
            )
            "C04", "C05", "C06" -> listOf(
                "Q: Find |A| for A = [[2, 3], [1, 4]]",
                "Step 1: |A| = (2)(4) + (3)(1)",
                "Step 2: |A| = 8 + 3 = 11",
                "Ans: Determinant = 11"
            )
            else -> listOf(
                "Q: x^2 - 7x + 12 = 0",
                "Step 1: x^2 - 3x - 4x + 12 = 0",
                "Step 2: (x - 3)(x - 4) = 0",
                "Step 3: x = 3, -4  (sign slip)"
            )
        }

        lines.forEachIndexed { idx, line ->
            canvas.drawText(line, 110f, 150f + idx * 96f, inkPaint)
        }
        return bitmap
    }

    private fun bitmapToBase64Jpeg(bitmap: Bitmap): String {
        val maxDim = 1024
        val scale = minOf(1f, maxDim.toFloat() / maxOf(bitmap.width, bitmap.height))
        val target = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap
        }
        val baos = ByteArrayOutputStream()
        target.compress(Bitmap.CompressFormat.JPEG, 85, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Step 1: Call meta/llama-3.2-11b-vision-instruct on NVIDIA NIM to transcribe handwritten steps
     * and identify OCR corrections, strong points, and weak points directly from the image.
     */
    suspend fun runLlamaVisionOcr(
        bitmap: Bitmap,
        assignment: AssignmentRow,
        studentNotes: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val key = nvidiaKey()
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("NVIDIA_API_KEY is not configured in local.properties"))
        }

        try {
            val base64Img = bitmapToBase64Jpeg(bitmap)
            val promptText = buildString {
                append("You are an educational OCR & math-vision assistant. ")
                append("Read the student's uploaded handwritten assignment image carefully.\n")
                append("Question: ${assignment.question}\n")
                if (studentNotes.isNotBlank()) {
                    append("Student extra note: $studentNotes\n")
                }
                append("Return ONLY valid JSON with two string fields:\n")
                append("{\n")
                append("  \"transcription\": \"verbatim step-by-step math/text seen in the image\",\n")
                append("  \"ocr_observations\": \"handwriting clarity, normalized notation corrections, and initial strong/weak observations\"\n")
                append("}")
            }

            val contentArray = JSONArray()
                .put(JSONObject().put("type", "text").put("text", promptText))
                .put(
                    JSONObject()
                        .put("type", "image_url")
                        .put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$base64Img"))
                )

            val payload = JSONObject()
                .put("model", "meta/llama-3.2-11b-vision-instruct")
                .put(
                    "messages",
                    JSONArray().put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", contentArray)
                    )
                )
                .put("max_tokens", 600)
                .put("temperature", 0.2)

            val conn = (URL("https://integrate.api.nvidia.com/v1/chat/completions").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $key")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 20000
                readTimeout = 45000
                doOutput = true
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""
            conn.disconnect()

            if (code !in 200..299) {
                return@withContext Result.failure(RuntimeException("Llama Vision OCR HTTP $code: $body"))
            }

            val rawContent = JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            val parsed = parseJsonLoose(rawContent)
            val transcription = parsed?.optString("transcription")?.takeIf { it.isNotBlank() } ?: rawContent
            val observations = parsed?.optString("ocr_observations")?.takeIf { it.isNotBlank() }
                ?: "OCR transcribed via Llama 3.2 Vision Instruct."
            Result.success(transcription to observations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Step 2: Use OpenAI GPT to check correct/wrong answers against the reference solution & rubric,
     * assign marks, and extract clear Strong Points and Weak Points.
     */
    suspend fun gradeWithOpenAi(
        assignment: AssignmentRow,
        rubrics: List<RubricRow>,
        ocrTranscription: String,
        ocrObservations: String
    ): Result<AiSubmissionAnalysis> = withContext(Dispatchers.IO) {
        val key = openAiKey()
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("OPENAI_API_KEY is not configured in local.properties"))
        }

        val rubricText = if (rubrics.isEmpty()) {
            "Method & Accuracy: ${assignment.maxMarks} marks total"
        } else {
            rubrics.joinToString("\n") { "- ${it.criterion} (${it.maxMarks} marks): ${it.description}" }
        }

        val systemPrompt = """
            You are an expert Mathematics and Physics teacher grading a student's OCR-scanned assignment.
            Compare the student's transcribed solution against the official question, reference answer, and rubric.
            Identify exact strong points (what the student got right) and weak points (mistakes/misconceptions),
            verify each step as Correct or Wrong, and compute a fair score out of max_marks.
            Respond strictly with valid JSON matching this schema:
            {
              "score": number,
              "verdict": "Correct" | "Partially Correct" | "Incorrect",
              "strong_points": ["..."],
              "weak_points": ["..."],
              "step_verdicts": [{"step": "...", "is_correct": true|false, "explanation": "..."}],
              "summary_feedback": "concise actionable teacher feedback"
            }
        """.trimIndent()

        val userPrompt = """
            Subject: ${assignment.subject}
            Question: ${assignment.question}
            Reference Answer: ${assignment.referenceAnswer}
            Max Marks: ${assignment.maxMarks}
            Rubric:
            $rubricText

            Llama 3.2 Vision OCR Transcription:
            $ocrTranscription

            OCR Notes:
            $ocrObservations
        """.trimIndent()

        // Try modern OpenAI models in order in case an account doesn't have one alias enabled
        val candidateModels = listOf("gpt-4o", "gpt-4o-mini", "gpt-4.1")
        var lastError: String = "OpenAI request failed"

        for (modelName in candidateModels) {
            try {
                val payload = JSONObject()
                    .put("model", modelName)
                    .put(
                        "messages",
                        JSONArray()
                            .put(JSONObject().put("role", "system").put("content", systemPrompt))
                            .put(JSONObject().put("role", "user").put("content", userPrompt))
                    )
                    .put("response_format", JSONObject().put("type", "json_object"))
                    .put("temperature", 0.2)
                    .put("max_tokens", 700)

                val conn = (URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $key")
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 20000
                    readTimeout = 45000
                    doOutput = true
                }

                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                val code = conn.responseCode
                val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.use { it.readText() } ?: ""
                conn.disconnect()

                if (code == 404) {
                    lastError = "Model $modelName unavailable ($code)"
                    continue
                }
                if (code !in 200..299) {
                    return@withContext Result.failure(RuntimeException("OpenAI HTTP $code: $body"))
                }

                val rawContent = JSONObject(body)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                val json = parseJsonLoose(rawContent) ?: JSONObject()
                val score = json.optDouble("score", 0.0).toFloat().coerceIn(0f, assignment.maxMarks)
                val verdict = json.optString("verdict", "Partially Correct")
                val strong = jsonArrayToList(json.optJSONArray("strong_points"))
                val weak = jsonArrayToList(json.optJSONArray("weak_points"))
                val stepsJson = json.optJSONArray("step_verdicts")
                val steps = mutableListOf<StepVerdict>()
                if (stepsJson != null) {
                    for (i in 0 until stepsJson.length()) {
                        val obj = stepsJson.optJSONObject(i) ?: continue
                        steps.add(
                            StepVerdict(
                                step = obj.optString("step", "Step ${i + 1}"),
                                isCorrect = obj.optBoolean("is_correct", false),
                                explanation = obj.optString("explanation", "")
                            )
                        )
                    }
                }
                val summary = json.optString("summary_feedback", "")
                    .ifBlank { "Graded against ${assignment.assignmentTitle} rubric." }

                val conceptTopic = EduDataRepository.getConceptById(assignment.conceptId)?.topic
                    ?: assignment.subject

                return@withContext Result.success(
                    AiSubmissionAnalysis(
                        ocrTranscription = ocrTranscription,
                        ocrObservations = ocrObservations,
                        score = score,
                        maxMarks = assignment.maxMarks,
                        verdict = verdict,
                        strongPoints = strong.ifEmpty { listOf("Attempted structured solution steps") },
                        weakPoints = weak,
                        stepVerdicts = steps,
                        summaryFeedback = summary,
                        gradingModelUsed = modelName
                    )
                )
            } catch (e: Exception) {
                lastError = e.message ?: e.toString()
            }
        }

        return@withContext Result.failure(RuntimeException(lastError))
    }

    private fun parseJsonLoose(text: String): JSONObject? {
        return try {
            val cleaned = text
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            if (start >= 0 && end > start) {
                JSONObject(cleaned.substring(start, end + 1))
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun jsonArrayToList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val item = arr.optString(i).trim()
            if (item.isNotBlank()) list.add(item)
        }
        return list
    }
}

