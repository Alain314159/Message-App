package com.example.messageapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.AvatarRepository
import com.example.messageapp.model.AvatarType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la selección de avatares
 */
class AvatarViewModel(
    private val avatarRepository: AvatarRepository = AvatarRepository()
) : ViewModel() {

    // Estado actual de la UI
    private val _uiState = MutableStateFlow(AvatarUiState())
    val uiState: StateFlow<AvatarUiState> = _uiState.asStateFlow()

    // Avatar seleccionado actualmente
    private val _selectedAvatar = MutableStateFlow<AvatarType>(AvatarType.CERDITA)
    val selectedAvatar: StateFlow<AvatarType> = _selectedAvatar.asStateFlow()

    /**
     * Carga el avatar actual del usuario
     */
    fun loadUserAvatar(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val avatar = avatarRepository.getUserAvatar(userId)
            _selectedAvatar.value = avatar
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentAvatar = avatar
            )
        }
    }

    /**
     * Selecciona un avatar temporalmente (sin guardar)
     */
    fun selectAvatar(avatarType: AvatarType) {
        _selectedAvatar.value = avatarType
    }

    /**
     * Guarda el avatar seleccionado en la base de datos
     */
    fun saveAvatar(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            val result = avatarRepository.setUserAvatar(userId, _selectedAvatar.value)
            
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveSuccess = result.isSuccess,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    /**
     * Obtiene todos los avatares disponibles
     */
    fun getAllAvatars(): List<AvatarType> = avatarRepository.getAllAvatars()

    /**
     * Resetea el estado de éxito/error
     */
    fun resetState() {
        _uiState.value = _uiState.value.copy(
            saveSuccess = null,
            errorMessage = null
        )
    }
}

/**
 * Estado de la UI para la selección de avatares
 */
data class AvatarUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val currentAvatar: AvatarType = AvatarType.CERDITA,
    val saveSuccess: Boolean? = null,
    val errorMessage: String? = null
)
