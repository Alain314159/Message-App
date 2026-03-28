package com.example.messageapp.storage

import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import kotlinx.coroutines.withContext
import io.github.jan-tennert.supabase.storage.storage

// ✅ CORREGIDO: Migrar FirebaseStorage → Supabase Storage
// TODO: Verificar que Supabase Storage esté configurado en el proyecto

object StorageAcl {
    // ✅ Usar Supabase Storage en lugar de FirebaseStorage
    private val storage = SupabaseConfig.client.storage

    suspend fun ensureMemberMarker(chatId: String, uid: String) {
        // TODO: Implementar con Supabase Storage
        // val path = "chats/$chatId/members/$uid"
        // storage.upload(path, ByteArray(0))
        Log.d("StorageAcl", "ensureMemberMarker: chat=$chatId uid=$uid (no implementado con Supabase aún)")
    }

    suspend fun removeMemberMarker(chatId: String, uid: String) {
        // TODO: Implementar con Supabase Storage
        // val path = "chats/$chatId/members/$uid"
        // storage.delete(path)
        Log.d("StorageAcl", "removeMemberMarker: chat=$chatId uid=$uid (no implementado con Supabase aún)")
    }
}
