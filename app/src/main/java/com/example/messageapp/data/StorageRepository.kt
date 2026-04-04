package com.example.messageapp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.exception.SupabaseException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG = "MessageApp"

/**
 * Parámetros para enviar multimedia
 * ✅ CORREGIDO: LongParameterList - Agrupar parámetros en data class
 */
data class MediaUploadParams(
    val chatId: String,
    val myUid: String,
    val uri: Uri,
    val type: String,
    val textEnc: String = "",
    val nonce: String = ""
)

/**
 * Repositorio para enviar multimedia usando Supabase Storage
 *
 * ✅ CORREGIDO 2026-03-28: 
 * - Inyección de Context en constructor
 * - readUriBytes implementado correctamente con ContentResolver
 */
class StorageRepository @Inject constructor(
    @android.content.Context.ApplicationContext private val context: Context
) {

    private val db = SupabaseConfig.client.plugin(Postgrest)
    private val storage = SupabaseConfig.client.plugin(Storage)

    /**
     * Envía multimedia (imagen/video/audio) a un chat
     * ✅ CORREGIDO: Usar data class para parámetros
     */
    suspend fun sendMedia(params: MediaUploadParams): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Bucket para multimedia de chats
            val bucket = storage.from("chat-media")

            // Extensión del archivo según tipo
            val ext = when (params.type) {
                "image" -> "jpg"
                "video" -> "mp4"
                "audio" -> "m4a"
                else -> "bin"
            }

            // Nombre del archivo: chat_id/timestamp_uid.ext
            val fileName = "${params.chatId}/${System.currentTimeMillis()}_${params.myUid}.$ext"

            // Leer el archivo como ByteArray
            val bytes = readUriBytes(params.uri)

            // Subir a Supabase Storage
            bucket.upload(fileName, bytes) {
                upsert = true
            }

            // Obtener URL pública
            val mediaUrl = bucket.publicUrl(fileName)

            // Insertar mensaje en la base de datos
            db.from("messages").insert(
                mapOf(
                    "chat_id" to params.chatId,
                    "sender_id" to params.myUid,
                    "type" to params.type,
                    "text_enc" to params.textEnc,
                    "nonce" to params.nonce,
                    "media_url" to mediaUrl,
                    "created_at" to (System.currentTimeMillis() / 1000),
                    "delivered_at" to null,
                    "read_at" to null,
                    "deleted_for_all" to false,
                    "deleted_for" to listOf<String>()
                )
            )

            // Actualizar último mensaje del chat
            db.from("chats").update(
                mapOf(
                    "last_message_enc" to params.textEnc,
                    "last_message_at" to (System.currentTimeMillis() / 1000),
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", params.chatId) }
            }

            Result.success(Unit)
        } catch (e: SupabaseException) {
            Log.w(TAG, "Supabase error sending media", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        } catch (e: IOException) {
            Log.w(TAG, "IO error reading media file", e)
            Result.failure(Exception("Error de archivo: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error sending media", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina multimedia del storage
     */
    suspend fun deleteMedia(mediaUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bucket = storage.from("chat-media")
            
            // Extraer el path de la URL
            val fileName = mediaUrl.substringAfter("/object/public/chat-media/")
            
            bucket.delete(fileName)

            Result.success(Unit)
        } catch (e: SupabaseException) {
            Log.w(TAG, "Supabase error deleting media", e)
            Result.failure(Exception("Error de almacenamiento: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error deleting media", e)
            Result.failure(e)
        }
    }

    /**
     * Lee los bytes de un URI usando ContentResolver
     * 
     * ✅ CORREGIDO 2026-03-28: Implementación correcta con ContentResolver
     * 
     * @param uri URI del archivo a leer
     * @return ByteArray con el contenido del archivo
     * @throws IOException si no se puede leer el URI
     */
    private suspend fun readUriBytes(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: throw IOException("No se pudo leer el URI: $uri")
        } catch (e: IOException) {
            Log.e(TAG, "StorageRepository: Error al leer URI: $uri", e)
            throw e
        }
    }
}
