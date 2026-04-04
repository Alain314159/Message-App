package com.example.messageapp.data

import android.util.Log
import android.util.Patterns
import com.example.messageapp.model.User
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Lectura de Autenticación
 *
 * Responsabilidad única: CONSULTAR estado de autenticación
 *
 * Funciones (5):
 * 1. isUserLoggedIn
 * 2. getCurrentUserId
 * 3. getCurrentUserEmail
 * 4. getCurrentUser
 * 5. isValidEmail (privada)
 */
class AuthReadRepository {

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
            Log.w(TAG, "AuthReadRepository: Error getting user: ${e.message}", e)
            null
        }
    }

    /**
     * Valida formato de email
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
