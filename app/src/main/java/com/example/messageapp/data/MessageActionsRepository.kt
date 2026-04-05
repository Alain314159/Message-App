package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.model.Message
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Acciones de Mensajes usando Supabase Postgrest
 *
 * Responsabilidad única: Operaciones de ESCRITURA sobre mensajes y chats
 *
 * Funciones:
 * 1. pinMessage
 * 2. unpinMessage
 * 3. deleteMessageForUser
 * 4. deleteMessageForAll
 */
class MessageActionsRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val db: Postgrest = client.postgrest

    /**
     * Fija un mensaje en el chat
     */
    suspend fun pinMessage(chatId: String, messageId: String, snippet: String) = withContext(Dispatchers.IO) {
        try {
            db.from("chats").update(
                mapOf(
                    "pinned_message_id" to messageId,
                    "pinned_snippet" to snippet,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", chatId) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MessageActionsRepository: Error pinning message: ${e.message}", e)
        }
    }

    /**
     * Desfija un mensaje
     */
    suspend fun unpinMessage(chatId: String) = withContext(Dispatchers.IO) {
        try {
            db.from("chats").update(
                mapOf(
                    "pinned_message_id" to null,
                    "pinned_snippet" to null
                )
            ) {
                filter { eq("id", chatId) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MessageActionsRepository: Error unpinning message: ${e.message}", e)
        }
    }

    /**
     * Elimina un mensaje solo para el usuario (soft delete)
     */
    suspend fun deleteMessageForUser(chatId: String, messageId: String, uid: String) = withContext(Dispatchers.IO) {
        try {
            val message = db.from("messages")
                .select(columns = Columns.list("deleted_for")) {
                    filter { eq("id", messageId) }
                }
                .decodeSingle<Message>()

            val currentDeletedFor = message?.deletedFor?.toMutableList() ?: mutableListOf()
            if (!currentDeletedFor.contains(uid)) {
                currentDeletedFor.add(uid)
            }

            db.from("messages").update(
                mapOf(
                    "deleted_for" to currentDeletedFor
                )
            ) {
                filter { eq("id", messageId) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MessageActionsRepository: Error deleting message for user: ${e.message}", e)
        }
    }

    /**
     * Elimina un mensaje para todos (hard delete del contenido)
     */
    suspend fun deleteMessageForAll(chatId: String, messageId: String) = withContext(Dispatchers.IO) {
        try {
            db.from("messages").update(
                mapOf(
                    "type" to "deleted",
                    "text_enc" to "",
                    "nonce" to null,
                    "auth_tag" to null,
                    "deleted_for_all" to true
                )
            ) {
                filter { eq("id", messageId) }
            }

            db.from("chats").update(
                mapOf(
                    "last_message_enc" to "[Mensaje eliminado]",
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", chatId) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MessageActionsRepository: Error deleting message for all: ${e.message}", e)
        }
    }
}
