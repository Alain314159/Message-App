package com.example.messageapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para usuarios almacenados localmente.
 *
 * ✅ CORREGIDO ERROR #49: Agrega UserEntity para consistencia con Room
 * 
 * Campos:
 * - id: ID único del usuario (UUID como String)
 * - email: Email del usuario
 * - displayName: Nombre para mostrar
 * - photoUrl: URL de foto de perfil
 * - bio: Biografía del usuario
 * - pairingCode: Código de emparejamiento
 * - partnerId: ID de la pareja
 * - isPaired: true si está emparejado
 * - isOnline: true si está online
 * - lastSeen: Timestamp de última vez visto
 * - isTyping: true si está escribiendo
 * - typingInChat: ID del chat donde está escribiendo
 * - jpushRegistrationId: ID de registro de JPush
 * - createdAt: Timestamp de creación
 * - updatedAt: Timestamp de última actualización
 * - synced: Boolean para tracking de sincronización
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    
    val email: String,
    
    val displayName: String,
    
    val photoUrl: String? = null,
    
    val bio: String = "",
    
    val pairingCode: String? = null,
    
    val partnerId: String? = null,
    
    val isPaired: Boolean = false,
    
    val isOnline: Boolean = false,
    
    val lastSeen: Long? = null,
    
    val isTyping: Boolean = false,
    
    val typingInChat: String? = null,
    
    val jpushRegistrationId: String? = null,
    
    val createdAt: Long,
    
    val updatedAt: Long,
    
    val synced: Boolean = false
)
