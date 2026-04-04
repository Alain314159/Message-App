package com.example.messageapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * TypeConverter para List<String> <-> JSON String
 */
class MessageConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        runCatching { Json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())
}

/**
 * Entidad Room para mensajes almacenados localmente.
 *
 * ✅ CORREGIDO ERROR #2: Ahora coincide con Message del dominio
 * 
 * Campos:
 * - id: ID único del mensaje (UUID como String)
 * - chatId: ID del chat/conversación
 * - senderId: ID del usuario que envió el mensaje
 * - type: Tipo de mensaje (text, image, video, audio)
 * - textEnc: Texto del mensaje cifrado
 * - nonce: Nonce para encriptación (IV de AES-256-GCM)
 * - mediaUrl: URL de multimedia en Supabase Storage
 * - createdAt: Timestamp de creación (epoch seconds)
 * - deliveredAt: Timestamp de entrega
 * - readAt: Timestamp de lectura
 * - deletedForAll: true si fue eliminado para todos
 * - deletedFor: Lista de IDs para quienes fue eliminado
 * - synced: Boolean para tracking de sincronización con servidor
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    
    val chatId: String,
    
    val senderId: String,
    
    val type: String = "text",
    
    val textEnc: String,
    
    val nonce: String? = null,
    
    val mediaUrl: String? = null,
    
    val createdAt: Long,
    
    val deliveredAt: Long? = null,
    
    val readAt: Long? = null,
    
    val deletedForAll: Boolean = false,
    
    val deletedFor: List<String> = emptyList(),
    
    val synced: Boolean = false
)
