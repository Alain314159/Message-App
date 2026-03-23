package com.example.messageapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Chat
import com.example.messageapp.model.Message
import com.example.messageapp.crypto.E2ECipher
import com.example.messageapp.crypto.SecureKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel de Chat Individual
 * 
 * Gestiona los mensajes de un chat específico en tiempo real
 * Incluye cifrado/descifrado E2E con libsodium
 */
class ChatViewModel(
    private val repo: ChatRepository = ChatRepository()
) : ViewModel() {
    
    private val _chat = MutableStateFlow<Chat?>(null)
    val chat = _chat.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private var currentChatId: String? = null
    private var currentUserId: String? = null
    
    /**
     * Empieza a observar un chat específico
     * Debe llamarse cuando se abre el chat
     */
    fun start(chatId: String, myUid: String) {
        if (currentChatId == chatId) return
        
        currentChatId = chatId
        currentUserId = myUid
        _isLoading.value = true
        
        // Observar información del chat
        viewModelScope.launch {
            try {
                repo.observeChat(chatId).collect { chat ->
                    _chat.value = chat
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar chat: ${e.message}"
            }
        }
        
        // Observar mensajes
        viewModelScope.launch {
            try {
                repo.observeMessages(chatId, myUid).collect { messageList ->
                    // Filtrar mensajes eliminados para este usuario
                    val filtered = messageList.filter { msg ->
                        !msg.deletedFor.contains(myUid)
                    }
                    _messages.value = filtered
                    _isLoading.value = false
                    
                    // Marcar como leídos automáticamente
                    if (filtered.isNotEmpty()) {
                        markAsRead(chatId, myUid)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar mensajes: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Detiene la observación del chat
     * Debe llamarse cuando se cierra el chat
     */
    fun stop() {
        currentChatId = null
        currentUserId = null
    }
    
    /**
     * Envía un mensaje de texto cifrado
     */
    fun sendText(chatId: String, myUid: String, plainText: String) {
        if (plainText.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Obtener clave maestra
                val masterKey = SecureKeyManager.getOrCreateMasterKey()
                
                // Derivar clave de sesión para este chat
                val sessionKey = E2ECipher.deriveSessionKey(
                    masterKey.encoded,
                    chatId
                )
                
                // Cifrar mensaje
                val encrypted = E2ECipher.encrypt(plainText, sessionKey)
                
                // Extraer partes del cifrado
                val parts = encrypted.split(":")
                val textEnc = parts[1] // ciphertext
                val nonce = parts[0]   // nonce
                val authTag = parts[2] // auth tag
                
                // Enviar a Supabase
                repo.sendText(
                    chatId = chatId,
                    senderId = myUid,
                    textEnc = textEnc,
                    nonce = nonce,
                    authTag = authTag
                )
            } catch (e: Exception) {
                _error.value = "Error al enviar mensaje: ${e.message}"
                android.util.Log.w("ChatViewModel", "Send message failed", e)
            }
        }
    }
    
    /**
     * Descifra un mensaje cifrado
     * Debe llamarse en la UI para mostrar el mensaje
     */
    fun decryptMessage(message: Message): String {
        if (message.type == "deleted") {
            return "[Mensaje eliminado]"
        }
        
        if (message.textEnc.isNullOrBlank()) {
            return ""
        }
        
        try {
            val chatId = currentChatId ?: return "[Error: Chat no disponible]"
            
            // Obtener clave maestra
            val masterKey = SecureKeyManager.getOrCreateMasterKey()
            
            // Derivar clave de sesión
            val sessionKey = E2ECipher.deriveSessionKey(
                masterKey.encoded,
                chatId
            )
            
            // Reconstruir mensaje cifrado completo
            val encrypted = "${message.nonce}:${message.textEnc}:${message.authTag}"
            
            // Descifrar
            return E2ECipher.decrypt(encrypted, sessionKey)
        } catch (e: Exception) {
            android.util.Log.w("ChatViewModel", "Decrypt failed", e)
            return "[Error: No se pudo descifrar]"
        }
    }
    
    /**
     * Marca los mensajes como leídos
     */
    fun markAsRead(chatId: String, myUid: String) {
        viewModelScope.launch {
            try {
                repo.markAsRead(chatId, myUid)
            } catch (e: Exception) {
                // Ignorar errores silenciosamente
            }
        }
    }
    
    /**
     * Fija un mensaje en el chat
     */
    fun pinMessage(chatId: String, message: Message) {
        viewModelScope.launch {
            try {
                val snippet = if (message.type == "text") {
                    decryptMessage(message).take(60)
                } else {
                    "[${message.type}]"
                }
                repo.pinMessage(chatId, message.id, snippet)
            } catch (e: Exception) {
                _error.value = "Error al fijar mensaje: ${e.message}"
            }
        }
    }
    
    /**
     * Desfija un mensaje
     */
    fun unpinMessage(chatId: String) {
        viewModelScope.launch {
            try {
                repo.unpinMessage(chatId)
            } catch (e: Exception) {
                _error.value = "Error al desfijar mensaje: ${e.message}"
            }
        }
    }
    
    /**
     * Elimina un mensaje solo para el usuario
     */
    fun deleteMessageForUser(chatId: String, messageId: String, uid: String) {
        viewModelScope.launch {
            try {
                repo.deleteMessageForUser(chatId, messageId, uid)
            } catch (e: Exception) {
                _error.value = "Error al eliminar mensaje: ${e.message}"
            }
        }
    }
    
    /**
     * Elimina un mensaje para todos
     */
    fun deleteMessageForAll(chatId: String, messageId: String) {
        viewModelScope.launch {
            try {
                repo.deleteMessageForAll(chatId, messageId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar mensaje: ${e.message}"
            }
        }
    }
}
