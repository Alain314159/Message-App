package com.example.messageapp.data

import android.content.Context
import android.util.Log
import cn.jiguang.jpush.android.JPushInterface
import cn.jiguang.jpush.android.api.JPushMessage
import cn.jiguang.jpush.android.api.TagAliasCallback
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

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
        val appKey = SupabaseConfig.JPUSH_APP_KEY
        
        // Verificar que el App Key es válido
        if (appKey.isBlank() || appKey == "TU_JPUSH_APP_KEY_AQUI") {
            Log.w(TAG, "JPush App Key no configurado en SupabaseConfig. " +
                "Las notificaciones push no funcionarán.")
            return
        }
        
        try {
            // Configurar modo debug (solo en desarrollo)
            JPushInterface.setDebugMode(true)
            
            // Inicializar JPush
            JPushInterface.init(context)
            
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
        return JPushInterface.getRegistrationID(App.context)
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
    fun setAlias(context: Context, alias: String, callback: ((Int: Int, Set<String>?, Int) -> Unit)? = null) {
        try {
            JPushInterface.setAlias(context, 0, alias)
            Log.d(TAG, "Alias establecido: $alias")
        } catch (e: Exception) {
            Log.e(TAG, "Error estableciendo alias", e)
        }
    }
    
    /**
     * Elimina el alias (cuando el usuario hace logout)
     */
    fun deleteAlias(context: Context) {
        try {
            JPushInterface.deleteAlias(context, 0)
            Log.d(TAG, "Alias eliminado")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando alias", e)
        }
    }
    
    /**
     * Establece tags para segmentación
     */
    fun setTags(context: Context, tags: Set<String>, callback: ((Int, Set<String>?, Int) -> Unit)? = null) {
        try {
            JPushInterface.setTags(context, 0, tags)
            Log.d(TAG, "Tags establecidos: $tags")
        } catch (e: Exception) {
            Log.e(TAG, "Error estableciendo tags", e)
        }
    }
    
    /**
     * Detiene JPush (para cuando el usuario hace logout)
     */
    fun stopPush(context: Context) {
        JPushInterface.stopPush(context)
        Log.d(TAG, "JPush detenido")
    }
    
    /**
     * Resume JPush (para cuando el usuario hace login)
     */
    fun resumePush(context: Context) {
        JPushInterface.resumePush(context)
        Log.d(TAG, "JPush resumed")
    }
    
    /**
     * Limpia todas las notificaciones
     */
    fun clearAllNotifications(context: Context) {
        try {
            JPushInterface.clearAllNotifications(context)
            Log.d(TAG, "Notificaciones limpiadas")
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando notificaciones", e)
        }
    }
    
    /**
     * Elimina una notificación específica por ID
     */
    fun clearNotification(context: Context, notificationId: Int) {
        try {
            JPushInterface.clearNotificationById(context, notificationId)
            Log.d(TAG, "Notificación $notificationId eliminada")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando notificación", e)
        }
    }
    
    /**
     * Verifica si las notificaciones están habilitadas
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return JPushInterface.isNotificationEnabled(context) == 1
    }
    
    /**
     * Abre la configuración de notificaciones del sistema
     */
    fun openNotificationSettings(context: Context) {
        try {
            JPushInterface.goToAppSettings(context)
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

/**
 * Referencia al contexto de la aplicación
 */
class App {
    companion object {
        lateinit var context: Context
            private set
    }
}
