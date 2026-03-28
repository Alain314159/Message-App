package com.example.messageapp.data.room

import com.example.messageapp.model.Chat

/**
 * Funciones de extensión para convertir entre Chat y ChatEntity
 * 
 * ✅ CORREGIDO ERROR #48: Agrega funciones de mapeo para Chat
 */

/**
 * Convierte Chat de dominio a ChatEntity para Room
 */
fun Chat.toEntity(): ChatEntity = ChatEntity(
    id = id,
    type = type,
    memberIds = memberIds,
    user1Typing = user1Typing,
    user2Typing = user2Typing,
    pinnedMessageId = pinnedMessageId,
    pinnedSnippet = pinnedSnippet,
    lastMessageEnc = lastMessageEnc,
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    synced = false
)

/**
 * Convierte ChatEntity de Room a Chat de dominio
 */
fun ChatEntity.toDomain(): Chat = Chat(
    id = id,
    type = type,
    memberIds = memberIds,
    user1Typing = user1Typing,
    user2Typing = user2Typing,
    pinnedMessageId = pinnedMessageId,
    pinnedSnippet = pinnedSnippet,
    lastMessageEnc = lastMessageEnc,
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
