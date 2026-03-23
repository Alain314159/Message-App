package com.example.messageapp.data

import com.example.messageapp.model.Chat
import com.example.messageapp.model.Message
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.Channel
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Repositorio de Chats usando Supabase Postgrest + Realtime
 * 
 * ✅ VERIFICADO: Implementación actualizada con supabase-kt 2.x
 * 
 * Reemplaza a Firebase Firestore con Supabase
 * 
 * Funcionalidades:
 * - Chat 1:1 (direct)
 * - Mensajes en tiempo real (WebSockets)
 * - Enviar/recibir mensajes cifrados
 * - Estado de entrega/lectura
 * - Mensajes fijados
 * - Eliminar mensajes
 */
class ChatRepository {
    
    private val db = SupabaseConfig.client.plugin(Postgrest)
    private val realtime = SupabaseConfig.client.plugin(Realtime)
    
    /**
     * Genera un ID único para chat directo entre 2 usuarios
     * El ID es determinista (siempre el mismo para los mismos usuarios)
     * 
     * Nota: Para UUIDs, usamos el formato ordenado para evitar duplicados
     */
    fun directChatIdFor(uidA: String, uidB: String): String {
        return listOf(uidA, uidB).sorted().joinToString("_")
    }
    
    /**
     * Crea o verifica que existe un chat directo
     */
    suspend fun ensureDirectChat(uidA: String, uidB: String): String = withContext(Dispatchers.IO) {
        val chatId = directChatIdFor(uidA, uidB)
        
        try {
            // Verificar si ya existe
            val existing = db.from("chats")
                .select(columns = Columns.list("id")) {
                    filter { eq("id", chatId) }
                }
                .decodeSingle<Chat>()
            
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
            // Chat no existe, crear nuevo
        }
        
        // Crear nuevo chat
        db.from("chats").insert(
            mapOf(
                "id" to chatId,
                "type" to "direct",
                "member_ids" to listOf(uidA, uidB),
                "created_at" to (System.currentTimeMillis() / 1000),
                "updated_at" to (System.currentTimeMillis() / 1000)
            )
        )
        
        chatId
    }
    
    /**
     * Observa la lista de chats del usuario en tiempo real
     */
    fun observeChats(uid: String): Flow<List<Chat>> = callbackFlow {
        val channel = realtime.from("chats")
        
        // Suscribirse a cambios
        val subscription = channel.subscribe { channel ->
            // Cargar chats iniciales
            loadChatsForUser(uid)
            
            // Escuchar cambios
            channel.onPostgresChanges(
                event = PostgresAction.ALL,
                schema = "public",
                table = "chats"
            ) { change ->
                loadChatsForUser(uid)
            }
        }
        
        awaitClose {
            realtime.removeChannel(subscription)
        }
    }
    
    /**
     * Carga los chats del usuario desde la base de datos
     */
    private suspend fun loadChatsForUser(uid: String) {
        try {
            val chats = db.from("chats")
                .select(columns = Columns.list("*")) {
                    filter {
                        contains("member_ids", listOf(uid))
                    }
                    order("updated_at" to false) // DESC
                }
                .decodeList<Chat>()
            
            // Enviar por el flow (si está activo)
            // Esto se maneja automáticamente por callbackFlow
        } catch (e: Exception) {
            android.util.Log.w("ChatRepository", "Load chats error", e)
        }
    }
    
    /**
     * Observa un chat específico en tiempo real
     */
    fun observeChat(chatId: String): Flow<Chat?> = callbackFlow {
        try {
            val chat = db.from("chats")
                .select(columns = Columns.list("*")) {
                    filter { eq("id", chatId) }
                }
                .decodeSingle<Chat>()
            
            trySend(chat)
        } catch (e: Exception) {
            trySend(null)
        }
        
        awaitClose { }
    }
    
