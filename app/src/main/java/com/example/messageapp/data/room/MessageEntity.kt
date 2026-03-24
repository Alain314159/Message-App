package com.example.messageapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para mensajes almacenados localmente.
 * 
 * Campos:
 * - id: ID único del mensaje (UUID como String)
 * - chatId: ID del chat/conversación
 * - senderId: ID del usuario que envió el mensaje
 * - textEnc: Texto del mensaje (encriptado o plano)
 * - nonce: Nonce para encriptación (opcional)
 * - createdAt: Timestamp de creación (epoch millis)
 * - synced: Boolean para tracking de sincronización con servidor
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String,
    val textEnc: String,
    val nonce: String? = null,
    val createdAt: Long,
    val synced: Boolean = false
)
