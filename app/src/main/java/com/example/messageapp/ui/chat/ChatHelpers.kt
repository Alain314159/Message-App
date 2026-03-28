package com.example.messageapp.ui.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Tag constante para logging
private const val TAG = "MessageApp.ChatHelpers"

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
                    try {
                        context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (myUid.isNotBlank()) {
                            scope.launch { storage.sendMedia(chatId, myUid!!, it, "image") }
                        }
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Permission denied for image URI", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process image URI", e)
                    }
                }
            },
            video = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (myUid.isNotBlank()) {
                            scope.launch { storage.sendMedia(chatId, myUid!!, it, "video") }
                        }
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Permission denied for video URI", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process video URI", e)
                    }
                }
            },
            audio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (myUid.isNotBlank()) {
                            scope.launch { storage.sendMedia(chatId, myUid!!, it, "audio") }
                        }
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Permission denied for audio URI", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process audio URI", e)
                    }
                }
            },
            file = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (myUid.isNotBlank()) {
                            scope.launch { storage.sendMedia(chatId, myUid!!, it, "file") }
                        }
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Permission denied for file URI", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process file URI", e)
                    }
                }
            }
        )
    }
}

// ============================================================================
// CARGA DE USUARIOS (para mostrar nombres/fotos de remitentes)
// NOTE: Esta función requiere migración a Supabase - temporalmente deshabilitada
// ============================================================================

private data class SenderUi(val name: String, val photo: String?)

@Composable
fun rememberUsers(msgs: List<Message>): Map<String, SenderUi> {
    val users = remember { mutableStateMapOf<String, SenderUi>() }
    // TODO: Migrar a Supabase cuando esté disponible el repository de usuarios
    // Por ahora, retornar mapa vacío - la UI debe manejar nulls
    return users
}

@Composable
fun rememberGroupedMessagesWithAuthors(msgs: List<Message>, queryText: String, myUid: String, users: Map<String, SenderUi>): List<Pair<String, List<MessageWithAuthor>>> {
    return remember(msgs, queryText, users) {
        val base = msgs.filter { !it.deletedFor.getOrDefault(myUid, false) }
        val filtered = if (queryText.isBlank()) {
            base
        } else {
            base.filter { 
                it.type == "text" && it.textEnc != null && 
                runCatching { Crypto.decrypt(it.textEnc!!) }.getOrElse { "" }.contains(queryText, ignoreCase = true) 
            }
        }
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
        if (queryText.isBlank()) {
            emptyList()
        } else {
            msgs.filter { 
                it.type == "text" && it.textEnc != null && 
                runCatching { Crypto.decrypt(it.textEnc!!) }.getOrElse { "" }.contains(queryText, ignoreCase = true) 
            }.map { it.id }
        }
    }
}
