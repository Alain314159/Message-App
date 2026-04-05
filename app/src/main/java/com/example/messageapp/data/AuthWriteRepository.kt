package com.example.messageapp.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.messageapp.crypto.E2ECipher
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Escritura de Autenticación
 *
 * Responsabilidad única: OPERACIONES de autenticación (login, registro, logout)
 *
 * Funciones:
 * 1. signUpWithEmail
 * 2. signInWithEmail
 * 3. signInAnonymously
 * 4. signInWithGoogle
 * 5. sendPasswordReset
 * 6. signOut
 */
class AuthWriteRepository(
    private val authReadRepository: AuthReadRepository = AuthReadRepository(),
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val auth = client.auth
    private val db: Postgrest = client.postgrest

    /**
     * Registro con email y password
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!authReadRepository.isValidEmail(email)) {
                return@withContext Result.failure(IllegalArgumentException("Email inválido"))
            }

            if (password.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("Password debe tener al menos 6 caracteres"))
            }

            val authResult = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val uid = authResult?.id?.toString() ?: return@withContext Result.failure(Exception("User ID is null"))

            createUserProfile(uid, email)

            Log.d(TAG, "AuthWriteRepository: Usuario registrado: $uid")
            Result.success(uid)

        } catch (e: Exception) {
            if (e.message?.contains("weak password", ignoreCase = true) == true) {
                Log.w(TAG, "AuthWriteRepository: Password débil", e)
                return@withContext Result.failure(IllegalArgumentException("Password demasiado débil"))
            }
            Log.e(TAG, "AuthWriteRepository: Unexpected sign up error", e)
            Result.failure(e)
        }
    }

    /**
     * Login con email y password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!authReadRepository.isValidEmail(email)) {
                return@withContext Result.failure(IllegalArgumentException("Email inválido"))
            }

            if (password.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Password no puede estar vacío"))
            }

            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val uid = auth.currentSessionOrNull()?.user?.id
                ?: error("User ID not found after login")

            Log.d(TAG, "AuthWriteRepository: Usuario logueado: $uid")
            Result.success(uid)

        } catch (e: Exception) {
            Log.w(TAG, "AuthWriteRepository: Auth error signing in: ${e.message}", e)
            Result.failure(Exception("Email o contraseña inválidos"))
        }
    }

    /**
     * Login anónimo (simulado con email temporal)
     */
    suspend fun signInAnonymously(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tempEmail = "anon_${System.currentTimeMillis()}@messageapp.local"
            val tempPassword = java.util.UUID.randomUUID().toString()

            val authResult = auth.signUpWith(Email) {
                email = tempEmail
                password = tempPassword
            }

            val uid = authResult?.id?.toString() ?: error("User ID is null after anonymous sign up")

            createUserProfile(uid, tempEmail)

            Log.d(TAG, "AuthWriteRepository: Usuario anónimo creado: $uid")
            Result.success(uid)

        } catch (e: Exception) {
            Log.w(TAG, "AuthWriteRepository: Auth error creating anonymous user: ${e.message}", e)
            Result.failure(Exception("Error al crear usuario anónimo"))
        }
    }

    /**
     * Login con Google usando Credential Manager
     */
    suspend fun signInWithGoogle(context: Context, webClientId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setNonce(java.util.UUID.randomUUID().toString())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleToken = googleIdTokenCredential.idToken

                auth.signInWith(IDToken) {
                    idToken = googleToken
                    provider = Google
                }

                val uid = auth.currentSessionOrNull()?.user?.id
                    ?: error("User ID not found after Google login")

                Log.d(TAG, "AuthWriteRepository: Google login exitoso: $uid")
                Result.success(uid)

            } else {
                Log.w(TAG, "AuthWriteRepository: Credencial de Google no válida")
                Result.failure(Exception("Credencial de Google no válida"))
            }

        } catch (e: GetCredentialException) {
            Log.w(TAG, "AuthWriteRepository: Credential Manager error: ${e.message}", e)
            Result.failure(Exception("Error de Google Sign In: ${e.message}"))
        } catch (e: SerializationException) {
            Log.w(TAG, "AuthWriteRepository: Serialization error in Google login", e)
            Result.failure(Exception("Error de datos en Google Sign In"))
        } catch (e: Exception) {
            Log.w(TAG, "AuthWriteRepository: Unexpected Google login error", e)
            Result.failure(e)
        }
    }

    /**
     * Envía email de recuperación de password
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.resetPasswordForEmail(email)
            Log.d(TAG, "AuthWriteRepository: Email de recuperación enviado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "AuthWriteRepository: Auth error sending password reset: ${e.message}", e)
            Result.failure(Exception("Error al enviar email de recuperación"))
        }
    }

    /**
     * Logout - Cierra sesión y limpia claves de cifrado
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            E2ECipher.deleteAllKeys()
            auth.signOut()
            Log.d(TAG, "AuthWriteRepository: Logout exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "AuthWriteRepository: Error during logout: ${e.message}", e)
            Result.failure(Exception("Error de conexión al cerrar sesión"))
        }
    }

    /**
     * Crea el perfil inicial del usuario en la tabla users
     */
    internal suspend fun createUserProfile(uid: String, email: String) = withContext(Dispatchers.IO) {
        try {
            db.from("users").insert(
                mapOf(
                    "id" to uid,
                    "display_name" to "Usuario",
                    "email" to email,
                    "bio" to "",
                    "is_online" to true,
                    "created_at" to (System.currentTimeMillis() / 1000),
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            )
            Log.d(TAG, "AuthWriteRepository: Perfil creado para: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "AuthWriteRepository: Postgrest error creating profile", e)
            throw e
        }
    }
}
