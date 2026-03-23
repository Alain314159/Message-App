package com.example.messageapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de Autenticación
 * 
 * Gestiona el estado de login/logout del usuario
 */
class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {
    
    private val _isLogged = MutableStateFlow(repo.isUserLoggedIn())
    val isLogged = _isLogged.asStateFlow()
    
    private val _currentUserId = MutableStateFlow(repo.getCurrentUserId())
    val currentUserId = _currentUserId.asStateFlow()
    
    /**
     * Inicializa el estado de autenticación
     * Debe llamarse cuando se crea la ViewModel
     */
    fun init() {
        _isLogged.value = repo.isUserLoggedIn()
        _currentUserId.value = repo.getCurrentUserId()
    }
    
    /**
     * Login anónimo
     */
    fun signInAnonymously() {
        viewModelScope.launch {
            repo.signInAnonymously()
                .onSuccess { uid ->
                    _isLogged.value = true
                    _currentUserId.value = uid
                }
                .onFailure { error ->
                    android.util.Log.w("AuthViewModel", "Anonymous sign in failed", error)
                }
        }
    }
    
    /**
     * Login con email y password
     */
    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.signInWithEmail(email, password)
                .onSuccess { uid ->
                    _isLogged.value = true
                    _currentUserId.value = uid
                    onSuccess()
                }
                .onFailure { error ->
                    android.util.Log.w("AuthViewModel", "Email sign in failed", error)
                }
        }
    }
    
    /**
     * Registro con email y password
     */
    fun signUpWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.signUpWithEmail(email, password)
                .onSuccess { uid ->
                    _isLogged.value = true
                    _currentUserId.value = uid
                    onSuccess()
                }
                .onFailure { error ->
                    android.util.Log.w("AuthViewModel", "Sign up failed", error)
                }
        }
    }
    
    /**
     * Logout
     */
    fun signOut() {
        viewModelScope.launch {
            repo.signOut()
            _isLogged.value = false
            _currentUserId.value = null
        }
    }
    
    /**
     * Actualiza el estado de presencia (online/offline)
     */
    fun updatePresence(online: Boolean) {
        viewModelScope.launch {
            repo.updatePresence(online)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Marcar como offline cuando se destruye la ViewModel
        updatePresence(false)
    }
}
