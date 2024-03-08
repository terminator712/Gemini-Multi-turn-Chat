package com.geminiai

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.geminiai.chat.ChatUiState
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class Interactor private constructor(private val context: WeakReference<Context>) {
    companion object {
        @Volatile
        private var interactor: Interactor? = null
        fun getInstance(context: WeakReference<Context>): Interactor {
            return interactor ?: synchronized(this) {
                val instance = Interactor(context)
                interactor = instance
                instance
            }
        }
    }

    private lateinit var chatViewModel: ChatUiState.ChatViewModel
    private lateinit var imageRequestBuilder: ImageRequest.Builder
    private lateinit var imageLoader: ImageLoader
    private val safetySettings = listOf(
        SafetySetting(
            harmCategory = HarmCategory.HARASSMENT,
            threshold = BlockThreshold.MEDIUM_AND_ABOVE
        ),
        SafetySetting(
            harmCategory = HarmCategory.HATE_SPEECH,
            threshold = BlockThreshold.MEDIUM_AND_ABOVE
        ),
        SafetySetting(
            harmCategory = HarmCategory.DANGEROUS_CONTENT,
            threshold = BlockThreshold.LOW_AND_ABOVE
        ),
        SafetySetting(
            harmCategory = HarmCategory.SEXUALLY_EXPLICIT,
            threshold = BlockThreshold.LOW_AND_ABOVE
        )
    )

    init {
        context.get()?.let {
            imageRequestBuilder =  ImageRequest.Builder(it)
            imageLoader = ImageLoader.Builder(it).build()
        }
    }

    fun initChat(
        apiKey: String,
        safetySetting: List<SafetySetting> = safetySettings
    ): StateFlow<ChatUiState> {

        chatViewModel = ChatUiState.ChatViewModel(apiKey, safetySetting)
        return chatViewModel.uiState
    }

    fun sendMessage(userMessage: String, images: List<Uri> = listOf()) {
        if (!this::chatViewModel.isInitialized || !this::imageLoader.isInitialized || !this::imageRequestBuilder.isInitialized) {
            throw IllegalStateException("Chat is not initialized. Please initialize the chat first, by calling initChat method.")
        }
        context.get()?.let { context ->
            val scope =
                if (context is LifecycleOwner) context.lifecycleScope else CoroutineScope(
                    Dispatchers.IO
                )
            scope.launch {
                val bitmaps = images.mapNotNull {
                    val imageRequest = imageRequestBuilder
                        .data(it)
                        .size(size = 768)
                        .precision(Precision.EXACT)
                        .build()
                    try {
                        val result = imageLoader.execute(imageRequest)
                        if (result is SuccessResult) {
                            return@mapNotNull (result.drawable as BitmapDrawable).bitmap
                        } else {
                            return@mapNotNull null
                        }
                    } catch (e: Exception) {
                        return@mapNotNull null
                    }
                }
                chatViewModel.sendMessage(userMessage, images, bitmaps)
            }
        }
    }
}