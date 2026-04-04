package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.model.Message
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Mensajes usando Supabase Postgrest + Realtime
 *
 * Responsabilidad única: Observación y lectura de MENSAJES
 *
 * Funciones (7):
 * 1. observeMessages
 * 2. loadMessagesPaginated (con paginación)
 * 3. sendText
 * 4. markDelivered
 * 5. markAsRead
 * 6. loadMessages
 * 7. loadOlderMessages (paginación)
 */
class MessageRepository {

    private val db = SupabaseConfig.client.postgrest
    private val realtime = SupabaseConfig.client.realtime

    companion object {
        const val PAGE_SIZE = 50 // Número de mensajes por página
    }

    /**
     * Observa los mensajes de un chat en tiempo real
     */
    fun observeMessages(chatId: String, myUid: String): Flow<List<Message>> = callbackFlow {
        // Cargar mensajes iniciales en una coroutine
        launch {
            try {
                val messages = loadMessages(chatId)
                trySend(messages)
            } catch (e: Exception) {
                Log.e(TAG, "MessageRepository: Error loading initial messages", e)
                trySend(emptyList())
            }
        }

        // Suscribirse a cambios en mensajes
        val channel = realtime.channel("messages:public:messages")

        // Flujo de cambios para INSERT/UPDATE/DELETE
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }

        // Suscribirse al canal
        launch {
            channel.subscribe()
        }

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
                            if (message.senderId != myUid) {
                                markDelivered(chatId, message.id, myUid)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "MessageRepository: Error processing message: ${e.message}", e)
                    }
                }
            }
        }

        awaitClose {
            job.cancel()
            runBlocking {
                realtime.removeChannel(channel)
            }
        }
    }

    /**
     * Carga los mensajes de un chat
     * @return Lista de mensajes ordenados por fecha
     */
    private suspend fun loadMessages(chatId: String): List<Message> {
        return try {
            val messages = db.from("messages")
                .select(columns = Columns.list("*")) {
                    filter { and { eq("chat_id", chatId) } }
                    order("created_at", Order.ASCENDING) // ASC (más antiguos primero)
                }
                .decodeList<Message>()

            Log.d(TAG, "MessageRepository: Loaded ${messages.size} messages for chat $chatId")
            messages
        } catch (e: Exception) {
            Log.w(TAG, "MessageRepository: Error loading messages: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Envía un mensaje de texto cifrado
     *
     * @param chatId ID del chat
     * @param senderId UID del remitente
     * @param textEnc Texto cifrado (ciphertext)
     * @param iv IV de cifrado (reemplaza a nonce)
     */
    suspend fun sendText(
        chatId: String,
        senderId: String,
        textEnc: String,
        iv: String
    ) = withContext(Dispatchers.IO) {
        // Validar parámetros
        require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
        require(senderId.isNotBlank()) { "senderId no puede estar vacío" }
        require(textEnc.isNotBlank()) { "textEnc no puede estar vacío" }
        require(iv.isNotBlank()) { "iv no puede estar vacío" }

        // Retry logic para envío de mensajes (crítico que no se pierdan)
        retryWithBackoff(
            maxRetries = 3,
            initialDelay = 1000,
            maxDelay = 5000,
            tag = TAG
        ) {
            db.from("messages").insert(
                mapOf(
                    "chat_id" to chatId,
                    "sender_id" to senderId,
                    "type" to "text",
                    "text_enc" to textEnc,
                    "nonce" to iv,
                    "auth_tag" to null,
                    "created_at" to (System.currentTimeMillis() / 1000),
                    "delivered_at" to null,
                    "read_at" to null
                )
            )

            // Actualizar último mensaje del chat
            db.from("chats").update(
                mapOf(
                    "last_message_enc" to textEnc,
                    "last_message_at" to (System.currentTimeMillis() / 1000),
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { and { eq("id", chatId) } }
            }

            Log.d(TAG, "MessageRepository: Message sent successfully")
        }
    }

    /**
     * Marca un mensaje como entregado
     */
    suspend fun markDelivered(chatId: String, messageId: String, uid: String) = withContext(Dispatchers.IO) {
        try {
            db.from("messages").update(
                mapOf(
                    "delivered_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter {
                    and { eq("id", messageId); neq("sender_id", uid) }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MessageRepository: Error marking delivered: ${e.message}", e)
        }
    }

    /**
     * Marca todos los mensajes como leídos
     */
    suspend fun markAsRead(chatId: String, uid: String) = withContext(Dispatchers.IO) {
        try {
            // TODO: implement markAsRead filter
            db.from("messages").update(
                mapOf(
                    "read_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                // eq("chat_id", chatId)
                // neq("sender_id", uid)
                // isNull("read_at")
            }
        } catch (e: Exception) {
            Log.w(TAG, "MessageRepository: Error marking as read: ${e.message}", e)
        }
    }

    /**
     * Carga mensajes con paginación (los más recientes primero)
     *
     * @param chatId ID del chat
     * @param page Número de página (0-based)
     * @param pageSize Tamaño de página (default: PAGE_SIZE)
     * @return Lista de mensajes ordenados por fecha (más recientes primero)
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
                    order("created_at", Order.DESCENDING) // DESC (más recientes primero)
                    range(from.toLong(), to.toLong())
                }
                .decodeList<Message>()

            Log.d(TAG, "MessageRepository: Loaded page $page (${messages.size} messages)")
            messages
        }
    }

    /**
     * Carga mensajes más antiguos (para scroll infinito)
     *
     * @param chatId ID del chat
     * @param beforeTimestamp Timestamp anterior para cargar mensajes más antiguos
     * @param limit Número máximo de mensajes a cargar
     * @return Lista de mensajes más antiguos
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
                    order("created_at", Order.DESCENDING) // DESC
                    limit(limit.toLong())
                }
                .decodeList<Message>()

            Log.d(TAG, "MessageRepository: Loaded $limit older messages (${messages.size} returned)")
            messages
        } catch (e: Exception) {
            Log.w(TAG, "MessageRepository: Error loading older messages: ${e.message}", e)
            emptyList()
        }
    }
}
