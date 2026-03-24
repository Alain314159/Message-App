package com.example.messageapp.data

import com.example.messageapp.model.AvatarType
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        } catch (e: Exception) {
            // Si hay error, retornar avatar por defecto
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
        } catch (e: Exception) {
            Result.failure(e)
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
