package com.example.messageapp.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetGoogleIdOption
import androidx.credentials.exceptions.GetCredentialException
import com.example.messageapp.crypto.E2ECipher
import com.example.messageapp.supabase.SupabaseConfig
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Email
import io.github.jan.supabase.auth.providers.IDToken
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Escritura de Autenticación
 *
 * Responsabilidad única: OPERACIONES de autenticación (login, registro, logout)
 *
 * Funciones (7):
 * 1. signUpWithEmail
 * 2. signInWithEmail
 * 3. signInAnonymously
 * 4. signInWithGoogle
 * 5. sendPasswordReset
 * 6. signOut
 * 7. createUserProfile (privada, usada por AuthProfileRepository)
 */
class AuthWriteRepository(
    private val authReadRepository: AuthReadRepository = AuthReadRepository()
) {

    private val auth = SupabaseConfig.client.plugin(Auth)
    private val db = SupabaseConfig.client.plugin(Postgrest)

    /**
     * Registro con email y password
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validar email
            if (!authReadRepository.isValidEmail(email)) {
                return@withContext Result.failure(IllegalArgumentException("Email inválido"))
            }

            // Validar password
            if (password.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("Password debe tener al menos 6 caracteres"))
            }

            // Registrar con Supabase
            val authResult = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val uid = authResult.user?.id ?: return@withContext Result.failure(Exception("User ID is null"))

            // Crear perfil en la tabla users
            createUserProfile(uid, email)

            Log.d(TAG, "AuthWriteRepository: Usuario registrado: $uid")
            Result.success(uid)

        } catch (e: AuthWeakPasswordException) {
            Log.w(TAG, "AuthWriteRepository: Password débil", e)
            Result.failure(IllegalArgumentException("Password demasiado débil"))
        } catch (e: AuthRestException) {
            Log.w(TAG, "AuthWriteRepository: Auth error: ${e.errorCode}", e)
            Result.failure(Exception("Error de autenticación: ${e.errorCode}"))
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "AuthWriteRepository: Serialization error signing up", e)
            Result.failure(Exception("Error de datos al registrar"))
        } catch (e: Exception) {
            Log.e(TAG, "AuthWriteRepository: Unexpected sign up error", e)
            Result.failure(e)
        }
    }

    /**
     * Login con email y password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validar email
            if (!authReadRepository.isValidEmail(email)) {
                return@withContext Result.failure(IllegalArgumentException("Email inválido"))
            }

            // Validar password
            if (password.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Password no puede estar vacío"))
            }

            // Login con Supabase
            auth.signInWith(Email) {
                email = email
                password = password
            }

            val uid = auth.currentSessionOrNull()?.user?.id
                ?: error("User ID not found after login")

            Log.d(TAG, "AuthWriteRepository: Usuario logueado: $uid")
            Result.success(uid)

        } catch (e: AuthRestException) {
            Log.w(TAG, "AuthWriteRepository: Auth error: ${e.errorCode}", e)
            Result.failure(Exception("Email o contraseña inválidos"))
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "AuthWriteRepository: Serialization error signing in", e)
            Result.failure(Exception("Error de datos al iniciar sesión"))
        } catch (e: Exception) {
            Log.e(TAG, "AuthWriteRepository: Unexpected sign in error", e)
            Result.failure(e)
        }
    }

    /**
     * Login anónimo (simulado con email temporal)
     */
    suspend fun signInAnonymously(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Generar email temporal único
            val tempEmail = "anon_${System.currentTimeMillis()}@messageapp.local"
            val tempPassword = java.util.UUID.randomUUID().toString()

            // Crear usuario anónimo
            val authResult = auth.signUpWith(Email) {
                email = tempEmail
                password = tempPassword
            }

            val uid = authResult.user?.id ?: error("User ID is null after anonymous sign up")

            // Crear perfil anónimo
            createUserProfile(uid, tempEmail)

            Log.d(TAG, "AuthWriteRepository: Usuario anónimo creado: $uid")
            Result.success(uid)

        } catch (e: AuthRestException) {
            Log.w(TAG, "AuthWriteRepository: Auth error creating anonymous user", e)
            Result.failure(Exception("Error al crear usuario anónimo"))
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "AuthWriteRepository: Serialization error creating anonymous user", e)
            Result.failure(Exception("Error de datos al crear usuario anónimo"))
        } catch (e: Exception) {
            Log.e(TAG, "AuthWriteRepository: Unexpected anonymous sign in error", e)
            Result.failure(e)
        }
    }

    /**
     * Login con Google usando Credential Manager
     */
    suspend fun signInWithGoogle(context: Context, webClientId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            // Configurar opción de Google ID
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setNonce(java.util.UUID.randomUUID().toString())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Obtener credencial
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            // Verificar que es Google ID Token
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Login con Supabase
                auth.signInWith(IDToken) {
                    provider = Google
                    idToken = idToken
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
        } catch (e: kotlinx.serialization.SerializationException) {
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
        } catch (e: AuthRestException) {
            Log.w(TAG, "AuthWriteRepository: Auth error sending password reset", e)
            Result.failure(Exception("Error al enviar email de recuperación"))
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "AuthWriteRepository: Serialization error sending password reset", e)
            Result.failure(Exception("Error de datos al enviar email"))
        } catch (e: Exception) {
            Log.w(TAG, "AuthWriteRepository: Unexpected password reset error", e)
            Result.failure(e)
        }
    }

    /**
     * Logout - Cierra sesión y limpia claves de cifrado
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Eliminar claves de cifrado E2E
            E2ECipher.deleteAllKeys()

            // Cerrar sesión con Supabase
            auth.signOut()

            Log.d(TAG, "AuthWriteRepository: Logout exitoso")
            Result.success(Unit)
        } catch (e: io.github.jan-tennert.supabase.exception.SupabaseException) {
            Log.w(TAG, "AuthWriteRepository: Supabase error during logout", e)
            Result.failure(Exception("Error de conexión al cerrar sesión"))
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "AuthWriteRepository: Serialization error during logout", e)
            Result.failure(Exception("Error de datos al cerrar sesión"))
        } catch (e: Exception) {
            Log.e(TAG, "AuthWriteRepository: Unexpected logout error", e)
            Result.failure(e)
        }
    }

    /**
     * Crea el perfil inicial del usuario en la tabla users
     * (paquete privado para uso interno)
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
        } catch (e: io.github.jan-tennert.supabase.postgrest.exception.PostgrestException) {
            Log.e(TAG, "AuthWriteRepository: Postgrest error creating profile", e)
            throw e
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e(TAG, "AuthWriteRepository: Serialization error creating profile", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AuthWriteRepository: Unexpected error creating profile", e)
            throw e
        }
    }
}
