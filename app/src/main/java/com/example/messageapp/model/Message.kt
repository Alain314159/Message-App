package com.example.messageapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estados posibles de un mensaje
 */
@Serializable
enum class MessageStatus {
    SENT,       // 1 tick Gris Koala
    DELIVERED,  // 2 ticks Gris Koala
    READ        // 2 ticks Rosa Chanchita
}

/**
 * Modelo de Mensaje para Supabase
 * 
 * El texto va cifrado con Android Keystore (AES-256-GCM)
 * Formato del cifrado: {iv}:{ciphertext} (Base64)
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
    val textEnc: String? = null, // Texto cifrado
    
    @SerialName("nonce")
    val nonce: String? = null, // IV para AES-256-GCM
    
    @SerialName("media_url")
    val mediaUrl: String? = null, // URL de Supabase Storage
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000,
    
    // Estados de mensaje (WhatsApp style)
    @SerialName("delivered_at")
    val deliveredAt: Long? = null,
    
    @SerialName("read_at")
    val readAt: Long? = null,
    
    @SerialName("deleted_for_all")
    val deletedForAll: Boolean = false,
    
    @SerialName("deleted_for")
    val deletedFor: List<String> = emptyList()
) {
    /**
     * Calcula el estado actual del mensaje
     * ✅ CORREGIDO ERROR #8: Manejar deletedForAll correctamente
     */
    val status: MessageStatus?
        get() = when {
            deletedForAll -> null  // ✅ Mensaje eliminado para todos
            readAt != null -> MessageStatus.READ      // Rosa Chanchita
            deliveredAt != null -> MessageStatus.DELIVERED  // Gris Koala
            else -> MessageStatus.SENT                // Gris Koala
        }
}
