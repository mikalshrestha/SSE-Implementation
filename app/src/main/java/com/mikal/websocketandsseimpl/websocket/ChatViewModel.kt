package com.mikal.websocketandsseimpl.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Created by Mikal Shrestha on 14/11/2024.
 */

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<String>>()
    val messages: LiveData<List<String>> = _messages
//    private val webSocketClient = WebSocketClient("http://10.13.161.102:8080/chat")
    private val webSocketClient = WebSocketClient("http://10.0.2.2:8080/chat")

    init {
        // Connect WebSocket
        webSocketClient.connect()
        webSocketClient.addListener(object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                _messages.postValue(_messages.value.orEmpty() + text)
            }
        })
    }

    fun sendMessage(message: String) {
        webSocketClient.sendMessage(message)
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.close()
    }
}
