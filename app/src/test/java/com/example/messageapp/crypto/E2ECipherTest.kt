package com.example.messageapp.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests para E2ECipher
 * 
 * Cubre: cifrado, descifrado, gestión de claves
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
class E2ECipherTest {

    @Before
    fun setup() {
        // Limpiar claves antes de cada test
        E2ECipher.deleteAllKeys()
    }

    @After
    fun teardown() {
        // Limpiar claves después de cada test
        E2ECipher.deleteAllKeys()
    }

    // ============================================
    // Tests para encrypt
    // ============================================

    @Test
    fun `encrypt with empty plaintext returns empty string`() {
        // Given: Plaintext vacío
        val plaintext = ""
        val chatId = "chat-123"

        // When: Cifro
        val result = E2ECipher.encrypt(plaintext, chatId)

        // Then: Debería retornar string vacío
        assertThat(result).isEmpty()
    }

    @Test
    fun `encrypt with valid plaintext returns iv:ciphertext format`() {
        // Given: Plaintext válido
        val plaintext = "Hello World"
        val chatId = "chat-123"

        // When: Cifro
        val result = E2ECipher.encrypt(plaintext, chatId)

        // Then: Debería retornar formato iv:ciphertext
        assertThat(result).contains(":")
        val parts = result.split(":")
        assertThat(parts).hasSize(2)
        assertThat(parts[0]).isNotEmpty() // IV
        assertThat(parts[1]).isNotEmpty() // Ciphertext
    }

    @Test
    fun `encrypt with same plaintext produces different ciphertext`() {
        // Given: Mismo plaintext dos veces
        val plaintext = "Hello World"
        val chatId = "chat-123"

        // When: Cifro dos veces
        val result1 = E2ECipher.encrypt(plaintext, chatId)
        val result2 = E2ECipher.encrypt(plaintext, chatId)

        // Then: Deberían ser diferentes (IV único por mensaje)
        assertThat(result1).isNotEqualTo(result2)
    }

    @Test
    fun `encrypt with different chatIds produces different keys`() {
        // Given: Mismo plaintext, diferentes chatIds
        val plaintext = "Hello World"
        val chatId1 = "chat-1"
        val chatId2 = "chat-2"

        // When: Cifro con diferentes chatIds
        val result1 = E2ECipher.encrypt(plaintext, chatId1)
        val result2 = E2ECipher.encrypt(plaintext, chatId2)

        // Then: Deberían ser diferentes (claves diferentes por chat)
        assertThat(result1).isNotEqualTo(result2)
    }

    @Test
    fun `encrypt with unicode plaintext works correctly`() {
        // Given: Plaintext con unicode
        val plaintext = "Hello 🌍 ¡Hola! 你好"
        val chatId = "chat-123"

        // When: Cifro
        val result = E2ECipher.encrypt(plaintext, chatId)

        // Then: Debería retornar formato válido
        assertThat(result).contains(":")
        assertThat(result).isNotEmpty()
    }

