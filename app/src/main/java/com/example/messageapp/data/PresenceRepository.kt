package com.example.messageapp.data

import com.example.messageapp.model.Chat
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan-tennert.supabase.postgrest.Postgrest
import io.github.jan-tennert.supabase.postgrest.query.Columns
import io.github.jan-tennert.supabase.realtime.Realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Repositorio de Presencia
 *
 * Maneja:
 * - Typing indicators ("Escribiendo...")
 * - Estado online/offline
 * - Last seen (última vez visto)
 * 
 * ✅ Actualizado para supabase-kt 3.x
 */
class PresenceRepository {

    private val db = SupabaseConfig.client.plugin(Postgrest)
    private val realtime = SupabaseConfig.client.plugin(Realtime)
    
    /**
     * Actualiza el estado de "escribiendo" en un chat
     * Se auto-limpia después de 5 segundos
     * 
     * @param chatId ID del chat
     * @param isTyping true si está escribiendo, false si no
     */
    suspend fun setTypingStatus(chatId: String, isTyping: Boolean) = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseConfig.client.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext
            
            // Determinar si soy user1 o user2 en el chat
            val chat = db.from("chats")
                .select(columns = Columns.list("member_ids")) {
                    filter { eq("id", chatId) }
                }
                .decodeSingleOrNull<Chat>()
                ?: return@withContext
            
            val isUser1 = chat.memberIds.firstOrNull() == userId
            val typingField = if (isUser1) "user1_typing" else "user2_typing"
            
            db.from("chats").update(
                mapOf(
                    typingField to isTyping,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", chatId) }
            }
            
            // Auto-limpiar después de 5 segundos
            if (isTyping) {
                kotlinx.coroutines.delay(5000)
                db.from("chats").update(
                    mapOf(
                        typingField to false,
                        "updated_at" to (System.currentTimeMillis() / 1000)
                    )
                ) {
                    filter { eq("id", chatId) }
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.w("PresenceRepository", "Error al actualizar typing", e)
        }
    }
    
    /**
     * Observa si la otra persona está escribiendo
     *
     * @param chatId ID del chat
     * @param myUid Mi ID de usuario
     * @return Flow que emite true cuando la pareja está escribiendo
     */
    fun observePartnerTyping(chatId: String, myUid: String): Flow<Boolean> = callbackFlow {
        try {
            val channel = realtime.channel("chats:public:chats")

            // Flujo de cambios
            val changeFlow = channel.postgrestChangeFlow(schema = "public") {
                table = "chats"
            }

            // Suscribirse
            channel.subscribe()

            val job = kotlinx.coroutines.launch {
                changeFlow.collect { change ->
                    val recordJson = change.record
                    if (recordJson != null) {
                        try {
                            val chat = kotlinx.serialization.json.Json.decodeFromJsonElement<Chat>(recordJson)
                            if (chat.id == chatId) {
                                // Determinar si la otra persona está escribiendo
                                val isPartnerTyping = if (chat.memberIds.firstOrNull() == myUid) {
                                    chat.user2Typing
                                } else {
                                    chat.user1Typing
                                }
                                trySend(isPartnerTyping)
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("PresenceRepository", "Error decoding chat", e)
                        }
                    }
                }
            }

            awaitClose {
                job.cancel()
                realtime.removeChannel(channel)
            }

        } catch (e: Exception) {
            android.util.Log.w("PresenceRepository", "Error al observar typing", e)
            close()
        }
    }
    
    /**
     * Actualiza el estado online/offline del usuario
     * 
     * @param isOnline true si está online, false si está offline
     */
    suspend fun updateOnlineStatus(isOnline: Boolean) = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseConfig.client.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext
            
            db.from("users").update(
                mapOf(
                    "is_online" to isOnline,
                    "last_seen" to (System.currentTimeMillis() / 1000),
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", userId) }
            }
        } catch (e: Exception) {
            android.util.Log.w("PresenceRepository", "Error al actualizar online status", e)
        }
    }
    
    /**
     * Observa el estado online de la pareja
     *
     * @param partnerId ID de la pareja
     * @return Flow que emite true cuando la pareja está online
     */
    fun observePartnerOnline(partnerId: String): Flow<Boolean> = callbackFlow {
        try {
            val channel = realtime.channel("users:public:users")

            // Flujo de cambios
            val changeFlow = channel.postgrestChangeFlow(schema = "public") {
                table = "users"
            }

            // Suscribirse
            channel.subscribe()

            val job = kotlinx.coroutines.launch {
                changeFlow.collect { change ->
                    val recordJson = change.record
                    if (recordJson != null) {
                        try {
                            val user = kotlinx.serialization.json.Json.decodeFromJsonElement<Chat>(recordJson)
                            if (user.id == partnerId) {
                                val isOnline = user.isOnline
                                trySend(isOnline)
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("PresenceRepository", "Error decoding user", e)
                        }
                    }
                }
            }

            awaitClose {
                job.cancel()
                realtime.removeChannel(channel)
            }

        } catch (e: Exception) {
            android.util.Log.w("PresenceRepository", "Error al observar online status", e)
            close()
        }
    }
    
    /**
     * Obtiene el last seen de la pareja
     *
     * @param partnerId ID de la pareja
     * @return Timestamp en segundos o null
     */
    suspend fun getPartnerLastSeen(partnerId: String): Long? = withContext(Dispatchers.IO) {
        try {
            val response = db.from("users")
                .select(columns = Columns.list("last_seen")) {
                    filter { eq("id", partnerId) }
                }
                .decodeSingleOrNull<UserLastSeenResponse>()

            response?.lastSeen
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Data class para respuesta de last_seen
 */
private data class UserLastSeenResponse(
    val lastSeen: Long?
)
