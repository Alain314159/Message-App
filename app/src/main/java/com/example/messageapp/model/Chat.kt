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
    init {
        // Validar que el chat tenga al menos 1 miembro
        require(memberIds.isNotEmpty()) { 
            "Chat debe tener al menos 1 miembro, tiene ${memberIds.size}" 
        }
        // Validar que los IDs no sean vacíos
        require(memberIds.all { it.isNotBlank() }) {
            "Member IDs no pueden ser vacíos: $memberIds"
        }
    }

    /**
     * Verifica si un usuario específico está escribiendo
     *
     * ✅ CORREGIDO ERROR #57: Maneja caso donde userId no está en el chat
     */
    fun isUserTyping(userId: String): Boolean {
        val index = memberIds.indexOf(userId)
        return when (index) {
            0 -> user1Typing
            1 -> user2Typing
            else -> error("User $userId no está en el chat (memberIds: $memberIds)")
        }
    }
}