    @Test
    fun `encrypt with very long plaintext works correctly`() {
        // Given: Plaintext muy largo
        val plaintext = "a".repeat(10000)
        val chatId = "chat-123"

        // When: Cifro
        val result = runCatching {
            E2ECipher.encrypt(plaintext, chatId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
        assertThat(result.getOrNull()).contains(":")
    }

    // ============================================
    // Tests para decrypt
    // ============================================

    @Test
    fun `decrypt with null encrypted returns empty string`() {
        // Given: Encrypted null
        val encrypted: String? = null
        val chatId = "chat-123"

        // When: Descifro
        val result = E2ECipher.decrypt(encrypted, chatId)

        // Then: Debería retornar string vacío
        assertThat(result).isEmpty()
    }

    @Test
    fun `decrypt with empty encrypted returns empty string`() {
        // Given: Encrypted vacío
        val encrypted = ""
        val chatId = "chat-123"

        // When: Descifro
        val result = E2ECipher.decrypt(encrypted, chatId)

        // Then: Debería retornar string vacío
        assertThat(result).isEmpty()
    }

    @Test
    fun `decrypt with invalid format returns error message`() {
        // Given: Formato inválido (sin :)
        val encrypted = "invalid-format-without-colon"
        val chatId = "chat-123"

        // When: Descifro
        val result = E2ECipher.decrypt(encrypted, chatId)

        // Then: Debería retornar mensaje de error
        assertThat(result).contains("Error")
        assertThat(result).contains("formato")
    }

    @Test
    fun `decrypt with correct ciphertext returns plaintext`() {
        // Given: Plaintext y ciphertext válido
        val plaintext = "Secret Message"
        val chatId = "chat-123"
        val encrypted = E2ECipher.encrypt(plaintext, chatId)

        // When: Descifro
        val decrypted = E2ECipher.decrypt(encrypted, chatId)

        // Then: Debería retornar plaintext original
        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `decrypt with wrong chatId returns error`() {
        // Given: Cifrado con un chatId, descifro con otro
        val plaintext = "Secret"
        val chatId1 = "chat-1"
        val chatId2 = "chat-2"
        val encrypted = E2ECipher.encrypt(plaintext, chatId1)

        // When: Descifro con chatId incorrecto
        val result = E2ECipher.decrypt(encrypted, chatId2)

        // Then: Debería retornar error (clave incorrecta)
        assertThat(result).contains("Error")
    }

    @Test
    fun `decrypt with tampered ciphertext returns error`() {
        // Given: Ciphertext alterado
        val plaintext = "Secret"
        val chatId = "chat-123"
        val encrypted = E2ECipher.encrypt(plaintext, chatId)
        val tampered = encrypted.replaceRange(0, 5, "XXXXX")

        // When: Descifro ciphertext alterado
        val result = E2ECipher.decrypt(tampered, chatId)

        // Then: Debería retornar error (auth tag inválido)
        assertThat(result).contains("Error")
    }

    // ============================================
    // Tests para hasKeyForChat
    // ============================================

    @Test
    fun `hasKeyForChat returns false for new chat`() {
        // Given: Chat nuevo sin claves
        val chatId = "new-chat-999"

        // When: Verifico si existe clave
        val result = E2ECipher.hasKeyForChat(chatId)

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    @Test
    fun `hasKeyForChat returns true after encrypt`() {
        // Given: Chat después de cifrar
        val chatId = "chat-after-encrypt"
        val plaintext = "Test"
        E2ECipher.encrypt(plaintext, chatId)

        // When: Verifico si existe clave
        val result = E2ECipher.hasKeyForChat(chatId)

        // Then: Debería ser true
        assertThat(result).isTrue()
    }

    // ============================================
    // Tests para deleteAllKeys
    // ============================================

    @Test
    fun `deleteAllKeys removes all keys`() {
        // Given: Claves creadas
        val chatId1 = "chat-1"
        val chatId2 = "chat-2"
        E2ECipher.encrypt("Test1", chatId1)
        E2ECipher.encrypt("Test2", chatId2)

        // When: Elimino todas las claves
        E2ECipher.deleteAllKeys()

        // Then: No deberían existir claves
        assertThat(E2ECipher.hasKeyForChat(chatId1)).isFalse()
        assertThat(E2ECipher.hasKeyForChat(chatId2)).isFalse()
    }

    @Test
    fun `deleteAllKeys does not throw when no keys exist`() {
        // When: Elimino claves sin haber creado ninguna
        val result = runCatching {
            E2ECipher.deleteAllKeys()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para toHex (utility)
    // ============================================

    @Test
    fun `ByteArray toHex converts correctly`() {
        // Given: ByteArray de ejemplo
        val bytes = byteArrayOf(0x00.toByte(), 0x01.toByte(), 0x0F.toByte(), 0x10.toByte())

        // When: Convierto a hex
        val hex = bytes.toHex()

        // Then: Debería convertir correctamente
        assertThat(hex).isEqualTo("00010f10")
    }

    @Test
    fun `empty ByteArray toHex returns empty string`() {
        // Given: ByteArray vacío
        val bytes = byteArrayOf()

        // When: Convierto a hex
        val hex = bytes.toHex()

        // Then: Debería retornar string vacío
        assertThat(hex).isEmpty()
    }

    // ============================================
    // Tests de integración: encrypt -> decrypt
    // ============================================

    @Test
    fun `encrypt then decrypt returns original plaintext`() {
        // Given: Plaintext original
        val plaintext = "Original Message"
        val chatId = "chat-integration"

        // When: Cifro y descifro
        val encrypted = E2ECipher.encrypt(plaintext, chatId)
        val decrypted = E2ECipher.decrypt(encrypted, chatId)

        // Then: Debería retornar plaintext original
        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt decrypt with multiple messages works`() {
        // Given: Múltiples mensajes
        val chatId = "chat-multi"
        val messages = listOf("Message 1", "Message 2", "Message 3")

        // When: Cifro y descifro cada uno
        val results = messages.map { msg ->
            val encrypted = E2ECipher.encrypt(msg, chatId)
            E2ECipher.decrypt(encrypted, chatId)
        }

        // Then: Deberían coincidir todos
        assertThat(results).isEqualTo(messages)
    }

    // ============================================
    // Tests edge cases: Null safety
    // ============================================

    @Test
    fun `encrypt handles null-like chatId`() {
        // Given: ChatId que parece null
        val plaintext = "Test"
        val chatId = "null"

        // When: Cifro
        val result = runCatching {
            E2ECipher.encrypt(plaintext, chatId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `decrypt handles null-like chatId`() {
        // Given: ChatId que parece null
        val encrypted = "abc:def"
        val chatId = "null"

        // When: Descifro
        val result = runCatching {
            E2ECipher.decrypt(encrypted, chatId)
        }

        // Then: Debería retornar error (no crash)
        assertThat(result.getOrNull()).contains("Error")
    }

    // ============================================
    // Tests edge cases: Special characters
    // ============================================

    @Test
    fun `encrypt decrypt with special characters in chatId works`() {
        // Given: ChatId con caracteres especiales
        val plaintext = "Test"
        val chatId = "chat-<>&\"'-123"

        // When: Cifro y descifro
        val encrypted = E2ECipher.encrypt(plaintext, chatId)
        val decrypted = E2ECipher.decrypt(encrypted, chatId)

        // Then: Debería retornar plaintext original
        assertThat(decrypted).isEqualTo(plaintext)
    }

    // ============================================
    // Tests de rendimiento
    // ============================================

    @Test
    fun `encrypt 100 messages performance test`() {
        // Given: 100 mensajes
        val chatId = "chat-perf"
        val messages = List(100) { "Message $it" }

        // When: Cifro todos
        val startTime = System.currentTimeMillis()
        val encrypted = messages.map { E2ECipher.encrypt(it, chatId) }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 5 segundos)
        assertThat(elapsed).isLessThan(5000)
        assertThat(encrypted).hasSize(100)
    }

    @Test
    fun `decrypt 100 messages performance test`() {
        // Given: 100 mensajes cifrados
        val chatId = "chat-perf-decrypt"
        val plaintexts = List(100) { "Message $it" }
        val encrypted = plaintexts.map { E2ECipher.encrypt(it, chatId) }

        // When: Descifro todos
        val startTime = System.currentTimeMillis()
        val decrypted = encrypted.map { E2ECipher.decrypt(it, chatId) }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 5 segundos)
        assertThat(elapsed).isLessThan(5000)
        assertThat(decrypted).isEqualTo(plaintexts)
    }
}
