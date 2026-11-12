package com.example.collisionengine.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object DatabricksClient {
    // Read Databricks Genie token from environment rather than hardcoding secrets
    private val HOST = System.getenv("DATABRICKS_HOST") ?: "https://dbc-cf0c6ab7-9230.cloud.databricks.com"
    private val TOKEN = System.getenv("DATABRICKS_TOKEN") ?: ""
    private val SPACE_ID = System.getenv("DATABRICKS_SPACE_ID") ?: "01f1a698e50d11bf8e9c69c03ce72576"

    suspend fun askGenie(question: String): String = withContext(Dispatchers.IO) {
        try {
            // 1. Start Genie Conversation
            val startUrl = URL("$HOST/api/2.0/genie/spaces/$SPACE_ID/start-conversation")
            val startConn = startUrl.openConnection() as HttpURLConnection
            startConn.requestMethod = "POST"
            startConn.setRequestProperty("Authorization", "Bearer $TOKEN")
            startConn.setRequestProperty("Content-Type", "application/json")
            startConn.doOutput = true

            val startPayload = JSONObject().put("content", question)
            OutputStreamWriter(startConn.outputStream).use { it.write(startPayload.toString()) }

            if (startConn.responseCode !in 200..299) {
                return@withContext "Failed to start conversation. Error code: ${startConn.responseCode}"
            }

            val startResponse = startConn.inputStream.bufferedReader().use { it.readText() }
            val startJson = JSONObject(startResponse)
            val conversationId = startJson.optString("conversation_id")
            val messageId = startJson.optString("message_id", startJson.optString("id"))

            if (conversationId.isNullOrEmpty() || messageId.isNullOrEmpty()) {
                return@withContext "Invalid response from Databricks Genie API"
            }

            // 2. Poll for message completion
            val pollUrl = URL("$HOST/api/2.0/genie/spaces/$SPACE_ID/conversations/$conversationId/messages/$messageId")
            
            for (i in 0 until 25) {
                delay(2000)
                val pollConn = pollUrl.openConnection() as HttpURLConnection
                pollConn.requestMethod = "GET"
                pollConn.setRequestProperty("Authorization", "Bearer $TOKEN")
                pollConn.setRequestProperty("Content-Type", "application/json")

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
                        return@withContext "Message processing failed on Databricks."
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
