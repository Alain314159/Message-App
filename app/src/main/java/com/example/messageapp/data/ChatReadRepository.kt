package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.model.Chat
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.utils.retryWithBackoff
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.*
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Chats usando Supabase Postgrest + Realtime
 *
 * Responsabilidad única: Gestión y observación de CHATS (no mensajes)
 *
 * Funciones (5):
 * 1. directChatIdFor
 * 2. ensureDirectChat
 * 3. observeChats
 * 4. observeChat
 * 5. loadChatsForUser (privada)
 */
class ChatReadRepository {

    private val db = SupabaseConfig.client.postgrest
    private val realtime = SupabaseConfig.client.realtime

    /**
     * Genera un ID único para chat directo entre 2 usuarios
     * El ID es determinista (siempre el mismo para los mismos usuarios)
     */
    fun directChatIdFor(uidA: String, uidB: String): String {
        return listOf(uidA.trim(), uidB.trim()).sorted().joinToString("_")
    }

    /**
     * Crea o verifica que existe un chat directo
     * Usa retry logic para evitar fallos en conexiones inestables
     */
    suspend fun ensureDirectChat(uidA: String, uidB: String): String = withContext(Dispatchers.IO) {
        val chatId = directChatIdFor(uidA, uidB)

        try {
            // Verificar si ya existe con retry
            val existing = retryWithBackoff(
                maxRetries = 3,
                initialDelay = 500,
                tag = TAG
            ) {
                db.from("chats")
                    .select(columns = Columns.list("id")) {
                        filter { eq("id", chatId) }
                    }
                    .decodeSingle<Chat>()
            }

            if (existing != null) {
                // Actualizar timestamp
                db.from("chats").update(
                    mapOf(
                        "updated_at" to (System.currentTimeMillis() / 1000)
                    )
                ) {
                    filter { eq("id", chatId) }
                }
                return@withContext chatId
            }
        } catch (e: Exception) {
            Log.e(TAG, "ChatReadRepository: Error verifying chat: ${e.message}", e)
        }

        // Crear nuevo chat con retry
        try {
            retryWithBackoff(
                maxRetries = 3,
                initialDelay = 500,
                tag = TAG
            ) {
                db.from("chats").insert(
                    mapOf(
                        "id" to chatId,
                        "type" to "direct",
                        "member_ids" to listOf(uidA, uidB),
                        "created_at" to (System.currentTimeMillis() / 1000),
                        "updated_at" to (System.currentTimeMillis() / 1000)
                    )
                )
            }
            Log.d(TAG, "ChatReadRepository: Direct chat created: $chatId")
        } catch (e: Exception) {
            Log.e(TAG, "ChatReadRepository: Error creating direct chat", e)
        }

        chatId
    }

    /**
     * Observa la lista de chats del usuario en tiempo real
     */
    fun observeChats(uid: String): Flow<List<Chat>> = callbackFlow {
        val channel = realtime.channel("chats:public:chats")

        // Flujo de cambios de PostgREST
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "chats" }
            table = "chats"
        }

        // Suscribirse al canal
        channel.subscribe()

        Log.d(TAG, "ChatReadRepository: Subscribed to chats channel for user $uid")

        // Cargar chats iniciales en background
        kotlinx.coroutines.launch {
            try {
                val initialChats = loadChatsForUser(uid)
                Log.d(TAG, "ChatReadRepository: Initial load completed, ${initialChats.size} chats")
                trySend(initialChats)
            } catch (e: Exception) {
                Log.e(TAG, "ChatReadRepository: Error loading initial chats: ${e.message}", e)
                trySend(emptyList())
            }
        }

        // Escuchar cambios
        val job = kotlinx.coroutines.launch {
            changeFlow.collect { change ->
                Log.d(TAG, "ChatReadRepository: Received change event, reloading chats")
                loadChatsForUser(uid)
            }
        }

        awaitClose {
            Log.d(TAG, "ChatReadRepository: Unsubscribing from chats channel")
            job.cancel()
            realtime.removeChannel(channel)
        }
    }

    /**
     * Observa un chat específico en tiempo real
     */
    fun observeChat(chatId: String): Flow<Chat?> = callbackFlow {
        // Cargar estado inicial
        launch {
            try {
                val chat = loadChat(chatId)
                trySend(chat)
                Log.d(TAG, "ChatReadRepository: Loaded initial chat state for $chatId")
            } catch (e: Exception) {
                Log.e(TAG, "ChatReadRepository: Error loading initial chat state", e)
                trySend(null)
            }
        }

        // Suscribirse a cambios en este chat específico
        val channel = realtime.channel("chats:public:chats")

        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "chats" }
            table = "chats"
        }

        // Suscribirse al canal
        launch {
            channel.subscribe()
        }

        // Escuchar cambios y recargar cuando haya actualizaciones
        val job = launch {
            changeFlow.collect { action ->
                val recordJson = when (action) {
                    is PostgresAction.Insert, is PostgresAction.Update, is PostgresAction.Select -> action.record
                    is PostgresAction.Delete -> action.oldRecord
                    else -> null
                }
                if (recordJson != null) {
                    val changedChat = Json.decodeFromString<Chat>(recordJson.toString())
                    if (changedChat.id == chatId) {
                        Log.d(TAG, "ChatReadRepository: Chat $chatId updated, emitting new state")
                        trySend(changedChat)
                    }
                }
            }
        }

        awaitClose {
            job.cancel()
            realtime.removeChannel(channel)
        }
    }

    /**
     * Carga un chat específico
     */
    private suspend fun loadChat(chatId: String): Chat? {
        return try {
            db.from("chats")
                .select(columns = Columns.list("*")) {
                    filter { eq("id", chatId) }
                }
                .decodeSingleOrNull<Chat>()
        } catch (e: Exception) {
            Log.e(TAG, "ChatReadRepository: Error loading chat $chatId: ${e.message}", e)
            null
        }
    }

    /**
     * Carga los chats del usuario desde la base de datos
     */
    private suspend fun loadChatsForUser(uid: String): List<Chat> {
        return try {
            val chats = db.from("chats")
                .select(columns = Columns.list("*")) {
                    filter {
                        contains("member_ids", listOf(uid))
                    }
                    order("updated_at", Order.DESCENDING) // DESC
                }
                .decodeList<Chat>()

            Log.d(TAG, "ChatReadRepository: Loaded ${chats.size} chats for user $uid")
            chats
        } catch (e: Exception) {
            Log.e(TAG, "ChatReadRepository: Error loading chats for user $uid: ${e.message}", e)
            emptyList()
        }
    }
}
