package com.mikal.websocketandsseimpl.sse

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by Mikal Shrestha on 24/11/2024.
 */
class SseClient {
    private val client = OkHttpClient()

    fun sendMessage(message: String, conversationId: String) {
        val client = OkHttpClient()
        val url = "http://<YOUR_SERVER_URL>/api/chat/send-message"

        val json = JSONObject().apply {
            put("message", message)
            put("conversationId", conversationId)
        }

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure in UI
            }

            override fun onResponse(call: Call, response: Response) {
                println("Message sent: ${response.body?.string()}")
                // After sending, start streaming responses
                startSSE(conversationId)
            }
        })
    }

    fun startSSE(conversationId: String) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // Keep connection alive for SSE
            .build()

        val request = Request.Builder()
            .url("http://<YOUR_SERVER_URL>/api/chat/stream-response?conversationId=$conversationId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure in UI
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val source = responseBody.source()
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line()
                        if (line != null) {
                            println("Received: $line")
                            // Handle UI updates here
                            if (line.contains("[DONE]")) break // End of stream
                        }
                    }
                }
            }
        })
    }
}
