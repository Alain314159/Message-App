package com.example.messageapp.ui.groups

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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

// Tag constante para logging
private const val TAG = "MessageApp"

data class UserItem(val uid: String, val name: String, val photo: String?)

/**
 * Pantalla de Creación de Grupo
 *
 * Refactorizada: 109 líneas → 3 composables
 * - GroupCreateScreen (orquestación): ~40 líneas
 * - GroupCreateForm: ~40 líneas
 * - GroupCreateMemberList: ~25 líneas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateScreen(
    onCreated: (String) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit = {}
) {
    // ✅ CORREGIDO: Usar Supabase en lugar de Firebase
    val client = remember { SupabaseConfig.client }
    val auth = remember { client.auth }
    val db = remember { client.postgrest }
    val myUid = remember { auth.currentUserOrNull()?.id?.toString().orEmpty() }
    val repo = remember { ChatRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<UserItem>()) }
    val selected: SnapshotStateList<String> = remember { mutableStateListOf<String>() }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }

    // Garante eu mesmo selecionado
    LaunchedEffect(myUid) {
        if (myUid.isNotBlank() && !selected.contains(myUid)) selected += myUid
    }

    // Carrega usuarios (Supabase)
    LaunchedEffect(Unit) {
        try {
            val result: List<Map<String, Any?>> = db.from("users").select().decodeList()
            users = result.map { m ->
                val uid = m["id"] as? String ?: ""
                UserItem(
                    uid = uid,
                    name = (m["displayName"] as? String) ?: "@${uid.take(6)}",
                    photo = m["photoUrl"] as? String
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading users", e)
        }
    }

    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    Scaffold(
        topBar = {
            GroupCreateTopBar(
                onBack = onBack,
                enabled = !creating
            )
        }
    ) { insets ->
        Column(Modifier.padding(insets).padding(16.dp)) {
            GroupCreateForm(
                name = name,
                onNameChange = { name = it },
                photoUri = photoUri,
                onPickPhoto = { pick.launch("image/*") },
                creating = creating,
                msg = msg
            )
            Spacer(Modifier.height(12.dp))
            GroupCreateMemberList(
                users = users,
                selected = selected,
                creating = creating
            )
            Spacer(Modifier.height(12.dp))
            GroupCreateActions(
                creating = creating,
                onCancel = onCancel,
                onCreate = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) { msg = "Informe o nome do grupo"; return@GroupCreateActions }
                    if (selected.isEmpty()) { msg = "Selecione ao menos 1 participante"; return@GroupCreateActions }

                    creating = true
                    scope.launch {
                        try {
                            // Note: createGroup no está implementado en ChatRepository
                            Log.w(TAG, "Creación de grupos no está implementada aún con Supabase")
                            msg = "Función no disponible - pendiente de implementación"
                        } catch (e: Exception) {
                            msg = e.message ?: "Erro ao criar grupo"
                            Log.e(TAG, "Error al crear grupo", e)
                        } finally {
                            creating = false
                        }
                    }
                }
            )
        }
    }
}

/**
 * TopBar de la pantalla de creación de grupo
 */
@Composable
private fun GroupCreateTopBar(onBack: () -> Unit, enabled: Boolean) {
    TopAppBar(
        navigationIcon = {
            IconButton(enabled = enabled, onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }
        },
        title = { Text("Novo grupo") }
    )
}

/**
 * Formulario de creación de grupo (nombre y foto)
 */
@Composable
private fun GroupCreateForm(
    name: String,
    onNameChange: (String) -> Unit,
    photoUri: Uri?,
    onPickPhoto: () -> Unit,
    creating: Boolean,
    msg: String?
) {
    OutlinedTextField(
        value = name, onValueChange = onNameChange,
        label = { Text("Nome do grupo") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !creating
    )
    Spacer(Modifier.height(8.dp))
    Row {
        OutlinedButton(enabled = !creating, onClick = onPickPhoto) {
            Text(if (photoUri == null) "Foto do grupo" else "Trocar foto")
        }
        Spacer(Modifier.width(12.dp))
        photoUri?.let { Image(rememberAsyncImagePainter(it), null, Modifier.size(48.dp)) }
    }
    Spacer(Modifier.height(12.dp))
    msg?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = MaterialTheme.colorScheme.error)
    }
}

/**
 * Lista de miembros para seleccionar
 */
@Composable
private fun GroupCreateMemberList(
    users: List<UserItem>,
    selected: MutableList<String>,
    creating: Boolean
) {
    Text("Participantes", style = MaterialTheme.typography.titleMedium)
    LazyColumn(Modifier.weight(1f)) {
        items(users) { u ->
            val checked = selected.contains(u.uid)
            ListItem(
                headlineContent = { Text(u.name) },
                supportingContent = { Text("@${u.uid.take(6)}") },
                leadingContent = {
                    Image(rememberAsyncImagePainter(u.photo), null, Modifier.size(40.dp))
                },
                trailingContent = {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { isChecked ->
                            if (creating) return@Checkbox
                            if (isChecked) selected.add(u.uid) else selected.remove(u.uid)
                        }
                    )
                }
            )
            Divider()
        }
    }
}

/**
 * Acciones de crear y cancelar
 */
@Composable
private fun GroupCreateActions(
    creating: Boolean,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(enabled = !creating, onClick = onCancel) { Text("Cancelar") }
        Button(
            enabled = !creating,
            onClick = onCreate
        ) { Text(if (creating) "Criando…" else "Criar") }
    }
}
