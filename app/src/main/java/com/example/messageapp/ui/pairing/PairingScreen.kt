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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messageapp.viewmodel.PairingUiState
import com.example.messageapp.viewmodel.PairingViewModel

/**
 * Pantalla de Emparejamiento por Código
 *
 * Permite:
 * - Ver mi código de 6 dígitos
 * - Ingresar código de otra persona
 * - Vincularse con ese código
 *
 * Refactorizada: 167 líneas → 4 composables
 * - PairingScreen (orquestación): ~35 líneas
 * - PairingMyCode: ~50 líneas
 * - PairingEnterCode: ~55 líneas
 * - PairingStatusCard: ~25 líneas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    viewModel: PairingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
    onPaired: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pairingStatus by viewModel.pairingStatus.collectAsStateWithLifecycle()

    // Código ingresado por el usuario
    var code by remember { mutableStateOf("") }

    // Mostrar diálogo de error
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Cargar estado inicial
    LaunchedEffect(Unit) {
        viewModel.loadPairingStatus()
    }

    // Manejar estados
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PairingUiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
            }
            is PairingUiState.Paired -> {
                onPaired() // Navegar al chat
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { PairingTopBar(onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PairingMyCode(
                pairingStatus = pairingStatus,
                onGenerateCode = { viewModel.generatePairingCode() }
            )
            PairingEnterCode(
                code = code,
                onCodeChange = {
                    // Solo permitir números y máximo 6 dígitos
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        code = it
                    }
                },
                isLoading = uiState is PairingUiState.Loading,
                onPair = { viewModel.pairWithCode(code) }
            )
            if (pairingStatus?.isPaired == true) {
                PairingStatusCard()
            }
        }
    }

    // Diálogo de error
    if (showErrorDialog) {
        PairingErrorDialog(
            errorMessage = errorMessage,
            onDismiss = {
                showErrorDialog = false
                viewModel.clearError()
            }
        )
    }
}

/**
 * TopBar de la pantalla de emparejamiento
 */
@Composable
private fun PairingTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Emparejar Dispositivo") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver")
            }
        }
    )
}

/**
 * Tarjeta con mi código de emparejamiento
 */
@Composable
private fun PairingMyCode(
    pairingStatus: com.example.messageapp.data.PairingStatus?,
    onGenerateCode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu código de emparejamiento",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar código o botón para generar
            if (pairingStatus?.pairingCode != null) {
                Text(
                    text = pairingStatus.pairingCode ?: "",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp
                )

                Text(
                    text = "Comparte este código con tu pareja",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Button(onClick = onGenerateCode) {
                    Text("Generar Código")
                }
            }
        }
    }
}

/**
 * Formulario para ingresar código de otra persona
 */
@Composable
private fun PairingEnterCode(
    code: String,
    onCodeChange: (String) -> Unit,
    isLoading: Boolean,
    onPair: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Tienes un código de tu pareja?",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input de código de 6 dígitos
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                label = { Text("Código de 6 dígitos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("123456") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onPair,
                enabled = code.length == 6 && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Vincular")
                }
            }
        }
    }
}

/**
 * Tarjeta de estado de emparejamiento exitoso
 */
@Composable
private fun PairingStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "¡Ya estás emparejado!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Diálogo de error
 */
@Composable
private fun PairingErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}
