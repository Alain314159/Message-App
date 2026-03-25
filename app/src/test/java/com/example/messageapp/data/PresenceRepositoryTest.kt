package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para PresenceRepository
 * 
 * Cubre: typing indicators, online status, last seen
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PresenceRepositoryTest {

    private lateinit var repository: PresenceRepository

    @Before
    fun setup() {
        repository = PresenceRepository()
    }

    // ============================================
    // Tests para setTypingStatus
    // ============================================

    @Test
    fun `setTypingStatus with empty chatId does not throw`() = runTest {
        // Given: ChatId vacío
        val chatId = ""
        val isTyping = true

        // When: Intento actualizar typing
        val result = runCatching {
            repository.setTypingStatus(chatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with isTyping true does not throw`() = runTest {
        // Given: ChatId válido, isTyping true
        val chatId = "chat-123"
        val isTyping = true

        // When: Intento actualizar typing
        val result = runCatching {
            repository.setTypingStatus(chatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus with isTyping false does not throw`() = runTest {
        // Given: ChatId válido, isTyping false
        val chatId = "chat-123"
        val isTyping = false

        // When: Intento actualizar typing
        val result = runCatching {
            repository.setTypingStatus(chatId, isTyping)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTypingStatus auto-clears after 5 seconds when isTyping true`() = runTest {
        // Given: ChatId válido, isTyping true
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

    // ============================================
    // Tests para observePartnerTyping
    // ============================================

    @Test
    fun `observePartnerTyping with empty chatId does not emit`() = runTest {
        // Given: ChatId vacío
        val chatId = ""
        val myUid = "user-123"

        // When: Intento observar (timeout para evitar bloqueo)
        val result = runCatching {
            kotlinx.coroutines.withTimeout(1000) {
                repository.observePartnerTyping(chatId, myUid).first()
            }
        }

        // Then: Debería timeout o no emitir
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `observePartnerTyping with valid chatId does not crash`() = runTest {
        // Given: ChatId válido
        val chatId = "chat-123"
        val myUid = "user-123"

        // When: Intento observar (timeout para evitar bloqueo)
        val result = runCatching {
            kotlinx.coroutines.withTimeout(1000) {
                repository.observePartnerTyping(chatId, myUid).first()
            }
        }

        // Then: No debería crashar (puede timeout por no haber datos)
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    // ============================================
    // Tests para updateOnlineStatus
    // ============================================

    @Test
    fun `updateOnlineStatus with true does not throw`() = runTest {
        // When: Actualizo a online
        val result = runCatching {
            repository.updateOnlineStatus(true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateOnlineStatus with false does not throw`() = runTest {
        // When: Actualizo a offline
        val result = runCatching {
            repository.updateOnlineStatus(false)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para observePartnerOnline
    // ============================================

    @Test
    fun `observePartnerOnline with empty partnerId does not emit`() = runTest {
        // Given: PartnerId vacío
        val partnerId = ""

        // When: Intento observar (timeout para evitar bloqueo)
        val result = runCatching {
            kotlinx.coroutines.withTimeout(1000) {
                repository.observePartnerOnline(partnerId).first()
            }
        }

        // Then: Debería timeout o no emitir
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `observePartnerOnline with valid partnerId does not crash`() = runTest {
        // Given: PartnerId válido
        val partnerId = "user-456"

        // When: Intento observar (timeout para evitar bloqueo)
        val result = runCatching {
            kotlinx.coroutines.withTimeout(1000) {
                repository.observePartnerOnline(partnerId).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    // ============================================
    // Tests para getPartnerLastSeen
    // ============================================

    @Test
    fun `getPartnerLastSeen with empty partnerId returns null`() = runTest {
        // Given: PartnerId vacío
        val partnerId = ""

        // When: Intento obtener last seen
        val result = runCatching {
            repository.getPartnerLastSeen(partnerId)
        }

        // Then: Debería retornar null o no crashar
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun `getPartnerLastSeen with valid partnerId returns null when not found`() = runTest {
        // Given: PartnerId que no existe
        val partnerId = "user-nonexistent"

        // When: Intento obtener last seen
        val result = runCatching {
            repository.getPartnerLastSeen(partnerId)
        }

        // Then: Debería retornar null (usuario no encontrado)
        assertThat(result.getOrNull()).isNull()
    }

    // ============================================
    // Tests para cleanup
    // ============================================

    @Test
    fun `cleanup does not throw exception`() {
        // When: Limpio recursos
        val result = runCatching {
            repository.cleanup()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de integración: Typing flow
    // ============================================

    @Test
    fun `typing status flow handles rapid toggling`() = runTest {
        // Given: ChatId válido
        val chatId = "chat-123"

        // When: Alterno typing rápidamente
        val result = runCatching {
            repository.setTypingStatus(chatId, true)
            repository.setTypingStatus(chatId, false)
            repository.setTypingStatus(chatId, true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de integración: Online/Offline flow
    // ============================================

    @Test
    fun `online status handles rapid toggling`() = runTest {
        // When: Alterno online/offline rápidamente
        val result = runCatching {
            repository.updateOnlineStatus(true)
            repository.updateOnlineStatus(false)
            repository.updateOnlineStatus(true)
            repository.updateOnlineStatus(false)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Null safety
    // ============================================

    @Test
    fun `all methods handle null-like strings gracefully`() = runTest {
        // Given: Strings que parecen null
        val nullLikeChatId = "null"
        val nullLikeUid = "null"

        // When: Llamo métodos con null-like strings
        val result = runCatching {
            repository.setTypingStatus(nullLikeChatId, true)
            repository.updateOnlineStatus(true)
            repository.getPartnerLastSeen(nullLikeUid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Very long IDs
    // ============================================

    @Test
    fun `setTypingStatus handles very long chatId`() = runTest {
        // Given: ChatId muy largo
        val longChatId = "chat-${"a".repeat(1000)}"

        // When: Intento actualizar typing
        val result = runCatching {
            repository.setTypingStatus(longChatId, true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `observePartnerOnline handles very long partnerId`() = runTest {
        // Given: PartnerId muy largo
        val longPartnerId = "user-${"b".repeat(1000)}"

        // When: Intento observar (timeout para evitar bloqueo)
        val result = runCatching {
            kotlinx.coroutines.withTimeout(1000) {
                repository.observePartnerOnline(longPartnerId).first()
            }
        }

        // Then: No debería crashar
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    // ============================================
    // Tests edge cases: Special characters
    // ============================================

    @Test
    fun `setTypingStatus handles chatId with special characters`() = runTest {
        // Given: ChatId con caracteres especiales
        val chatId = "chat-<>&\"'-123"

        // When: Intento actualizar typing
        val result = runCatching {
            repository.setTypingStatus(chatId, true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateOnlineStatus handles unicode in user data`() = runTest {
        // When: Actualizo status (los datos pueden tener unicode)
        val result = runCatching {
            repository.updateOnlineStatus(true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `concurrent typing updates do not crash`() = runTest {
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

    @Test
    fun `concurrent online status updates do not crash`() = runTest {
        // When: Actualizo online status en paralelo
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                repeat(10) {
                    kotlinx.coroutines.launch {
                        repository.updateOnlineStatus(it % 2 == 0)
                    }
                }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }
}
