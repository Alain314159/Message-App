package com.example.messageapp.core

import android.content.Context
import com.example.messageapp.data.NotificationRepository

/**
 * Gestiona el ciclo de vida de la presencia del usuario (online/offline)
 *
 * Responsabilidad única: Actualizar el estado de presencia basado en
 * el ciclo de vida de la actividad.
 *
 * Uso:
 * - onResume() → online
 * - onPause() → offline
 * - onDestroy() → offline
 */
class PresenceLifecycleManager(
    private val context: Context,
    private val onUpdatePresence: suspend (Boolean) -> Unit
) {

    /**
     * Called when activity resumes - mark user as online
     */
    fun onResume() {
        onUpdatePresence(true)
    }

    /**
     * Called when activity pauses - mark user as offline
     */
    fun onPause() {
        onUpdatePresence(false)
    }

    /**
     * Called when activity is destroyed - mark user as offline
     */
    fun onDestroy() {
        onUpdatePresence(false)
    }
}
