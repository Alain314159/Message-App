package com.example.messageapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.data.PresenceRepository
import com.example.messageapp.model.Chat
import com.example.messageapp.model.Message
import com.example.messageapp.crypto.E2ECipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * ViewModel de Chat Individual
 *
 * ✅ VERIFICADO: Implementación actualizada con E2ECipher usando Android Keystore
 *
 * Gestiona los mensajes de un chat específico en tiempo real
 * Incluye cifrado/descifrado E2E con AES-256-GCM
 * Incluye typing indicators y presencia
 */
class ChatViewModel(
    private val repo: ChatRepository = ChatRepository(),
    private val presenceRepo: PresenceRepository = PresenceRepository()
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
     * 
     * ✅ CIFRADO: Usa Android Keystore + AES-256-GCM
     */
    fun sendText(chatId: String, myUid: String, plainText: String) {
        if (plainText.isBlank()) return
        
        viewModelScope.launch {
            try {
                // ✅ API CORRECTA: E2ECipher.encrypt() con chatId
                val encrypted = E2ECipher.encrypt(plainText, chatId)
                
                // Extraer partes del cifrado (formato: iv:ciphertext)
                val parts = encrypted.split(":")
                if (parts.size != 2) {
                    _error.value = "Error: Formato de cifrado inválido"
                    return@launch
                }
                
                val iv = parts[0]      // IV (reemplaza nonce)
                val textEnc = parts[1] // Ciphertext
                
                // Enviar a Supabase
                repo.sendText(
                    chatId = chatId,
                    senderId = myUid,
                    textEnc = textEnc,
                    iv = iv
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
     * 
     * ✅ DESCIFRADO: Usa Android Keystore + AES-256-GCM
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
            
            // ✅ API CORRECTA: E2ECipher.decrypt() con chatId
            // Reconstruir mensaje cifrado (formato: iv:ciphertext)
            val encrypted = "${message.nonce}:${message.textEnc}"
            
            // Descifrar
            return E2ECipher.decrypt(encrypted, chatId)
            
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
    
    // ============================================
    // TYPING INDICATORS Y PRESENCIA
    // ============================================
    
    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping = _isPartnerTyping.asStateFlow()
    
    private val _isPartnerOnline = MutableStateFlow(false)
    val isPartnerOnline = _isPartnerOnline.asStateFlow()
    
    private val _partnerLastSeen = MutableStateFlow<Long?>(null)
    val partnerLastSeen = _partnerLastSeen.asStateFlow()
    
    /**
     * Empieza a observar typing indicator de la pareja
     */
    @OptIn(FlowPreview::class)
    fun observePartnerTyping(chatId: String, myUid: String) {
        viewModelScope.launch {
            presenceRepo.observePartnerTyping(chatId, myUid)
                .debounce(300) // Evitar cambios muy rápidos
                .collect { isTyping ->
                    _isPartnerTyping.value = isTyping
                }
        }
    }
    
    /**
     * Empieza a observar estado online de la pareja
     */
    fun observePartnerOnline(partnerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            presenceRepo.observePartnerOnline(partnerId)
                .collect { isOnline ->
                    _isPartnerOnline.value = isOnline
                }
        }
        
        // Cargar last seen inicial
        viewModelScope.launch(Dispatchers.IO) {
            _partnerLastSeen.value = presenceRepo.getPartnerLastSeen(partnerId)
        }
    }
    
    /**
     * Actualiza estado de "escribiendo"
     * Se auto-limpia después de 5 segundos
     */
    fun setTyping(chatId: String, isTyping: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            presenceRepo.setTypingStatus(chatId, isTyping)
        }
    }
    
    /**
     * Actualiza estado online/offline
     */
    fun setOnline(online: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            presenceRepo.updateOnlineStatus(online)
        }
    }
    
    /**
     * Limpia recursos cuando se destruye el ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        // Marcar como offline y dejar de escribir
        setOnline(false)
        currentChatId?.let { setTyping(it, false) }
        presenceRepo.cleanup()
    }
}
