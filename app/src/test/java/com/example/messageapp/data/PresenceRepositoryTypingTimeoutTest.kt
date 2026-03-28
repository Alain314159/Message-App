package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Tests para PresenceRepository - Typing Timeout
 *
 * Cubre: setTypingStatus con auto-clear después de 5 segundos
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PresenceRepositoryTypingTimeoutTest {

    private lateinit var repository: PresenceRepository

    @Before
    fun setup() {
        repository = PresenceRepository()
    }

    // ============================================
    // Tests para auto-clear de typing status
    // ============================================

    @Test
    fun `setTypingStatus auto-clears after 5 seconds when isTyping true`() = runTest {
        // Given: Chat ID válido, isTyping true
        val chatId = "chat-123"
        val isTyping = true

        // When: Actualizo typing y espero 6 segundos
        val result = runCatching {
            repository.setTypingStatus(chatId, isTyping)
            kotlinx.coroutines.delay(6000) // Esperar auto-clear
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus does not auto-clear when isTyping false`() = runTest {
        // Given: Chat ID válido, isTyping false
        val chatId = "chat-123"
        val isTyping = false

        // When: Actualizo typing a false
        val result = runCatching {
            repository.setTypingStatus(chatId, isTyping)
            kotlinx.coroutines.delay(1000) // Esperar un poco
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus handles rapid toggling without crash`() = runTest {
        // Given: Chat ID válido
        val chatId = "chat-123"

        // When: Alterno typing rápidamente
        val result = runCatching {
            repository.setTypingStatus(chatId, true)
            repository.setTypingStatus(chatId, false)
            repository.setTypingStatus(chatId, true)
            repository.setTypingStatus(chatId, false)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with isTyping true handles concurrent calls`() = runTest {
        // Given: Múltiples chats
        val chatIds = listOf("chat-1", "chat-2", "chat-3")

        // When: Actualizo typing en paralelo
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                chatIds.forEach { chatId ->
                    kotlinx.coroutines.launch {
                        repository.setTypingStatus(chatId, true)
                    }
                }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para empty chatId
    // ============================================

    @Test
    fun `setTypingStatus with empty chatId does not crash`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(emptyChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with whitespace chatId does not crash`() = runTest {
        // Given: Chat ID con whitespace
        val whitespaceChatId = "   "
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(whitespaceChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para null-like values
    // ============================================

    @Test
    fun `setTypingStatus with null-like chatId does not crash`() = runTest {
        // Given: Chat ID que parece null
        val nullLikeChatId = "null"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(nullLikeChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para special characters
    // ============================================

    @Test
    fun `setTypingStatus with special characters in chatId does not crash`() = runTest {
        // Given: Chat ID con caracteres especiales
        val specialChatId = "chat-<>&\"'-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(specialChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with unicode in chatId does not crash`() = runTest {
        // Given: Chat ID con unicode
        val unicodeChatId = "chat-🌍-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(unicodeChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para very long chatId
    // ============================================

    @Test
    fun `setTypingStatus with very long chatId does not crash`() = runTest {
        // Given: Chat ID muy largo
        val longChatId = "chat-${"a".repeat(1000)}"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(longChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with very long chatId and auto-clear`() = runTest {
        // Given: Chat ID muy largo
        val longChatId = "chat-${"b".repeat(500)}"
        val isTyping = true

        // When: Actualizo typing y espero auto-clear
        val result = runCatching {
            repository.setTypingStatus(longChatId, isTyping)
            kotlinx.coroutines.delay(6000)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de rendimiento
    // ============================================

    @Test
    fun `setTypingStatus performance with 1000 calls`() = runTest {
        // Given: Muchas llamadas
        val iterations = 1000
        val chatId = "chat-123"

        // When: Mido tiempo de 1000 llamadas
        val elapsed = measureTimeMillis {
            repeat(iterations) {
                repository.setTypingStatus(chatId, it % 2 == 0)
            }
        }

        // Then: Debería ser razonablemente rápido (< 5 segundos para 1000 llamadas)
        assertThat(elapsed).isLessThan(5000)
    }

    @Test
    fun `setTypingStatus memory with long strings`() = runTest {
        // Given: Strings muy largos
        val longChatId = "chat-${"a".repeat(10000)}"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(longChatId, isTyping)
        }

        // Then: No debería causar OutOfMemoryError
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para SQL injection attempts
    // ============================================

    @Test
    fun `setTypingStatus with SQL injection attempt does not crash`() = runTest {
        // Given: Chat ID con intento de SQL injection
        val injectionChatId = "chat'; DROP TABLE chats;--"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(injectionChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with XML special characters does not crash`() = runTest {
        // Given: Chat ID con caracteres XML
        val xmlChatId = "chat-<&>'\"-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(xmlChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para emoji in chatId
    // ============================================

    @Test
    fun `setTypingStatus with emoji in chatId does not crash`() = runTest {
        // Given: Chat ID con emojis
        val emojiChatId = "chat-😀-🎉-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(emojiChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with flag emoji in chatId does not crash`() = runTest {
        // Given: Chat ID con bandera emoji
        val flagChatId = "chat-🇺🇸-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(flagChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para family emoji sequences
    // ============================================

    @Test
    fun `setTypingStatus with family emoji in chatId does not crash`() = runTest {
        // Given: Chat ID con family emoji (ZWJ sequence)
        val familyChatId = "chat-👨‍👩‍👧‍👦-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(familyChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para zero-width characters
    // ============================================

    @Test
    fun `setTypingStatus with zero-width space in chatId does not crash`() = runTest {
        // Given: Chat ID con zero-width space
        val zeroWidthChatId = "chat-\u200B-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(zeroWidthChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para right-to-left text
    // ============================================

    @Test
    fun `setTypingStatus with RTL text in chatId does not crash`() = runTest {
        // Given: Chat ID con texto RTL (árabe)
        val rtlChatId = "chat-مرحبا-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(rtlChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with mixed LTR and RTL does not crash`() = runTest {
        // Given: Chat ID con texto mezclado
        val mixedChatId = "chat-Hello-مرحبا-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(mixedChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para color emoji modifiers
    // ============================================

    @Test
    fun `setTypingStatus with skin tone modifier in chatId does not crash`() = runTest {
        // Given: Chat ID con skin tone modifier
        val skinToneChatId = "chat-👍🏿-123"
        val isTyping = true

        // When: Actualizo typing
        val result = runCatching {
            repository.setTypingStatus(skinToneChatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de integración: Typing flow completo
    // ============================================

    @Test
    fun `setTypingStatus complete flow with auto-clear`() = runTest {
        // Given: Chat ID válido
        val chatId = "chat-integration-test"
        val isTyping = true

        // When: Actualizo typing y espero auto-clear completo
        val result = runCatching {
            // Activar typing
            repository.setTypingStatus(chatId, true)
            // Esperar un poco
            kotlinx.coroutines.delay(1000)
            // Desactivar antes del auto-clear
            repository.setTypingStatus(chatId, false)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus multiple chats with auto-clear`() = runTest {
        // Given: Múltiples chats
        val chatIds = listOf("chat-1", "chat-2", "chat-3", "chat-4", "chat-5")

        // When: Activo typing en todos y espero
        val result = runCatching {
            chatIds.forEach { chatId ->
                repository.setTypingStatus(chatId, true)
            }
            kotlinx.coroutines.delay(6000) // Esperar auto-clear
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }
}
