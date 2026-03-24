package com.example.messageapp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.jiguang.jpush.android.api.JPushInterface
import cn.jiguang.jpush.android.api.JPushMessage
import com.example.messageapp.MainActivity
import com.example.messageapp.R

/**
 * JPush BroadcastReceiver para manejar eventos de notificaciones push
 * 
 * Reemplaza el antiguo FirebaseMessagingService
 */
class JPushBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "JPushReceiver"
        private const val CHANNEL_ID = "messages"
        private const val CHANNEL_NAME = "Mensajes"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            JPushInterface.ACTION_REGISTRATION_ID -> {
                // Obtener el Registration ID
                val registrationId = JPushInterface.getRegistrationID(context, intent)
                Log.d(TAG, "Registration ID: $registrationId")
                // Aquí deberías guardar el registration ID en tu backend
            }
            
            JPushInterface.ACTION_MESSAGE_RECEIVED -> {
                // Mensaje personalizado recibido (data payload)
                val message = intent.getStringExtra(JPushInterface.EXTRA_MESSAGE)
                val extras = intent.getStringExtra(JPushInterface.EXTRA_EXTRA)
                val messageId = intent.getStringExtra(JPushInterface.EXTRA_MSG_ID)
                
                Log.d(TAG, "Mensaje recibido: $message, Extras: $extras")
                
                // Parsear extras para obtener datos del chat
                val chatId = parseExtras(extras, "chat_id")
                val senderId = parseExtras(extras, "sender_id")
                
                // Mostrar notificación si la app está en background
                if (chatId != null) {
                    showNotification(
                        context,
                        "Nuevo mensaje",
                        message ?: "Tienes un nuevo mensaje",
                        chatId,
                        extras
                    )
                }
            }
            
            JPushInterface.ACTION_NOTIFICATION_RECEIVED -> {
                // Notificación recibida (se muestra automáticamente)
                val notificationTitle = intent.getStringExtra(JPushInterface.EXTRA_NOTIFICATION_TITLE)
                val notificationAlert = intent.getStringExtra(JPushInterface.EXTRA_ALERT)
                val notificationId = intent.getIntExtra(JPushInterface.EXTRA_NOTIFICATION_ID, 0)
                val extras = intent.getStringExtra(JPushInterface.EXTRA_EXTRA)
                
                Log.d(TAG, "Notificación recibida: $notificationTitle - $notificationAlert")
            }
            
            JPushInterface.ACTION_NOTIFICATION_OPENED -> {
                // Usuario tocó la notificación
                val notificationTitle = intent.getStringExtra(JPushInterface.EXTRA_NOTIFICATION_TITLE)
                val extras = intent.getStringExtra(JPushInterface.EXTRA_EXTRA)
                
                Log.d(TAG, "Notificación tocada: $notificationTitle")
                
                // Abrir la app y navegar al chat correspondiente
                openChatFromNotification(context, extras)
            }
            
            JPushInterface.ACTION_CONNECTION_CHANGE -> {
                // Cambio en la conexión
                val connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false)
                Log.d(TAG, "Conexión JPush cambiada: $connected")
            }
            
            JPushInterface.ACTION_RICHPUSH_CALLBACK -> {
                // Callback para notificaciones ricas
                val richUri = intent.getStringExtra(JPushInterface.EXTRA_RICHPUSH_FILE_PATH)
                val richAction = intent.getStringExtra(JPushInterface.EXTRA_RICHPUSH_HTML_PATH)
                val richHtml = intent.getStringExtra(JPushInterface.EXTRA_RICHPUSH_HTML)
                Log.d(TAG, "Rich push callback: uri=$richUri, action=$richAction")
            }
        }
    }
    
    /**
     * Parsea el JSON de extras para obtener un valor específico
     */
    private fun parseExtras(extras: String?, key: String): String? {
        if (extras.isNullOrEmpty()) return null
        return try {
            val json = org.json.JSONObject(extras)
            json.optString(key)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing extras: ${e.message}")
            null
        }
    }
    
    /**
     * Muestra una notificación personalizada
     */
    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        chatId: String,
        extras: String?
    ) {
        createNotificationChannel(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("chatId", chatId)
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(chatId.hashCode(), notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Error mostrando notificación: ${e.message}")
        }
    }
    
    /**
     * Abre el chat correspondiente cuando se toca una notificación
     */
    private fun openChatFromNotification(context: Context, extras: String?) {
        val chatId = parseExtras(extras, "chat_id")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (chatId != null) {
                putExtra("chatId", chatId)
            }
            putExtra("fromNotification", true)
        }
        
        context.startActivity(intent)
    }
    
    /**
     * Crea el canal de notificación para Android O+
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

/**
 * Servicio de JPush para manejar mensajes en foreground
 */
class JPushService : cn.jiguang.jpush.android.service.JPushService() {
    
    companion object {
        private const val TAG = "JPushService"
    }
    
    override fun onMessage(context: Context?, message: cn.jiguang.jpush.android.api.JPushMessage?) {
        Log.d(TAG, "Mensaje en foreground: ${message?.toString()}")
    }
    
    override fun onNotifyMessageArrived(context: Context?, message: cn.jiguang.jpush.android.api.JPushMessage?) {
        Log.d(TAG, "Notificación chegou: ${message?.notification?.toString()}")
    }
    
    override fun onNotifyMessageOpened(context: Context?, message: cn.jiguang.jpush.android.api.JPushMessage?) {
        Log.d(TAG, "Notificación aberta: ${message?.notification?.toString()}")
    }
}
