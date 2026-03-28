package com.example.messageapp.ui.chat

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        // val snap = db.collection("chats").document(chatId).get().await()
        // type = snap.getString("type") ?: "direct"
        // title = snap.getString("name") ?: "Conversa"
        // photo = snap.getString("photoUrl")
        // owner = snap.getString("ownerId")
        // val memberIds = snap.get("members")?.safeCastToList<String>().orEmpty()
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

    // ✅ CORREGIDO: Scaffold DENTRO de la función (antes estaba fuera)
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                title = { Text(if (type == "group") "Informações do grupo" else "Perfil") }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {

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
                        Text("${members.size} participantes", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (type == "group") {
                    OutlinedButton(
                        enabled = !loading,
                        onClick = { pickGroupPhoto.launch(arrayOf("image/*")) }
                    ) { Text(if (loading) "Enviando..." else "Trocar foto") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

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
                            Text("@${m.id.take(6)}")
                        }
                    }
                }
            } else {
                Text("Participantes", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(members, key = { it.id }) { m ->
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
                                if (owner == m.id) Text("Administrador")
                                else Text("@${m.id.take(6)}")
                            }
                        )
                        Divider()
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            try {
                                repo.hideChatForUser(chatId, myUid)
                                onBack()
                            } finally { loading = false }
                        }
                    }
                ) { Text("Esconder chat") }

                if (type == "group") {
                    OutlinedButton(
                        enabled = !loading,
                        onClick = {
                            scope.launch {
                                loading = true
                                try {
                                    repo.leaveGroup(chatId, myUid)
                                    onBack()
                                } finally { loading = false }
                            }
                        }
                    ) { Text("Sair do grupo") }

                    if (owner == myUid) {
                        Button(
                            enabled = !loading,
                            onClick = {
                                scope.launch {
                                    loading = true
                                    try {
                                        repo.deleteGroup(chatId)
                                        onBack()
                                    } finally { loading = false }
                                }
                            }
                        ) { Text("Apagar grupo para todos") }
                    }
                }
            }
        }
    }
}
