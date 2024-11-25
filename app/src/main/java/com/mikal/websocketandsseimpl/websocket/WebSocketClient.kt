package com.mikal.websocketandsseimpl.websocket

import okhttp3.*

/**
 * Created by Mikal Shrestha on 14/11/2024.
 */
class WebSocketClient(private val serverUrl: String) {
    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket
    private val listeners = mutableListOf<WebSocketListener>()

    fun connect() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Handle connection open
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Notify listeners about incoming message
                listeners.forEach { it.onMessage(webSocket, text) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Handle failure
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // Handle WebSocket closed
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket.send(message)
    }

    fun addListener(listener: WebSocketListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: WebSocketListener) {
        listeners.remove(listener)
    }

    fun close() {
        webSocket.close(1000, "Client closed")
    }
}
