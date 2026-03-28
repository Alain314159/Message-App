package com.example.messageapp.ui.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.messageapp.data.AuthRepository
import com.example.messageapp.data.ProfileRepository
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.launch

// Tag constante para logging
private const val TAG = "MessageApp"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // ✅ CORREGIDO: Usar Supabase en lugar de Firebase
    val client = remember { SupabaseConfig.client }
    val auth = client.auth
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository() }
    val profileRepo = remember { ProfileRepository() }

    // ✅ CORREGIDO: Obtener usuario actual de Supabase
    val uid = auth.currentUserOrNull()?.id?.value ?: return

    var name by remember { mutableStateOf("") }
    var bio  by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        // TODO: Implementar getUserProfile en ProfileRepository con Supabase
        runCatching {
            // val snap = db.collection("users").document(uid).get().await()
            // bio = snap.getString("bio") ?: ""
            Log.d(TAG, "Cargando perfil de $uid")
            snap.getString("photoUrl")?.let { photoUrl = it }
        }
    }

    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                busy = true
                runCatching {
                    profileRepo.uploadAvatar(uri)
                }.onSuccess { url ->
                    photoUrl = url
                    msg = "Foto atualizada!"
                }.onFailure { e ->
                    msg = e.message
                }
                busy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = { Text("Meu perfil") }
            )
        }
    ) { insets ->
        Column(modifier.padding(insets).padding(16.dp)) {

            val painter = rememberAsyncImagePainter(photoUrl)
            Image(
                painter, contentDescription = null,
                modifier = Modifier.size(96.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                enabled = !busy,
                onClick = { pick.launch("image/*") }
            ) { Text(if (busy) "Enviando..." else "Trocar foto") }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = bio, onValueChange = { bio = it },
                label = { Text("Bio") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Button(enabled = !busy, onClick = {
                scope.launch {
                    // TODO: Implementar updateProfile en ProfileRepository con Supabase
                    runCatching {
                        // val doc = db.collection("users").document(uid)
                        // val data = mutableMapOf<String, Any>(
                        //     "displayName" to name,
                        //     "bio" to bio,
                        //     "lastSeen" to FieldValue.serverTimestamp()
                        // )
                        // photoUrl?.let { data["photoUrl"] = it }
                        // doc.update(data).await()
                        profileRepo.updateProfile(name, bio)
                    }.onSuccess { msg = "Salvo!" }
                        .onFailure { 
                            msg = it.message
                            Log.e(TAG, "Error al guardar perfil", it)
                        }
                }
            }) { Text("Salvar") }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                enabled = !busy,
                onClick = {
                    scope.launch {
                        // ✅ CORREGIDO: signOut en lugar de signOutAndRemoveToken
                        runCatching { repo.signOut() }
                        onLoggedOut()
                    }
                }
            ) { Text("Sair da conta") }

            msg?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
