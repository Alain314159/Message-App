package com.example.messageapp.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Data class para parámetros de FindPartnerScreen
 * Reduce LongParameterList detekt warning
 */
data class FindPartnerScreenParams(
    val viewModel: PairingViewModel,
    val onBack: () -> Unit,
    val onPairingComplete: () -> Unit
)

/**
 * Pantalla de Búsqueda de Pareja por Email
 *
 * Permite:
 * - Buscar usuario por email
 * - Ver perfil del usuario encontrado
 * - Enviar solicitud de emparejamiento
 *
 * Refactorizada: 213 líneas → 5 composables
 * - FindPartnerScreen (orquestación): ~40 líneas
 * - FindPartnerInstructions: ~35 líneas
 * - FindPartnerSearchForm: ~45 líneas
 * - FindPartnerLoadingState: ~15 líneas
 * - FindPartnerDialogs: ~50 líneas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPartnerScreen(
    viewModel: PairingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
    onPairingComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado local
    var email by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
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
        topBar = { FindPartnerTopBar(onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FindPartnerInstructions()
            FindPartnerSearchForm(
                email = email,
                onEmailChange = { email = it },
                hasSearched = hasSearched,
                isLoading = uiState is PairingUiState.Loading,
                onSearch = {
                    hasSearched = true
                    viewModel.searchByEmail(email)
                }
            )
            if (uiState is PairingUiState.Loading && hasSearched) {
                FindPartnerLoadingState()
            }
        }
    }

    FindPartnerDialogs(
        showConfirmDialog = showConfirmDialog,
        foundUser = foundUser,
        showErrorDialog = showErrorDialog,
        errorMessage = errorMessage,
        onConfirmRequest = {
            viewModel.sendPairingRequest(foundUser?.id ?: return@FindPartnerDialogs)
            showConfirmDialog = false
        },
        onDismissConfirm = {
            showConfirmDialog = false
            hasSearched = false
            viewModel.clearError()
        },
        onDismissError = {
            showErrorDialog = false
            viewModel.clearError()
        }
    )
}

/**
 * TopBar de la pantalla de búsqueda
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FindPartnerTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Buscar por Email") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver")
            }
        }
    )
}

/**
 * Tarjeta de instrucciones
 */
@Composable
private fun FindPartnerInstructions() {
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
}

/**
 * Formulario de búsqueda por email
 */
@Composable
private fun FindPartnerSearchForm(
    email: String,
    onEmailChange: (String) -> Unit,
    hasSearched: Boolean,
    isLoading: Boolean,
    onSearch: () -> Unit
) {
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
                onValueChange = onEmailChange,
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
                onClick = onSearch,
                enabled = email.isNotBlank() &&
                        email.contains("@") &&
                        !isLoading &&
                        !hasSearched,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading && !hasSearched) {
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
}

/**
 * Estado de carga
 */
@Composable
private fun FindPartnerLoadingState() {
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

/**
 * Diálogos de confirmación y error
 */
@Composable
private fun FindPartnerDialogs(
    showConfirmDialog: Boolean,
    foundUser: User?,
    showErrorDialog: Boolean,
    errorMessage: String,
    onConfirmRequest: () -> Unit,
    onDismissConfirm: () -> Unit,
    onDismissError: () -> Unit
) {
    // Diálogo de confirmación
    if (showConfirmDialog && foundUser != null) {
        AlertDialog(
            onDismissRequest = onDismissConfirm,
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
                        text = foundUser.displayName ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = foundUser.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmRequest) {
                    Text("Enviar solicitud")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissConfirm) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("Aceptar")
                }
            }
        )
    }
}
