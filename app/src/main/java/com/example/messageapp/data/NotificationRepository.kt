package com.example.messageapp.data

import android.content.Context
import com.example.messageapp.supabase.SupabaseConfig
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Repositorio de Notificaciones usando OneSignal SDK 5.6.1+
 * 
 * ✅ VERIFICADO: Implementación actualizada con la API oficial de OneSignal 2024-2025
 * 
 * Documentación oficial:
 * https://documentation.onesignal.com/docs/en/android-sdk-setup
 * https://documentation.onesignal.com/docs/en/mobile-sdk-reference
 * 
 * Cambios importantes en OneSignal 5.x:
 * - OneSignal.initialize() reemplaza a initWithContext() + setAppId()
 * - OneSignal.User.pushSubscription.id reemplaza a getDeviceState().userId
 * - API asíncrona para obtener el Player ID
 */
class NotificationRepository {
    
    private var isInitialized = false
    
    /**
     * Inicializa OneSignal con el App ID configurado
     * Debe llamarse UNA sola vez, preferiblemente en Application.onCreate()
     * 
     * @param context Contexto de aplicación
     */
    fun initialize(context: Context) {
        val appId = SupabaseConfig.ONESIGNAL_APP_ID
        
        // Verificar que el App ID es válido
        if (appId.isBlank() || appId == "TU_ONESIGNAL_APP_ID_AQUI") {
            android.util.Log.w("NotificationRepository", 
                "OneSignal App ID no configurado en SupabaseConfig. " +
                "Las notificaciones push no funcionarán.")
            return
        }
        
        try {
            // ✅ API CORRECTA para OneSignal 5.6.1+
            OneSignal.initialize(context, appId)
            
            // Configurar logging para debugging (solo en debug)
            OneSignal.Debug.setLogLevel(LogLevel.VERBOSE)
            
            isInitialized = true
            android.util.Log.d("NotificationRepository", "OneSignal inicializado correctamente")
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Error al inicializar OneSignal", e)
        }
    }
    
    /**
     * Obtiene el Player ID único de este dispositivo
     * Este ID se usa para enviar notificaciones push a este dispositivo específico
     * 
     * Nota: El Player ID solo está disponible DESPUÉS de que OneSignal se registra
     * con el servidor (puede tomar 1-3 segundos después de initialize())
     * 
     * @return Player ID o null si no está disponible
     */
    suspend fun getPlayerId(): String? = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            android.util.Log.w("NotificationRepository", "OneSignal no está inicializado")
            return@withContext null
        }
        
        try {
            // ✅ API CORRECTA para OneSignal 5.6.1+
            // OneSignal 5.x usa pushSubscription.id en lugar de getDeviceState().userId
            
            // Esperar a que OneSignal esté listo (máximo 5 segundos)
            val latch = CountDownLatch(1)
            var playerId: String? = null
            
            // Suscribirse a cambios en el push subscription
            val observer = { state: com.onesignal.user.subscriptions.IPushSubscriptionState ->
                playerId = state.id
                latch.countDown()
            }
            
            // Agregar observer
            OneSignal.User.pushSubscription.addObserver(observer)
            
            // Verificar si ya tenemos el ID
            val existingId = OneSignal.User.pushSubscription.id
            if (!existingId.isNullOrBlank()) {
                OneSignal.User.pushSubscription.removeObserver(observer)
                return@withContext existingId
            }
            
            // Esperar a que llegue el ID
            val awaited = latch.await(5, TimeUnit.SECONDS)
            
            // Remover observer
            OneSignal.User.pushSubscription.removeObserver(observer)
            
            if (awaited && !playerId.isNullOrBlank()) {
                android.util.Log.d("NotificationRepository", "Player ID obtenido: $playerId")
                return@withContext playerId
            } else {
                android.util.Log.w("NotificationRepository", 
                    "Timeout esperando Player ID de OneSignal")
                return@withContext null
            }
            
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error getting Player ID", e)
            return@withContext null
        }
    }
    
    /**
     * Obtiene el Player ID de forma asíncrona con callback
     * Útil cuando no quieres usar coroutines
     */
    fun getPlayerIdAsync(callback: (String?) -> Unit) {
        if (!isInitialized) {
            callback(null)
            return
        }
        
        try {
            // Verificar si ya tenemos el ID
            val existingId = OneSignal.User.pushSubscription.id
            if (!existingId.isNullOrBlank()) {
                callback(existingId)
                return
            }
            
            // Esperar con observer
            val observer = object : com.onesignal.user.subscriptions.IPushSubscriptionObserver {
                override fun onPushSubscriptionChange(
                    state: com.onesignal.user.subscriptions.IPushSubscriptionState
                ) {
                    if (!state.id.isNullOrBlank()) {
                        OneSignal.User.pushSubscription.removeObserver(this)
                        callback(state.id)
                    }
                }
            }
            
            OneSignal.User.pushSubscription.addObserver(observer)
            
            // Timeout de 5 segundos
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                OneSignal.User.pushSubscription.removeObserver(observer)
                callback(null)
            }, 5000)
            
        } catch (e: Exception) {
            callback(null)
        }
    }
    
    /**
     * Verifica si OneSignal está disponible y configurado
     */
    fun isOneSignalAvailable(): Boolean {
        val appId = SupabaseConfig.ONESIGNAL_APP_ID
        return appId.isNotBlank() && appId != "TU_ONESIGNAL_APP_ID_AQUI" && isInitialized
    }
    
    /**
     * Suscribe el dispositivo a un tema (opcional, para notificaciones grupales)
     */
    suspend fun subscribeToTopic(topic: String) = withContext(Dispatchers.IO) {
        try {
            OneSignal.User.addTag(topic, "true")
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
            OneSignal.User.removeTag(topic)
            android.util.Log.d("NotificationRepository", "Desuscrito del tema: $topic")
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error unsubscribing from topic", e)
        }
    }
    
    /**
     * Establece el ID de usuario externo (opcional, para vincular con tu sistema)
     */
    fun setExternalUserId(externalId: String) {
        try {
            OneSignal.User.setExternalId(externalId)
            android.util.Log.d("NotificationRepository", "External ID establecido: $externalId")
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error setting external ID", e)
        }
    }
    
    /**
     * Limpia todas las notificaciones
     */
    fun clearAllNotifications(context: Context) {
        try {
            OneSignal.Notifications.clearAll()
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error clearing notifications", e)
        }
    }
    
    /**
     * Elimina una notificación específica por ID
     */
    fun clearNotification(context: Context, notificationId: String) {
        try {
            OneSignal.Notifications.removeNotification(notificationId)
        } catch (e: Exception) {
            android.util.Log.w("NotificationRepository", "Error clearing notification", e)
        }
    }
    
    /**
     * Verifica si el usuario tiene permiso para notificaciones
     */
    fun canRequestPermission(): Boolean {
        return OneSignal.Notifications.canRequestPermission()
    }
    
    /**
     * Solicita permiso para notificaciones (Android 13+)
     */
    fun requestPermission(callback: (Boolean) -> Unit) {
        OneSignal.Notifications.requestPermission(callback)
    }
}
