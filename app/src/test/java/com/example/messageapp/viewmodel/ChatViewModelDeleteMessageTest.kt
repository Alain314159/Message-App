package com.example.messageapp.viewmodel

import app.cash.turbine.test
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.data.PresenceRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para ChatViewModel - Delete Message Methods
 *
 * Cubre: deleteMessageForUser, deleteMessageForAll
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelDeleteMessageTest {

    private lateinit var viewModel: ChatViewModel
    private lateinit var chatRepository: ChatRepository
    private lateinit var presenceRepository: PresenceRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        chatRepository = mockk()
        presenceRepository = mockk()
        viewModel = ChatViewModel(chatRepository, presenceRepository)
    }

    // ============================================
    // Tests para deleteMessageForUser
    // ============================================

    @Test
    fun `deleteMessageForUser calls repository with correct parameters`() = runTest {
        // Given: Chat ID, message ID y UID válidos
        val chatId = "chat-123"
        val messageId = "msg-456"
        val uid = "user-789"

        coEvery { chatRepository.deleteMessageForUser(any(), any(), any()) } returns Unit

        // When: Elimino mensaje para usuario
        viewModel.deleteMessageForUser(chatId, messageId, uid)
        advanceUntilIdle()

        // Then: Debería llamar al repository con parámetros correctos
        coVerify {
            chatRepository.deleteMessageForUser(chatId, messageId, uid)
        }
    }

    @Test
    fun `deleteMessageForUser updates error state on failure`() = runTest {
        // Given: Repository que lanza excepción
        val chatId = "chat-123"
        val messageId = "msg-456"
        val uid = "user-789"
        val errorMessage = "Delete failed"

        coEvery { chatRepository.deleteMessageForUser(any(), any(), any()) } throws Exception(errorMessage)

        // When: Elimino mensaje con error
        viewModel.deleteMessageForUser(chatId, messageId, uid)
        advanceUntilIdle()

        // Then: Debería actualizar estado de error
        val error = viewModel.error.value
        assertThat(error).isNotNull()
        assertThat(error).contains(errorMessage)
    }

    @Test
    fun `deleteMessageForUser with empty chatId does not crash`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val messageId = "msg-456"
        val uid = "user-789"

        // When: Elimino mensaje con chatId vacío
        val result = runCatching {
            viewModel.deleteMessageForUser(emptyChatId, messageId, uid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser with empty messageId does not crash`() = runTest {
        // Given: Message ID vacío
        val chatId = "chat-123"
        val emptyMessageId = ""
        val uid = "user-789"

        // When: Elimino mensaje con messageId vacío
        val result = runCatching {
            viewModel.deleteMessageForUser(chatId, emptyMessageId, uid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser with empty uid does not crash`() = runTest {
        // Given: UID vacío
        val chatId = "chat-123"
        val messageId = "msg-456"
        val emptyUid = ""

        // When: Elimino mensaje con uid vacío
        val result = runCatching {
            viewModel.deleteMessageForUser(chatId, messageId, emptyUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser with all empty parameters does not crash`() = runTest {
        // Given: Todos los parámetros vacíos
        val emptyChatId = ""
        val emptyMessageId = ""
        val emptyUid = ""

        // When: Elimino mensaje con todo vacío
        val result = runCatching {
            viewModel.deleteMessageForUser(emptyChatId, emptyMessageId, emptyUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para deleteMessageForAll
    // ============================================

    @Test
    fun `deleteMessageForAll calls repository with correct parameters`() = runTest {
        // Given: Chat ID y message ID válidos
        val chatId = "chat-123"
        val messageId = "msg-456"

        coEvery { chatRepository.deleteMessageForAll(any(), any()) } returns Unit

        // When: Elimino mensaje para todos
        viewModel.deleteMessageForAll(chatId, messageId)
        advanceUntilIdle()

        // Then: Debería llamar al repository con parámetros correctos
        coVerify {
            chatRepository.deleteMessageForAll(chatId, messageId)
        }
    }

    @Test
    fun `deleteMessageForAll updates error state on failure`() = runTest {
        // Given: Repository que lanza excepción
        val chatId = "chat-123"
        val messageId = "msg-456"
        val errorMessage = "Delete for all failed"

        coEvery { chatRepository.deleteMessageForAll(any(), any()) } throws Exception(errorMessage)

        // When: Elimino mensaje para todos con error
        viewModel.deleteMessageForAll(chatId, messageId)
        advanceUntilIdle()

        // Then: Debería actualizar estado de error
        val error = viewModel.error.value
        assertThat(error).isNotNull()
        assertThat(error).contains(errorMessage)
    }

    @Test
    fun `deleteMessageForAll with empty chatId does not crash`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val messageId = "msg-456"

        // When: Elimino mensaje para todos con chatId vacío
        val result = runCatching {
            viewModel.deleteMessageForAll(emptyChatId, messageId)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll with empty messageId does not crash`() = runTest {
        // Given: Message ID vacío
        val chatId = "chat-123"
        val emptyMessageId = ""

        // When: Elimino mensaje para todos con messageId vacío
        val result = runCatching {
            viewModel.deleteMessageForAll(chatId, emptyMessageId)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll with all empty parameters does not crash`() = runTest {
        // Given: Todos los parámetros vacíos
        val emptyChatId = ""
        val emptyMessageId = ""

        // When: Elimino mensaje para todos con todo vacío
        val result = runCatching {
            viewModel.deleteMessageForAll(emptyChatId, emptyMessageId)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `multiple rapid deleteMessageForUser calls all process`() = runTest {
        // Given: Múltiples mensajes
        val chatId = "chat-123"
        val uid = "user-789"
        val messageIds = listOf("msg-1", "msg-2", "msg-3", "msg-4", "msg-5")

        coEvery { chatRepository.deleteMessageForUser(any(), any(), any()) } returns Unit

        // When: Elimino múltiples mensajes rápidamente
        messageIds.forEach { messageId ->
            viewModel.deleteMessageForUser(chatId, messageId, uid)
        }
        advanceUntilIdle()

        // Then: Todos deberían procesarse
        coVerify(exactly = 5) {
            chatRepository.deleteMessageForUser(any(), any(), any())
        }
    }

    @Test
    fun `multiple rapid deleteMessageForAll calls all process`() = runTest {
        // Given: Múltiples mensajes
        val chatId = "chat-123"
        val messageIds = listOf("msg-1", "msg-2", "msg-3")

        coEvery { chatRepository.deleteMessageForAll(any(), any()) } returns Unit

        // When: Elimino múltiples mensajes para todos
        messageIds.forEach { messageId ->
            viewModel.deleteMessageForAll(chatId, messageId)
        }
        advanceUntilIdle()

        // Then: Todos deberían procesarse
        coVerify(exactly = 3) {
            chatRepository.deleteMessageForAll(any(), any())
        }
    }

    @Test
    fun `mixed delete operations do not crash`() = runTest {
        // Given: Chat y mensajes
        val chatId = "chat-123"
        val messageId = "msg-456"
        val uid = "user-789"

        coEvery { chatRepository.deleteMessageForUser(any(), any(), any()) } returns Unit
        coEvery { chatRepository.deleteMessageForAll(any(), any()) } returns Unit

        // When: Ejecuto operaciones mixtas
        val result = runCatching {
            viewModel.deleteMessageForUser(chatId, messageId, uid)
            viewModel.deleteMessageForAll(chatId, messageId)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Special values
    // ============================================

    @Test
    fun `deleteMessageForUser with whitespace parameters does not crash`() = runTest {
        // Given: Parámetros con whitespace
        val whitespaceChatId = "   "
        val whitespaceMessageId = "   "
        val whitespaceUid = "   "

        // When: Elimino mensaje con whitespace
        val result = runCatching {
            viewModel.deleteMessageForUser(whitespaceChatId, whitespaceMessageId, whitespaceUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll with whitespace parameters does not crash`() = runTest {
        // Given: Parámetros con whitespace
        val whitespaceChatId = "   "
        val whitespaceMessageId = "   "

        // When: Elimino mensaje para todos con whitespace
        val result = runCatching {
            viewModel.deleteMessageForAll(whitespaceChatId, whitespaceMessageId)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForUser with special characters does not crash`() = runTest {
        // Given: Parámetros con caracteres especiales
        val chatId = "chat-<>&-123"
        val messageId = "msg-\"'-456"
        val uid = "user-ñ-789"

        coEvery { chatRepository.deleteMessageForUser(any(), any(), any()) } returns Unit

        // When: Elimino mensaje
        val result = runCatching {
            viewModel.deleteMessageForUser(chatId, messageId, uid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `deleteMessageForAll with unicode does not crash`() = runTest {
        // Given: Parámetros con unicode
        val chatId = "chat-🌍-123"
        val messageId = "msg-🎉-456"

        coEvery { chatRepository.deleteMessageForAll(any(), any()) } returns Unit

        // When: Elimino mensaje para todos
        val result = runCatching {
            viewModel.deleteMessageForAll(chatId, messageId)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de estado
    // ============================================

    @Test
    fun `error state is null before any delete operation`() {
        // When: Verifico estado inicial
        val error = viewModel.error.value

        // Then: Debería ser null
        assertThat(error).isNull()
    }

    @Test
    fun `error state is cleared after successful deleteMessageForUser`() = runTest {
        // Given: Repository exitoso
        val chatId = "chat-123"
        val messageId = "msg-456"
        val uid = "user-789"

        coEvery { chatRepository.deleteMessageForUser(any(), any(), any()) } returns Unit

        // When: Elimino mensaje exitosamente
        viewModel.deleteMessageForUser(chatId, messageId, uid)
        advanceUntilIdle()

        // Then: Error debería ser null después de éxito
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `error state is cleared after successful deleteMessageForAll`() = runTest {
        // Given: Repository exitoso
        val chatId = "chat-123"
        val messageId = "msg-456"

        coEvery { chatRepository.deleteMessageForAll(any(), any()) } returns Unit

        // When: Elimino mensaje para todos exitosamente
        viewModel.deleteMessageForAll(chatId, messageId)
        advanceUntilIdle()

        // Then: Error debería ser null después de éxito
        assertThat(viewModel.error.value).isNull()
    }
}
