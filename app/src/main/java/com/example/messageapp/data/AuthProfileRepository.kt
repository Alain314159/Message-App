package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ✅ TAG constante para logging
private const val TAG = "MessageApp"

/**
 * Repositorio de Perfil de Usuario
 *
 * Responsabilidad única: Gestión de PERFIL y presencia del usuario
 *
 * Funciones (4):
 * 1. upsertUserProfile
 * 2. updatePresence
 * 3. updateJPushRegistrationId
 * 4. createUserProfile (privada, delega a AuthWriteRepository)
 */
class AuthProfileRepository(
    private val authReadRepository: AuthReadRepository = AuthReadRepository(),
    private val authWriteRepository: AuthWriteRepository = AuthWriteRepository(authReadRepository)
) {

    private val auth = SupabaseConfig.client.plugin(Auth)
    private val db = SupabaseConfig.client.plugin(Postgrest)

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
                .decodeSingleOrNull()

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
                Log.d(TAG, "AuthProfileRepository: Perfil actualizado para: $uid")
            } else {
                // Crear perfil
                val email = auth.currentSessionOrNull()?.user?.email ?: ""
                authWriteRepository.createUserProfile(uid, email)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AuthProfileRepository: Error in upsert profile: ${e.message}", e)
        }
    }

    /**
     * Actualiza el estado de presencia del usuario (online/offline)
     */
    suspend fun updatePresence(online: Boolean) = withContext(Dispatchers.IO) {
        val uid = authReadRepository.getCurrentUserId() ?: return@withContext

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
            Log.d(TAG, "AuthProfileRepository: Presencia actualizada: $uid online=$online")
        } catch (e: Exception) {
            Log.e(TAG, "AuthProfileRepository: Error updating presence: ${e.message}", e)
        }
    }

    /**
     * Actualiza el JPush Registration ID para notificaciones push
     */
    suspend fun updateJPushRegistrationId(registrationId: String) = withContext(Dispatchers.IO) {
        val uid = authReadRepository.getCurrentUserId() ?: return@withContext

        try {
            db.from("users").update(
                mapOf(
                    "jpush_registration_id" to registrationId,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", uid) }
            }
            Log.d(TAG, "AuthProfileRepository: JPush Registration ID actualizado: $registrationId")
        } catch (e: Exception) {
            Log.e(TAG, "AuthProfileRepository: Error updating JPush ID: ${e.message}", e)
        }
    }
}
