package com.geminiai.chat

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatUiState(
    messages: List<Message> = emptyList()
) {
    private val _messages: MutableList<Message> = messages.toMutableStateList()
    val messages: List<Message> = _messages

    private fun addMessage(msg: Message) {
        _messages.add(msg)
    }

    private fun replaceLastPendingMessage() {
        val lastMessage = _messages.lastOrNull()
        lastMessage?.let {
            val newMessage = lastMessage.apply { isPending = false }
            _messages.removeLast()
            _messages.add(newMessage)
        }
    }

    class ChatViewModel(apiKey: String, safetySetting: List<SafetySetting>) : ViewModel() {

        private val textModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey, safetySettings = safetySetting
        )
        private val imageModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = apiKey, safetySettings = safetySetting
        )
        private val chat = textModel.startChat(
            history = listOf()
        )

        private val _uiState: MutableStateFlow<ChatUiState> =
            MutableStateFlow(ChatUiState(chat.history.map { content ->
                Message(
                    text = content.parts.first().asTextOrNull() ?: "",
                    sender = if (content.role == "user") Sender.USER else Sender.MODEL,
                    isPending = false
                )
            }))
        val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

        fun sendMessage(userMessage: String, uris: List<Uri>, selectedImages: List<Bitmap>) {
            _uiState.value.addMessage(
                Message(
                    text = userMessage,
                    sender = Sender.USER,
                    isPending = true,
                    imageUris = uris
                )
            )

            viewModelScope.launch {
                try {
                    val mediaContent = content {
                        for (bitmap in selectedImages) {
                            image(bitmap)
                        }
                        text(userMessage)
                        role = Sender.USER.toString().lowercase()
                    }
                    val content = content {
                        text(userMessage)
                        role = Sender.USER.toString().lowercase()
                    }
                    val response = if (selectedImages.isNotEmpty()) {
                        val res = imageModel.generateContent(mediaContent)
                        chat.history.add(content)
                        chat.history.add(res.candidates.first().content)
                        res
                    } else {
                        chat.sendMessage(content)
                    }

                    _uiState.value.replaceLastPendingMessage()
                    response.text?.let { modelResponse ->
                        _uiState.value.addMessage(
                            Message(
                                text = modelResponse,
                                sender = Sender.MODEL,
                                isPending = false
                            )
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value.replaceLastPendingMessage()
                    _uiState.value.addMessage(
                        Message(
                            text = e.localizedMessage ?: "Error occurred. Please try again",
                            sender = Sender.ERROR
                        )
                    )
                }
            }
        }
    }
}
