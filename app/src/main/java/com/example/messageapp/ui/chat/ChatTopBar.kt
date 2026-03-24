package com.example.messageapp.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

data class ChatTopBarState(
    val chatName: String?,
    val hasPinnedMessage: Boolean,
    val pinnedSnippet: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    state: ChatTopBarState,
    onBack: () -> Unit,
    onOpenInfo: () -> Unit,
    onUnpin: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Volver")
            }
        },
        title = {
            Column {
                Text(
                    text = state.chatName ?: "Conversación",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.hasPinnedMessage && state.pinnedSnippet != null) {
                    Text(
                        text = "Mensaje fijado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onOpenInfo) {
                Icon(Icons.Filled.MoreVert, "Información")
            }
            if (state.hasPinnedMessage) {
                IconButton(onClick = onUnpin) {
                    Icon(Icons.Filled.PushPin, "Desfijar")
                }
            }
        }
    )
}
