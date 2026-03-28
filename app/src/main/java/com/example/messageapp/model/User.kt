package com.example.messageapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Usuario para Supabase
 *
 * Incluye sistema de emparejamiento y presencia
 * ✅ CORREGIDO ERROR #9: uid → id para consistencia con la base de datos
 */
@Serializable
data class User(
    @SerialName("id")
    val id: String = "",  // ✅ Renombrado de uid a id
    
    @SerialName("email")
    val email: String = "",
    
    @SerialName("display_name")
    val displayName: String = "",
    
    @SerialName("photo_url")
    val photoUrl: String? = null,
    
    @SerialName("bio")
    val bio: String = "",
    
    // Sistema de emparejamiento
    @SerialName("pairing_code")
    val pairingCode: String? = null,
    
    @SerialName("partner_id")
    val partnerId: String? = null,
    
    @SerialName("is_paired")
    val isPaired: Boolean = false,
    
    // Presencia
    @SerialName("is_online")
    val isOnline: Boolean = false,
    
    @SerialName("last_seen")
    val lastSeen: Long? = null,
    
    @SerialName("is_typing")
    val isTyping: Boolean = false,
    
    @SerialName("typing_in_chat")
    val typingInChat: String? = null,
    
    // Notificaciones - JPush (Aurora Mobile)
    @SerialName("jpush_registration_id")
    val jpushRegistrationId: String? = null,
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000,
    
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis() / 1000
)
