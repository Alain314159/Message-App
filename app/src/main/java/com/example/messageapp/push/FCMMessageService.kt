package com.example.messageapp.push

import android.util.Log
import com.example.messageapp.data.FCMNotificationData
import com.example.messageapp.data.FCMTokenRepository
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de Firebase Cloud Messaging para recibir notificaciones
 *
 * Responsabilidad única: Manejar mensajes FCM entrantes y tokens
 *
 * Documentación: https://firebase.google.com/docs/cloud-messaging/android/client
 */
class FCMMessageService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMMessageService"
    }

    // CoroutineScope para operaciones asíncronas en el servicio
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val tokenRepository = FCMTokenRepository()

    /**
     * Called when a new FCM token is generated
     * Envía el token al servidor para actualizar el registro del usuario
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: ${token.take(10)}...")

        // Enviar el token al servidor de forma inmediata
        sendRegistrationToServer(token)
    }

    /**
     * Called when a message is received while app is in foreground
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Manejar notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "Nuevo mensaje"
            val body = notification.body ?: "Tienes un nuevo mensaje"

            Log.d(TAG, "Notification: $title - $body")
            // La notificación se muestra automáticamente por el sistema
        }

        // Manejar data payload (información adicional)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")

            val title = message.data["title"] ?: "Nuevo mensaje"
            val body = message.data["message"] ?: "Tienes un nuevo mensaje"

            // Crear objeto de notificación para manejo interno
            val notificationData = FCMNotificationData(
                title = title,
                message = body,
                data = message.data,
                notificationId = message.data.hashCode(),
                messageId = message.messageId ?: "unknown"
            )

            // Aquí se podría:
            // 1. Enviar evento a través de un Flow/Channel
            // 2. Actualizar base de datos local
            // 3. Trigger de actualización de mensajes
        }
    }

    /**
     * Envía el token de registro al servidor (Supabase)
     *
     * El token se usa para enviar notificaciones push al dispositivo correcto.
     * Se actualiza en la tabla 'users' de Supabase en la columna 'fcm_token'.
     */
    private fun sendRegistrationToServer(token: String) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Sending FCM token to Supabase: ${token.take(10)}...")

                // Obtener el ID del usuario actual
                val userId = SupabaseConfig.client.auth.currentSessionOrNull()?.user?.id

                if (userId != null) {
                    // Actualizar el token en la base de datos
                    SupabaseConfig.client.plugin(Postgrest)
                        .from("users")
                        .update(
                            mapOf("fcm_token" to token)
                        ) {
                            filter { eq("id", userId) }
                        }
                    
                    Log.d(TAG, "✅ FCM token updated in Supabase for user: $userId")
                } else {
                    Log.w(TAG, "⚠️ No user logged in - cannot update FCM token")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send FCM token to Supabase", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
