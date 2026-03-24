package com.example.messageapp.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.messageapp.model.Message
import com.example.messageapp.utils.Crypto

@Composable
fun MessageBubble(m: Message, isMine: Boolean, authorName: String? = null, authorPhoto: String? = null, onLongPress: () -> Unit = {}, highlight: String = "") {
    val bgMine = MaterialTheme.colorScheme.primaryContainer
    val bgOther = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val bubbleColor = if (isMine) bgMine else bgOther
    val isDeletedAll = m.deletedForAll || m.type == "deleted"
    val plain = if (m.type == "text") Crypto.decrypt(m.textEnc) else ""

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)) {
        if (!isMine) Spacer(Modifier.weight(0.15f)) else Spacer(Modifier.weight(0.35f))
        Surface(color = bubbleColor, shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp, modifier = Modifier.weight(0.5f).pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                MessageAuthorRow(isMine, authorName, authorPhoto, m.senderId)
                Spacer(Modifier.heightIn(min = 6.dp))
                if (isDeletedAll) {
                    Text(text = "Mensaje eliminado", style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    when (m.type) {
                        "text" -> {
                            val content = if (highlight.isBlank()) AnnotatedString(plain) else buildHighlighted(plain, highlight)
                            Text(content, color = textColor)
                        }
                        else -> MessageMediaContent(m)
                    }
                }
                Spacer(Modifier.heightIn(min = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = com.example.messageapp.utils.Time.timeFor(m.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    if (isMine) DeliveryTicks(m)
                }
            }
        }
        if (isMine) Spacer(Modifier.weight(0.15f)) else Spacer(Modifier.weight(0.35f))
    }
}
