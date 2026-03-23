package com.example.messageapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Chat para Supabase
 * 
 * Incluye typing indicators para ambos usuarios
 */
@Serializable
data class Chat(
    @SerialName("id")
    val id: String = "",
    
    @SerialName("type")
    val type: String = "couple", // 'couple' siempre para esta app
    
    @SerialName("member_ids")
    val memberIds: List<String> = emptyList(),
    
    // Typing indicators
    @SerialName("user1_typing")
    val user1Typing: Boolean = false,
    
    @SerialName("user2_typing")
    val user2Typing: Boolean = false,
    
    // Mensaje fijado
    @SerialName("pinned_message_id")
    val pinnedMessageId: String? = null,
    
    @SerialName("pinned_snippet")
    val pinnedSnippet: String? = null,
    
    // Metadatos
    @SerialName("last_message_enc")
    val lastMessageEnc: String? = null,
    
    @SerialName("last_message_at")
    val lastMessageAt: Long? = null,
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000,
    
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis() / 1000
) {
    /**
     * Verifica si un usuario específico está escribiendo
     */
    fun isUserTyping(userId: String): Boolean {
        val index = memberIds.indexOf(userId)
        return if (index == 0) user1Typing else if (index == 1) user2Typing else false
    }
}
