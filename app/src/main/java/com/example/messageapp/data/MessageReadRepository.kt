package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.model.Message
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.utils.retryWithBackoff
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.*
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Lectura de Mensajes
 *
 * Responsabilidad única: Observación y lectura de MENSAJES
 *
 * Funciones:
 * 1. observeMessages
 * 2. loadMessagesPaginated
 * 3. loadOlderMessages
 * 4. countUnreadMessages
 */
class MessageReadRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val db: Postgrest = client.postgrest
    private val realtime = client.realtime

    companion object {
        const val PAGE_SIZE = 50
    }

    /**
     * Observa los mensajes de un chat en tiempo real
     * Auto-marks messages as delivered when received
     */
    fun observeMessages(chatId: String, myUid: String): Flow<List<Message>> = callbackFlow {
        // Cargar mensajes iniciales
        launch {
            try {
                val messages = loadMessages(chatId)
                trySend(messages)
            } catch (e: Exception) {
                Log.e(TAG, "MessageReadRepository: Error loading initial messages", e)
                trySend(emptyList())
            }
        }

        // Suscribirse a cambios en mensajes
        val channel = realtime.channel("messages:public:messages")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }

        launch { channel.subscribe() }

        // Escuchar cambios
        val job = launch {
            changeFlow.collect { action ->
                val recordJson = when (action) {
                    is PostgresAction.Insert, is PostgresAction.Update, is PostgresAction.Select -> action.record
                    is PostgresAction.Delete -> action.oldRecord
                    else -> null
                }
                if (recordJson != null) {
                    try {
                        val message = Json.decodeFromString<Message>(recordJson.toString())
                        if (message.chatId == chatId) {
                            val messages = loadMessages(chatId)
                            trySend(messages)
                            // Mark as delivered for incoming messages
                            if (message.senderId != myUid) {
                                MessageWriteRepository(client).markDelivered(chatId, message.id, myUid)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "MessageReadRepository: Error processing message: ${e.message}", e)
                    }
                }
            }
        }

        awaitClose {
            job.cancel()
            runBlocking { realtime.removeChannel(channel) }
        }
    }

    /**
     * Carga los mensajes de un chat ordenados por fecha (más antiguos primero)
     */
    private suspend fun loadMessages(chatId: String): List<Message> {
        return try {
            val messages = db.from("messages")
                .select(columns = Columns.list("*")) {
                    filter { and { eq("chat_id", chatId) } }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Message>()

            Log.d(TAG, "MessageReadRepository: Loaded ${messages.size} messages for chat $chatId")
            messages
        } catch (e: Exception) {
            Log.w(TAG, "MessageReadRepository: Error loading messages: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Carga mensajes con paginación (los más recientes primero)
     */
    suspend fun loadMessagesPaginated(
        chatId: String,
        page: Int = 0,
        pageSize: Int = PAGE_SIZE
    ): List<Message> = withContext(Dispatchers.IO) {
        require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
        require(page >= 0) { "page debe ser >= 0" }
        require(pageSize > 0) { "pageSize debe ser > 0" }

        retryWithBackoff(
            maxRetries = 3,
            initialDelay = 1000,
            tag = TAG
        ) {
            val from = page * pageSize
            val to = from + pageSize - 1

            val messages = db.from("messages")
                .select(columns = Columns.list("*")) {
                    filter { and { eq("chat_id", chatId) } }
                    order("created_at", Order.DESCENDING)
                    range(from.toLong(), to.toLong())
                }
                .decodeList<Message>()

            Log.d(TAG, "MessageReadRepository: Loaded page $page (${messages.size} messages)")
            messages
        }
    }

    /**
     * Carga mensajes más antiguos (para scroll infinito)
     */
    suspend fun loadOlderMessages(
        chatId: String,
        beforeTimestamp: Long,
        limit: Int = PAGE_SIZE
    ): List<Message> = withContext(Dispatchers.IO) {
        require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
        require(beforeTimestamp > 0) { "beforeTimestamp debe ser > 0" }

        try {
            val messages = db.from("messages")
                .select(columns = Columns.list("*")) {
                    filter {
                        and {
                            eq("chat_id", chatId)
                            lt("created_at", beforeTimestamp)
                        }
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Message>()

            Log.d(TAG, "MessageReadRepository: Loaded $limit older messages (${messages.size} returned)")
            messages
        } catch (e: Exception) {
            Log.w(TAG, "MessageReadRepository: Error loading older messages: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Cuenta mensajes no leídos en un chat
     */
    suspend fun countUnreadMessages(chatId: String, uid: String): Int = withContext(Dispatchers.IO) {
        try {
            // TODO: implement count query
            return@withContext 0
        } catch (e: Exception) {
            Log.w(TAG, "MessageReadRepository: Error counting unread messages: ${e.message}", e)
            throw e
        }
    }
}
