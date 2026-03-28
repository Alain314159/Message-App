package com.example.messageapp.data

import android.content.Context
import android.util.Log
import cn.jiguang.jpush.android.api.JPushInterface
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Repositorio de Notificaciones usando JPush (Aurora Mobile)
 * 
 * JPush es completamente GRATIS y funciona perfectamente desde Cuba
 * sin los bloqueos que tienen Firebase y OneSignal
 * 
 * Documentación: https://docs.jiguang.cn/en/
 */
class NotificationRepository {
    
    companion object {
        private const val TAG = "JPushNotification"
    }
    
    private var isInitialized = false
    private var appContext: Context? = null
    
    // Eventos de notificaciones recibidas
    private val _notificationReceived = MutableSharedFlow<JPushNotificationData>()
    val notificationReceived: SharedFlow<JPushNotificationData> = _notificationReceived.asSharedFlow()
    
    // Eventos cuando se abre una notificación
    private val _notificationOpened = MutableSharedFlow<JPushNotificationData>()
    val notificationOpened: SharedFlow<JPushNotificationData> = _notificationOpened.asSharedFlow()
    
    /**
     * Inicializa JPush con el App Key configurado
     * Debe llamarse UNA sola vez, preferiblemente en Application.onCreate()
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        val appKey = SupabaseConfig.JPUSH_APP_KEY
        
        // Verificar que el App Key es válido
        if (appKey.isBlank() || appKey == "TU_JPUSH_APP_KEY_AQUI") {
            Log.w(TAG, "JPush App Key no configurado en SupabaseConfig. " +
                "Las notificaciones push no funcionarán.")
            return
        }
        
        try {
            // Configurar modo debug (solo en desarrollo)
            if (BuildConfig.DEBUG) {
                JPushInterface.setDebugMode(true)
            }
            
            // Inicializar JPush
            JPushInterface.init(appContext)
            
            isInitialized = true
            Log.d(TAG, "JPush inicializado correctamente")
            Log.d(TAG, "Registration ID: ${getRegistrationId()}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar JPush", e)
        }
    }
    
    /**
     * Obtiene el Registration ID único de este dispositivo
     * Este ID se usa para enviar notificaciones push a este dispositivo específico
     */
    fun getRegistrationId(): String {
        val ctx = appContext ?: return ""
        return JPushInterface.getRegistrationID(ctx)
    }
    
    /**
     * Verifica si JPush está disponible y configurado
     */
    fun isJPushAvailable(): Boolean {
        val appKey = SupabaseConfig.JPUSH_APP_KEY
        return appKey.isNotBlank() && appKey != "TU_JPUSH_APP_KEY_AQUI" && isInitialized
    }
    
    /**
     * Establece un alias para identificar al usuario
     * Esto permite enviar notificaciones a usuarios específicos
     */
    fun setAlias(alias: String) {
        val ctx = appContext ?: return
        try {
            JPushInterface.setAlias(ctx, 0, alias)
            Log.d(TAG, "Alias establecido: $alias")
        } catch (e: Exception) {
            Log.e(TAG, "Error estableciendo alias", e)
        }
    }
    
    /**
     * Elimina el alias (cuando el usuario hace logout)
     */
    fun deleteAlias() {
        val ctx = appContext ?: return
        try {
            JPushInterface.deleteAlias(ctx, 0)
            Log.d(TAG, "Alias eliminado")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando alias", e)
        }
    }
    
    /**
     * Establece tags para segmentación
     */
    fun setTags(tags: Set<String>) {
        val ctx = appContext ?: return
        try {
            JPushInterface.setTags(ctx, 0, tags)
            Log.d(TAG, "Tags establecidos: $tags")
        } catch (e: Exception) {
            Log.e(TAG, "Error estableciendo tags", e)
        }
    }
    
    /**
     * Detiene JPush (para cuando el usuario hace logout)
     */
    fun stopPush() {
        val ctx = appContext ?: return
        JPushInterface.stopPush(ctx)
        Log.d(TAG, "JPush detenido")
    }
    
    /**
     * Resume JPush (para cuando el usuario hace login)
     */
    fun resumePush() {
        val ctx = appContext ?: return
        JPushInterface.resumePush(ctx)
        Log.d(TAG, "JPush reanudado")
    }
    
    /**
     * Limpia todas las notificaciones
     */
    fun clearAllNotifications() {
        val ctx = appContext ?: return
        try {
            JPushInterface.clearAllNotifications(ctx)
            Log.d(TAG, "Notificaciones limpiadas")
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando notificaciones", e)
        }
    }
    
    /**
     * Elimina una notificación específica por ID
     */
    fun clearNotification(notificationId: Int) {
        val ctx = appContext ?: return
        try {
            JPushInterface.clearNotificationById(ctx, notificationId)
            Log.d(TAG, "Notificación $notificationId eliminada")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando notificación", e)
        }
    }
    
    /**
     * Verifica si las notificaciones están habilitadas
     */
    fun areNotificationsEnabled(): Boolean {
        val ctx = appContext ?: return false
        return JPushInterface.isNotificationEnabled(ctx) == 1
    }
    
    /**
     * Abre la configuración de notificaciones del sistema
     */
    fun openNotificationSettings() {
        val ctx = appContext ?: return
        try {
            JPushInterface.goToAppSettings(ctx)
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo settings", e)
        }
    }
}

/**
 * Data class para los datos de notificación recibidos
 */
data class JPushNotificationData(
    val title: String,
    val message: String,
    val extras: Map<String, String>,
    val notificationId: Int,
    val messageId: String
)
