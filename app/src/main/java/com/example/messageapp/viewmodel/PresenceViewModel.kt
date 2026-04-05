package com.example.messageapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.TypingRepository
import com.example.messageapp.data.UserPresenceRepository
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

// ✅ ERR-009: Tag constante para logging
private const val TAG = "MessageApp"

/**
 * ViewModel para Presence y Typing Indicators
 *
 * Gestiona:
 * - Indicador de "escribiendo..."
 * - Estado online/offline de la pareja
 * - Última vez en línea
 */
class PresenceViewModel(
    private val typingRepo: TypingRepository = TypingRepository(SupabaseConfig.client),
    private val presenceRepo: UserPresenceRepository = UserPresenceRepository(SupabaseConfig.client)
) : ViewModel() {

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
    fun observeTyping(chatId: String, myUid: String) {
        viewModelScope.launch {
            typingRepo.observePartnerTyping(chatId, myUid)
                .debounce(300)
                .collect { isTyping ->
                    _isPartnerTyping.value = isTyping
                }
        }
    }

    /**
     * Empieza a observar estado online de la pareja
     */
    fun observeOnline(partnerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            presenceRepo.observePartnerOnline(partnerId)
                .collect { isOnline ->
                    _isPartnerOnline.value = isOnline
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            _partnerLastSeen.value = presenceRepo.getPartnerLastSeen(partnerId)
        }
    }

    /**
     * Actualiza estado de "escribiendo"
     */
    fun setTyping(chatId: String, isTyping: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            typingRepo.setTypingStatus(chatId, isTyping)
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
     * Limpia estado de typing
     */
    fun clearTyping(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            typingRepo.setTypingStatus(chatId, false)
        }
    }

    /**
     * Obtiene última vez en línea
     */
    suspend fun getPartnerLastSeen(partnerId: String): Long? {
        return withContext(Dispatchers.IO) {
            presenceRepo.getPartnerLastSeen(partnerId)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
