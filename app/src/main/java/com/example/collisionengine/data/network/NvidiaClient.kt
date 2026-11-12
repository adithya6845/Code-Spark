package com.example.collisionengine.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object NvidiaClient {
    private val NVIDIA_API_KEY = System.getenv("NVIDIA_API_KEY") ?: ""
    private const val MODEL_NAME = "meta/llama-3.2-11b-vision-instruct"

    suspend fun enhanceAnswer(userQuery: String, genieData: String): String = withContext(Dispatchers.IO) {
        if (NVIDIA_API_KEY.isBlank()) {
            return@withContext genieData
        }

        try {
            val url = URL("https://integrate.api.nvidia.com/v1/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $NVIDIA_API_KEY")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 12000
            conn.doOutput = true

            val systemPrompt = "You are Campus Connect AI's concise Decision Engine. Provide a brief, punchy, high-impact answer (maximum 2 to 3 short bullet points, under 50-60 words total). Keep it clear, direct, and easy to read on mobile. Highlight team synergy, key mentor, and next step."

            val userContent = "Student Question: $userQuery\n\nCampus Data:\n$genieData\n\nPlease formulate a very concise, punchy 2-3 bullet response."

            val messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", systemPrompt))
                .put(JSONObject().put("role", "user").put("content", userContent))

            val payload = JSONObject()
                .put("model", MODEL_NAME)
                .put("messages", messages)
                .put("max_tokens", 140)
                .put("temperature", 0.3)

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            if (conn.responseCode in 200..299) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    val content = message?.optString("content")?.trim() ?: ""
                    if (content.isNotBlank()) {
                        return@withContext content
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext genieData
    }

    suspend fun extractNames(text: String): List<String> = withContext(Dispatchers.IO) {
        val regex = Regex("""\*\*(.*?)\*\*""")
        val matches = regex.findAll(text)
        val extracted = matches.map { it.groupValues[1].trim() }.toList()

        return@withContext extracted.filter { name ->
            val words = name.split(" ")
            words.size in 2..3 && words.all { it.isNotEmpty() && it.first().isUpperCase() }
        }
    }
}
