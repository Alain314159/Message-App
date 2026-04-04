package com.example.messageapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.crypto.MessageDecryptor
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * ViewModel para acciones sobre mensajes (fijar, eliminar)
 * 
 * Responsabilidad única: Gestionar operaciones avanzadas sobre mensajes
 * 
 * Esta clase extrae las acciones de mensajes de ChatViewModel
 * para cumplir con el principio de responsabilidad única.
 * 
 * Funciones (4):
 * 1. pinMessage
 * 2. unpinMessage
 * 3. deleteMessageForUser
 * 4. deleteMessageForAll
 */
class MessageActionsViewModel(
    private val repo: ChatRepository = ChatRepository(),
    private val decryptor: MessageDecryptor = MessageDecryptor()
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /**
     * Fija un mensaje en el chat
     */
    fun pinMessage(chatId: String, message: Message) {
        viewModelScope.launch {
            try {
                val snippet = if (message.type == "text") {
                    decryptor.decrypt(message, chatId).take(60)
                } else {
                    "[${message.type}]"
                }
                repo.pinMessage(chatId, message.id, snippet)
            } catch (e: Exception) {
                Log.e(TAG, "Error pinning message", e)
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
                Log.e(TAG, "Error unpinning message", e)
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
                Log.e(TAG, "Error deleting message", e)
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
                Log.e(TAG, "Error deleting message", e)
                _error.value = "Error al eliminar mensaje: ${e.message}"
            }
        }
    }
}
