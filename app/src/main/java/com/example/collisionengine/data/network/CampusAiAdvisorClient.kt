package com.example.collisionengine.data.network

import android.content.Context
import com.example.collisionengine.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CampusAiAdvisorClient {

    private const val PREFS_NAME = "campus_ai_keys"
    private const val KEY_OPENAI = "openai_api_key"
    private const val KEY_NVIDIA = "nvidia_api_key"

    fun getOpenAiKey(context: Context?): String {
        val saved = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(KEY_OPENAI, null)?.trim()
        if (!saved.isNullOrBlank()) return saved
        return BuildConfig.OPENAI_API_KEY.ifBlank {
            System.getenv("OPENAI_API_KEY") ?: ""
        }
    }

    fun getNvidiaKey(context: Context?): String {
        val saved = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(KEY_NVIDIA, null)?.trim()
        if (!saved.isNullOrBlank()) return saved
        return BuildConfig.NVIDIA_API_KEY.ifBlank {
            System.getenv("NVIDIA_API_KEY") ?: ""
        }
    }

    fun saveKeys(context: Context, openAiKey: String, nvidiaKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            if (openAiKey.isNotBlank()) putString(KEY_OPENAI, openAiKey.trim()) else remove(KEY_OPENAI)
            if (nvidiaKey.isNotBlank()) putString(KEY_NVIDIA, nvidiaKey.trim()) else remove(KEY_NVIDIA)
            apply()
        }
    }

    /**
     * Executes the AI query cascade:
     * 1. Databricks Genie Agent (if configured and returns a valid answer)
     * 2. User OpenAI API Key (sk-vHBcAiFknURAPLsllx3Vbeh5Ic5u9r8K73H6lv37yWlVUA4j)
     * 3. NVIDIA NIM API Key (nvidia/nemotron-3.5-super-vl-120b-a12b)
     * 4. Smart Local Fallback
     */
    suspend fun getAdvice(query: String, context: Context): Pair<String, String> = withContext(Dispatchers.IO) {
        // Step 1: Try Databricks Genie if configured
        try {
            val genieResp = DatabricksClient.askGenie(query, context)
            if (genieResp.isNotBlank() &&
                !genieResp.contains("Failed", ignoreCase = true) &&
                !genieResp.contains("Error", ignoreCase = true) &&
                genieResp.length > 30
            ) {
                return@withContext Pair(genieResp, "Databricks Genie")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val systemPrompt = """
            You are Campus Connect AI, a world-class technology, skills, and placement advisor for university engineering students.
            
            GUIDELINES:
            1. If the student asks about SKILLS, PLACEMENTS, or "HOW TO FIX IT" (e.g. "what skills should I learn for better placements in software engineering role", "tell me fix it"):
               - Structure your response into clear, punchy sections:
                 • 🎯 Essential Core Skills (DSA, Language mastery, CS Fundamentals)
                 • 💻 Stack & Role-Specific Skills (Backend/Frontend, System Design basics, APIs, Databases)
                 • 🛠️ How to 'Fix It' (Action Roadmap: Diagnosing gaps, 12-week schedule, portfolio projects with metrics, mock interviews)
                 • ⚡ Pro Tips & Common Traps (avoid tutorial hell, master 1 language, referrals)
            2. If the user asks "WHO CAN HELP ME IN THIS TECHNOLOGIES" or asks for PROFILES / MENTORS:
               - List recommended faculty and student peers by name and expertise (e.g., Dr. Sameer Iyer, Dr. Yash Malhotra, Dr. Emily Chen, Sophia Kim, Aarav Sharma) with their department and research domains.
            3. Keep the tone inspiring, direct, practical, and formatted cleanly with bullet points and bold headers for mobile readability.
        """.trimIndent()

        // Step 2: Try User's OpenAI Key (sk-...)
        val openAiKey = getOpenAiKey(context).trim()
        if (openAiKey.isNotBlank()) {
            try {
                val openAiResult = callOpenAiCompatible(
                    endpoint = "https://api.openai.com/v1/chat/completions",
                    apiKey = openAiKey,
                    model = "gpt-4o-mini",
                    systemPrompt = systemPrompt,
                    userQuery = query
                )
                if (openAiResult.isNotBlank()) {
                    return@withContext Pair(openAiResult, "OpenAI")
                }
            } catch (e: Exception) {
                android.util.Log.w("CampusAiAdvisorClient", "OpenAI call failed, falling back to NVIDIA: ${e.message}")
            }
        }

        // Step 3: Try NVIDIA NIM API (model: nvidia/nemotron-3.5-super-vl-120b-a12b)
        val nvidiaKey = getNvidiaKey(context).trim()
        if (nvidiaKey.isNotBlank()) {
            try {
                val nvidiaResult = callOpenAiCompatible(
                    endpoint = "https://integrate.api.nvidia.com/v1/chat/completions",
                    apiKey = nvidiaKey,
                    model = "nvidia/nemotron-3.5-super-vl-120b-a12b",
                    systemPrompt = systemPrompt,
                    userQuery = query,
                    maxTokens = 1500
                )
                if (nvidiaResult.isNotBlank()) {
                    return@withContext Pair(nvidiaResult, "NVIDIA Nemotron 120B")
                }
            } catch (e: Exception) {
                android.util.Log.w("CampusAiAdvisorClient", "NVIDIA call failed, falling back to Local Engine: ${e.message}")
            }
        }

        // Step 4: Local Smart Fallback Engine
        val local = CampusGenieSearchEngine.answerQuery(query)
        return@withContext Pair(local.text, "Campus Engine")
    }

    private fun callOpenAiCompatible(
        endpoint: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userQuery: String,
        maxTokens: Int = 1200
    ): String {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 12000
            readTimeout = 25000
            doOutput = true
        }

        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", systemPrompt))
            .put(JSONObject().put("role", "user").put("content", userQuery))

        val payload = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("max_tokens", maxTokens)
            put("temperature", 0.3)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

        val code = conn.responseCode
        if (code in 200..299) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val msg = choices.getJSONObject(0).optJSONObject("message")
                val content = msg?.optString("content")?.trim()
                val reasoning = msg?.optString("reasoning_content")?.trim()
                if (!content.isNullOrBlank()) {
                    return content
                } else if (!reasoning.isNullOrBlank()) {
                    return reasoning
                }
            }
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            throw IllegalStateException("API HTTP $code: $err")
        }
        return ""
    }
}

