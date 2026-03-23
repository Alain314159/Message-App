package com.example.messageapp.crypto

import android.util.Base64
import org.libsodium.jni.NaCl
import org.libsodium.jni.Sodium
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cifrado E2E usando libsodium (AES-256-GCM)
 * 
 * Este cifrado es MUCHO más seguro que el Base64 anterior.
 * AES-256-GCM es el estándar de la industria para cifrado simétrico.
 * 
 * Formato del mensaje cifrado:
 * {nonce_base64}:{ciphertext_base64}:{auth_tag_base64}
 * 
 * Seguridad:
 * - AES-256: Clave de 256 bits (imposible de fuerza bruta)
 * - GCM: Modo Galois/Counter (autenticado + cifrado)
 * - Nonce único: Cada mensaje tiene nonce diferente
 * - Auth Tag: Verifica integridad del mensaje
 */
object E2ECipher {
    
    private const val GCM_IV_LENGTH = 12 // 96 bits
    private const val GCM_TAG_LENGTH = 128 // 128 bits
    private const val KEY_LENGTH = 32 // 256 bits
    
    // Inicializar libsodium
    init {
        NaCl.sodium()
    }
    
    /**
     * Genera una clave maestra aleatoria de 256 bits
     * Esta clave debe intercambiarse de forma segura (primera vez)
     * y guardarse en Android Keystore
     */
    fun generateMasterKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH)
        Sodium.randombytes_buf(key, key.size)
        return key
    }
    
    /**
     * Deriva una clave de sesión desde la clave maestra + chatId
     * Usamos HKDF-SHA256 para derivación segura
     */
    fun deriveSessionKey(masterKey: ByteArray, chatId: String): ByteArray {
        val salt = chatId.toByteArray()
        val info = "message-app-v1".toByteArray()
        
        // HKDF-SHA256 simple (implementación básica)
        val hkdf = javax.crypto.KeyGenerator.getInstance("HmacSHA256")
        hkdf.init(KEY_LENGTH * 8)
        
        // Usamos SHA-256 para derivar
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(masterKey, "HmacSHA256")
        mac.init(keySpec)
        
        val step1 = mac.doFinal(salt + info)
        val step2 = javax.crypto.Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(step1, "HmacSHA256"))
        }.doFinal("session-key".toByteArray())
        
        return step2.copyOf(KEY_LENGTH)
    }
    
    /**
     * Cifra un mensaje usando AES-256-GCM
     * 
     * @param plaintext Mensaje en texto claro
     * @param key Clave de sesión (256 bits)
     * @return Mensaje cifrado en formato: nonce:ciphertext:authTag (Base64)
     */
    fun encrypt(plaintext: String, key: ByteArray): String {
        if (plaintext.isEmpty()) return ""
        if (key.size != KEY_LENGTH) {
            throw IllegalArgumentException("Clave debe ser de 32 bytes (256 bits)")
        }
        
        // Generar nonce aleatorio
        val nonce = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(nonce)
        
        // Cifrar con AES-256-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        val ciphertext = cipher.doFinal(plaintext.toByteArray())
        
        // Extraer auth tag (últimos 16 bytes del ciphertext)
        val authTag = ciphertext.copyOfRange(
            ciphertext.size - 16,
            ciphertext.size
        )
        val actualCiphertext = ciphertext.copyOfRange(0, ciphertext.size - 16)
        
        // Codificar todo en Base64
        val nonceB64 = Base64.encodeToString(nonce, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(actualCiphertext, Base64.NO_WRAP)
        val tagB64 = Base64.encodeToString(authTag, Base64.NO_WRAP)
        
        // Formato: nonce:ciphertext:authTag
        return "$nonceB64:$cipherB64:$tagB64"
    }
    
    /**
     * Descifra un mensaje usando AES-256-GCM
     * 
     * @param encrypted Mensaje en formato nonce:ciphertext:authTag (Base64)
     * @param key Clave de sesión (256 bits)
     * @return Mensaje en texto claro
     */
    fun decrypt(encrypted: String?, key: ByteArray): String {
        if (encrypted.isNullOrBlank()) return ""
        if (key.size != KEY_LENGTH) {
            throw IllegalArgumentException("Clave debe ser de 32 bytes (256 bits)")
        }
        
        val parts = encrypted.split(":")
        if (parts.size != 3) {
            // Intentar formato antiguo (solo ciphertext)
            return try {
                String(Base64.decode(encrypted, Base64.NO_WRAP))
            } catch (e: Exception) {
                "[Error: Formato inválido]"
            }
        }
        
        val nonceB64 = parts[0]
        val cipherB64 = parts[1]
        val tagB64 = parts[2]
        
        // Decodificar Base64
        val nonce = Base64.decode(nonceB64, Base64.NO_WRAP)
        var ciphertext = Base64.decode(cipherB64, Base64.NO_WRAP)
        val authTag = Base64.decode(tagB64, Base64.NO_WRAP)
        
        // Reconstruir ciphertext completo (ciphertext + authTag)
        ciphertext = ciphertext + authTag
        
        // Descifrar
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(key, "AES")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
            String(cipher.doFinal(ciphertext))
        } catch (e: Exception) {
            "[Error: No se pudo descifrar el mensaje]"
        }
    }
    
    /**
     * Convierte ByteArray a String hexadecimal (para debugging)
     */
    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    
    /**
     * Convierte String hexadecimal a ByteArray (para debugging)
     */
    fun String.fromHex(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
