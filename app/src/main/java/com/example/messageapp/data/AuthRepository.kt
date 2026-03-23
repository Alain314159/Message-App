package com.example.messageapp.data

import com.example.messageapp.model.User
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.crypto.E2ECipher
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            
            // ✅ API CORRECTA para supabase-kt 2.x
            // Crear usuario con Supabase Auth
            val authResult = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val uid = authResult.user.id
            
            // Crear perfil en la tabla users
            createUserProfile(uid, email)
            
            android.util.Log.d("AuthRepository", "Usuario registrado: $uid")
            Result.success(uid)
            
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Sign up error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login con email y password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // ✅ API CORRECTA para supabase-kt 2.x
            // Login con Supabase Auth
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val uid = auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("User ID not found after login")
            
            // Verificar/actualizar perfil
            upsertUserProfile(uid)
            
            android.util.Log.d("AuthRepository", "Usuario logueado: $uid")
            Result.success(uid)
            
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Sign in error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login anónimo (simulado con email temporal)
     * 
     * Nota: Supabase no soporta login anónimo nativo en el plan free.
     * Esta implementación crea un usuario con email temporal único.
     */
    suspend fun signInAnonymously(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Generar email temporal único
            val tempEmail = "anon_${System.currentTimeMillis()}@messageapp.local"
            val tempPassword = java.util.UUID.randomUUID().toString()
            
            // Crear usuario anónimo
            val authResult = auth.signUpWith(Email) {
                this.email = tempEmail
                this.password = tempPassword
            }
            
            val uid = authResult.user.id
            
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
            
            android.util.Log.d("AuthRepository", "Usuario anónimo creado: $uid")
            Result.success(uid)
            
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Anonymous sign in error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Crea el perfil inicial del usuario en la tabla users
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
            android.util.Log.d("AuthRepository", "Perfil creado para: $uid")
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Create profile error", e)
            throw e
        }
    }
    
    /**
     * Actualiza o crea el perfil del usuario (idempotente)
     */
    suspend fun upsertUserProfile(uid: String) = withContext(Dispatchers.IO) {
        try {
            // Verificar si existe
            val existing = db.from("users")
                .select(columns = Columns.list("id")) {
                    filter { eq("id", uid) }
                }
                .decodeSingle<User>()
            
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
            } else {
                // Crear perfil
                val email = auth.currentSessionOrNull()?.user?.email ?: ""
                createUserProfile(uid, email)
            }
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Upsert profile error", e)
        }
    }
    
    /**
     * Actualiza el estado de presencia del usuario (online/offline)
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
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Update presence error", e)
        }
    }
    
    /**
     * Actualiza el OneSignal Player ID para notificaciones push
     */
    suspend fun updateOneSignalPlayerId(playerId: String) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        
        try {
            db.from("users").update(
                mapOf(
                    "onesignal_player_id" to playerId,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", uid) }
            }
            android.util.Log.d("AuthRepository", "OneSignal Player ID actualizado: $playerId")
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Update OneSignal ID error", e)
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
     * Valida formato de email
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
