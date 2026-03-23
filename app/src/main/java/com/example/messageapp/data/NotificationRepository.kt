package com.example.messageapp.data

import android.content.Context
import com.example.messageapp.supabase.SupabaseConfig
import com.onesignal.OneSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio de Notificaciones usando OneSignal
 * 
 * Reemplaza a Firebase Cloud Messaging (FCM)
 * 
 * Funcionalidades:
 * - Inicializar OneSignal
 * - Obtener Player ID del dispositivo
 * - Suscribirse a notificaciones
 * - Enviar notificaciones push (vía servidor/Edge Function)
 */
class NotificationRepository {
    
    /**
     * Inicializa OneSignal con el App ID configurado
     * Debe llamarse una sola vez, preferiblemente en Application.onCreate()
     */
    fun initialize(context: Context) {
        val appId = SupabaseConfig.ONESIGNAL_APP_ID
        
        if (appId.isBlank() || appId == "TU_ONESIGNAL_APP_ID_AQUI") {
            android.util.Log.w("NotificationRepository", "OneSignal App ID no configurado")
            return
        }
        
        // Inicializar OneSignal
        OneSignal.initWithContext(context)
        OneSignal.setAppId(appId)
        
        android.util.Log.d("NotificationRepository", "OneSignal inicializado correctamente")
    }
    
    /**
     * Obtiene el Player ID único de este dispositivo
     * Este ID se usa para enviar notificaciones push a este dispositivo específico
     */
    suspend fun getPlayerId(): String? = withContext(Dispatchers.IO) {
        try {
            // OneSignal 5.x usa getDeviceState() en lugar de getIds()
            val deviceState = OneSignal.getDeviceState()
            val playerId = deviceState?.userId
            
            android.util.Log.d("NotificationRepository", "Player ID: $playerId")
            playerId
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error getting Player ID", e)
            null
        }
    }
    
    /**
     * Verifica si OneSignal está disponible y configurado
     */
    fun isOneSignalAvailable(): Boolean {
        val appId = SupabaseConfig.ONESIGNAL_APP_ID
        return appId.isNotBlank() && appId != "TU_ONESIGNAL_APP_ID_AQUI"
    }
    
    /**
     * Suscribe el dispositivo a un tema (opcional, para notificaciones grupales)
     */
    suspend fun subscribeToTopic(topic: String) = withContext(Dispatchers.IO) {
        try {
            OneSignal.sendTag(topic, "true")
            android.util.Log.d("NotificationRepository", "Suscrito al tema: $topic")
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error subscribing to topic", e)
        }
    }
    
    /**
     * Desuscribe el dispositivo de un tema
     */
    suspend fun unsubscribeFromTopic(topic: String) = withContext(Dispatchers.IO) {
        try {
            OneSignal.deleteTag(topic)
            android.util.Log.d("NotificationRepository", "Desuscrito del tema: $topic")
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error unsubscribing from topic", e)
        }
    }
    
    /**
     * Muestra una notificación local de prueba (para debugging)
     */
    fun showTestNotification(context: Context, title: String, message: String) {
        try {
            OneSignal.postNotification(title, message)
            android.util.Log.d("NotificationRepository", "Notificación de prueba: $title - $message")
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error showing test notification", e)
        }
    }
    
    /**
     * Limpia todas las notificaciones
     */
    fun clearAllNotifications(context: Context) {
        try {
            OneSignal.clearAllNotifications()
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error clearing notifications", e)
        }
    }
    
    /**
     * Elimina una notificación específica
     */
    fun clearNotification(context: Context, groupId: String) {
        try {
            OneSignal.removeNotification(groupId)
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error clearing notification", e)
        }
    }
}
