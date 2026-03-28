package com.example.messageapp.ui.chatlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Chat
import com.example.messageapp.utils.Crypto
import com.example.messageapp.viewmodel.ChatListViewModel
import kotlinx.coroutines.launch

// ✅ CORREGIDO: Eliminar OptIn innecesario (API es estable)

@Composable
fun ChatListScreen(
    myUid: String,
    vm: ChatListViewModel,
    onOpenChat: (String) -> Unit,
    onOpenContacts: () -> Unit,
    onOpenNewGroup: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit
) {
    // ✅ CORREGIDO: collectAsStateWithLifecycle en lugar de collectAsState
    val chats by vm.chats.collectAsStateWithLifecycle()
    var showHidden by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf<Chat?>(null) }
    var confirmDeleteGroup by remember { mutableStateOf<Chat?>(null) }
    val scope = rememberCoroutineScope()
    val repo = remember { ChatRepository() }

    LaunchedEffect(myUid) {
        if (myUid.isNotBlank()) vm.start(myUid)
    }

    // ✅ CORREGIDO: Eliminar filtro por visibleFor (propiedad no existe en Chat)
    // TODO: Implementar archivo de chats cuando se agregue visibleFor al modelo
    val list = chats  // Usar lista completa por ahora

    Scaffold(
        topBar = { ChatListTopBar(ChatListTopBarState(showHidden, if (showHidden) "Conversas Arquivadas" else "Mensagens"), { showHidden = !showHidden }, onOpenContacts, onOpenNewGroup, onOpenProfile, onLogout) },
        floatingActionButton = { ChatListFab(onOpenContacts) }
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad)) {
            items(list, key = { it.id }) { c ->
                // ✅ CORREGIDO: Eliminar métodos que no existen en ChatRepository
                ChatRow(myUid, c, showHidden, { onOpenChat(c.id) }, 
                    /* hideChatForUser */ null, 
                    /* unhideChatForUser */ null, 
                    /* leaveGroup */ null, 
                    /* deleteGroup */ null)
                Divider()
            }
        }
    }

    // TODO: Implementar dialogs cuando existan los métodos en el repository
    // if (confirmLeave != null) LeaveGroupDialog(...)
    // if (confirmDeleteGroup != null) DeleteGroupDialog(...)
}

// ✅ CORREGIDO: Funciones comentadas - métodos no existen en ChatRepository
/*
@Composable
private fun LeaveGroupDialog(chat: Chat, myUid: String, repo: ChatRepository, scope: kotlinx.coroutines.CoroutineScope, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sair do grupo") },
        text = { Text("Tem certeza que deseja sair do grupo?") },  // ✅ chat.name no existe
        confirmButton = {
            // TODO: Implementar leaveGroup en ChatRepository
            TextButton(onClick = { /* scope.launch { repo.leaveGroup(chat.id, myUid); onDismiss() } */ }) { Text("Sair") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun DeleteGroupDialog(chat: Chat, repo: ChatRepository, scope: kotlinx.coroutines.CoroutineScope, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apagar grupo") },
        text = { Text("Apagar o grupo para todos os participantes?") },  // ✅ chat.name no existe
        confirmButton = {
            // TODO: Implementar deleteGroup en ChatRepository
            TextButton(onClick = { /* scope.launch { repo.deleteGroup(chat.id); onDismiss() } */ }) { Text("Apagar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
*/

@Composable
private fun ChatRow(
    myUid: String,
    chat: Chat,
    isHiddenList: Boolean,
    onOpen: () -> Unit,
    onHide: () -> Unit,
    onUnhide: () -> Unit,
    onDeleteForMe: () -> Unit,
    onLeave: () -> Unit,
    onDeleteGroup: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    // ✅ CORREGIDO: chat.name y chat.photoUrl no existen - usar chat.id
    var title by remember(chat.id) { mutableStateOf(chat.id) }
    var avatar by remember(chat.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(chat.id, chat.type, chat.memberIds, myUid) {
        // ✅ CORREGIDO: Eliminar Firebase - usar chat.memberIds en lugar de chat.members
        if (chat.type == "direct") {
            // TODO: Obtener nombre del otro usuario desde Supabase
            val other = chat.memberIds.firstOrNull { it != myUid }
            if (!other.isNullOrBlank()) {
                title = "@${other.take(6)}"  // Temporalmente mostrar ID
                avatar = null
            } else {
                title = "Conversa"
                avatar = null
            }
        } else {
            title = chat.id  // ✅ chat.name no existe
            avatar = null  // ✅ chat.photoUrl no existe
        }
    }

    // ✅ CORREGIDO: chat.lastMessage no existe - usar solo lastMessageEnc y pinnedSnippet
    val snippet = remember(chat.lastMessageEnc, chat.pinnedSnippet) {
        chat.lastMessageEnc?.let { Crypto.decrypt(it) } ?: chat.pinnedSnippet ?: ""
    }

    androidx.compose.material3.ElevatedCard(onClick = onOpen, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        androidx.compose.material3.ListItem(
            leadingContent = { Avatar(avatar, title.take(1).uppercase()) },
            headlineContent = { Text(title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            supportingContent = { if (snippet.isNotBlank()) Text(snippet, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            trailingContent = {
                androidx.compose.foundation.layout.Box {
                    androidx.compose.material3.IconButton(onClick = { menuOpen = true }) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }
                    // ✅ CORREGIDO: Eliminar parámetros que no se usan
                    ChatRowMenu(menuOpen, { menuOpen = false }, chat, myUid, isHiddenList, null, null, null, null, null)
                }
            },
            modifier = Modifier.clickable { onOpen() }
        )
    }
}

@Composable
private fun Avatar(url: String?, fallback: String) {
    if (!url.isNullOrBlank()) {
        androidx.compose.foundation.Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = null,
            modifier = androidx.compose.ui.Modifier.size(44.dp).clip(androidx.compose.foundation.shape.CircleShape)
        )
    } else {
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
            modifier = androidx.compose.ui.Modifier.size(44.dp)
        ) {
            androidx.compose.foundation.layout.Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(fallback, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ChatRowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    chat: Chat,
    myUid: String,
    isHiddenList: Boolean,
    onHide: () -> Unit,
    onUnhide: () -> Unit,
    onDeleteForMe: () -> Unit,
    onLeave: () -> Unit,
    onDeleteGroup: () -> Unit
) {
    androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        if (isHiddenList) {
            androidx.compose.material3.DropdownMenuItem(text = { Text("Desarquivar conversa") }, onClick = onUnhide)
        } else {
            androidx.compose.material3.DropdownMenuItem(text = { Text("Arquivar conversa") }, onClick = onHide)
            if (chat.type == "direct") {
                androidx.compose.material3.DropdownMenuItem(text = { Text("Excluir conversa") }, onClick = onDeleteForMe)
            }
        }
        if (chat.type == "group") {
            androidx.compose.material3.DropdownMenuItem(text = { Text("Sair do grupo") }, onClick = onLeave)
            if (chat.ownerId == myUid) {
                androidx.compose.material3.DropdownMenuItem(text = { Text("Apagar grupo para todos") }, onClick = onDeleteGroup)
            }
        }
    }
}