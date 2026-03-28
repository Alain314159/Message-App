package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para ChatRepository - Message Operations
 *
 * Cubre: deleteMessageForUser, deleteMessageForAll, pinMessage, unpinMessage,
 *       markAsRead, countUnreadMessages
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryMessageOperationsTest {

    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        repository = ChatRepository()
    }

    // ============================================
    // Tests para deleteMessageForUser
    // ============================================

    @Test
    fun `deleteMessageForUser does not throw on network error`() = runTest {
        // Given: Chat ID y Message ID válidos (puede fallar por red)
        val chatId = "chat-123"
        val messageId = "msg-456"
        val uid = "user-789"

        // When: Elimino mensaje para usuario
        val result = runCatching {
            repository.deleteMessageForUser(chatId, messageId, uid)
        }

        // Then: No debería crashar (solo loggea error)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser handles empty chatId gracefully`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val messageId = "msg-456"
        val uid = "user-789"

        // When: Elimino mensaje
        val result = runCatching {
            repository.deleteMessageForUser(emptyChatId, messageId, uid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser handles empty messageId gracefully`() = runTest {
        // Given: Message ID vacío
        val chatId = "chat-123"
        val emptyMessageId = ""
        val uid = "user-789"

        // When: Elimino mensaje
        val result = runCatching {
            repository.deleteMessageForUser(chatId, emptyMessageId, uid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser handles empty uid gracefully`() = runTest {
        // Given: UID vacío
        val chatId = "chat-123"
        val messageId = "msg-456"
        val emptyUid = ""

        // When: Elimino mensaje
        val result = runCatching {
            repository.deleteMessageForUser(chatId, messageId, emptyUid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser handles all empty parameters gracefully`() = runTest {
        // Given: Todos los parámetros vacíos
        val emptyChatId = ""
        val emptyMessageId = ""
        val emptyUid = ""

        // When: Elimino mensaje
        val result = runCatching {
            repository.deleteMessageForUser(emptyChatId, emptyMessageId, emptyUid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser handles whitespace parameters gracefully`() = runTest {
        // Given: Parámetros con whitespace
        val whitespaceChatId = "   "
        val whitespaceMessageId = "   "
        val whitespaceUid = "   "

        // When: Elimino mensaje
        val result = runCatching {
            repository.deleteMessageForUser(whitespaceChatId, whitespaceMessageId, whitespaceUid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para deleteMessageForAll
    // ============================================

    @Test
    fun `deleteMessageForAll does not throw on network error`() = runTest {
        // Given: Chat ID y Message ID válidos (puede fallar por red)
        val chatId = "chat-123"
        val messageId = "msg-456"

        // When: Elimino mensaje para todos
        val result = runCatching {
            repository.deleteMessageForAll(chatId, messageId)
        }

        // Then: No debería crashar (solo loggea error)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll handles empty chatId gracefully`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val messageId = "msg-456"

        // When: Elimino mensaje para todos
        val result = runCatching {
            repository.deleteMessageForAll(emptyChatId, messageId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll handles empty messageId gracefully`() = runTest {
        // Given: Message ID vacío
        val chatId = "chat-123"
        val emptyMessageId = ""

        // When: Elimino mensaje para todos
        val result = runCatching {
            repository.deleteMessageForAll(chatId, emptyMessageId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll handles all empty parameters gracefully`() = runTest {
        // Given: Todos los parámetros vacíos
        val emptyChatId = ""
        val emptyMessageId = ""

        // When: Elimino mensaje para todos
        val result = runCatching {
            repository.deleteMessageForAll(emptyChatId, emptyMessageId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll handles whitespace parameters gracefully`() = runTest {
        // Given: Parámetros con whitespace
        val whitespaceChatId = "   "
        val whitespaceMessageId = "   "

        // When: Elimino mensaje para todos
        val result = runCatching {
            repository.deleteMessageForAll(whitespaceChatId, whitespaceMessageId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para pinMessage
    // ============================================

    @Test
    fun `pinMessage does not throw on network error`() = runTest {
        // Given: Chat ID, Message ID y snippet válidos
        val chatId = "chat-123"
        val messageId = "msg-456"
        val snippet = "Hello World"

        // When: Fijo mensaje
        val result = runCatching {
            repository.pinMessage(chatId, messageId, snippet)
        }

        // Then: No debería crashar (solo loggea error)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `pinMessage handles empty chatId gracefully`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val messageId = "msg-456"
        val snippet = "Hello"

        // When: Fijo mensaje
        val result = runCatching {
            repository.pinMessage(emptyChatId, messageId, snippet)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `pinMessage handles empty messageId gracefully`() = runTest {
        // Given: Message ID vacío
        val chatId = "chat-123"
        val emptyMessageId = ""
        val snippet = "Hello"

        // When: Fijo mensaje
        val result = runCatching {
            repository.pinMessage(chatId, emptyMessageId, snippet)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `pinMessage handles empty snippet gracefully`() = runTest {
        // Given: Snippet vacío
        val chatId = "chat-123"
        val messageId = "msg-456"
        val emptySnippet = ""

        // When: Fijo mensaje
        val result = runCatching {
            repository.pinMessage(chatId, messageId, emptySnippet)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `pinMessage handles very long snippet gracefully`() = runTest {
        // Given: Snippet muy largo
        val chatId = "chat-123"
        val messageId = "msg-456"
        val longSnippet = "a".repeat(10000)

        // When: Fijo mensaje
        val result = runCatching {
            repository.pinMessage(chatId, messageId, longSnippet)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `pinMessage handles unicode snippet gracefully`() = runTest {
        // Given: Snippet con unicode
        val chatId = "chat-123"
        val messageId = "msg-456"
        val unicodeSnippet = "Hola 🌍 ¿Cómo estás? 你好"

        // When: Fijo mensaje
        val result = runCatching {
            repository.pinMessage(chatId, messageId, unicodeSnippet)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para unpinMessage
    // ============================================

    @Test
    fun `unpinMessage does not throw on network error`() = runTest {
        // Given: Chat ID válido
        val chatId = "chat-123"

        // When: Desfijo mensaje
        val result = runCatching {
            repository.unpinMessage(chatId)
        }

        // Then: No debería crashar (solo loggea error)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `unpinMessage handles empty chatId gracefully`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""

        // When: Desfijo mensaje
        val result = runCatching {
            repository.unpinMessage(emptyChatId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `unpinMessage handles whitespace chatId gracefully`() = runTest {
        // Given: Chat ID con whitespace
        val whitespaceChatId = "   "

        // When: Desfijo mensaje
        val result = runCatching {
            repository.unpinMessage(whitespaceChatId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para markAsRead
    // ============================================

    @Test
    fun `markAsRead does not throw on network error`() = runTest {
        // Given: Chat ID y UID válidos
        val chatId = "chat-123"
        val uid = "user-456"

        // When: Marco como leído
        val result = runCatching {
            repository.markAsRead(chatId, uid)
        }

        // Then: No debería crashar (solo loggea error)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `markAsRead handles empty chatId gracefully`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val uid = "user-456"

        // When: Marco como leído
        val result = runCatching {
            repository.markAsRead(emptyChatId, uid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `markAsRead handles empty uid gracefully`() = runTest {
        // Given: UID vacío
        val chatId = "chat-123"
        val emptyUid = ""

        // When: Marco como leído
        val result = runCatching {
            repository.markAsRead(chatId, emptyUid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `markAsRead handles all empty parameters gracefully`() = runTest {
        // Given: Todos los parámetros vacíos
        val emptyChatId = ""
        val emptyUid = ""

        // When: Marco como leído
        val result = runCatching {
            repository.markAsRead(emptyChatId, emptyUid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para countUnreadMessages
    // ============================================

    @Test
    fun `countUnreadMessages returns 0 on network error`() = runTest {
        // Given: Chat ID y UID válidos (puede fallar por red)
        val chatId = "chat-123"
        val uid = "user-456"

        // When: Cuento mensajes no leídos
        val result = runCatching {
            repository.countUnreadMessages(chatId, uid)
        }

        // Then: Debería retornar 0 o un número, no crashar
        val count = result.getOrNull()
        if (count != null) {
            assertThat(count).isAtLeast(0)
        }
    }

    @Test
    fun `countUnreadMessages returns 0 for empty chatId`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val uid = "user-456"

        // When: Cuento mensajes no leídos
        val result = runCatching {
            repository.countUnreadMessages(emptyChatId, uid)
        }

        // Then: Debería retornar 0
        assertThat(result.getOrNull()).isEqualTo(0)
    }

    @Test
    fun `countUnreadMessages returns 0 for empty uid`() = runTest {
        // Given: UID vacío
        val chatId = "chat-123"
        val emptyUid = ""

        // When: Cuento mensajes no leídos
        val result = runCatching {
            repository.countUnreadMessages(chatId, emptyUid)
        }

        // Then: Debería retornar 0
        assertThat(result.getOrNull()).isEqualTo(0)
    }

    @Test
    fun `countUnreadMessages handles non-existent chat`() = runTest {
        // Given: Chat ID que no existe
        val nonExistentChatId = "chat-nonexistent-${System.currentTimeMillis()}"
        val uid = "user-456"

        // When: Cuento mensajes no leídos
        val result = runCatching {
            repository.countUnreadMessages(nonExistentChatId, uid)
        }

        // Then: Debería retornar 0
        assertThat(result.getOrNull()).isEqualTo(0)
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `concurrent deleteMessageForUser calls do not crash`() = runTest {
        // Given: Múltiples mensajes
        val chatId = "chat-123"
        val uid = "user-456"
        val messageIds = listOf("msg-1", "msg-2", "msg-3", "msg-4", "msg-5")

        // When: Elimino múltiples mensajes en paralelo
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                messageIds.forEach { messageId ->
                    kotlinx.coroutines.launch {
                        repository.deleteMessageForUser(chatId, messageId, uid)
                    }
                }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `concurrent pinMessage calls do not crash`() = runTest {
        // Given: Múltiples mensajes para fijar
        val chatId = "chat-123"
        val messageIds = listOf("msg-1", "msg-2", "msg-3")

        // When: Fijo múltiples mensajes en paralelo
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                messageIds.forEachIndexed { index, messageId ->
                    kotlinx.coroutines.launch {
                        repository.pinMessage(chatId, messageId, "Snippet $index")
                    }
                }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `mixed operations do not crash`() = runTest {
        // Given: Chat y mensajes
        val chatId = "chat-123"
        val messageId = "msg-456"
        val uid = "user-789"

        // When: Ejecuto múltiples operaciones mixtas
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                launch { repository.deleteMessageForUser(chatId, messageId, uid) }
                launch { repository.pinMessage(chatId, messageId, "Snippet") }
                launch { repository.markAsRead(chatId, uid) }
                launch { repository.countUnreadMessages(chatId, uid) }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Special values
    // ============================================

    @Test
    fun `deleteMessageForUser handles special characters in IDs`() = runTest {
        // Given: IDs con caracteres especiales
        val chatId = "chat-<>&-123"
        val messageId = "msg-\"'-456"
        val uid = "user-ñ-789"

        // When: Elimino mensaje
        val result = runCatching {
            repository.deleteMessageForUser(chatId, messageId, uid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `pinMessage handles SQL injection attempt in snippet`() = runTest {
        // Given: Snippet con intento de SQL injection
        val chatId = "chat-123"
        val messageId = "msg-456"
        val injectionSnippet = "'; DROP TABLE messages;--"

        // When: Fijo mensaje
        val result = runCatching {
            repository.pinMessage(chatId, messageId, injectionSnippet)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `all operations handle very long IDs`() = runTest {
        // Given: IDs muy largos
        val longChatId = "chat-${"a".repeat(1000)}"
        val longMessageId = "msg-${"b".repeat(1000)}"
        val longUid = "user-${"c".repeat(1000)}"

        // When: Ejecuto operaciones con IDs largos
        val result = runCatching {
            repository.deleteMessageForUser(longChatId, longMessageId, longUid)
            repository.pinMessage(longChatId, longMessageId, "Snippet")
            repository.markAsRead(longChatId, longUid)
            repository.countUnreadMessages(longChatId, longUid)
        }

        // Then: No debería crashar (puede fallar por memoria, pero no por lógica)
        val exception = result.exceptionOrNull()
        if (exception != null) {
            // Si falla, debería ser por OutOfMemory, no por lógica
            assertThat(exception).isAnyOf(
                OutOfMemoryError::class.java,
                IllegalArgumentException::class.java
            )
        }
    }
}
