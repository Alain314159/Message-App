package com.example.messageapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para chats almacenados localmente.
 *
 * ✅ CORREGIDO ERROR #48: Agrega ChatEntity para consistencia con Room
 * 
 * Campos:
 * - id: ID único del chat (UUID como String)
 * - type: Tipo de chat ('couple' para esta app)
 * - memberIds: IDs de los miembros del chat
 * - user1Typing: true si user1 está escribiendo
 * - user2Typing: true si user2 está escribiendo
 * - pinnedMessageId: ID del mensaje fijado
 * - pinnedSnippet: Fragmento del mensaje fijado
 * - lastMessageEnc: Último mensaje cifrado
 * - lastMessageAt: Timestamp del último mensaje
 * - createdAt: Timestamp de creación
 * - updatedAt: Timestamp de última actualización
 * - synced: Boolean para tracking de sincronización
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    
    val type: String = "couple",
    
    val memberIds: List<String> = emptyList(),
    
    val user1Typing: Boolean = false,
    
    val user2Typing: Boolean = false,
    
    val pinnedMessageId: String? = null,
    
    val pinnedSnippet: String? = null,
    
    val lastMessageEnc: String? = null,
    
    val lastMessageAt: Long? = null,
    
    val createdAt: Long,
    
    val updatedAt: Long,
    
    val synced: Boolean = false
)
