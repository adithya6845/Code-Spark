package com.example.collisionengine.data.network

import android.content.Context
import com.example.collisionengine.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object DatabricksClient {
    private const val PREFS_NAME = "databricks_genie_prefs"
    private const val KEY_HOST = "databricks_host"
    private const val KEY_TOKEN = "databricks_token"
    private const val KEY_SPACE = "databricks_space_id"

    private const val DEFAULT_HOST = "https://dbc-d4fdaac8-e4fd.cloud.databricks.com"
    private const val DEFAULT_SPACE_ID = "01f1a511d4491c78a550a4ece2546db8"

    fun getHost(context: Context? = null): String {
        val saved = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(KEY_HOST, null)?.trim()
        if (!saved.isNullOrBlank()) return saved
        return BuildConfig.DATABRICKS_HOST.ifBlank {
            System.getenv("DATABRICKS_HOST") ?: DEFAULT_HOST
        }
    }

    fun getToken(context: Context? = null): String {
        val saved = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(KEY_TOKEN, null)?.trim()
        if (!saved.isNullOrBlank()) return saved
        return BuildConfig.DATABRICKS_TOKEN.ifBlank {
            System.getenv("DATABRICKS_TOKEN") ?: ""
        }
    }

    fun getSpaceId(context: Context? = null): String {
        val saved = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(KEY_SPACE, null)?.trim()
        if (!saved.isNullOrBlank()) return saved
        return BuildConfig.DATABRICKS_SPACE_ID.ifBlank {
            System.getenv("DATABRICKS_SPACE_ID") ?: DEFAULT_SPACE_ID
        }
    }

    fun saveSettings(context: Context, host: String, token: String, spaceId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            if (host.isNotBlank()) putString(KEY_HOST, host.trim()) else remove(KEY_HOST)
            if (token.isNotBlank()) putString(KEY_TOKEN, token.trim()) else remove(KEY_TOKEN)
            if (spaceId.isNotBlank()) putString(KEY_SPACE, spaceId.trim()) else remove(KEY_SPACE)
            apply()
        }
    }

    suspend fun askGenie(question: String, context: Context? = null): String = withContext(Dispatchers.IO) {
        val rawHost = getHost(context).trim().trimEnd('/')
        val host = if (rawHost.contains("?")) rawHost.substringBefore("?") else rawHost
        val token = getToken(context).trim()
        val spaceId = getSpaceId(context).trim()

        if (token.isBlank() || spaceId.isBlank()) {
            return@withContext ""
        }

        try {
            // 1. Start Genie Conversation
            val startUrl = URL("$host/api/2.0/genie/spaces/$spaceId/start-conversation")
            val startConn = (startUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 15000
                readTimeout = 25000
                doOutput = true
            }

            val startPayload = JSONObject().put("content", question)
            OutputStreamWriter(startConn.outputStream).use { it.write(startPayload.toString()) }

            val code = startConn.responseCode
            if (code !in 200..299) {
                val errBody = startConn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                android.util.Log.w("DatabricksClient", "start-conversation HTTP $code: $errBody")
                return@withContext ""
            }

            val startResponse = startConn.inputStream.bufferedReader().use { it.readText() }
            val startJson = JSONObject(startResponse)
            val conversationId = startJson.optString("conversation_id")
            val messageId = startJson.optString("message_id", startJson.optString("id"))

            if (conversationId.isNullOrEmpty() || messageId.isNullOrEmpty()) {
                return@withContext ""
            }

            // 2. Poll for message completion
            val pollUrl = URL("$host/api/2.0/genie/spaces/$spaceId/conversations/$conversationId/messages/$messageId")

            for (i in 0 until 35) {
                delay(2000)
                val pollConn = (pollUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 15000
                    readTimeout = 20000
                }

                if (pollConn.responseCode in 200..299) {
                    val pollResponse = pollConn.inputStream.bufferedReader().use { it.readText() }
                    val pollJson = JSONObject(pollResponse)
                    val status = pollJson.optString("status")

                    if (status == "COMPLETED" || status == "EXECUTED" || status == "SUCCESS") {
                        val attachments = pollJson.optJSONArray("attachments")
                        var answerText = ""

                        if (attachments != null) {
                            for (j in 0 until attachments.length()) {
                                val att = attachments.getJSONObject(j)
                                val textData = att.optJSONObject("text")
                                if (textData != null && textData.has("content")) {
                                    val content = textData.getString("content")
                                    if (textData.optString("purpose") == "TEXT_ATTACHMENT_PURPOSE_ANSWER") {
                                        answerText = content
                                        break
                                    } else if (answerText.isEmpty()) {
                                        answerText = content
                                    }
                                }
                            }
                        }
                        if (answerText.isNotBlank()) {
                            return@withContext answerText
                        }
                    } else if (status == "FAILED" || status == "ERROR") {
                        return@withContext ""
                    }
                }
                pollConn.disconnect()
            }
            return@withContext ""
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ""
        }
    }
}
