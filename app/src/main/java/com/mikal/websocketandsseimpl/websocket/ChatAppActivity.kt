package com.mikal.websocketandsseimpl.websocket

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikal.websocketandsseimpl.R

/**
 * Created by Mikal Shrestha on 14/11/2024.
 */
class ChatAppActivity : AppCompatActivity() {

    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_chat)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val adapter = ChatAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        chatViewModel.messages.observe(this, Observer { messages ->
            adapter.submitList(messages)
            recyclerView.scrollToPosition(messages.size - 1) // Scroll to the latest message
        })

        val sendButton = findViewById<Button>(R.id.sendButton)
        val messageInput = findViewById<EditText>(R.id.messageInput)

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotBlank()) {
                chatViewModel.sendMessage(message)
                messageInput.text.clear()
            }
        }
    }
}