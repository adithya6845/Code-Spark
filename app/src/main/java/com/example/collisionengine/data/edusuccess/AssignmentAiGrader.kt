package com.example.collisionengine.data.edusuccess

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
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
    val status: String, // "Correct" | "Partial" | "Wrong"
    val explanation: String,
    val yStartFraction: Float = 0.1f,
    val yEndFraction: Float = 0.25f
) {
    val isCorrect: Boolean get() = status.equals("Correct", ignoreCase = true)
}

data class UploadedFileMeta(
    val fileName: String,
    val sizeText: String
)

data class AiSubmissionAnalysis(
    val ocrTranscription: String,
    val ocrObservations: String,
    val score: Float,
    val maxMarks: Float,
    val scorePercent: Int,
    val correctMarks: Float,
    val partialMarks: Float,
    val wrongMarks: Float,
    val verdict: String,
    val strongPoints: List<String>,
    val weakPoints: List<String>,
    val weakConcepts: List<String>,
    val stepVerdicts: List<StepVerdict>,
    val summaryFeedback: String,
    val ocrModelUsed: String = "meta/llama-3.2-11b-vision-instruct",
    val gradingModelUsed: String = "gpt-4o"
)

/**
 * Two-stage Student Assignment OCR & Grading pipeline:
 * 1) Llama 3.2 Vision Instruct (NVIDIA NIM) extracts handwritten work, OCR lines, and region locations.
 * 2) OpenAI GPT evaluates Correct / Partial / Wrong marks, strong points, weak points, and weak concepts.
 */
object AssignmentAiGrader {

    private const val PREFS_NAME = "edusuccess_ai_keys"
    private const val KEY_NVIDIA = "nvidia_api_key"
    private const val KEY_OPENAI = "openai_api_key"

