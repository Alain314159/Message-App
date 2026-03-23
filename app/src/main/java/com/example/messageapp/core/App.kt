package com.example.messageapp

import android.app.Application
import com.example.messageapp.data.NotificationRepository
import com.example.messageapp.supabase.SupabaseConfig

/**
 * Clase de Aplicación
 * 
 * Inicializa componentes globales:
 * - Supabase (cliente)
 * - OneSignal (notificaciones push)
 */
class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar OneSignal para notificaciones push
        // Esto debe hacerse ANTES de cualquier uso de notificaciones
        val notificationRepo = NotificationRepository()
        
        if (notificationRepo.isOneSignalAvailable()) {
            notificationRepo.initialize(this)
            android.util.Log.d("App", "OneSignal inicializado en App.onCreate()")
        } else {
            android.util.Log.w("App", "OneSignal no configurado - revisa SupabaseConfig.ONESIGNAL_APP_ID")
        }
        
        // Supabase se inicializa automáticamente cuando se usa SupabaseConfig.client
        // No necesitamos inicialización explícita aquí
    }
}
