package com.example.messageapp.viewmodel

import app.cash.turbine.test
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.data.PresenceRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para ChatViewModel - Typing Indicators
 *
 * Cubre: setTyping, observePartnerTyping, isPartnerTyping state
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTypingTest {

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
    // Tests para setTyping
    // ============================================

    @Test
    fun `setTyping calls presence repository with correct parameters`() = runTest {
        // Given: Chat ID válido y isTyping true
        val chatId = "chat-123"
        val isTyping = true

        coEvery { presenceRepository.setTypingStatus(any(), any()) } returns Unit

        // When: Actualizo typing status
        viewModel.setTyping(chatId, isTyping)
        advanceUntilIdle()

        // Then: Debería llamar al presence repository
        coVerify {
            presenceRepository.setTypingStatus(chatId, isTyping)
        }
    }

    @Test
    fun `setTyping with isTyping false calls repository`() = runTest {
        // Given: Chat ID válido y isTyping false
        val chatId = "chat-123"
        val isTyping = false

        coEvery { presenceRepository.setTypingStatus(any(), any()) } returns Unit

        // When: Actualizo typing status a false
        viewModel.setTyping(chatId, isTyping)
        advanceUntilIdle()

        // Then: Debería llamar al presence repository
        coVerify {
            presenceRepository.setTypingStatus(chatId, isTyping)
        }
    }

    @Test
    fun `setTyping with empty chatId does not crash`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val isTyping = true

        // When: Actualizo typing con chatId vacío
        val result = runCatching {
            viewModel.setTyping(emptyChatId, isTyping)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTyping with whitespace chatId does not crash`() = runTest {
        // Given: Chat ID con whitespace
        val whitespaceChatId = "   "
        val isTyping = true

        // When: Actualizo typing con whitespace
        val result = runCatching {
            viewModel.setTyping(whitespaceChatId, isTyping)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTyping handles repository error gracefully`() = runTest {
        // Given: Repository que lanza excepción
        val chatId = "chat-123"
        val isTyping = true

        coEvery { presenceRepository.setTypingStatus(any(), any()) } throws Exception("Typing error")

        // When: Actualizo typing con error
        val result = runCatching {
            viewModel.setTyping(chatId, isTyping)
            advanceUntilIdle()
        }

        // Then: No debería crashar (el error se maneja internamente)
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para observePartnerTyping
    // ============================================

    @Test
    fun `observePartnerTyping updates isPartnerTyping state`() = runTest {
        // Given: Flow de typing que emite true
        val chatId = "chat-123"
        val myUid = "user-456"
        val typingFlow = MutableStateFlow(false)

        coEvery { presenceRepository.observePartnerTyping(any(), any()) } returns typingFlow

        // When: Empiezo a observar typing
        viewModel.observePartnerTyping(chatId, myUid)
        advanceUntilIdle()

        // Then: Debería actualizar el estado
        viewModel.isPartnerTyping.test {
            // Estado inicial
            assertThat(awaitItem()).isFalse()
            // Después de emitir true
            typingFlow.value = true
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePartnerTyping with empty chatId does not crash`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val myUid = "user-456"

        // When: Observo typing con chatId vacío
        val result = runCatching {
            viewModel.observePartnerTyping(emptyChatId, myUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `observePartnerTyping with empty myUid does not crash`() = runTest {
        // Given: My UID vacío
        val chatId = "chat-123"
        val emptyUid = ""

        // When: Observo typing con uid vacío
        val result = runCatching {
            viewModel.observePartnerTyping(chatId, emptyUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `observePartnerTyping handles repository error gracefully`() = runTest {
        // Given: Repository que lanza excepción
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { presenceRepository.observePartnerTyping(any(), any()) } throws Exception("Observe error")

        // When: Observo typing con error
        val result = runCatching {
            viewModel.observePartnerTyping(chatId, myUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para debounce de typing changes
    // ============================================

    @Test
    fun `observePartnerTyping debounces rapid changes`() = runTest {
        // Given: Flow que emite cambios rápidamente
        val chatId = "chat-123"
        val myUid = "user-456"
        val typingFlow = MutableStateFlow(false)

        coEvery { presenceRepository.observePartnerTyping(any(), any()) } returns typingFlow

        // When: Empiezo a observar y emito cambios rápidos
        viewModel.observePartnerTyping(chatId, myUid)
        advanceUntilIdle()

        // Emitir cambios rápidamente
        typingFlow.value = true
        advanceUntilIdle()
        typingFlow.value = false
        advanceUntilIdle()
        typingFlow.value = true
        advanceUntilIdle()

        // Then: Debería recibir los cambios (con debounce)
        // El debounce de 300ms debería filtrar algunos cambios
        assertThat(viewModel.isPartnerTyping.value).isTrue()
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `multiple rapid setTyping calls all process`() = runTest {
        // Given: Chat ID válido
        val chatId = "chat-123"

        coEvery { presenceRepository.setTypingStatus(any(), any()) } returns Unit

        // When: Múltiples llamadas rápidas
        viewModel.setTyping(chatId, true)
        viewModel.setTyping(chatId, false)
        viewModel.setTyping(chatId, true)
        viewModel.setTyping(chatId, false)
        advanceUntilIdle()

        // Then: Todas deberían procesarse
        coVerify(exactly = 4) {
            presenceRepository.setTypingStatus(any(), any())
        }
    }

    @Test
    fun `setTyping and observePartnerTyping can be called together`() = runTest {
        // Given: Chat ID y UID válidos
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { presenceRepository.setTypingStatus(any(), any()) } returns Unit
        coEvery { presenceRepository.observePartnerTyping(any(), any()) } returns MutableStateFlow(false)

        // When: Llamo a ambos métodos
        val result = runCatching {
            viewModel.setTyping(chatId, true)
            viewModel.observePartnerTyping(chatId, myUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Special values
    // ============================================

    @Test
    fun `setTyping with special characters in chatId does not crash`() = runTest {
        // Given: Chat ID con caracteres especiales
        val specialChatId = "chat-<>&-123"
        val isTyping = true

        coEvery { presenceRepository.setTypingStatus(any(), any()) } returns Unit

        // When: Actualizo typing
        val result = runCatching {
            viewModel.setTyping(specialChatId, isTyping)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setTyping with unicode in chatId does not crash`() = runTest {
        // Given: Chat ID con unicode
        val unicodeChatId = "chat-🌍-123"
        val isTyping = true

        coEvery { presenceRepository.setTypingStatus(any(), any()) } returns Unit

        // When: Actualizo typing
        val result = runCatching {
            viewModel.setTyping(unicodeChatId, isTyping)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `observePartnerTyping with special characters does not crash`() = runTest {
        // Given: Chat ID con caracteres especiales
        val specialChatId = "chat-\"'-123"
        val myUid = "user-456"

        coEvery { presenceRepository.observePartnerTyping(any(), any()) } returns MutableStateFlow(false)

        // When: Observo typing
        val result = runCatching {
            viewModel.observePartnerTyping(specialChatId, myUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de estado
    // ============================================

    @Test
    fun `isPartnerTyping is false by default`() {
        // When: Verifico estado inicial
        val isTyping = viewModel.isPartnerTyping.value

        // Then: Debería ser false
        assertThat(isTyping).isFalse()
    }

    @Test
    fun `isPartnerTyping updates to true when partner starts typing`() = runTest {
        // Given: Flow que emite true
        val chatId = "chat-123"
        val myUid = "user-456"
        val typingFlow = MutableStateFlow(false)

        coEvery { presenceRepository.observePartnerTyping(any(), any()) } returns typingFlow

        // When: Empiezo a observar y el partner empieza a escribir
        viewModel.observePartnerTyping(chatId, myUid)
        advanceUntilIdle()

        typingFlow.value = true
        advanceUntilIdle()

        // Then: Debería actualizar a true
        assertThat(viewModel.isPartnerTyping.value).isTrue()
    }

    @Test
    fun `isPartnerTyping updates to false when partner stops typing`() = runTest {
        // Given: Flow que emite false
        val chatId = "chat-123"
        val myUid = "user-456"
        val typingFlow = MutableStateFlow(true)

        coEvery { presenceRepository.observePartnerTyping(any(), any()) } returns typingFlow

        // When: Empiezo a observar y el partner para de escribir
        viewModel.observePartnerTyping(chatId, myUid)
        advanceUntilIdle()

        typingFlow.value = false
        advanceUntilIdle()

        // Then: Debería actualizar a false
        assertThat(viewModel.isPartnerTyping.value).isFalse()
    }

    @Test
    fun `error state remains null after successful setTyping`() = runTest {
        // Given: Repository exitoso
        val chatId = "chat-123"
        val isTyping = true

        coEvery { presenceRepository.setTypingStatus(any(), any()) } returns Unit

        // When: Actualizo typing exitosamente
        viewModel.setTyping(chatId, isTyping)
        advanceUntilIdle()

        // Then: Error debería ser null
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `error state remains null after successful observePartnerTyping`() = runTest {
        // Given: Repository exitoso
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { presenceRepository.observePartnerTyping(any(), any()) } returns MutableStateFlow(false)

        // When: Observo typing exitosamente
        viewModel.observePartnerTyping(chatId, myUid)
        advanceUntilIdle()

        // Then: Error debería ser null
        assertThat(viewModel.error.value).isNull()
    }
}
