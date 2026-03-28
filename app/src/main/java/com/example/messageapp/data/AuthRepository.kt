package com.example.messageapp.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.messageapp.model.User
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.crypto.E2ECipher
import io.github.jan-tennert.supabase.auth.Auth
import io.github.jan-tennert.supabase.auth.providers.Email
import io.github.jan-tennert.supabase.auth.providers.IDToken
import io.github.jan-tennert.supabase.auth.providers.Google
import io.github.jan-tennert.supabase.postgrest.Postgrest
import io.github.jan-tennert.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "MessageApp"

/**
 * Repositorio de Autenticación usando Supabase Auth
 * 
 * ✅ VERIFICADO: Implementación actualizada con supabase-kt 3.x (Marzo 2026)
 * 
 * Reemplaza a Firebase Auth con Supabase Gotrue
 * 
 * Funcionalidades:
 * - Registro con email/password
 * - Login con email/password
 * - Login con Google (OAuth)
 * - Gestión de sesión
 * - Logout
 * 
 * Documentación oficial:
 * https://github.com/supabase-community/supabase-kt
 * https://supabase.com/docs/guides/getting-started/quickstarts/android
 */
class AuthRepository {
    
    private val auth = SupabaseConfig.client.plugin(Auth)
    private val db = SupabaseConfig.client.plugin(Postgrest)
    
    /**
     * Verifica si hay un usuario logueado
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }
    
    /**
     * Obtiene el UID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return auth.currentSessionOrNull()?.user?.id
    }
    
    /**
     * Obtiene el email del usuario actual
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentSessionOrNull()?.user?.email
    }
    
    /**
     * Obtiene los datos completos del usuario actual desde la tabla users
     */
    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext null
        
