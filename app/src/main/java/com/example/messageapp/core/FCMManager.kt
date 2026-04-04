package com.example.messageapp.core

import android.content.Context
import android.util.Log
import com.example.messageapp.data.AuthRepository
import com.example.messageapp.data.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "MessageApp"

/**
 * Gestiona la inicialización y actualización de FCM
 *
 * Responsabilidad única: Manejar el ciclo de vida de Firebase Cloud Messaging
 *
 * Uso:
 * - initialize(): Inicializa FCM al iniciar la app
 * - updateTokenAfterLogin(): Actualiza token después de login exitoso
 */
class FCMManager(
    private val context: Context,
    private val lifecycleScope: CoroutineScope,
    private val notificationRepo: NotificationRepository = NotificationRepository()
) {

    /**
     * Inicializa FCM al iniciar la aplicación
     */
    fun initialize() {
        notificationRepo.initialize(context)
        Log.d(TAG, "FCM initialized")
    }

    /**
     * Actualiza el token FCM en Supabase después de un login exitoso
     */
    fun updateTokenAfterLogin() {
        lifecycleScope.launch {
            try {
                val token = notificationRepo.getRegistrationId()
                if (token.isNotBlank()) {
                    // TODO: Implementar actualización de token en Supabase
                    // AuthRepository().updateFcmToken(token)
                    Log.d(TAG, "FCM token updated: ${token.take(10)}...")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error updating FCM token", e)
            }
        }
    }
}
