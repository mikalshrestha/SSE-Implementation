package com.mikal.websocketandsseimpl.sse

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mikal.websocketandsseimpl.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by Mikal Shrestha on 24/11/2024.
 */

class SSEImplMainActivity : AppCompatActivity() {

    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var stopButton: Button
    private lateinit var textViewResponse: TextView
    private var currentCall: Call? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sse_main)

        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        stopButton = findViewById(R.id.btnStop)
        textViewResponse = findViewById(R.id.textViewResponse)

        buttonSend.setOnClickListener {
            val message = editTextMessage.text.toString()
            val conversationId = "12345" // This should be dynamically generated or retrieved

            textViewResponse.text = ""
            sendMessage(message, conversationId)
        }
        stopButton.setOnClickListener { stopSSE() }

    }

    // Method to send a message to the backend
    private fun sendMessage(message: String, conversationId: String) {
        val client = OkHttpClient()
        val url = "http://10.13.161.102:8080/api/chat/send-message"

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
                runOnUiThread {
                    textViewResponse.text = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    ///textViewResponse.text = "${response.body?.string()}"
                }

                // Start SSE to receive response from server
                startSSE(conversationId)
            }
        })
    }


    private fun startSSE(conversationId: String) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // Keep connection alive for SSE
            .build()

        val request = Request.Builder()
            .url("http://10.13.161.102:8080/api/chat/stream-response?conversationId=$conversationId")
            .build()

        currentCall = client.newCall(request)

        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    textViewResponse.text = "SSE Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val source = responseBody.source()
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line()
                        if (line != null) {
                            runOnUiThread {
                                val actualContent = line.replaceFirst("data:", "").trim()
                                textViewResponse.append(" $actualContent")
                            }

                            // Check for [DONE] to stop SSE
                            if (line.contains("[DONE]")) {
                                break
                            }
                        }
                    }
                }
            }
        })
    }

    fun stopSSE() {
        // Cancel the ongoing SSE connection
        currentCall?.cancel()
        println("SSE connection canceled.")
        textViewResponse.text = "Request stopped."
    }

}


