package com.example.messageapp.ui.avatar

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.Image
import com.example.messageapp.model.AvatarType
import com.example.messageapp.viewmodel.AvatarUiState
import com.example.messageapp.viewmodel.AvatarViewModel

/**
 * Pantalla para seleccionar avatar entre Koala y Cerdita
 */
@Composable
fun AvatarPickerScreen(
    viewModel: AvatarViewModel,
    userId: String,
    onAvatarSelected: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAvatar by viewModel.selectedAvatar.collectAsStateWithLifecycle()
    val avatars = viewModel.getAllAvatars()

    // Cargar avatar actual al iniciar
    LaunchedEffect(userId) {
        viewModel.loadUserAvatar(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Elige tu Avatar",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Selecciona entre Koala o Cerdita",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Selector de avatares
        AvatarSelector(
            avatars = avatars,
            selectedAvatar = selectedAvatar,
            onAvatarSelected = { viewModel.selectAvatar(it) },
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Avatar seleccionado preview
        AvatarPreview(
            avatarType = selectedAvatar,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Mensajes de estado
        if (uiState.saveSuccess == true) {
            Text(
                text = "✅ ¡Avatar guardado exitosamente!",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (uiState.errorMessage != null) {
            Text(
                text = "❌ Error: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    viewModel.saveAvatar(userId)
                    onAvatarSelected()
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving && !uiState.isLoading
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}

/**
 * Componente para mostrar el selector de avatares
 */
@Composable
private fun AvatarSelector(
    avatars: List<AvatarType>,
    selectedAvatar: AvatarType,
    onAvatarSelected: (AvatarType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(avatars) { avatar ->
            AvatarOption(
                avatarType = avatar,
                isSelected = avatar == selectedAvatar,
                onClick = { onAvatarSelected(avatar) }
            )
        }
    }
}

/**
 * Componente individual para cada opción de avatar
 */
@Composable
private fun AvatarOption(
    avatarType: AvatarType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // Círculo del avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                    } else {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                    }
                )
        ) {
            // Imagen real del avatar
            Image(
                painter = painterResource(id = avatarType.drawableResId),
                contentDescription = avatarType.displayName,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Nombre del avatar
        Text(
            text = avatarType.displayName,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Componente para mostrar preview grande del avatar seleccionado
 */
@Composable
private fun AvatarPreview(
    avatarType: AvatarType,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "Vista previa:",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            // Imagen real del avatar en vista previa
            Image(
                painter = painterResource(id = avatarType.drawableResId),
                contentDescription = avatarType.displayName,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