    fun getNvidiaKey(context: Context): String {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NVIDIA, null)?.trim()
        return saved?.takeIf { it.isNotBlank() }
            ?: BuildConfig.NVIDIA_API_KEY.ifBlank { System.getenv("NVIDIA_API_KEY") ?: "" }
    }

    fun getOpenAiKey(context: Context): String {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_OPENAI, null)?.trim()
        return saved?.takeIf { it.isNotBlank() }
            ?: BuildConfig.OPENAI_API_KEY.ifBlank { System.getenv("OPENAI_API_KEY") ?: "" }
    }

    fun saveApiKeys(context: Context, nvidiaKey: String, openAiKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            if (nvidiaKey.isNotBlank()) putString(KEY_NVIDIA, nvidiaKey.trim()) else remove(KEY_NVIDIA)
            if (openAiKey.isNotBlank()) putString(KEY_OPENAI, openAiKey.trim()) else remove(KEY_OPENAI)
            apply()
        }
    }

    fun loadSampleAssignmentBitmap(context: Context): Bitmap? {
        return try {
            context.assets.open("sample_a01_assignment.jpg").use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    fun readFileMeta(context: Context, uri: Uri): UploadedFileMeta {
        var name = "Math_Assignment.jpg"
        var sizeBytes = -1L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx >= 0) {
                        cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }?.let { name = it }
                    }
                    if (sizeIdx >= 0) {
                        sizeBytes = cursor.getLong(sizeIdx)
                    }
                }
            }
        } catch (_: Exception) { }

        val sizeText = when {
            sizeBytes >= 1024 * 1024 -> "%.1f MB".format(sizeBytes / (1024f * 1024f))
            sizeBytes >= 1024 -> "${sizeBytes / 1024} KB"
            sizeBytes > 0 -> "$sizeBytes B"
            else -> "Uploaded"
        }
        return UploadedFileMeta(fileName = name, sizeText = sizeText)
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
     * Step 1: Call meta/llama-3.2-11b-vision-instruct on NVIDIA NIM to extract all handwritten lines,
     * approximate vertical positions (0.0 top to 1.0 bottom), and OCR corrections/observations.
     */
    suspend fun runLlamaVisionOcr(
        context: Context,
        bitmap: Bitmap,
        assignment: AssignmentRow?,
        studentNotes: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val key = getNvidiaKey(context)
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("NVIDIA Llama 3.2 Vision API key is missing. Tap the key icon to add it."))
        }

        try {
            val base64Img = bitmapToBase64Jpeg(bitmap)
            val promptText = buildString {
                append("You are an educational OCR & handwriting vision expert.\n")
                append("Read every line of the student's uploaded assignment image from top to bottom.\n")
                if (assignment != null) {
                    append("Expected Assignment (${assignment.assignmentId}): ${assignment.question}\n")
                }
                if (studentNotes.isNotBlank()) {
                    append("Student note: $studentNotes\n")
                }
                append("Respond strictly with valid JSON containing:\n")
                append("{\n")
                append("  \"transcription\": \"exact multi-line transcription including line numbers and approximate vertical position [0.00-1.00] for each equation/step\",\n")
                append("  \"ocr_observations\": \"handwriting clarity, any arithmetic/algebra slips or notation corrections spotted directly in the image\"\n")
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
                .put("max_tokens", 700)
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
     * Step 2: Evaluate OCR text with OpenAI GPT to determine Correct / Partial / Wrong marks,
     * Strong Points, Weak Points, Weak Concepts, and step bounding fractions.
     */
    suspend fun gradeWithOpenAi(
        context: Context,
        assignment: AssignmentRow?,
        rubrics: List<RubricRow>,
        ocrTranscription: String,
        ocrObservations: String
    ): Result<AiSubmissionAnalysis> = withContext(Dispatchers.IO) {
        val key = getOpenAiKey(context)
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("OpenAI API key is missing. Tap the key icon to add it."))
        }

        val maxMarks = assignment?.maxMarks ?: 5f
        val rubricText = if (rubrics.isEmpty()) {
            "Method, Calculation Accuracy, Final Answer: $maxMarks marks total"
        } else {
            rubrics.joinToString("\n") { "- ${it.criterion} (${it.maxMarks} marks): ${it.description}" }
        }

        val systemPrompt = """
            You are an expert AI teacher grading a student's OCR-scanned handwritten assignment.
            Analyze the OCR transcription line by line against the assignment question, reference answer, and rubric.
            Identify exact Strong Points (what is right), Weak Points (errors/slips/gaps), Weak Concepts,
            and classify each step as "Correct", "Partial", or "Wrong" with approximate normalized vertical position
            (y_start and y_end in 0.0..1.0 from top to bottom of the page).
            Also split the marks out of $maxMarks into: correct_marks, partial_marks, wrong_marks (where total earned score = correct_marks + partial_marks, and wrong_marks = marks lost due to mistakes).
            Respond strictly with valid JSON matching this schema:
            {
              "score": number,
              "correct_marks": number,
              "partial_marks": number,
              "wrong_marks": number,
              "verdict": "Correct" | "Partially Correct" | "Incorrect",
              "strong_points": ["..."],
              "weak_points": ["..."],
              "weak_concepts": ["..."],
              "step_verdicts": [
                {"step": "...", "status": "Correct" | "Partial" | "Wrong", "explanation": "...", "y_start": 0.15, "y_end": 0.30}
              ],
              "summary_feedback": "concise teacher feedback"
            }
        """.trimIndent()

        val userPrompt = """
            Assignment: ${assignment?.assignmentTitle ?: "Uploaded Assignment"} (${assignment?.subject ?: "Mathematics"})
            Question: ${assignment?.question ?: "Grade the problem written in the uploaded sheet"}
            Reference Answer: ${assignment?.referenceAnswer ?: "Verify mathematically from the sheet itself"}
            Max Marks: $maxMarks
            Rubric:
            $rubricText

            Llama 3.2 Vision OCR Transcription:
            $ocrTranscription

            OCR Notes:
            $ocrObservations
        """.trimIndent()

        val candidateModels = listOf("gpt-4o", "gpt-4o-mini", "gpt-4.1")
        var lastError = "OpenAI request failed"

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
                    .put("max_tokens", 850)

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
                val score = json.optDouble("score", 0.0).toFloat().coerceIn(0f, maxMarks)
                val correctMarks = json.optDouble("correct_marks", score.toDouble()).toFloat().coerceIn(0f, maxMarks)
                val partialMarks = json.optDouble("partial_marks", 0.0).toFloat().coerceIn(0f, maxMarks)
                val wrongMarks = json.optDouble("wrong_marks", (maxMarks - score).toDouble()).toFloat().coerceIn(0f, maxMarks)
                val pct = if (maxMarks > 0f) ((score / maxMarks) * 100f).toInt().coerceIn(0, 100) else 0

                val verdict = json.optString("verdict", "Partially Correct")
                val strong = jsonArrayToList(json.optJSONArray("strong_points"))
                val weak = jsonArrayToList(json.optJSONArray("weak_points"))
                val defaultConcept = assignment?.conceptId?.let { EduDataRepository.getConceptById(it)?.topic }
                    ?: assignment?.subject
                    ?: "Algebra"
                val weakConcepts = jsonArrayToList(json.optJSONArray("weak_concepts"))
                    .ifEmpty { if (weak.isNotEmpty()) listOf(defaultConcept) else emptyList() }

                val stepsJson = json.optJSONArray("step_verdicts")
                val steps = mutableListOf<StepVerdict>()
                if (stepsJson != null) {
                    val total = stepsJson.length().coerceAtLeast(1)
                    for (i in 0 until stepsJson.length()) {
                        val obj = stepsJson.optJSONObject(i) ?: continue
                        val fallbackStart = 0.14f + (i.toFloat() / total) * 0.72f
                        val fallbackEnd = (fallbackStart + (0.72f / total) * 0.85f).coerceAtMost(0.96f)
                        val yStart = obj.optDouble("y_start", fallbackStart.toDouble()).toFloat().coerceIn(0.05f, 0.90f)
                        val yEnd = obj.optDouble("y_end", fallbackEnd.toDouble()).toFloat().coerceIn(yStart + 0.05f, 0.98f)
                        steps.add(
                            StepVerdict(
                                step = obj.optString("step", "Step ${i + 1}"),
                                status = obj.optString("status", if (obj.optBoolean("is_correct", true)) "Correct" else "Wrong"),
                                explanation = obj.optString("explanation", ""),
                                yStartFraction = yStart,
                                yEndFraction = yEnd
                            )
                        )
                    }
                }

                val summary = json.optString("summary_feedback", "")
                    .ifBlank { "Analyzed via Llama 3.2 Vision OCR + $modelName." }

                return@withContext Result.success(
                    AiSubmissionAnalysis(
                        ocrTranscription = ocrTranscription,
                        ocrObservations = ocrObservations,
                        score = score,
                        maxMarks = maxMarks,
                        scorePercent = pct,
                        correctMarks = correctMarks,
                        partialMarks = partialMarks,
                        wrongMarks = wrongMarks,
                        verdict = verdict,
                        strongPoints = strong.ifEmpty { listOf("Clear step-by-step mathematical setup") },
                        weakPoints = weak,
                        weakConcepts = weakConcepts,
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
