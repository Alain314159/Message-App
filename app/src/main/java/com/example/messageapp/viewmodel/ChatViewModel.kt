package com.example.messageapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.crypto.MessageDecryptor
import com.example.messageapp.crypto.E2ECipher
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Chat
import com.example.messageapp.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ✅ ERR-009: Tag constante para logging
private const val TAG = "MessageApp"

/**
 * ViewModel de Chat Individual
 *
 * Responsabilidad única: Gestionar el estado de la pantalla de chat
 * - Ciclo de vida (start, stop)
 * - Observación de datos (chat, mensajes)
 * - Envío de mensajes
 * - Marcar como leído
 *
 * Funciones (6):
 * 1. start
 * 2. stop
 * 3. sendText
 * 4. decryptMessage (delega a MessageDecryptor)
 * 5. markAsRead
 *
 * Notas:
 * - pinMessage, unpinMessage, deleteMessage* → MessageActionsViewModel
 * - decryptMessage → delega a MessageDecryptor
 */
class ChatViewModel(
    private val repo: ChatRepository = ChatRepository(),
    private val decryptor: MessageDecryptor = MessageDecryptor()
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

    /**
     * Empieza a observar un chat específico
     */
    fun start(chatId: String, myUid: String) {
        require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
        require(myUid.isNotBlank()) { "myUid no puede estar vacío" }

        if (currentChatId == chatId) return

        currentChatId = chatId
        _isLoading.value = true

        observeChatInfo(chatId)
        observeChatMessages(chatId, myUid)
    }

    /**
     * Observa información del chat (extraído para reducir complejidad)
     */
    private fun observeChatInfo(chatId: String) {
        viewModelScope.launch {
            try {
                repo.observeChat(chatId).collect { chat ->
                    _chat.value = chat
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading chat", e)
                _error.value = "Error al cargar chat: ${e.message}"
            }
        }
    }

    /**
     * Observa mensajes del chat (extraído para reducir complejidad)
     */
    private fun observeChatMessages(chatId: String, myUid: String) {
        viewModelScope.launch {
            try {
                repo.observeMessages(chatId, myUid).collect { messageList ->
                    val filtered = messageList.filter { msg ->
                        !msg.deletedFor.contains(myUid)
                    }
                    _messages.value = filtered
                    _isLoading.value = false

                    if (filtered.isNotEmpty()) {
                        markAsRead(chatId, myUid)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading messages", e)
                _error.value = "Error al cargar mensajes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Detiene la observación del chat
     */
    fun stop() {
        currentChatId = null
    }

    /**
     * Envía un mensaje de texto cifrado
     */
    fun sendText(chatId: String, myUid: String, plainText: String) {
        if (plainText.isBlank()) return

        viewModelScope.launch {
            try {
                val encrypted = E2ECipher.encrypt(plainText, chatId)

                val parts = encrypted.split(":")
                if (parts.size != 2) {
                    _error.value = "Error: Formato de cifrado inválido"
                    return@launch
                }

                val iv = parts[0]
                val textEnc = parts[1]

                repo.sendText(
                    chatId = chatId,
                    senderId = myUid,
                    textEnc = textEnc,
                    iv = iv
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _error.value = "Error al enviar mensaje: ${e.message}"
            }
        }
    }

    /**
     * Descifra un mensaje cifrado (delega a MessageDecryptor)
     */
    fun decryptMessage(message: Message): String {
        return decryptor.decrypt(message, currentChatId)
    }

    /**
     * Marca los mensajes como leídos
     */
    fun markAsRead(chatId: String, myUid: String) {
        viewModelScope.launch {
            try {
                repo.markAsRead(chatId, myUid)
            } catch (e: Exception) {
                Log.w(TAG, "Mark as read failed: $chatId", e)
                _error.value = "No se pudo marcar como leído"
            }
        }
    }
}
