package com.example.messageapp.ui.chat
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messageapp.model.Message

data class MessageGroup(
    val header: String,
    val messages: List<Message>
)

data class MessageWithAuthor(
    val message: Message,
    val authorName: String?,
    val authorPhoto: String?
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatMessageList(
    groups: List<Pair<String, List<MessageWithAuthor>>>,
    listState: LazyListState,
    searchQuery: String,
    myUid: String,
    onMessageLongPress: (Message) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        for ((header, list) in groups) {
            stickyHeader {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Divider(modifier = Modifier.fillMaxWidth().padding(start = 12.dp))
                    Text(text = header, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
                    Divider(modifier = Modifier.fillMaxWidth().padding(end = 12.dp))
                }
            }
            items(list, key = { it.message.id }) { item ->
                MessageBubble(
                    params = MessageBubbleParams(
                        message = item.message,
                        isMine = item.message.senderId == myUid,
                        authorName = item.authorName,
                        authorPhoto = item.authorPhoto,
                        onLongPress = { onMessageLongPress(item.message) },
                        highlight = searchQuery
                    )
                )
                Spacer(modifier = Modifier.heightIn(min = 2.dp))
            }
        }
    }
}
