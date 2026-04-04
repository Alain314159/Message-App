package com.example.messageapp.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.messageapp.data.AuthRepository
import com.example.messageapp.data.ProfileRepository
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.auth.auth
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
    val auth = remember { client.auth }
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository() }
    val profileRepo = remember { ProfileRepository() }

    // ✅ CORREGIDO: Obtener usuario actual de Supabase
    val uid = auth.currentUserOrNull()?.id?.toString() ?: return

    var name by remember { mutableStateOf("") }
    var bio  by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        // Note: getUserProfile pendiente de implementar en ProfileRepository con Supabase
        runCatching {
            // Por ahora solo logueamos el UID
            Log.d(TAG, "Cargando perfil de $uid")
            // val bio = profileRepo.getUserProfile(uid)?.bio
            // val photoUrl = profileRepo.getUserProfile(uid)?.photoUrl
        }
    }

    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                busy = true
                profileRepo.uploadAvatar(uri).onSuccess { url ->
                    photoUrl = url
                    msg = "Foto atualizada!"
                }.onFailure { e ->
                    msg = e.message ?: "Erro ao fazer upload da foto"
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
                    // Note: updateProfile ya está implementado en ProfileRepository
                    runCatching {
                        profileRepo.updateProfile(name, bio)
                    }.onSuccess { msg = "Salvo!" }
                        .onFailure { e ->
                            msg = e.message ?: "Erro ao salvar"
                            Log.e(TAG, "Error al guardar perfil", e)
                        }
                }
            }) { Text("Salvar") }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                enabled = !busy,
                onClick = {
                    scope.launch {
                        // ✅ CORREGIDO: signOut en lugar de signOutAndRemoveToken
                        repo.signOut().fold(
                            onSuccess = { onLoggedOut() },
                            onFailure = { error ->
                                android.util.Log.w("ProfileScreen", "Sign out failed", error)
                                onLoggedOut() // Still logout even if there's an error
                            }
                        )
                    }
                }
            ) { Text("Sair da conta") }

            msg?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
