package com.example.messageapp.ui.pairing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messageapp.model.User
import com.example.messageapp.viewmodel.PairingUiState
import com.example.messageapp.viewmodel.PairingViewModel

/**
 * Pantalla de Búsqueda de Pareja por Email
 * 
 * Permite:
 * - Buscar usuario por email
 * - Ver perfil del usuario encontrado
 * - Enviar solicitud de emparejamiento
 */
@Composable
fun FindPartnerScreen(
    viewModel: PairingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
    onPairingComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Email ingresado por el usuario
    var email by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    
    // Mostrar diálogo de error
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Mostrar diálogo de confirmación
    var showConfirmDialog by remember { mutableStateOf(false) }
    var foundUser by remember { mutableStateOf<User?>(null) }
    
    // Manejar estados
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PairingUiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
            }
            is PairingUiState.UserFound -> {
                foundUser = state.user
                showConfirmDialog = true
            }
            is PairingUiState.RequestSent -> {
                onPairingComplete()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar por Email") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Instrucciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Busca a tu pareja por email",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Ingresa el email de la persona con la que quieres emparejarte. Solo se mostrarán usuarios disponibles.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Búsqueda por email
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("pareja@ejemplo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, "Email")
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            hasSearched = true
                            viewModel.searchByEmail(email)
                        },
                        enabled = email.isNotBlank() && 
                                email.contains("@") && 
                                uiState !is PairingUiState.Loading &&
                                !hasSearched,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState is PairingUiState.Loading && !hasSearched) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Buscar")
                        }
                    }
                }
            }
            
            // Estado de carga
            if (uiState is PairingUiState.Loading && hasSearched) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Buscando usuario...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Diálogo de confirmación
    if (showConfirmDialog && foundUser != null) {
        AlertDialog(
            onDismissRequest = { 
                showConfirmDialog = false
                hasSearched = false
                viewModel.clearError()
            },
            icon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Usuario encontrado") },
            text = {
                Column {
                    Text(
                        text = "¿Quieres enviar una solicitud de emparejamiento a:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = foundUser?.displayName ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = foundUser?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendPairingRequest(foundUser?.id ?: return@onClick)
                        showConfirmDialog = false
                    }
                ) {
                    Text("Enviar solicitud")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showConfirmDialog = false
                        hasSearched = false
                        viewModel.clearError()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false 
                viewModel.clearError()
            },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showErrorDialog = false
                        viewModel.clearError()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}
