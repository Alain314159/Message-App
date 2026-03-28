package com.example.messageapp.data.room

import com.example.messageapp.model.User

/**
 * Funciones de extensión para convertir entre User y UserEntity
 * 
 * ✅ CORREGIDO ERROR #49: Agrega funciones de mapeo para User
 */

/**
 * Convierte User de dominio a UserEntity para Room
 */
fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    bio = bio,
    pairingCode = pairingCode,
    partnerId = partnerId,
    isPaired = isPaired,
    isOnline = isOnline,
    lastSeen = lastSeen,
    isTyping = isTyping,
    typingInChat = typingInChat,
    jpushRegistrationId = jpushRegistrationId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    synced = false
)

/**
 * Convierte UserEntity de Room a User de dominio
 */
fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    bio = bio,
    pairingCode = pairingCode,
    partnerId = partnerId,
    isPaired = isPaired,
    isOnline = isOnline,
    lastSeen = lastSeen,
    isTyping = isTyping,
    typingInChat = typingInChat,
    jpushRegistrationId = jpushRegistrationId,
    createdAt = createdAt,
    updatedAt = updatedAt
)
