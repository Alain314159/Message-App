package com.example.messageapp.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Composable para solicitar permiso de notificaciones una sola vez
 *
 * Responsabilidad única: Manejar la solicitud de permiso POST_NOTIFICATIONS
 * en Android 13+ (API 33+).
 *
 * Uso:
 * ```
 * RequestNotificationPermission()
 * ```
 */
@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // El resultado se maneja automáticamente por el sistema
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            launcher.launch(permission)
        }
    }
}
