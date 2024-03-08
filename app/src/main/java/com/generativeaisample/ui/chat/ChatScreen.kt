package com.generativeaisample.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.geminiai.chat.ChatUiState
import com.geminiai.chat.Message
import com.geminiai.chat.Sender
import com.generativeaisample.R
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
internal fun ChatScreen(
    chatUIState: StateFlow<ChatUiState>, onSendMessage: (String, List<Uri>) -> Unit
) {
    val chatUiState by chatUIState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            MessageInput(
                onSendMessage = onSendMessage,
                resetScroll = {
                    coroutineScope.launch {
                        listState.scrollToItem(0)
                    }
                }
            )
        }, topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(5.dp, shape = RoundedCornerShape(2.dp))
                    .padding(vertical = 10.dp, horizontal = 20.dp)
                    .height(55.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gemini),
                    contentDescription = "Gemini",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(text = "Gemini Multi-turn Chat")
                    Text(text = "with text+images", fontSize = 12.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Messages List
            ChatList(chatUiState.messages, listState)
        }
    }
}

@Composable
fun ChatList(
    messages: List<Message>,
    listState: LazyListState
) {
    LazyColumn(
        reverseLayout = true,
        state = listState
    ) {
        items(messages.reversed()) { message ->
            ChatBubbleItem(message)
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: Message
) {
    val isModelMessage = message.sender == Sender.MODEL ||
            message.sender == Sender.ERROR

    val backgroundColor = when (message.sender) {
        Sender.MODEL -> MaterialTheme.colorScheme.primaryContainer
        Sender.USER -> MaterialTheme.colorScheme.tertiaryContainer
        Sender.ERROR -> MaterialTheme.colorScheme.errorContainer
    }

    val bubbleShape = if (isModelMessage) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }

    val horizontalAlignment = if (isModelMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }

    // Add shadows to the chat bubbles
    val elevation = 4.dp

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = message.sender.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 3.dp)
        )
        Row {
            if (message.isPending) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(all = 4.dp)
                )
            }
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = bubbleShape,
                    modifier = Modifier
                        .widthIn(0.dp, maxWidth * 0.9f)
                        // Apply shadows to the chat bubbles
                        .shadow(elevation)
                ) {
                    Column {
                        LazyRow(
                            reverseLayout = true,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .padding(top = 5.dp)
                        ) {
                            items(message.imageUris.reversed()) { imageUri ->
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .requiredSize(150.dp)
                                )
                            }
                        }
                        SelectionContainer {
                            Text(
                                text = message.text,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MessageInput(
    onSendMessage: (String, List<Uri>) -> Unit,
    resetScroll: () -> Unit = {}
) {
    var userMessage by rememberSaveable { mutableStateOf("") }
    val imageUris = rememberSaveable(saver = UriSaver()) { mutableStateListOf() }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri ->
        imageUri?.let {
            imageUris.add(it)
        }
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 5.dp, horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Add Image",
                )
            }
            OutlinedTextField(
                value = userMessage,
                label = { Text(stringResource(R.string.chat_label)) },
                placeholder = { Text(stringResource(R.string.summarize_hint)) },
                onValueChange = { userMessage = it },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.90f)
            )
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage, imageUris.toList())
                        userMessage = ""
                        imageUris.clear()
                        resetScroll()
                    }
                },
                modifier = Modifier
                    .padding(start = 12.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = stringResource(R.string.action_send),
                    modifier = Modifier
                )
            }
        }
        LazyRow(
            modifier = Modifier.padding(all = 8.dp)
        ) {
            items(imageUris) { imageUri ->
                val showDialog = remember { mutableStateOf(false) }
                if (showDialog.value) {
                    Alert(showDialog = showDialog.value,
                        onDismiss = { showDialog.value = false }) {
                        showDialog.value = false
                        imageUris.remove(imageUri)
                    }
                }
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .requiredSize(72.dp)
                        .clickable {
                            showDialog.value = true
                        }
                )
            }
        }
    }
}

@Composable
fun Alert(
    showDialog: Boolean,
    onDismiss: () -> Unit, confirmAction: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text("Remove image")
            },
            text = {
                Text("Are you sure you want to remove this image?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = confirmAction) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("No")
                }
            }
        )
    }
}


class UriSaver : Saver<MutableList<Uri>, List<String>> {
    override fun restore(value: List<String>): MutableList<Uri> = value.map {
        Uri.parse(it)
    }.toMutableList()

    override fun SaverScope.save(value: MutableList<Uri>): List<String> =
        value.map { it.toString() }
}