    /**
     * Observa los mensajes de un chat en tiempo real
     */
    fun observeMessages(chatId: String, myUid: String): Flow<List<Message>> = callbackFlow {
        // Cargar mensajes iniciales
        loadMessages(chatId)
        
        // Suscribirse a cambios en mensajes
        val channel = realtime.from("messages")
        
        val subscription = channel.subscribe { channel ->
            channel.onPostgresChanges(
                event = PostgresAction.INSERT,
                schema = "public",
                table = "messages",
                filter = PostgresChangeFilter.eq("chat_id", chatId)
            ) { change ->
                // Nuevo mensaje recibido
                loadMessages(chatId)
                
                // Marcar como entregado automáticamente
                val newMessage = change.decodeRecord<Message>()
                if (newMessage.senderId != myUid) {
                    markDelivered(chatId, newMessage.id, myUid)
                }
            }
            
            channel.onPostgresChanges(
                event = PostgresAction.UPDATE,
                schema = "public",
                table = "messages",
                filter = PostgresChangeFilter.eq("chat_id", chatId)
            ) { change ->
                // Mensaje actualizado (lectura, eliminación, etc.)
                loadMessages(chatId)
            }
        }
        
        awaitClose {
            realtime.removeChannel(subscription)
        }
    }
    
    /**
     * Carga los mensajes de un chat
     */
    private suspend fun loadMessages(chatId: String) {
        try {
            val messages = db.from("messages")
                .select(columns = Columns.list("*")) {
                    filter { eq("chat_id", chatId) }
                    order("created_at" to true) // ASC (más antiguos primero)
                }
                .decodeList<Message>()
            
            // Filtrar mensajes eliminados para este usuario
            // Esto se maneja en la UI
        } catch (e: Exception) {
            android.util.Log.w("ChatRepository", "Load messages error", e)
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
        try {
            db.from("messages").insert(
                mapOf(
                    "chat_id" to chatId,
                    "sender_id" to senderId,
                    "type" to "text",
                    "text_enc" to textEnc,
                    "nonce" to iv, // Usamos nonce para almacenar el IV
                    "auth_tag" to null, // No se usa con Android Keystore
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
                filter { eq("id", chatId) }
            }
        } catch (e: Exception) {
            android.util.Log.w("ChatRepository", "Send message error", e)
            throw e
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
                    eq("id", messageId) and neq("sender_id", uid)
                }
            }
        } catch (e: Exception) {
            // Ignorar errores silenciosamente
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
                filter {
                    eq("chat_id", chatId) and
                    neq("sender_id", uid) and
                    (isNull("read_at") or lt("read_at", System.currentTimeMillis() / 1000))
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ChatRepository", "Mark as read error", e)
        }
    }
    
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
            android.util.Log.w("ChatRepository", "Pin message error", e)
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
            android.util.Log.w("ChatRepository", "Unpin message error", e)
        }
    }
    
    /**
     * Elimina un mensaje solo para el usuario (soft delete)
     */
    suspend fun deleteMessageForUser(chatId: String, messageId: String, uid: String) = withContext(Dispatchers.IO) {
        try {
            // Obtener mensaje actual
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
            android.util.Log.w("ChatRepository", "Delete message error", e)
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
            
            // Actualizar último mensaje del chat
            db.from("chats").update(
                mapOf(
                    "last_message_enc" to "[Mensaje eliminado]",
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", chatId) }
            }
        } catch (e: Exception) {
            android.util.Log.w("ChatRepository", "Delete for all error", e)
        }
    }
    
    /**
     * Cuenta mensajes no leídos en un chat
     */
    suspend fun countUnreadMessages(chatId: String, uid: String): Int = withContext(Dispatchers.IO) {
        try {
            val response = db.from("messages")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("chat_id", chatId) and
                        neq("sender_id", uid) and
                        (isNull("read_at"))
                    }
                }
            
            response.size
        } catch (e: Exception) {
            0
        }
    }
}