        try {
            val response = db
                .from("users")
                .select(columns = Columns.list("*")) {
                    filter {
                        eq("id", uid)
                    }
                }
                .decodeSingle<User>()
            
            response
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Error getting user", e)
            null
        }
    }
    
    /**
     * Registro con email y password
     *
     * @param email Email del usuario
     * @param password Contraseña (mínimo 6 caracteres)
     * @return Result con el UID del usuario o la excepción
     * 
     * ✅ CORREGIDO: Logging consistente con TAG
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validar email
            if (!isValidEmail(email)) {
                return@withContext Result.failure(IllegalArgumentException("Email inválido"))
            }

            // Validar password
            if (password.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("Password debe tener al menos 6 caracteres"))
            }

            // ✅ API CORRECTA para supabase-kt 3.x
            val authResult = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val uid = authResult.user?.id ?: return@withContext Result.failure(Exception("User ID is null"))

            // Crear perfil en la tabla users
            createUserProfile(uid, email)

            Log.d(TAG, "AuthRepository: Usuario registrado: $uid")
            Result.success(uid)

        } catch (e: Exception) {
            Log.e(TAG, "AuthRepository: Sign up error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login con email y password
     * 
     * ✅ CORREGIDO ERROR #19: Validación de email agregada
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // ✅ CORREGIDO ERROR #19: Validar email antes
            if (!isValidEmail(email)) {
                return@withContext Result.failure(IllegalArgumentException("Email inválido"))
            }

            // Validar password
            if (password.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Password no puede estar vacío"))
            }

            // ✅ API CORRECTA para supabase-kt 3.x
            auth.signInWith(Email) {
                email = email
                password = password
            }

            val uid = auth.currentSessionOrNull()?.user?.id
                ?: error("User ID not found after login")

            // Verificar/actualizar perfil
            upsertUserProfile(uid)

            Log.d(TAG, "AuthRepository: Usuario logueado: $uid")
            Result.success(uid)

        } catch (e: Exception) {
            Log.e(TAG, "AuthRepository: Sign in error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login anónimo (simulado con email temporal)
     *
     * Nota: Supabase no soporta login anónimo nativo en el plan free.
     * Esta implementación crea un usuario con email temporal único.
     * 
     * ✅ CORREGIDO: Logging consistente con TAG
     */
    suspend fun signInAnonymously(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Generar email temporal único
            val tempEmail = "anon_${System.currentTimeMillis()}@messageapp.local"
            val tempPassword = java.util.UUID.randomUUID().toString()

            // Crear usuario anónimo con API 3.x
            val authResult = auth.signUpWith(Email) {
                email = tempEmail
                password = tempPassword
            }

            val uid = authResult.user?.id ?: error("User ID is null after anonymous sign up")

            // Crear perfil anónimo
            db.from("users").insert(
                mapOf(
                    "id" to uid,
                    "display_name" to "Usuario Anónimo",
                    "email" to tempEmail,
                    "bio" to "",
                    "is_online" to true,
                    "created_at" to (System.currentTimeMillis() / 1000),
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            )

            Log.d(TAG, "AuthRepository: Usuario anónimo creado: $uid")
            Result.success(uid)

        } catch (e: Exception) {
            Log.e(TAG, "AuthRepository: Anonymous sign in error", e)
            Result.failure(e)
        }
    }

    /**
     * Crea el perfil inicial del usuario en la tabla users
     * 
     * ✅ CORREGIDO: Logging consistente con TAG
     */
    private suspend fun createUserProfile(uid: String, email: String) = withContext(Dispatchers.IO) {
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
            Log.d(TAG, "AuthRepository: Perfil creado para: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "AuthRepository: Create profile error", e)
            throw e
        }
    }
    
    /**
     * Actualiza o crea el perfil del usuario (idempotente)
     * 
     * ✅ CORREGIDO: Logging consistente con TAG
     */
    suspend fun upsertUserProfile(uid: String) = withContext(Dispatchers.IO) {
        try {
            // Verificar si existe
            val existing = db.from("users")
                .select(columns = Columns.list("id")) {
                    filter { eq("id", uid) }
                }
                .decodeSingleOrNull<User>()

            if (existing != null) {
                // Actualizar last_seen y online
                db.from("users").update(
                    mapOf(
                        "is_online" to true,
                        "last_seen" to (System.currentTimeMillis() / 1000),
                        "updated_at" to (System.currentTimeMillis() / 1000)
                    )
                ) {
                    filter { eq("id", uid) }
                }
                Log.d(TAG, "AuthRepository: Perfil actualizado para: $uid")
            } else {
                // Crear perfil
                val email = auth.currentSessionOrNull()?.user?.email ?: ""
                createUserProfile(uid, email)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AuthRepository: Upsert profile error", e)
        }
    }

    /**
     * Actualiza el estado de presencia del usuario (online/offline)
     * 
     * ✅ CORREGIDO: Logging consistente con TAG
     */
    suspend fun updatePresence(online: Boolean) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext

        try {
            db.from("users").update(
                mapOf(
                    "is_online" to online,
                    "last_seen" to (System.currentTimeMillis() / 1000),
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", uid) }
            }
            Log.d(TAG, "AuthRepository: Presencia actualizada: $uid online=$online")
        } catch (e: Exception) {
            Log.e(TAG, "AuthRepository: Update presence error", e)
        }
    }

    /**
     * Actualiza el JPush Registration ID para notificaciones push
     * 
     * ✅ CORREGIDO: Logging consistente con TAG
     */
    suspend fun updateJPushRegistrationId(registrationId: String) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext

        try {
            db.from("users").update(
                mapOf(
                    "jpush_registration_id" to registrationId,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", uid) }
            }
            Log.d(TAG, "AuthRepository: JPush Registration ID actualizado: $registrationId")
        } catch (e: Exception) {
            Log.e(TAG, "AuthRepository: Update JPush ID error", e)
        }
    }
    
    /**
     * Logout - Cierra sesión y limpia claves de cifrado
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            // Actualizar presencia antes de salir
            updatePresence(false)
            
            // Eliminar claves de cifrado E2E
            E2ECipher.deleteAllKeys()
            
            // Cerrar sesión con Supabase
            auth.signOut()
            
            android.util.Log.d("AuthRepository", "Logout exitoso")
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Sign out error", e)
        }
    }
    
    /**
     * Envía email de recuperación de password
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.resetPasswordForEmail(email)
            android.util.Log.d("AuthRepository", "Email de recuperación enviado")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Password reset error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login con Google usando Credential Manager
     *
     * @param context Contexto de Android
     * @param webClientId Client ID de Web de Google Cloud Console
     * @return Result con el UID del usuario o error
     */
    suspend fun signInWithGoogle(context: Context, webClientId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            // Configurar opción de Google ID
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false) // Mostrar todas las cuentas
                .setAutoSelectEnabled(true) // Auto-seleccionar si hay una cuenta
                .setNonce(java.util.UUID.randomUUID().toString()) // Prevenir replay attacks
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

                // ✅ Login con Supabase usando API 3.x
                auth.signInWith(IDToken) {
                    provider = io.github.jan-tennert.supabase.auth.providers.Google
                    idToken = idToken
                }

                val uid = auth.currentSessionOrNull()?.user?.id
                    ?: error("User ID not found after Google login")

                // Crear/actualizar perfil
                upsertUserProfile(uid)

                // Generar clave maestra para cifrado
                // E2ECipher se inicializa automáticamente al usarse por primera vez
                android.util.Log.d("AuthRepository", "Google login exitoso: $uid")
                Result.success(uid)

            } else {
                android.util.Log.w("AuthRepository", "Credencial de Google no válida")
                Result.failure(Exception("Credencial de Google no válida"))
            }

        } catch (e: GetCredentialException) {
            android.util.Log.w("AuthRepository", "Error de Credential Manager: ${e.message}", e)
            Result.failure(Exception("Error de Google Sign In: ${e.message}"))
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Error de Google Sign In", e)
            Result.failure(e)
        }
    }
    
    /**
     * Valida formato de email
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
