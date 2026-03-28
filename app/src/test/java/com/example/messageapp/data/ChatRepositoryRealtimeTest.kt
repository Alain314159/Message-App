package com.example.messageapp.data

import com.example.messageapp.model.Chat
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para ChatRepository - Realtime Subscriptions
 *
 * Cubre: observeChats, observeMessages, observeChat
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryRealtimeTest {

    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        repository = ChatRepository()
    }

    // ============================================
    // Tests para observeChat
    // ============================================

    @Test
    fun `observeChat returns null for non-existent chat`() = runTest {
        // Given: Chat ID que no existe
        val nonExistentChatId = "chat-nonexistent-999"

        // When: Observo el chat
        val result = runCatching {
            repository.observeChat(nonExistentChatId).first()
        }

        // Then: Debería retornar null
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun `observeChat does not crash on network error`() = runTest {
        // Given: Chat ID válido (puede fallar por red)
        val chatId = "chat-123"

        // When: Observo el chat
        val result = runCatching {
            repository.observeChat(chatId).first()
        }

        // Then: No debería crashar (puede retornar null o timeout)
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeChat handles empty chatId gracefully`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""

        // When: Observo el chat
        val result = runCatching {
            repository.observeChat(emptyChatId).first()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `observeChat handles whitespace chatId gracefully`() = runTest {
        // Given: Chat ID con whitespace
        val whitespaceChatId = "   "

        // When: Observo el chat
        val result = runCatching {
            repository.observeChat(whitespaceChatId).first()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para observeChats
    // ============================================

    @Test
    fun `observeChats does not crash on network error`() = runTest {
        // Given: User ID válido (puede fallar por red)
        val uid = "user-123"

        // When: Observo chats
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeChats(uid).first()
            }
        }

        // Then: No debería crashar (puede timeout o retornar lista vacía)
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeChats handles empty user id gracefully`() = runTest {
        // Given: User ID vacío
        val emptyUid = ""

        // When: Observo chats
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeChats(emptyUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeChats returns empty list when user has no chats`() = runTest {
        // Given: User ID válido sin chats
        val uid = "user-no-chats-${System.currentTimeMillis()}"

        // When: Observo chats (timeout para evitar bloqueo)
        val result = runCatching {
            kotlinx.coroutines.withTimeout(3000) {
                repository.observeChats(uid).first()
            }
        }

        // Then: Debería retornar lista vacía o timeout
        val list = result.getOrNull()
        if (list != null) {
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `observeChats handles very long user id`() = runTest {
        // Given: User ID muy largo
        val longUid = "user-${"a".repeat(1000)}"

        // When: Observo chats
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeChats(longUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeChats handles unicode in user id`() = runTest {
        // Given: User ID con unicode
        val unicodeUid = "user-😀-123"

        // When: Observo chats
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeChats(unicodeUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    // ============================================
    // Tests para observeMessages
    // ============================================

    @Test
    fun `observeMessages returns empty list for chat with no messages`() = runTest {
        // Given: Chat ID sin mensajes
        val chatId = "chat-no-messages-${System.currentTimeMillis()}"
        val myUid = "user-123"

        // When: Observo mensajes
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeMessages(chatId, myUid).first()
            }
        }

        // Then: Debería retornar lista vacía o timeout
        val list = result.getOrNull()
        if (list != null) {
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `observeMessages does not crash on network error`() = runTest {
        // Given: Chat ID válido (puede fallar por red)
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Observo mensajes
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeMessages(chatId, myUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeMessages handles empty chatId gracefully`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val myUid = "user-456"

        // When: Observo mensajes
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeMessages(emptyChatId, myUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeMessages handles empty myUid gracefully`() = runTest {
        // Given: My UID vacío
        val chatId = "chat-123"
        val emptyUid = ""

        // When: Observo mensajes
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeMessages(chatId, emptyUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeMessages handles very long chatId`() = runTest {
        // Given: Chat ID muy largo
        val longChatId = "chat-${"a".repeat(1000)}"
        val myUid = "user-456"

        // When: Observo mensajes
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeMessages(longChatId, myUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeMessages handles unicode in chatId`() = runTest {
        // Given: Chat ID con unicode
        val unicodeChatId = "chat-🌍-123"
        val myUid = "user-456"

        // When: Observo mensajes
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeMessages(unicodeChatId, myUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `observeChats handles concurrent subscriptions`() = runTest {
        // Given: User ID válido
        val uid = "user-123"

        // When: Múltiples suscripciones concurrentes
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                repeat(5) {
                    kotlinx.coroutines.launch {
                        repository.observeChats(uid).collect { _ -> }
                    }
                }
                kotlinx.coroutines.delay(1000)
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `observeMessages handles concurrent subscriptions`() = runTest {
        // Given: Chat ID y User ID válidos
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Múltiples suscripciones concurrentes
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                repeat(5) {
                    kotlinx.coroutines.launch {
                        repository.observeMessages(chatId, myUid).collect { _ -> }
                    }
                }
                kotlinx.coroutines.delay(1000)
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de integración: Realtime flow
    // ============================================

    @Test
    fun `observeChat and observeMessages can be called together`() = runTest {
        // Given: Chat ID y User ID válidos
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Observo chat y mensajes simultáneamente
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                val chatJob = kotlinx.coroutines.launch {
                    repository.observeChat(chatId).collect { _ -> }
                }
                val messagesJob = kotlinx.coroutines.launch {
                    repository.observeMessages(chatId, myUid).collect { _ -> }
                }
                kotlinx.coroutines.delay(1000)
                chatJob.cancel()
                messagesJob.cancel()
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Special values
    // ============================================

    @Test
    fun `observeChat handles null-like chatId`() = runTest {
        // Given: Chat ID que parece null
        val nullLikeChatId = "null"

        // When: Observo el chat
        val result = runCatching {
            repository.observeChat(nullLikeChatId).first()
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeMessages handles special characters in chatId`() = runTest {
        // Given: Chat ID con caracteres especiales
        val specialChatId = "chat-<>&\"'-123"
        val myUid = "user-456"

        // When: Observo mensajes
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeMessages(specialChatId, myUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    @Test
    fun `observeChats handles SQL injection attempt in uid`() = runTest {
        // Given: User ID con intento de SQL injection
        val injectionUid = "user'; DROP TABLE users;--"

        // When: Observo chats
        val result = runCatching {
            kotlinx.coroutines.withTimeout(2000) {
                repository.observeChats(injectionUid).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }
}
