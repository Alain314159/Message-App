package com.example.messageapp.data.room

import com.example.messageapp.model.Message

/**
 * Funciones de extensión para convertir entre Message y MessageEntity
 * 
 * ✅ CORREGIDO ERROR #47: Agrega funciones de mapeo
 */

/**
 * Convierte Message de dominio a MessageEntity para Room
 */
fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    chatId = chatId,
    senderId = senderId,
    type = type,
    textEnc = textEnc ?: "",
    nonce = nonce,
    mediaUrl = mediaUrl,
    createdAt = createdAt,
    deliveredAt = deliveredAt,
    readAt = readAt,
    deletedForAll = deletedForAll,
    deletedFor = deletedFor,
    synced = false
)

/**
 * Convierte MessageEntity de Room a Message de dominio
 */
fun MessageEntity.toDomain(): Message = Message(
    id = id,
    chatId = chatId,
    senderId = senderId,
    type = type,
    textEnc = textEnc.takeIf { it.isNotEmpty() },
    nonce = nonce,
    mediaUrl = mediaUrl,
    createdAt = createdAt,
    deliveredAt = deliveredAt,
    readAt = readAt,
    deletedForAll = deletedForAll,
    deletedFor = deletedFor
)
