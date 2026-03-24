package com.example.messageapp.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.messageapp.model.Message

data class ChatActionsDialogState(
    val selectedMessage: Message?,
    val isMine: Boolean,
    val isDeleted: Boolean
)

@Composable
fun ChatActionsDialog(
    state: ChatActionsDialogState,
    onPin: () -> Unit,
    onHideForMe: () -> Unit,
    onDeleteForAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val msg = state.selectedMessage ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acciones del mensaje") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!state.isDeleted) {
                    TextButton(onClick = {
                        onPin()
                        onDismiss()
                    }) {
                        Text("Fijar")
                    }
                }
                TextButton(onClick = {
                    onHideForMe()
                    onDismiss()
                }) {
                    Text("Excluir para mí")
                }
                if (state.isMine && !state.isDeleted) {
                    TextButton(onClick = {
                        onDeleteForAll()
                        onDismiss()
                    }) {
                        Text("Excluir para todos")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
