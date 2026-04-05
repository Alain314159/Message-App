package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.model.Chat
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.utils.retryWithBackoff
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
 * Repositorio de Escritura de Chats
 *
 * Responsabilidad única: Creación y actualización de CHATS
 *
 * Funciones:
 * 1. ensureDirectChat - crea o verifica chat directo
 */
class ChatWriteRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val db: Postgrest = client.postgrest
    private val chatReadRepo = ChatReadRepository(client)

    /**
     * Crea o verifica que existe un chat directo
     * Usa retry logic para evitar fallos en conexiones inestables
     */
    suspend fun ensureDirectChat(uidA: String, uidB: String): String = withContext(Dispatchers.IO) {
        val chatId = chatReadRepo.directChatIdFor(uidA, uidB)

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
            Log.e(TAG, "ChatWriteRepository: Error verifying chat: ${e.message}", e)
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
            Log.d(TAG, "ChatWriteRepository: Direct chat created: $chatId")
        } catch (e: Exception) {
            Log.e(TAG, "ChatWriteRepository: Error creating direct chat", e)
        }

        chatId
    }
}
