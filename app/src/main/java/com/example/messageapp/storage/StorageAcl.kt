package com.example.messageapp.storage

import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.withContext
import io.github.jan.supabase.storage.storage

// Note: StorageAcl usa Supabase Storage - pendiente de implementación completa

object StorageAcl {
    private val storage = SupabaseConfig.client.storage

    suspend fun ensureMemberMarker(chatId: String, uid: String) {
        // Note: Pendiente de implementación con Supabase Storage
        Log.d("StorageAcl", "ensureMemberMarker: chat=$chatId uid=$uid (no implementado)")
    }

    suspend fun removeMemberMarker(chatId: String, uid: String) {
        // Note: Pendiente de implementación con Supabase Storage
        Log.d("StorageAcl", "removeMemberMarker: chat=$chatId uid=$uid (no implementado)")
    }
}
