package com.example.messageapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Usuario para Supabase
 */
@Serializable
data class User(
    @SerialName("id")
    val uid: String = "",
    
    @SerialName("display_name")
    val displayName: String = "",
    
    @SerialName("photo_url")
    val photoUrl: String? = null,
    
    @SerialName("bio")
    val bio: String = "",
    
    @SerialName("is_online")
    val isOnline: Boolean = false,
    
    @SerialName("last_seen")
    val lastSeen: Long? = null,
    
    @SerialName("onesignal_player_id")
    val oneSignalPlayerId: String? = null,
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000,
    
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis() / 1000
)
