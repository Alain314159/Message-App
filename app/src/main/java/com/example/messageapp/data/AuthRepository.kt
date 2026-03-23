package com.example.messageapp.data

import com.example.messageapp.model.User
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.crypto.SecureKeyManager
import io.github.jan.tennert.supabase.auth.Auth
import io.github.jan.tennert.supabase.auth.providers.builtin.Email
import io.github.jan.tennert.supabase.postgrest.Postgrest
import io.github.jan.tennert.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.tennert.supabase.postgrest.postgrest

/**
 * Repositorio de Autenticación usando Supabase Auth
 * 
 * Reemplaza a Firebase Auth con Supabase Gotrue
 * 
 * Funcionalidades:
 * - Registro con email/password
 * - Login con email/password
 * - Login anónimo (simulado con email temporal)
 * - Gestión de sesión
 * - Logout
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
     * Obtiene los datos completos del usuario actual
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
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Crear usuario con Supabase Auth
            val authResult = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val uid = authResult.user.id
            
            // Crear perfil en la tabla users
            createUserProfile(uid, email)
            
            // Generar clave maestra para cifrado E2E
            SecureKeyManager.getOrCreateMasterKey()
            
            Result.success(uid)
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Sign up error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login con email y password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Login con Supabase Auth
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val uid = auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("User ID not found after login")
            
            // Verificar/actualizar perfil
            upsertUserProfile(uid)
            
            Result.success(uid)
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Sign in error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Login anónimo (simulado con email temporal)
     * Supabase no soporta login anónimo nativo, así que creamos
     * un usuario con email temporal
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
            
            // Generar clave maestra
            SecureKeyManager.getOrCreateMasterKey()
            
            Result.success(uid)
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Anonymous sign in error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Crea el perfil inicial del usuario
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
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Create profile error", e)
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
                // Actualizar last_seen
                db.from("users").update(
                    mapOf(
                        "is_online" to true,
                        "last_seen" to (System.currentTimeMillis() / 1000),
                        "updated_at" to (System.currentTimeMillis() / 1000)
                    )
                ) {
                    filter { eq("id", uid) }
                }
            }
        } catch (e: Exception) {
            // Si no existe, crear
            createUserProfile(uid, auth.currentSessionOrNull()?.user?.email ?: "")
        }
    }
    
    /**
     * Actualiza el estado de presencia del usuario
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
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Update OneSignal ID error", e)
        }
    }
    
    /**
     * Logout - Cierra sesión y limpia claves
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            // Actualizar presencia antes de salir
            updatePresence(false)
            
            // Eliminar clave maestra
            SecureKeyManager.deleteMasterKey()
            
            // Cerrar sesión con Supabase
            auth.signOut()
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
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Password reset error", e)
            Result.failure(e)
        }
    }
}
