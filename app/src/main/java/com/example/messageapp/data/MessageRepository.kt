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
 * Repositorio de Mensajes usando Supabase Postgrest + Realtime
 *
 * @deprecated Usar MessageReadRepository y MessageWriteRepository por separado
 * Este facade mantiene compatibilidad con código existente.
 */
@Deprecated(
    "Usar MessageReadRepository y MessageWriteRepository por separado",
    ReplaceWith("MessageReadRepository, MessageWriteRepository")
)
class MessageRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val readRepo = MessageReadRepository(client)
    private val writeRepo = MessageWriteRepository(client)

    companion object {
        const val PAGE_SIZE = 50
    }

    /**
     * Observa los mensajes de un chat en tiempo real
     */
    fun observeMessages(chatId: String, myUid: String): Flow<List<Message>> =
        readRepo.observeMessages(chatId, myUid)

    /**
     * Envía un mensaje de texto cifrado
     */
    suspend fun sendText(chatId: String, senderId: String, textEnc: String, iv: String) =
        writeRepo.sendText(chatId, senderId, textEnc, iv)

    /**
     * Marca un mensaje como entregado
     */
    suspend fun markDelivered(chatId: String, messageId: String, uid: String) =
        writeRepo.markDelivered(chatId, messageId, uid)

    /**
     * Marca todos los mensajes como leídos
     */
    suspend fun markAsRead(chatId: String, uid: String) =
        writeRepo.markAsRead(chatId, uid)

    /**
     * Carga mensajes con paginación
     */
    suspend fun loadMessagesPaginated(
        chatId: String,
        page: Int = 0,
        pageSize: Int = PAGE_SIZE
    ): List<Message> = readRepo.loadMessagesPaginated(chatId, page, pageSize)

    /**
     * Carga mensajes más antiguos
     */
    suspend fun loadOlderMessages(
        chatId: String,
        beforeTimestamp: Long,
        limit: Int = PAGE_SIZE
    ): List<Message> = readRepo.loadOlderMessages(chatId, beforeTimestamp, limit)
}
