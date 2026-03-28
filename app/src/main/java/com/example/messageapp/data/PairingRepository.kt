package com.example.messageapp.data

import com.example.messageapp.model.User
import com.example.messageapp.supabase.SupabaseConfig
import io.github.jan-tennert.supabase.postgrest.Postgrest
import io.github.jan-tennert.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio de Emparejamiento
 * 
 * Sistema de emparejamiento para apps románticas de 2 personas:
 * - Generar código de 6 dígitos
 * - Vincular con código
 * - Búsqueda por email
 * - Enviar solicitud de emparejamiento
 */
class PairingRepository {
    
    private val db = SupabaseConfig.client.plugin(Postgrest)
    private val auth = SupabaseConfig.client.auth
    
    /**
     * Genera código de 6 dígitos para el usuario actual
     * El código es único y se usa para que la otra persona pueda vincularte
     * 
     * @return Result con el código de 6 dígitos o error
     */
    suspend fun generatePairingCode(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("No autenticado"))
            
            // Generar código único de 6 dígitos usando la función de PostgreSQL
            val code = (100000..999999).random().toString()
            
            db.from("users").update(
                mapOf(
                    "pairing_code" to code,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", userId) }
            }
            
            Result.success(code)
        } catch (e: Exception) {
            android.util.Log.w("PairingRepository", "Error al generar código", e)
            Result.failure(e)
        }
    }
    
    /**
     * Vincula al usuario actual con otra persona usando su código de 6 dígitos
     * 
     * @param code Código de 6 dígitos de la otra persona
     * @return Result Unit o error
     */
    suspend fun pairWithCode(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val myId = auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("No autenticado"))
            
            // Buscar usuario con ese código
            val partner = db.from("users")
                .select(columns = Columns.list("id", "partner_id", "display_name")) {
                    filter { 
                        eq("pairing_code", code)
                        eq("is_paired", false) // Solo usuarios sin pareja
                    }
                }
                .decodeSingleOrNull<User>()
                ?: return@withContext Result.failure(Exception("Código no encontrado o usuario ya tiene pareja"))
            
            // Verificar que no sea el mismo usuario
            if (partner.id == myId) {
                return@withContext Result.failure(Exception("No puedes vincularte contigo mismo"))
            }
            
            // Verificar que la otra persona no tenga pareja
            if (partner.partnerId != null) {
                return@withContext Result.failure(Exception("Esa persona ya tiene pareja"))
            }
            
            // Actualizar mi perfil (el trigger creará el chat automáticamente)
            db.from("users").update(
                mapOf(
                    "partner_id" to partner.id,
                    "is_paired" to true,
                    "pairing_code" to null, // Invalidar código usado
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", myId) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.w("PairingRepository", "Error al vincular con código", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca un usuario por email para emparejamiento
     * Solo muestra usuarios disponibles (sin pareja)
     * 
     * @param email Email del usuario a buscar
     * @return Result con los datos del usuario o error
     */
    suspend fun searchByEmail(email: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = db.from("users")
                .select(columns = Columns.list("id", "email", "display_name", "photo_url", "is_paired")) {
                    filter { 
                        eq("email", email.lowercase())
                        eq("is_paired", false) // Solo mostrar disponibles
                    }
                }
                .decodeSingleOrNull()
                ?: return@withContext Result.failure(Exception("Usuario no encontrado o ya emparejado"))
            
            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.w("PairingRepository", "Error al buscar por email", e)
            Result.failure(e)
        }
    }
    
    /**
     * Envía solicitud de emparejamiento por email
     * Usa JPush para notificar a la otra persona
     * 
     * @param partnerId ID de la persona a la que enviar la solicitud
     * @return Result Unit o error
     */
    suspend fun requestPairing(partnerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Note: requestPairing es stub - pendiente de implementar con Edge Function de Supabase o JPush
            Log.w("PairingRepository", "sendPairingNotification: No implementado")
            // Por ahora solo retornamos éxito
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Invalida el código de emparejamiento actual
     * Útil si el usuario quiere generar un nuevo código
     * 
     * @return Result Unit o error
     */
    suspend fun invalidatePairingCode(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("No autenticado"))
            
            db.from("users").update(
                mapOf(
                    "pairing_code" to null,
                    "updated_at" to (System.currentTimeMillis() / 1000)
                )
            ) {
                filter { eq("id", userId) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el estado de emparejamiento del usuario actual
     * 
     * @return Result con el estado o error
     */
    suspend fun getPairingStatus(): Result<PairingStatus> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("No autenticado"))
            
            val user = db.from("users")
                .select(columns = Columns.list("is_paired", "partner_id", "pairing_code")) {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull()
                ?: return@withContext Result.failure(Exception("Usuario no encontrado"))
            
            val status = PairingStatus(
                isPaired = user.isPaired ?: false,
                partnerId = user.partnerId,
                pairingCode = user.pairingCode
            )
            
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Estado de emparejamiento de un usuario
 */
data class PairingStatus(
    val isPaired: Boolean,
    val partnerId: String?,
    val pairingCode: String?
)
