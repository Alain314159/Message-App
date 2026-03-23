package com.example.messageapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Chat para Supabase
 */
@Serializable
data class Chat(
    @SerialName("id")
    val id: String = "",
    
    @SerialName("type")
    val type: String = "direct", // direct, group
    
    @SerialName("name")
    val name: String? = null, // Solo para grupos
    
    @SerialName("photo_url")
    val photoUrl: String? = null,
    
    @SerialName("owner_id")
    val ownerId: String? = null, // Solo para grupos
    
    @SerialName("member_ids")
    val memberIds: List<String> = emptyList(),
    
    @SerialName("last_message_enc")
    val lastMessageEnc: String? = null, // Último mensaje cifrado
    
    @SerialName("last_message_at")
    val lastMessageAt: Long? = null,
    
    @SerialName("pinned_message_id")
    val pinnedMessageId: String? = null,
    
    @SerialName("pinned_snippet")
    val pinnedSnippet: String? = null,
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000,
    
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis() / 1000
)
