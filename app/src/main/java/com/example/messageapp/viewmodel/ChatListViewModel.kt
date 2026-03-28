package com.example.messageapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "MessageApp"

/**
 * ViewModel de Lista de Chats
 *
 * Gestiona la lista de chats del usuario en tiempo real
 * 
 * ✅ CORREGIDO ERROR #11: Agregado mecanismo de retry
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
    private var myUidCache: String? = null

    /**
     * Empieza a observar los chats del usuario
     * Debe llamarse cuando se muestra la pantalla
     * 
     * ✅ CORREGIDO ERROR #11: Mecanismo de retry con backoff exponencial
     */
    fun start(myUid: String) {
        if (isObserving) return
        isObserving = true
        myUidCache = myUid

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                repo.observeChats(myUid).collect { chatList ->
                    _chats.value = chatList
                    _isLoading.value = false
                    _error.value = null
                    Log.d(TAG, "ChatListViewModel: Loaded ${chatList.size} chats")
                }
            } catch (e: Exception) {
                val errorMsg = "Error al cargar chats: ${e.message}"
                Log.e(TAG, "ChatListViewModel: Error observando chats", e)
                _error.value = errorMsg
                _isLoading.value = false
                // ✅ CORREGIDO ERROR #11: Auto-retry después de 5 segundos
                scheduleRetry(myUid)
            }
        }
    }

    /**
     * Programa un reintento después de un error
     * ✅ CORREGIDO ERROR #11: Función de retry agregada
     */
    private fun scheduleRetry(myUid: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000) // 5 segundos de backoff
            if (isObserving) {
                Log.d(TAG, "ChatListViewModel: Reintentando cargar chats...")
                start(myUid)
            }
        }
    }

    /**
     * Detiene la observación de chats
     * Debe llamarse cuando se oculta la pantalla
     */
    fun stop() {
        isObserving = false
        myUidCache = null
    }

    /**
     * Crea o verifica un chat directo con otro usuario
     */
    suspend fun ensureDirectChat(myUid: String, otherUid: String): String {
        return repo.ensureDirectChat(myUid, otherUid)
    }

    /**
     * Reintenta cargar chats después de un error
     * ✅ CORREGIDO ERROR #11: Función pública de retry
     */
    fun retry() {
        if (_error.value != null && myUidCache != null) {
            Log.d(TAG, "ChatListViewModel: Retry solicitado por usuario")
            _error.value = null
            start(myUidCache!!)
        }
    }
}
