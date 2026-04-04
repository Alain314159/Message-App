package com.example.messageapp.ui.chat
import io.github.jan.supabase.auth.auth

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.launch

data class MemberUi(val uid: String, val name: String, val photo: String?)

// Tag constante para logging
private const val TAG = "MessageApp"

/**
 * Pantalla de Información del Chat
 *
 * Refactorizada: 147 líneas → 4 composables
 * - ChatInfoScreen (orquestación): ~40 líneas
 * - ChatInfoHeader: ~35 líneas
 * - ChatInfoMembers: ~45 líneas
 * - ChatInfoActions: ~25 líneas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInfoScreen(
    chatId: String,
    onBack: () -> Unit = {}
) {
    // ✅ CORREGIDO: Usar Supabase en lugar de Firebase
    val client = remember { SupabaseConfig.client }
    val repo = remember { ChatRepository() }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var title by remember { mutableStateOf("Informações") }
    var photo by remember { mutableStateOf<String?>(null) }
    var type by remember { mutableStateOf("direct") }
    var members by remember { mutableStateOf(listOf<MemberUi>()) }
    var owner by remember { mutableStateOf<String?>(null) }
    val myUid = remember { client.auth.currentUserOrNull()?.id?.value.orEmpty() }

    var loading by remember { mutableStateOf(false) }

    val pickGroupPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                loading = true
                try {
                    // Note: Cambio de foto de grupo pendiente de implementar con Supabase Storage
                    Log.w(TAG, "Cambio de foto de grupo no implementado con Supabase aún")
                } finally {
                    loading = false
                }
            }
        }
    }

    LaunchedEffect(chatId) {
        // Note: getChatInfo pendiente de implementar en ChatRepository con Supabase
        Log.d(TAG, "Cargando info del chat: $chatId")
    }

    // ✅ HELPER FUNCTION - Safe cast para evitar ClassCastException
    @Suppress("UNCHECKED_CAST")
    fun <T> Any.safeCastToList(): List<T> {
        return try {
            this as? List<T> ?: emptyList()
        } catch (e: ClassCastException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            ChatInfoTopBar(
                title = if (type == "group") "Informações do grupo" else "Perfil",
                onBack = onBack
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            ChatInfoHeader(
                title = title,
                photo = photo,
                type = type,
                membersCount = members.size,
                loading = loading,
                onPickGroupPhoto = { pickGroupPhoto.launch(arrayOf("image/*")) }
            )

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            ChatInfoMembers(
                type = type,
                members = members,
                owner = owner
            )

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            ChatInfoActions(
                type = type,
                owner = owner,
                myUid = myUid,
                chatId = chatId,
                loading = loading,
                repo = repo,
                onBack = onBack
            )
        }
    }
}

/**
 * TopBar de la pantalla de información
 */
@Composable
private fun ChatInfoTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
        },
        title = { Text(title) }
    )
}

/**
 * Header con foto y título del chat
 */
@Composable
private fun ChatInfoHeader(
    title: String,
    photo: String?,
    type: String,
    membersCount: Int,
    loading: Boolean,
    onPickGroupPhoto: () -> Unit
) {
    Row {
        Image(
            painter = rememberAsyncImagePainter(photo),
            contentDescription = null,
            modifier = Modifier.size(72.dp).clip(CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (type == "group") {
                Text("${membersCount} participantes", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (type == "group") {
            OutlinedButton(
                enabled = !loading,
                onClick = onPickGroupPhoto
            ) { Text(if (loading) "Enviando..." else "Trocar foto") }
        }
    }
}

/**
 * Lista de miembros del chat
 */
@Composable
private fun ChatInfoMembers(
    type: String,
    members: List<MemberUi>,
    owner: String?
) {
    if (type == "direct") {
        members.firstOrNull()?.let { m ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(m.photo),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                )
                Column(Modifier.weight(1f)) {
                    Text(m.name, style = MaterialTheme.typography.titleMedium)
                    Text("@${m.uid.take(6)}")
                }
            }
        }
    } else {
        Text("Participantes", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(members, key = { it.uid }) { m ->
                ListItem(
                    leadingContent = {
                        Image(
                            painter = rememberAsyncImagePainter(m.photo),
                            contentDescription = null,
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                        )
                    },
                    headlineContent = { Text(m.name) },
                    supportingContent = {
                        if (owner == m.uid) Text("Administrador")
                        else Text("@${m.uid.take(6)}")
                    }
                )
                Divider()
            }
        }
    }
}

/**
 * Acciones (esconder, sair, apagar)
 */
@Composable
private fun ChatInfoActions(
    type: String,
    owner: String?,
    myUid: String,
    chatId: String,
    loading: Boolean,
    repo: ChatRepository,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            enabled = !loading,
            onClick = {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    repo.hideChatForUser(chatId, myUid)
                    onBack()
                }
            }
        ) { Text("Esconder chat") }

        if (type == "group") {
            OutlinedButton(
                enabled = !loading,
                onClick = {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        repo.leaveGroup(chatId, myUid)
                        onBack()
                    }
                }
            ) { Text("Sair do grupo") }

            if (owner == myUid) {
                Button(
                    enabled = !loading,
                    onClick = {
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            repo.deleteGroup(chatId)
                            onBack()
                        }
                    }
                ) { Text("Apagar grupo para todos") }
            }
        }
    }
}
