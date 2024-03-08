package com.generativeaisample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.geminiai.Interactor
import com.generativeaisample.ui.chat.ChatScreen
import com.generativeaisample.ui.theme.GenerativeAISampleTheme
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GenerativeAISampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val safetySetting = listOf(
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

                    val interactor = Interactor.getInstance(WeakReference(this))
                    val uiState = interactor.initChat(BuildConfig.apiKey, safetySetting)
                    ChatScreen(uiState) { message, images ->
                        interactor.sendMessage(message, images)
                    }
                }
            }
        }
    }
}
