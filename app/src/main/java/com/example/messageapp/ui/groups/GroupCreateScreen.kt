package com.example.messageapp.ui.groups

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.launch

// Tag constante para logging
private const val TAG = "MessageApp"

data class UserItem(val uid: String, val name: String, val photo: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateScreen(
    onCreated: (String) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit = {}
) {
    // ✅ CORREGIDO: Usar Supabase en lugar de Firebase
    val client = remember { SupabaseConfig.client }
    val myUid = remember { client.auth.currentUserOrNull()?.id?.value.orEmpty() }
    val repo = remember { ChatRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<UserItem>()) }
    val selected = remember { mutableStateListOf<String>() }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }

    // Garante eu mesmo selecionado
    LaunchedEffect(myUid) {
        if (myUid.isNotBlank() && !selected.contains(myUid)) selected += myUid
    }

    // Carrega usuários (simples)
    LaunchedEffect(Unit) {
        val qs = db.collection("users").get().await()
        users = qs.documents.map { d ->
            UserItem(
                uid = d.id,
                name = d.getString("displayName") ?: "@${d.id.take(6)}",
                photo = d.getString("photoUrl")
            )
        }
    }

    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(enabled = !creating, onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = { Text("Novo grupo") }
            )
        }
    ) { insets ->
        Column(Modifier.padding(insets).padding(16.dp)) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nome do grupo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !creating
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedButton(enabled = !creating, onClick = { pick.launch("image/*") }) {
                    Text(if (photoUri == null) "Foto do grupo" else "Trocar foto")
                }
                Spacer(Modifier.width(12.dp))
                photoUri?.let { Image(rememberAsyncImagePainter(it), null, Modifier.size(48.dp)) }
            }
            Spacer(Modifier.height(12.dp))

            Text("Participantes", style = MaterialTheme.typography.titleMedium)
            LazyColumn(Modifier.weight(1f)) {
                items(users) { u ->
                    val checked = selected.contains(u.id)
                    ListItem(
                        headlineContent = { Text(u.name) },
                        supportingContent = { Text("@${u.id.take(6)}") },
                        leadingContent = {
                            Image(rememberAsyncImagePainter(u.photo), null, Modifier.size(40.dp))
                        },
                        trailingContent = {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    if (creating) return@Checkbox
                                    if (it) selected.add(u.id) else selected.remove(u.id)
                                }
                            )
                        }
                    )
                    Divider()
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(enabled = !creating, onClick = onCancel) { Text("Cancelar") }
                Button(
                    enabled = !creating,
                    onClick = {
                        val trimmed = name.trim()
                        if (trimmed.isEmpty()) { msg = "Informe o nome do grupo"; return@Button }
                        if (selected.isEmpty()) { msg = "Selecione ao menos 1 participante"; return@Button }

                        creating = true
                        scope.launch {
                            try {
                                // TODO: Implementar createGroup en ChatRepository con Supabase
                                // Por ahora, mostrar mensaje de no implementado
                                Log.w(TAG, "Creación de grupos no está implementada aún con Supabase")
                                msg = "Función no disponible - pendiente de implementación"
                                
                                // 1) Cria o grupo
                                // val chatId = repo.createGroup(...)
                                
                                // 2) NAVEGA JÁ pro chat
                                // onCreated(chatId)
                                
                                // 3) (Assíncrono) sobe foto e atualiza photoUrl
                                // TODO: Migrar FirebaseStorage → Supabase Storage
                                // if (photoUri != null) { ... }
                            } catch (e: Exception) {
                                msg = e.message
                                Log.e(TAG, "Error al crear grupo", e)
                            } finally {
                                creating = false
                            }
                        }
                    }
                ) { Text(if (creating) "Criando…" else "Criar") }
            }

            msg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
