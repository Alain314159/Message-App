package com.example.messageapp.data

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

private const val TAG = "MessageApp.ContactsRepository"

/**
 * Repositorio para gestionar contactos usando Supabase
 */
class ContactsRepository {

    private val db = SupabaseConfig.client.plugin(Postgrest)

    /**
     * Agrega un contacto a la lista del usuario
     */
    suspend fun addContact(myUid: String, otherUid: String, alias: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.from("contacts").insert(
                mapOf(
                    "user_id" to myUid,
                    "contact_user_id" to otherUid,
                    "alias" to (alias ?: ""),
                    "created_at" to (System.currentTimeMillis() / 1000)
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "ContactsRepository: Error adding contact: ${e.message}", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        }
    }

    /**
     * Elimina un contacto de la lista del usuario
     */
    suspend fun removeContact(myUid: String, otherUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.from("contacts").delete {
                filter { eq("user_id", myUid) }
                filter { eq("contact_user_id", otherUid) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "ContactsRepository: Error removing contact: ${e.message}", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        }
    }

    /**
     * Lista todos los contactos del usuario
     */
    suspend fun listContacts(myUid: String): Result<List<ContactItem>> = withContext(Dispatchers.IO) {
        try {
            val response = db.from("contacts")
                .select {
                    filter { eq("user_id", myUid) }
                }
                .decodeList<ContactResponse>()

            val contacts = response.map { contact ->
                // Obtener información del usuario contactado
                val userInfo = db.from("users")
                    .select {
                        filter { eq("id", contact.contactUserId) }
                    }
                    .decodeSingleOrNull<UserInfoResponse>()

                ContactItem(
                    userId = contact.contactUserId,
                    alias = contact.alias,
                    displayName = userInfo?.displayName ?: "Usuario",
                    photoUrl = userInfo?.photoUrl,
                    isPaired = userInfo?.isPaired ?: false
                )
            }

            Result.success(contacts)
        } catch (e: Exception) {
            Log.w(TAG, "ContactsRepository: Error listing contacts: ${e.message}", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        }
    }

    /**
     * Importa contactos del dispositivo
     */
    fun importDeviceContacts(resolver: ContentResolver): List<String> {
        val phones = mutableListOf<String>()
        
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null, null, null
        )
        
        cursor?.use {
            val idx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val raw = it.getString(idx) ?: continue
                // Normalizar: eliminar espacios y caracteres especiales
                phones.add(raw.replace("\\s+".toRegex(), "").replace("[^0-9+]".toRegex(), ""))
            }
        }
        
        return phones.distinct()
    }

    /**
     * Busca usuarios por email para agregar como contacto
     */
    suspend fun searchUsersByEmail(email: String): Result<List<UserSearchResult>> = withContext(Dispatchers.IO) {
        try {
            val users = db.from("users")
                .select {
                    filter {
                        eq("email", email)
                        not { eq("id", SupabaseConfig.client.plugin(Auth).currentUserOrNull()?.id) }
                    }
                }
                .decodeList<UserSearchResponse>()

            Result.success(
                users.map { user ->
                    UserSearchResult(
                        id = user.id,
                        email = user.email,
                        displayName = user.displayName,
                        photoUrl = user.photoUrl,
                        isPaired = user.isPaired
                    )
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "ContactsRepository: Error searching users: ${e.message}", e)
            Result.failure(Exception("Error de base de datos: ${e.message}"))
        }
    }
}

/**
 * Item de contacto
 */
data class ContactItem(
    val userId: String,
    val alias: String,
    val displayName: String,
    val photoUrl: String?,
    val isPaired: Boolean
)

/**
 * Resultado de búsqueda de usuario
 */
data class UserSearchResult(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isPaired: Boolean
)

/**
 * Data class para la respuesta de contactos
 */
private data class ContactResponse(
    val user_id: String,
    val contact_user_id: String,
    val alias: String,
    val created_at: Long
)

/**
 * Data class para información de usuario
 */
private data class UserInfoResponse(
    val display_name: String,
    val photo_url: String?,
    val is_paired: Boolean
)

/**
 * Data class para búsqueda de usuarios
 */
private data class UserSearchResponse(
    val id: String,
    val email: String,
    val display_name: String,
    val photo_url: String?,
    val is_paired: Boolean
)
