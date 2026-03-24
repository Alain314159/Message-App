package com.example.messageapp.ui.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.messageapp.data.StorageRepository
import com.example.messageapp.model.Message
import com.example.messageapp.utils.Crypto
import com.example.messageapp.utils.Time
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ============================================================================
// PICKERS DE MEDIOS (Image, Video, Audio, File)
// ============================================================================

class ChatMediaPickers(
    val image: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    val video: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    val audio: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    val file: androidx.activity.result.ActivityResultLauncher<Array<String>>
)

@Composable
fun rememberMediaPickers(chatId: String, myUid: String?, storage: StorageRepository, scope: CoroutineScope, context: Context): ChatMediaPickers {
    return remember {
        ChatMediaPickers(
            image = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    if (myUid.isNotBlank()) scope.launch { storage.sendMedia(chatId, myUid!!, it, "image") }
                }
            },
            video = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    if (myUid.isNotBlank()) scope.launch { storage.sendMedia(chatId, myUid!!, it, "video") }
                }
            },
            audio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    if (myUid.isNotBlank()) scope.launch { storage.sendMedia(chatId, myUid!!, it, "audio") }
                }
            },
            file = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    if (myUid.isNotBlank()) scope.launch { storage.sendMedia(chatId, myUid!!, it, "file") }
                }
            }
        )
    }
}

// ============================================================================
// CARGA DE USUARIOS (para mostrar nombres/fotos de remitentes)
// ============================================================================

private data class SenderUi(val name: String, val photo: String?)

@Composable
fun rememberUsers(msgs: List<Message>): Map<String, SenderUi> {
    val users = remember { mutableStateMapOf<String, SenderUi>() }
    val db = remember { FirebaseFirestore.getInstance() }
    LaunchedEffect(msgs) {
        val missing = msgs.map { it.senderId }.toSet().filter { it.isNotBlank() && !users.containsKey(it) }
        if (missing.isEmpty()) return@LaunchedEffect
        missing.chunked(10).forEach { chunk ->
            db.collection("users").whereIn(FieldPath.documentId(), chunk).get().await().documents.forEach { d ->
                users[d.id] = SenderUi(d.getString("displayName") ?: "@${d.id.take(6)}", d.getString("photoUrl"))
            }
        }
    }
    return users
}

@Composable
fun rememberGroupedMessagesWithAuthors(msgs: List<Message>, queryText: String, myUid: String, users: Map<String, SenderUi>): List<Pair<String, List<MessageWithAuthor>>> {
    return remember(msgs, queryText, users) {
        val base = msgs.filter { !it.deletedFor.getOrDefault(myUid, false) }
        val filtered = if (queryText.isBlank()) base else base.filter { it.type == "text" && Crypto.decrypt(it.textEnc).contains(queryText, ignoreCase = true) }
        val map = linkedMapOf<String, MutableList<MessageWithAuthor>>()
        filtered.forEach { m ->
            val h = Time.headerFor(m.createdAt).ifBlank { " " }
            val author = users[m.senderId]
            map.getOrPut(h) { mutableListOf() }.add(MessageWithAuthor(m, author?.name, author?.photo))
        }
        map.entries.map { it.key to it.value }
    }
}

@Composable
fun rememberSearchMatches(msgs: List<Message>, queryText: String): List<String> {
    return remember(msgs, queryText) {
        if (queryText.isBlank()) emptyList() else msgs.filter { it.type == "text" && Crypto.decrypt(it.textEnc).contains(queryText, ignoreCase = true) }.map { it.id }
    }
}
