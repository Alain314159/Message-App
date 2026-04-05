package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.utils.retryWithBackoff
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Escritura de Mensajes
 *
 * Responsabilidad única: Envío y actualización de estado de MENSAJES
 *
 * Funciones:
 * 1. sendText
 * 2. markDelivered
 * 3. markAsRead
 */
class MessageWriteRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val db: Postgrest = client.postgrest

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

            Log.d(TAG, "MessageWriteRepository: Message sent successfully")
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
            Log.w(TAG, "MessageWriteRepository: Error marking delivered: ${e.message}", e)
        }
    }

    /**
     * Marca todos los mensajes como leídos
     */
    suspend fun markAsRead(chatId: String, uid: String) = withContext(Dispatchers.IO) {
        try {
            db.from("messages").update(
                mapOf(
                    "read_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                // TODO: implement markAsRead filter
                // eq("chat_id", chatId)
                // neq("sender_id", uid)
                // isNull("read_at")
            }
        } catch (e: Exception) {
            Log.w(TAG, "MessageWriteRepository: Error marking as read: ${e.message}", e)
        }
    }
}
