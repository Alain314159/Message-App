package com.example.messageapp.data

import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de Presencia
 *
 * @deprecated Usar TypingRepository y UserPresenceRepository por separado
 * Este facade mantiene compatibilidad con código existente.
 */
@Deprecated(
    "Usar TypingRepository y UserPresenceRepository por separado",
    ReplaceWith("TypingRepository, UserPresenceRepository")
)
class PresenceRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val typingRepo = TypingRepository(client)
    private val presenceRepo = UserPresenceRepository(client)

    /**
     * Actualiza el estado de "escribiendo" en un chat
     * Se auto-limpia después de 5 segundos
     */
    suspend fun setTypingStatus(chatId: String, isTyping: Boolean) =
        typingRepo.setTypingStatus(chatId, isTyping)

    /**
     * Observa si la otra persona está escribiendo
     */
    fun observePartnerTyping(chatId: String, myUid: String): Flow<Boolean> =
        typingRepo.observePartnerTyping(chatId, myUid)

    /**
     * Actualiza el estado online/offline del usuario
     */
    suspend fun updateOnlineStatus(isOnline: Boolean) =
        presenceRepo.updateOnlineStatus(isOnline)

    /**
     * Observa el estado online de la pareja
     */
    fun observePartnerOnline(partnerId: String): Flow<Boolean> =
        presenceRepo.observePartnerOnline(partnerId)

    /**
     * Obtiene el last seen de la pareja
     */
    suspend fun getPartnerLastSeen(partnerId: String): Long? =
        presenceRepo.getPartnerLastSeen(partnerId)
}
