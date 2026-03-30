package com.example.messageapp.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.messageapp.data.AuthRepository
import kotlinx.coroutines.launch

// Tag constante para logging
private const val TAG = "MessageApp"

@Composable
fun AuthScreen(
    repo: AuthRepository,
    onLogged: () -> Unit
) {
    val ctx = LocalContext.current
    val act = ctx as? Activity ?: return
    var tab by remember { mutableStateOf(0) } // 0=Email, 1=Telefone

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Entrar no Chat", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Email") })
            // Tab deshabilitado hasta implementar phone auth con Supabase
            // Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Telefone") })
        }
        Spacer(Modifier.height(12.dp))

        when (tab) {
            0 -> EmailAuthSection(repo, onLogged)
            // else -> PhoneAuthSection(repo, act, onLogged)  // Pendiente de implementación
        }
    }
}


@Composable
private fun EmailAuthSection(
    repo: AuthRepository,
    onLogged: () -> Unit
) {
    var mode by remember { mutableStateOf(0) } // 0=entrar, 1=cadastrar
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var msg  by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("Entrar") })
            FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("Criar conta") })
        }

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Outlined.AlternateEmail, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pass, onValueChange = { pass = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Outlined.Key, null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            enabled = email.isNotBlank() && pass.length >= 6,
            onClick = {
                scope.launch {
                    runCatching {
                        // ✅ CORREGIDO: Nombres correctos de métodos
                        if (mode == 0) repo.signInWithEmail(email, pass)
                        else repo.signUpWithEmail(email, pass)
                    }.onSuccess { onLogged() }
                        .onFailure { e ->
                            msg = e.message ?: "Erro no login"
                            Log.e(TAG, "Auth failed: ${e.message}", e)
                        }
                }
            }
        ) { Text(if (mode == 0) "Entrar" else "Criar conta") }

        TextButton(onClick = {
            if (email.isNotBlank()) {
                scope.launch {
                    // Note: sendPasswordReset no está implementado en AuthRepository
                    Log.w(TAG, "Password reset no está implementado aún")
                    msg = "Función no disponible"
                }
            } else msg = "Preencha o email."
        }) { Text("Esqueci minha senha") }

        if (msg != null) Text(msg, color = MaterialTheme.colorScheme.error)
    }
}

// ============================================================================
// PHONE AUTH SECTION - COMENTADO HASTA IMPLEMENTACIÓN CON SUPABASE
// ============================================================================
// Los siguientes métodos de AuthRepository no existen:
// - phoneVerifyCallbacks()
// - startPhoneVerification()
// - signInWithPhoneCredential()
// - upsertUserProfile()
// - saveFcmTokenInBackground()
// ============================================================================
/*
@Composable
private fun PhoneAuthSection(
    repo: AuthRepository,
    activity: Activity,
    onLogged: () -> Unit
) {
    var phone by remember { mutableStateOf("+55") }
    var code  by remember { mutableStateOf("") }
    var vid   by remember { mutableStateOf<String?>(null) }
    var msg   by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        if (vid == null) {
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Telefone (+55...)") },
                leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                // Note: Phone auth pendiente de implementar con Supabase
                Log.w(TAG, "Phone auth no está implementado aún")
                msg = "Phone auth no disponible"
            }) { Text("Enviar código") }

        } else {
            OutlinedTextField(
                value = code, onValueChange = { code = it },
                label = { Text("Código") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                // Note: Phone auth pendiente de implementar con Supabase
                Log.w(TAG, "Phone auth no está implementado aún")
                msg = "Phone auth no disponible"
            }) { Text("Confirmar") }
        }

        if (msg != null) Text(msg, color = MaterialTheme.colorScheme.error)
    }
}
