package com.example.messageapp.ui.chat
import io.github.jan.supabase.auth.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.data.StorageRepository
import com.example.messageapp.model.Message
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

// ✅ TAG constante para logging consistente
private const val TAG = "MessageApp"

@Composable
fun ChatScreen(chatId: String, vm: ChatViewModel, onBack: () -> Unit = {}, onOpenInfo: (String) -> Unit = {}) {
    // ✅ CORREGIDO: collectAsStateWithLifecycle en lugar de collectAsState
    val chat by vm.chat.collectAsStateWithLifecycle()
    val msgs by vm.messages.collectAsStateWithLifecycle()
    // ✅ CORREGIDO: Usar Supabase en lugar de Firebase
    val myUid = remember { SupabaseConfig.client.auth.currentUserOrNull()?.id.orEmpty() }
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var selected by remember { mutableStateOf<Message?>(null) }
    val listState: LazyListState = rememberLazyListState()
    val context = LocalContext.current
    val storage = remember { StorageRepository() }
    val repo = remember { ChatRepository() }

    val pickers = rememberMediaPickers(chatId, myUid, storage, scope, context)
    val users = rememberUsers(msgs)
    val grouped = rememberGroupedMessagesWithAuthors(msgs, query.text.trim(), myUid, users)
    val matchIds = rememberSearchMatches(msgs, query.text.trim())
    var currentMatchIdx by remember(query) { mutableIntStateOf(0) }
    val showScrollToBottom by remember { derivedStateOf { listState.canScrollForward } }

    LaunchedEffect(chatId) {
        vm.start(chatId, myUid)
        // ✅ CORREGIDO: Nombres de métodos correctos + eliminar StorageAcl
        if (myUid.isNotBlank()) { 
            vm.markAsRead(chatId, myUid)
            // StorageAcl migrado a Supabase Storage - pendiente de implementación
        }
    }
    DisposableEffect(Unit) { onDispose { vm.stop() } }

    Scaffold(
        topBar = { 
            ChatTopBar(
                ChatTopBarState(
                    chat?.memberIds?.joinToString(", ") ?: "Chat",
                    chat?.pinnedSnippet != null,
                    chat?.pinnedSnippet
                ),
                onBack,
                { onOpenInfo(chatId) },
                { /* vm.unpinMessage(chatId) */ }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = showScrollToBottom, enter = fadeIn(), exit = fadeOut()) {
                FloatingActionButton(
                    onClick = { 
                        scope.launch { 
                            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1) 
                        } 
                    }
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Ir al fim")
                }
            }
        }
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            if (chat?.pinnedSnippet != null) {
                PinnedMessageBar(chat?.pinnedSnippet) { 
                    // vm.unpinMessage(chatId) // TODO // ✅ CORREGIDO: unpinMessage en lugar de unpin
                }
            }
            OutlinedTextField(
                value = query, 
                onValueChange = { query = it }, 
                placeholder = { Text("Buscar…") }, 
                singleLine = true, 
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
            if (query.text.isNotBlank()) {
                SearchNavigation(matchIds, currentMatchIdx) { 
                    currentMatchIdx = (it + matchIds.size) % matchIds.size 
                }
            }
            ChatMessageList(grouped, listState, query.text.trim(), myUid) { selected = it }
            ChatAttachmentBar(
                onImageClick = { pickers.image.launch(arrayOf("image/*")) },
                onVideoClick = { pickers.video.launch(arrayOf("video/*")) },
                onAudioClick = { pickers.audio.launch(arrayOf("audio/*")) },
                onFileClick = { pickers.file.launch(arrayOf("*/*")) }
            )
            ChatMessageInput(input, { input = it }) {
                val text = input.text.trim()
                if (text.isNotBlank() && myUid.isNotBlank()) { 
                    vm.sendText(chatId, myUid, text)
                    input = TextFieldValue("") 
                }
            }
        }
    }
    // ✅ CORREGIDO: Nombres de métodos correctos del ViewModel
    // ✅ CORREGIDO: Eliminar !! usando safe calls
    selected?.let { selectedMessage ->
        ChatActionsDialog(
            ChatActionsDialogState(
                selectedMessage,
                selectedMessage.senderId == myUid,
                selectedMessage.deletedForAll == true
            ),
            onPin = { /* vm.pinMessage(chatId, selectedMessage) */ },
            onHideForMe = {
                scope.launch {
                    repo.deleteMessageForUser(chatId, selectedMessage.id, myUid)
                    selected = null
                }
            },
            onDeleteForAll = {
                scope.launch {
                    repo.deleteMessageForAll(chatId, selectedMessage.id)
                    selected = null
                }
            },
            onDismiss = { selected = null }
        )
    }
}
