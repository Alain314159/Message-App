package com.example.messageapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messageapp.core.FCMManager
import com.example.messageapp.core.PresenceLifecycleManager
import com.example.messageapp.navigation.AppNavigationHost
import com.example.messageapp.ui.theme.RomanticTheme
import com.example.messageapp.viewmodel.AuthViewModel

/**
 * Actividad Principal de la Aplicación
 *
 * Responsabilidad única: Coordinar componentes de alto nivel
 * - Inicialización de managers (FCM, Presence)
 * - Setup de UI theme
 * - Delegar navegación al AppNavigationHost
 *
 * Principio de Responsabilidad Única:
 * - FCM → FCMManager
 * - Presence → PresenceLifecycleManager
 * - Navigation → AppNavigationHost
 * - Permissions → RequestNotificationPermission
 */
class MainActivity : ComponentActivity() {

    // ============================================
    // MANAGERS - Responsabilidades específicas
    // ============================================

    private val authVm by lazy { AuthViewModel() }
    private lateinit var fcmManager: FCMManager
    private lateinit var presenceManager: PresenceLifecycleManager

    // ============================================
    // LIFECYCLE
    // ============================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar managers
        initializeManagers()

        // Setup UI
        setContent {
            RomanticTheme {
                // Solicitar permiso de notificaciones
                com.example.messageapp.core.RequestNotificationPermission()

                // Observar estado de autenticación
                val isLogged by authVm.isLogged.collectAsStateWithLifecycle()
                val initialChatId = intent?.getStringExtra("chatId")

                // Delegar navegación al Navigation Host
                AppNavigationHost(
                    authVm = authVm,
                    isLogged = isLogged,
                    initialChatId = initialChatId
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        presenceManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenceManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenceManager.onDestroy()
    }

    // ============================================
    // INITIALIZATION
    // ============================================

    /**
     * Inicializa todos los managers
     */
    private fun initializeManagers() {
        // FCM Manager - Notificaciones push
        fcmManager = FCMManager(
            context = this,
            lifecycleScope = lifecycleScope
        )
        fcmManager.initialize()

        // Presence Manager - Estado online/offline
        presenceManager = PresenceLifecycleManager(
            context = this,
            onUpdatePresence = { online ->
                authVm.updatePresence(online)
            }
        )
    }
}
