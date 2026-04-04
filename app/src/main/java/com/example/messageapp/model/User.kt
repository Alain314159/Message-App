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

    // Notificaciones - FCM
    @SerialName("fcm_token")
    val fcmToken: String? = null,

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000,

    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis() / 1000
) {
    init {
        // Validar que el ID no sea vacío (solo para usuarios válidos)
        if (id.isNotEmpty()) {
            require(id.isNotBlank()) { 
                "User ID no puede ser solo whitespace: '$id'" 
            }
        }
        // Validar que el display name no sea vacío
        require(displayName.isNotBlank()) { 
            "Display name no puede estar vacío" 
        }
        // Validar longitud máxima de display name
        require(displayName.length <= 100) { 
            "Display name máximo 100 caracteres, tiene ${displayName.length}" 
        }
        // Validar consistencia entre isPaired y partnerId
        if (isPaired) {
            require(partnerId != null) { 
                "isPaired=true pero partnerId es null" 
            }
        } else {
            require(partnerId == null) { 
                "isPaired=false pero partnerId no es null" 
            }
        }
        // Validar consistencia entre isTyping y typingInChat
        if (isTyping) {
            require(typingInChat != null) { 
                "isTyping=true pero typingInChat es null" 
            }
        } else {
            require(typingInChat == null) { 
                "isTyping=false pero typingInChat no es null" 
            }
        }
    }
}
