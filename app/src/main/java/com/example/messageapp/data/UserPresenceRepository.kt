package com.example.messageapp.data

import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.*
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

// ✅ TAG constante para logging
private const val TAG = "MessageApp.Presence"

/**
 * Repositorio de Presencia de Usuario
 *
 * Responsabilidad única: Estado online/offline y last seen
 *
 * Funciones:
 * 1. updateOnlineStatus
 * 2. observePartnerOnline
 * 3. getPartnerLastSeen
 */
class UserPresenceRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    private val db: Postgrest = client.postgrest
    private val realtime = client.realtime

    /**
     * Actualiza el estado online/offline del usuario
     */
    suspend fun updateOnlineStatus(isOnline: Boolean) = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext

            db.from("users").update(
                mapOf(
                    "is_online" to isOnline,
                    "last_seen" to (System.currentTimeMillis() / 1000),
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", userId) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error updating online status: ${e.message}", e)
        }
    }

    /**
     * Observa el estado online de la pareja
     */
    fun observePartnerOnline(partnerId: String): Flow<Boolean> = callbackFlow {
        try {
            val channel = realtime.channel("users:public:users")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "users"
            }

            channel.subscribe()

            val job = launch {
                changeFlow.collect { action ->
                    val recordJson = when (action) {
                        is PostgresAction.Insert, is PostgresAction.Update, is PostgresAction.Select -> action.record
                        is PostgresAction.Delete -> action.oldRecord
                        else -> null
                    }
                    if (recordJson != null) {
                        val userStatus = Json.decodeFromString<UserStatusResponse>(recordJson.toString())
                        if (userStatus.id == partnerId) {
                            trySend(userStatus.isOnline)
                        }
                    }
                }
            }

            awaitClose {
                job.cancel()
                runBlocking { realtime.removeChannel(channel) }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Error observing online status: ${e.message}", e)
            close()
        }
    }

    /**
     * Obtiene el last seen de la pareja
     */
    suspend fun getPartnerLastSeen(partnerId: String): Long? = withContext(Dispatchers.IO) {
        try {
            val response = db.from("users")
                .select(columns = Columns.list("last_seen")) {
                    filter { eq("id", partnerId) }
                }
                .decodeSingleOrNull<UserLastSeenResponse>()

            response?.lastSeen
        } catch (e: Exception) {
            Log.w(TAG, "getPartnerLastSeen failed: partnerId=$partnerId", e)
            null
        }
    }
}

/**
 * Data class para respuesta de last_seen
 */
private data class UserLastSeenResponse(
    val lastSeen: Long?
)

/**
 * Data class para observar estado online/offline
 */
private data class UserStatusResponse(
    val id: String,
    val isOnline: Boolean
)
