package com.example.messageapp

import android.app.Application
import android.util.Log
import com.example.messageapp.data.NotificationRepository
import com.example.messageapp.supabase.SupabaseConfig

/**
 * Clase de Aplicación
 * 
 * Inicializa componentes globales:
 * - Supabase (cliente)
 * - JPush (notificaciones push - Aurora Mobile)
 */
class App : Application() {
    
    companion object {
        private const val TAG = "MessageApp"
        lateinit var instance: Application
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Inicializar JPush para notificaciones push
        // JPush es GRATIS y funciona perfectamente desde Cuba
        val notificationRepo = NotificationRepository()
        
        if (notificationRepo.isJPushAvailable()) {
            notificationRepo.initialize(this)
            Log.d(TAG, "JPush inicializado en App.onCreate()")
            Log.d(TAG, "JPush Registration ID: ${notificationRepo.getRegistrationId()}")
        } else {
            Log.w(TAG, "JPush no configurado - revisa SupabaseConfig.JPUSH_APP_KEY")
        }
        
        // Supabase se inicializa automáticamente cuando se usa SupabaseConfig.client
        // No necesitamos inicialización explícita aquí
    }
}
