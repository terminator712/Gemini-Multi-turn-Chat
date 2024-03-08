package com.geminiai.chat

import android.net.Uri
import java.util.UUID

enum class Sender {
    USER, MODEL, ERROR
}

data class Message(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val sender: Sender = Sender.USER,
    var isPending: Boolean = false,
    val imageUris: List<Uri> =  listOf()
)
