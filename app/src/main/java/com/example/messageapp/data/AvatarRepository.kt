package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.model.AvatarType
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Tag constante para logging
private const val TAG = "MessageApp.AvatarRepository"

/**
 * Repositorio para gestionar la selección y almacenamiento de avatares
 */
class AvatarRepository {

    private val db = SupabaseConfig.client.plugin(Postgrest)

    /**
     * Obtiene el avatar seleccionado por el usuario
     */
    suspend fun getUserAvatar(userId: String): AvatarType = withContext(Dispatchers.IO) {
        try {
            val response = db.from("users")
                .select(columns = Columns.list("avatar_type")) {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserAvatarResponse>()

            response?.avatarType?.let { AvatarType.fromId(it) } ?: AvatarType.CERDITA
        } catch (e: io.github.jan-tennert.supabase.exception.SupabaseException) {
            Log.w(TAG, "Supabase error getting avatar for $userId", e)
            AvatarType.CERDITA
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "Serialization error getting avatar", e)
            AvatarType.CERDITA
        } catch (e: java.net.UnknownHostException) {
            Log.w(TAG, "Network error getting avatar for $userId - no connectivity", e)
            AvatarType.CERDITA
        } catch (e: java.net.SocketTimeoutException) {
            Log.w(TAG, "Timeout error getting avatar for $userId", e)
            AvatarType.CERDITA
        } catch (e: java.io.IOException) {
            Log.w(TAG, "IO error getting avatar for $userId", e)
            AvatarType.CERDITA
        }
    }

    /**
     * Actualiza el avatar del usuario en la base de datos
     */
    suspend fun setUserAvatar(userId: String, avatarType: AvatarType): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.from("users").update(
                mapOf(
                    "avatar_type" to avatarType.id,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", userId) }
            }

            Result.success(Unit)
        } catch (e: io.github.jan-tennert.supabase.exception.SupabaseException) {
            Log.w(TAG, "Supabase error updating avatar for $userId", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "Serialization error updating avatar", e)
            Result.failure(Exception("Error de serialización: ${e.message}"))
        } catch (e: java.net.UnknownHostException) {
            Log.w(TAG, "Network error updating avatar for $userId - no connectivity", e)
            Result.failure(Exception("Error de conexión: sin red"))
        } catch (e: java.net.SocketTimeoutException) {
            Log.w(TAG, "Timeout error updating avatar for $userId", e)
            Result.failure(Exception("Tiempo de espera agotado"))
        } catch (e: java.io.IOException) {
            Log.w(TAG, "IO error updating avatar for $userId", e)
            Result.failure(Exception("Error de E/S: ${e.message}"))
        }
    }

    /**
     * Obtiene todos los avatares disponibles
     */
    fun getAllAvatars(): List<AvatarType> = AvatarType.getAll()
}

/**
 * Data class para la respuesta de la base de datos
 */
private data class UserAvatarResponse(
    val avatarType: String?
)
