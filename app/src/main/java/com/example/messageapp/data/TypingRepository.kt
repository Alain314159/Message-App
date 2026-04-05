package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.model.Chat
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.*
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

// ✅ TAG constante para logging
private const val TAG = "MessageApp.Typing"

/**
 * Repositorio de Indicadores de Typing
 *
 * Responsabilidad única: "Escribiendo..." indicators por chat
 *
 * Funciones:
 * 1. setTypingStatus
 * 2. observePartnerTyping
 */
class TypingRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val db: Postgrest = client.postgrest
    private val realtime = client.realtime

    /**
     * Actualiza el estado de "escribiendo" en un chat
     * Se auto-limpia después de 5 segundos
     */
    suspend fun setTypingStatus(chatId: String, isTyping: Boolean) = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentSessionOrNull()?.user?.id
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
                delay(5000)
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
            Log.w(TAG, "setTypingStatus failed: chatId=$chatId isTyping=$isTyping", e)
        }
    }

    /**
     * Observa si la otra persona está escribiendo
     */
    fun observePartnerTyping(chatId: String, myUid: String): Flow<Boolean> = callbackFlow {
        try {
            val channel = realtime.channel("chats:public:chats")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "chats"
            }

            channel.subscribe()

            val job = launch {
                changeFlow.collect { action ->
                    val recordJson = when (action) {
                        is PostgresAction.Insert, is PostgresAction.Update, is PostgresAction.Select -> action.record
                        is PostgresAction.Delete -> action.oldRecord
                        else -> null
                    }
                    if (recordJson != null) {
                        val chat = Json.decodeFromString<Chat>(recordJson.toString())
                        if (chat.id == chatId) {
                            val isPartnerTyping = if (chat.memberIds.firstOrNull() == myUid) {
                                chat.user2Typing ?: false
                            } else {
                                chat.user1Typing ?: false
                            }
                            trySend(isPartnerTyping)
                        }
                    }
                }
            }

            awaitClose {
                job.cancel()
                runBlocking { realtime.removeChannel(channel) }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Error observing typing: ${e.message}", e)
            close()
        }
    }
}
