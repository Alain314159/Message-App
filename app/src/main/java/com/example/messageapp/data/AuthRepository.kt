package com.example.messageapp.data

import android.content.Context
import com.example.messageapp.model.User
import kotlinx.coroutines.flow.Flow

/**
 * FACADE TEMPORAL - Para compatibilidad con código existente
 *
 * Este facade combina los 3 nuevos repositorios para mantener
 * la API antigua de AuthRepository mientras se actualiza el código.
 *
 * TODO: Eliminar este archivo cuando todo el código use los nuevos repositorios
 *
 * @property authReadRepository Repositorio de lectura de auth
 * @property authWriteRepository Repositorio de escritura de auth
 * @property authProfileRepository Repositorio de perfil
 */
@Deprecated(
    "Usar AuthReadRepository, AuthWriteRepository y AuthProfileRepository por separado",
    ReplaceWith("AuthReadRepository, AuthWriteRepository, AuthProfileRepository")
)
class AuthRepository(
    private val authReadRepository: AuthReadRepository = AuthReadRepository(),
    private val authWriteRepository: AuthWriteRepository = AuthWriteRepository(authReadRepository),
    private val authProfileRepository: AuthProfileRepository = AuthProfileRepository(authReadRepository, authWriteRepository)
) {

    // Delegados a AuthReadRepository
    fun isUserLoggedIn(): Boolean =
        authReadRepository.isUserLoggedIn()

    fun getCurrentUserId(): String? =
        authReadRepository.getCurrentUserId()

    fun getCurrentUserEmail(): String? =
        authReadRepository.getCurrentUserEmail()

    suspend fun getCurrentUser(): User? =
        authReadRepository.getCurrentUser()

    // Delegados a AuthWriteRepository
    suspend fun signUpWithEmail(email: String, password: String): Result<String> =
        authWriteRepository.signUpWithEmail(email, password)

    suspend fun signInWithEmail(email: String, password: String): Result<String> =
        authWriteRepository.signInWithEmail(email, password)

    suspend fun signInAnonymously(): Result<String> =
        authWriteRepository.signInAnonymously()

    suspend fun signInWithGoogle(context: Context, webClientId: String): Result<String> =
        authWriteRepository.signInWithGoogle(context, webClientId)

    suspend fun sendPasswordReset(email: String): Result<Unit> =
        authWriteRepository.sendPasswordReset(email)

    suspend fun signOut(): Result<Unit> =
        authWriteRepository.signOut()

    // Delegados a AuthProfileRepository
    suspend fun upsertUserProfile(uid: String) =
        authProfileRepository.upsertUserProfile(uid)

    suspend fun updatePresence(online: Boolean) =
        authProfileRepository.updatePresence(online)

    suspend fun updateFCMToken(token: String) =
        authProfileRepository.updateFCMToken(token)
}
