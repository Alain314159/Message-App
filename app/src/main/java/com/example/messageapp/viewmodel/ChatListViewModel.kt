package com.example.messageapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de Lista de Chats
 * 
 * Gestiona la lista de chats del usuario en tiempo real
 */
class ChatListViewModel(
    private val repo: ChatRepository = ChatRepository()
) : ViewModel() {
    
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private var isObserving = false
    
    /**
     * Empieza a observar los chats del usuario
     * Debe llamarse cuando se muestra la pantalla
     */
    fun start(myUid: String) {
        if (isObserving) return
        isObserving = true
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                repo.observeChats(myUid).collect { chatList ->
                    _chats.value = chatList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar chats: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Detiene la observación de chats
     * Debe llamarse cuando se oculta la pantalla
     */
    fun stop() {
        isObserving = false
    }
    
    /**
     * Crea o verifica un chat directo con otro usuario
     */
    suspend fun ensureDirectChat(myUid: String, otherUid: String): String {
        return repo.ensureDirectChat(myUid, otherUid)
    }
}
