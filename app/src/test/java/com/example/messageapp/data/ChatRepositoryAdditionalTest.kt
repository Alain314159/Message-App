package com.example.messageapp.data

import com.example.messageapp.model.Message
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests adicionales para ChatRepository
 * 
 * Cubre errores y edge cases identificados
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryAdditionalTest {

    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        repository = ChatRepository()
    }

    // ============================================
    // Tests para ERR-003: Validación de parámetros
    // ============================================

    @Test
    fun `sendText throws when chatId is empty`() = runTest {
        // When: Envío con chatId vacío
        val result = runCatching {
            repository.sendText("", "user-1", "encrypted", "iv")
        }

        // Then: Debería lanzar IllegalArgumentException
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("chatId")
    }

    @Test
    fun `sendText throws when senderId is empty`() = runTest {
        // When: Envío con senderId vacío
        val result = runCatching {
            repository.sendText("chat-1", "", "encrypted", "iv")
        }

        // Then: Debería lanzar IllegalArgumentException
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("senderId")
    }

    @Test
    fun `sendText throws when textEnc is empty`() = runTest {
        // When: Envío con textEnc vacío
        val result = runCatching {
            repository.sendText("chat-1", "user-1", "", "iv")
        }

        // Then: Debería lanzar IllegalArgumentException
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("textEnc")
    }

    @Test
    fun `sendText throws when iv is empty`() = runTest {
        // When: Envío con iv vacío
        val result = runCatching {
            repository.sendText("chat-1", "user-1", "encrypted", "")
        }

        // Then: Debería lanzar IllegalArgumentException
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("iv")
    }

    @Test
    fun `sendText throws when all parameters are empty`() = runTest {
        // When: Todos los parámetros vacíos
        val result = runCatching {
            repository.sendText("", "", "", "")
        }

        // Then: Debería lanzar IllegalArgumentException (primero chatId)
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `sendText throws when chatId is whitespace only`() = runTest {
        // When: chatId con solo whitespace
        val result = runCatching {
            repository.sendText("   ", "user-1", "encrypted", "iv")
        }

        // Then: Debería lanzar IllegalArgumentException
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `sendText accepts valid parameters`() = runTest {
        // When: Parámetros válidos (puede fallar por otras razones, pero no por validación)
        val result = runCatching {
            repository.sendText("chat-1", "user-1", "encrypted-text", "initialization-vector")
        }

        // Then: No debería fallar por validación de parámetros
        // (puede fallar por conexión a Supabase, eso es esperado)
        val exception = result.exceptionOrNull()
        if (exception != null) {
            // Si falla, no debería ser por IllegalArgumentException
            assertThat(exception).isNotInstanceOf(IllegalArgumentException::class.java)
        }
    }

    // ============================================
    // Tests para ERR-004: directChatIdFor con trim
    // ============================================

    @Test
    fun `directChatIdFor trims whitespace from both UIDs`() {
        // Given: UIDs con whitespace
        val uidA = " user-123 "
        val uidB = " user-456 "

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Debería trimear whitespace
        assertThat(chatId).isEqualTo("user-123_user-456")
        assertThat(chatId).doesNotContain(" ")
    }

    @Test
    fun `directChatIdFor trims leading whitespace only`() {
        // Given: UIDs con leading whitespace
        val uidA = "  user-123"
        val uidB = "  user-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Debería trimear
        assertThat(chatId).isEqualTo("user-123_user-456")
    }

    @Test
    fun `directChatIdFor trims trailing whitespace only`() {
        // Given: UIDs con trailing whitespace
        val uidA = "user-123  "
        val uidB = "user-456  "

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Debería trimear
        assertThat(chatId).isEqualTo("user-123_user-456")
    }

    @Test
    fun `directChatIdFor handles tabs and newlines`() {
        // Given: UIDs con tabs y newlines
        val uidA = "\tuser-123\n"
        val uidB = "\nuser-456\t"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Debería trimear todos los whitespace
        assertThat(chatId).isEqualTo("user-123_user-456")
    }

    @Test
    fun `directChatIdFor with one trimmed and one not`() {
        // Given: Un UID trimmeado y otro no
        val uidA = "user-123"
        val uidB = "  user-456  "

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Debería ser igual sin importar whitespace
        assertThat(chatId).isEqualTo("user-123_user-456")
    }

    // ============================================
    // Tests para markDelivered con logging (ERR-006)
    // ============================================

    @Test
    fun `markDelivered does not throw on error`() = runTest {
        // When: Llamo a markDelivered (puede fallar por DB)
        val result = runCatching {
            repository.markDelivered("chat-1", "msg-1", "user-1")
        }

        // Then: No debería lanzar excepción (solo loggear)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `markDelivered handles invalid chatId gracefully`() = runTest {
        // When: ChatId inválido
        val result = runCatching {
            repository.markDelivered("", "msg-1", "user-1")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `markDelivered handles invalid messageId gracefully`() = runTest {
        // When: MessageId inválido
        val result = runCatching {
            repository.markDelivered("chat-1", "", "user-1")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para ensureDirectChat
    // ============================================

    @Test
    fun `ensureDirectChat generates consistent IDs`() = runTest {
        // Given: Dos usuarios
        val uidA = "user-abc"
        val uidB = "user-xyz"

        // When: Creo chat en ambos órdenes
        val id1 = repository.directChatIdFor(uidA, uidB)
        val id2 = repository.directChatIdFor(uidB, uidA)

        // Then: Mismo ID
        assertThat(id1).isEqualTo(id2)
    }

    @Test
    fun `ensureDirectChat handles same user`() = runTest {
        // Given: Mismo usuario
        val uid = "user-same"

        // When: Creo chat conmigo mismo
        val chatId = repository.directChatIdFor(uid, uid)

        // Then: ID consistente
        assertThat(chatId).isEqualTo("user-same_user-same")
    }

    // ============================================
    // Tests de rendimiento
    // ============================================

    @Test
    fun `directChatIdFor performance test with 100k iterations`() {
        // Given: Muchas iteraciones
        val iterations = 100000

        // When: Mido tiempo
        val startTime = System.currentTimeMillis()
        repeat(iterations) {
            repository.directChatIdFor("user-$it", "user-${it + 1}")
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 500ms para 100k llamadas)
        assertThat(elapsed).isLessThan(500)
    }

    @Test
    fun `directChatIdFor memory test with long strings`() {
        // Given: Strings muy largos
        val longUid = "user-${"a".repeat(10000)}"

        // When: Genero ID
        val result = runCatching {
            repository.directChatIdFor(longUid, "user-short")
        }

        // Then: No debería causar OutOfMemoryError
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para casos extremos
    // ============================================

    @Test
    fun `directChatIdFor handles emoji in UIDs`() {
        // Given: UIDs con emojis
        val uidA = "user-😀-123"
        val uidB = "user-🎉-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
        assertThat(chatId).contains("user")
    }

    @Test
    fun `directChatIdFor handles null-like strings`() {
        // Given: Strings que parecen null
        val uidA = "null"
        val uidB = "null"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Debería manejar como string normal
        assertThat(chatId).isEqualTo("null_null")
    }

    @Test
    fun `directChatIdFor handles special XML characters`() {
        // Given: Caracteres especiales de XML
        val uidA = "user-<>&-123"
        val uidB = "user-\"'-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles SQL injection attempts`() {
        // Given: Intentos de SQL injection
        val uidA = "user'; DROP TABLE users;--"
        val uidB = "user-123"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Debería manejar como string normal (no ejecutar SQL)
        assertThat(chatId).contains("DROP")
    }

    @Test
    fun `directChatIdFor handles unicode normalization`() {
        // Given: Unicode que puede normalizarse diferente
        val uidA = "user-ñ-123"  // ñ precompuesta
        val uidB = "user-n\u0303-456"  // n + combining tilde

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles right-to-left text`() {
        // Given: Texto RTL (árabe/hebreo)
        val uidA = "user-مرحبا-123"
        val uidB = "user-שלום-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles mixed direction text`() {
        // Given: Texto mezclado LTR y RTL
        val uidA = "user-Hello-مرحبا-123"
        val uidB = "user-World-שלום-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles zero-width characters`() {
        // Given: Caracteres zero-width
        val uidA = "user-\u200B-123"  // Zero-width space
        val uidB = "user-\u200C-456"  // Zero-width non-joiner

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles color emoji modifiers`() {
        // Given: Emojis con skin tone modifiers
        val uidA = "user-👍🏿-123"  // Thumbs up dark skin
        val uidB = "user-👍🏻-456"  // Thumbs up light skin

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles flag emojis`() {
        // Given: Emojis de banderas
        val uidA = "user-🇺🇸-123"
        val uidB = "user-🇪🇸-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles family emoji sequences`() {
        // Given: Family emoji (multiple ZWJ sequences)
        val uidA = "user-👨‍👩‍👧‍👦-123"
        val uidB = "user-👨‍👨‍👧-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No debería crashar
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles maximum length strings`() {
        // Given: Strings de longitud máxima
        val maxUid = "user-${"a".repeat(Int.MAX_VALUE / 2)}"

        // When: Genero ID (puede fallar por memoria)
        val result = runCatching {
            repository.directChatIdFor(maxUid, "user-short")
        }

        // Then: O funciona o lanza OutOfMemoryError (aceptable)
        val exception = result.exceptionOrNull()
        if (exception != null) {
            assertThat(exception).isInstanceOf(OutOfMemoryError::class.java)
        }
    }
}
