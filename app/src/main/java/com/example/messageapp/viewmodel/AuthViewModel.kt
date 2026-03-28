package com.example.messageapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "MessageApp"

/**
 * ViewModel de Autenticación
 *
 * Gestiona el estado de login/logout del usuario
 * 
 * ✅ CORREGIDO ERROR #10: Agregado estado de error
 */
class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _isLogged = MutableStateFlow(repo.isUserLoggedIn())
    val isLogged = _isLogged.asStateFlow()

    private val _currentUserId = MutableStateFlow(repo.getCurrentUserId())
    val currentUserId = _currentUserId.asStateFlow()

    // ✅ CORREGIDO ERROR #10: Agregado estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /**
     * Inicializa el estado de autenticación
     * Debe llamarse cuando se crea la ViewModel
     */
    fun init() {
        _isLogged.value = repo.isUserLoggedIn()
        _currentUserId.value = repo.getCurrentUserId()
        _error.value = null
        _isLoading.value = false
    }

    /**
     * Login anónimo
     * 
     * ✅ CORREGIDO: Manejo de error con estado y logging
     */
    fun signInAnonymously() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.signInAnonymously()
                .onSuccess { uid ->
                    _isLogged.value = true
                    _currentUserId.value = uid
                    _isLoading.value = false
                    Log.d(TAG, "AuthViewModel: Anonymous sign in successful - uid: $uid")
                }
                .onFailure { error ->
                    _error.value = "Error de autenticación: ${error.message}"
                    _isLoading.value = false
                    Log.e(TAG, "AuthViewModel: Anonymous sign in failed", error)
                }
        }
    }

    /**
     * Login con email y password
     * 
     * ✅ CORREGIDO: Manejo de error con estado y logging
     */
    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.signInWithEmail(email, password)
                .onSuccess { uid ->
                    _isLogged.value = true
                    _currentUserId.value = uid
                    _isLoading.value = false
                    Log.d(TAG, "AuthViewModel: Email sign in successful - uid: $uid")
                    onSuccess()
                }
                .onFailure { error ->
                    _error.value = "Error de inicio de sesión: ${error.message}"
                    _isLoading.value = false
                    Log.e(TAG, "AuthViewModel: Email sign in failed", error)
                }
        }
    }

    /**
     * Registro con email y password
     * 
     * ✅ CORREGIDO: Manejo de error con estado y logging
     */
    fun signUpWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.signUpWithEmail(email, password)
                .onSuccess { uid ->
                    _isLogged.value = true
                    _currentUserId.value = uid
                    _isLoading.value = false
                    Log.d(TAG, "AuthViewModel: Sign up successful - uid: $uid")
                    onSuccess()
                }
                .onFailure { error ->
                    _error.value = "Error de registro: ${error.message}"
                    _isLoading.value = false
                    Log.e(TAG, "AuthViewModel: Sign up failed", error)
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
            _error.value = null
            Log.d(TAG, "AuthViewModel: User signed out")
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

    /**
     * Limpia el estado de error
     */
    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Marcar como offline cuando se destruye la ViewModel
        updatePresence(false)
    }
}
