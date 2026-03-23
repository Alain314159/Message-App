package com.example.messageapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Mensaje para Supabase
 * 
 * El texto va cifrado con libsodium (AES-256-GCM)
 * Formato del cifrado: {nonce}:{ciphertext}:{authTag}
 * Todo en Base64
 */
@Serializable
data class Message(
    @SerialName("id")
    val id: String = "",
    
    @SerialName("chat_id")
    val chatId: String = "",
    
    @SerialName("sender_id")
    val senderId: String = "",
    
    @SerialName("type")
    val type: String = "text", // text, image, video, audio
    
    @SerialName("text_enc")
    val textEnc: String? = null, // Texto cifrado con libsodium
    
    @SerialName("media_url")
    val mediaUrl: String? = null, // URL para multimedia
    
    @SerialName("nonce")
    val nonce: String? = null, // Nonce para AES-256-GCM
    
    @SerialName("auth_tag")
    val authTag: String? = null, // Tag de autenticación
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000,
    
    @SerialName("delivered_at")
    val deliveredAt: Long? = null,
    
    @SerialName("read_at")
    val readAt: Long? = null,
    
    @SerialName("deleted_for_all")
    val deletedForAll: Boolean = false,
    
    @SerialName("deleted_for")
    val deletedFor: List<String> = emptyList() // UIDs de usuarios que borraron el mensaje
)
