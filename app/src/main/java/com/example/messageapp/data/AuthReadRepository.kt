package com.example.messageapp.data

import android.util.Log
import android.util.Patterns
import com.example.messageapp.model.User
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Lectura de Autenticación
 *
 * Responsabilidad única: CONSULTAR estado de autenticación
 *
 * Funciones:
 * 1. isUserLoggedIn
 * 2. getCurrentUserId
 * 3. getCurrentUserEmail
 * 4. getCurrentUser
 * 5. isValidEmail
 */
class AuthReadRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val auth = client.auth
    private val db: Postgrest = client.postgrest

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
                    filter { eq("id", uid) }
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
