package com.example.messageapp.data

import android.net.Uri
import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Tag constante para logging consistente
 */
private const val TAG = "MessageApp.MediaRepository"

/**
 * Repositorio de Multimedia usando SOLO Supabase Storage
 * 
 * ✅ Sin Cloudinary - Todo se guarda en Supabase Storage
 * ✅ Bucket: "chat-media"
 * ✅ URLs firmadas con validez de 1 semana
 * 
 * Estructura de carpetas:
 * chat-media/
 *   ├── {chatId}/
 *   │     ├── {uuid}_image.jpg
 *   │     ├── {uuid}_video.mp4
 *   │     └── {uuid}_audio.m4a
 */
class MediaRepository {
    
    private val storage = SupabaseConfig.client.storage
    private val bucketName = "chat-media"
    
    /**
     * Sube una imagen, video o audio a Supabase Storage
     * 
     * @param uri URI del archivo a subir
     * @param chatId ID del chat (para organizar carpetas)
     * @param type Tipo de archivo: "image", "video", "audio"
     * @return URL firmada del archivo (válida por 7 días)
     */
    suspend fun uploadMedia(
        uri: Uri,
        chatId: String,
        type: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Obtener bucket
            val bucket = storage.from(bucketName)
            
            // Generar nombre único
            val extension = getFileExtension(uri, type)
            val fileName = "${UUID.randomUUID()}_$type.$extension"
            val path = "$chatId/$fileName"
            
            // Leer bytes del archivo
            val bytes = uri.readBytes()
            
            // Determinar content type
            val contentType = when (type) {
                "image" -> "image/jpeg"
                "video" -> "video/mp4"
                "audio" -> "audio/m4a"
                else -> "application/octet-stream"
            }
            
            // Subir archivo
            bucket.upload(path, bytes) {
                this.contentType = contentType
            }
            
            // Crear URL firmada (válida por 7 días)
            val signedUrl = bucket.createSignedUrl(
                path,
                expiresIn = 7 * 24 * 60 * 60 // 7 días en segundos
            )
            
            Result.success(signedUrl)

        } catch (e: Exception) {
            Log.w(TAG, "MediaRepository: Error uploading media: ${e.message}", e)
            Result.failure(Exception("Error de almacenamiento: ${e.message}"))
        } catch (e: IOException) {
            Log.w(TAG, "MediaRepository: IO error reading media file", e)
            Result.failure(Exception("Error de archivo: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "MediaRepository: Unexpected error uploading media", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un archivo de multimedia
     */
    suspend fun deleteMedia(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bucket = storage.from(bucketName)
            bucket.delete(filePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "MediaRepository: Error deleting media: ${e.message}", e)
            Result.failure(Exception("Error de almacenamiento: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "MediaRepository: Unexpected error deleting media", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene la extensión del archivo según el tipo
     */
    private fun getFileExtension(uri: Uri, type: String): String {
        return when (type) {
            "image" -> "jpg"
            "video" -> "mp4"
            "audio" -> "m4a"
            else -> "bin"
        }
    }
    
    /**
     * Lee los bytes de un Uri (extensión para Uri)
     */
    private fun Uri.readBytes(): ByteArray {
        val inputStream = when {
            this.scheme == "content" -> {
                android.content.ContentResolver::class.java
                    .getDeclaredMethod("openInputStream", android.net.Uri::class.java)
                    .let { method ->
                        val resolver = android.content.ContentResolver::class.java
                            .getDeclaredField("this$0")
                            .apply { isAccessible = true }
                            .get(this)
                        method.invoke(resolver, this) as? java.io.InputStream
                    }
            }
            this.scheme == "file" -> java.io.File(this.path!!).inputStream()
            else -> null
        }
        
        return inputStream?.use { it.readBytes() } ?: ByteArray(0)
    }
}
