package com.example.messageapp.ui.chat

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
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.data.StorageRepository
import com.example.messageapp.model.Message
import com.example.messageapp.storage.StorageAcl
import com.example.messageapp.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(chatId: String, vm: ChatViewModel, onBack: () -> Unit = {}, onOpenInfo: (String) -> Unit = {}) {
    val chat by vm.chat.collectAsState()
    val msgs by vm.messages.collectAsState()
    val myUid = remember { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
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
        vm.start(chatId)
        if (myUid.isNotBlank()) { vm.markRead(chatId, myUid); StorageAcl.ensureMemberMarker(chatId, myUid) }
    }
    DisposableEffect(Unit) { onDispose { vm.stop() } }

    Scaffold(
        topBar = { ChatTopBar(ChatTopBarState(chat?.name, chat?.pinnedSnippet != null, chat?.pinnedSnippet), onBack, { onOpenInfo(chatId) }, { vm.unpin(chatId) }) },
        floatingActionButton = {
            AnimatedVisibility(visible = showScrollToBottom, enter = fadeIn(), exit = fadeOut()) {
                FloatingActionButton(onClick = { scope.launch { listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1) } }) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Ir al fim")
                }
            }
        }
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            if (chat?.pinnedSnippet != null) PinnedMessageBar(chat?.pinnedSnippet) { vm.unpin(chatId) }
            OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Buscar…") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(12.dp))
            if (query.text.isNotBlank()) SearchNavigation(matchIds, currentMatchIdx) { currentMatchIdx = (it + matchIds.size) % matchIds.size }
            ChatMessageList(grouped, listState, query.text.trim(), myUid) { selected = it }
            ChatAttachmentBar(pickers.image, pickers.video, pickers.audio, pickers.file)
            ChatMessageInput(input, { input = it }) {
                val text = input.text.trim()
                if (text.isNotBlank() && myUid.isNotBlank()) { vm.sendText(chatId, myUid, text); input = TextFieldValue("") }
            }
        }
    }
    if (selected != null) ChatActionsDialog(ChatActionsDialogState(selected, selected!!.senderId == myUid, selected!!.deletedForAll == true), { vm.pin(chatId, selected!!) }, { scope.launch { repo.hideMessageForUser(chatId, selected!!.id, myUid); selected = null } }, { scope.launch { repo.deleteMessageForAll(chatId, selected!!.id); selected = null } }) { selected = null }
}
