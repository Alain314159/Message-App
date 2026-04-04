package com.example.messageapp.data

import android.net.Uri
import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import com.example.messageapp.crypto.E2ECipher
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.exception.SupabaseException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.exception.PostgrestException
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

private const val TAG = "MessageApp.ProfileRepository"

/**
 * Repositorio para gestionar el perfil del usuario usando Supabase
 */
class ProfileRepository {

    private val auth = SupabaseConfig.client.plugin(Auth)
    private val db = SupabaseConfig.client.plugin(Postgrest)
    private val storage = SupabaseConfig.client.plugin(Storage)

    /**
     * Actualiza el perfil del usuario en Supabase
     */
    suspend fun updateProfile(displayName: String, bio: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(IllegalStateException("Usuario no logueado"))

            db.from("users").update(
                mapOf(
                    "display_name" to displayName,
                    "bio" to bio,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", uid) }
            }

            Result.success(Unit)
        } catch (e: PostgrestException) {
            Log.w(TAG, "Postgrest error updating profile", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        } catch (e: SupabaseException) {
            Log.w(TAG, "Supabase error updating profile", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        } catch (e: SerializationException) {
            Log.w(TAG, "Serialization error updating profile", e)
            Result.failure(Exception("Error de datos: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error updating profile", e)
            Result.failure(e)
        }
    }

    /**
     * Sube un avatar a Supabase Storage y actualiza el perfil
     */
    suspend fun uploadAvatar(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(IllegalStateException("Usuario no logueado"))

            // Bucket para avatares
            val bucket = storage.from("avatars")
            
            // Nombre del archivo
            val fileName = "$uid/avatar.jpg"

            // Leer el archivo como ByteArray
            val bytes = readUriBytes(uri)
            
            // Subir a Supabase Storage
            bucket.upload(fileName, bytes) {
                upsert = true
            }

            // Obtener URL pública
            val url = bucket.publicUrl(fileName)

            // Actualizar en la base de datos
            db.from("users").update(
                mapOf(
                    "photo_url" to url,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", uid) }
            }

            Result.success(url)
        } catch (e: PostgrestException) {
            Log.w(TAG, "Postgrest error uploading avatar", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        } catch (e: SupabaseException) {
            Log.w(TAG, "Supabase error uploading avatar", e)
            Result.failure(Exception("Error de almacenamiento: ${e.message}"))
        } catch (e: SerializationException) {
            Log.w(TAG, "Serialization error uploading avatar", e)
            Result.failure(Exception("Error de datos: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error uploading avatar", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el perfil actual del usuario
     */
    suspend fun getCurrentProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(IllegalStateException("Usuario no logueado"))

            val user = db.from("users")
                .select {
                    filter { eq("id", uid) }
                }
                .decodeSingle<UserProfileResponse>()

            Result.success(
                UserProfile(
                    displayName = user.displayName,
                    bio = user.bio,
                    photoUrl = user.photoUrl,
                    isPaired = user.isPaired,
                    pairingCode = user.pairingCode
                )
            )
        } catch (e: SupabaseException) {
            Log.w(TAG, "Supabase error getting profile", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        } catch (e: SerializationException) {
            Log.w(TAG, "Serialization error getting profile", e)
            Result.failure(Exception("Error de datos: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting profile", e)
            Result.failure(e)
        }
    }

    /**
     * Lee los bytes de un URI
     */
    private suspend fun readUriBytes(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        SupabaseConfig.client.httpClient.request(uri.toString()).body<ByteArray>()
    }
}

/**
 * Data class para el perfil de usuario
 */
data class UserProfile(
    val displayName: String,
    val bio: String,
    val photoUrl: String?,
    val isPaired: Boolean,
    val pairingCode: String?
)

/**
 * Data class para la respuesta de la base de datos
 */
private data class UserProfileResponse(
    val display_name: String,
    val bio: String,
    val photo_url: String?,
    val is_paired: Boolean,
    val pairing_code: String?
)
