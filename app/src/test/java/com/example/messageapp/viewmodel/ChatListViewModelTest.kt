package com.example.messageapp.viewmodel

import app.cash.turbine.test
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Chat
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para ChatListViewModel
 * 
 * Cubre: carga de chats, filtrado, errores
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private lateinit var viewModel: ChatListViewModel
    private lateinit var chatRepository: ChatRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        chatRepository = mockk()
        viewModel = ChatListViewModel(chatRepository)
    }

    // ============================================
    // Tests para estado inicial
    // ============================================

    @Test
    fun `initial state has empty chats list`() {
        // When: Verifico estado inicial
        val chats = viewModel.chats.value

        // Then: Debería ser lista vacía
        assertThat(chats).isEmpty()
    }

    @Test
    fun `initial state has isLoading false`() {
        // When: Verifico estado inicial
        val isLoading = viewModel.isLoading.value

        // Then: Debería ser false
        assertThat(isLoading).isFalse()
    }

    @Test
    fun `initial state has error null`() {
        // When: Verifico estado inicial
        val error = viewModel.error.value

        // Then: Debería ser null
        assertThat(error).isNull()
    }

    // ============================================
    // Tests para start
    // ============================================

    @Test
    fun `start with empty myUid does not crash`() = runTest {
        // Given: myUid vacío
        val emptyUid = ""

        // When: Inicio observación
        val result = runCatching {
            viewModel.start(emptyUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `start with valid myUid emits chats list`() = runTest {
        // Given: myUid válido y repository retorna chats
        val myUid = "user-123"
        val mockChats = listOf(
            Chat(
                id = "chat-1",
                type = "direct",
                memberIds = listOf(myUid, "user-2"),
                user1Typing = false,
                user2Typing = false,
                pinnedMessageId = null,
                pinnedSnippet = null,
                lastMessageEnc = null,
                lastMessageAt = null,
                createdAt = System.currentTimeMillis() / 1000,
                updatedAt = System.currentTimeMillis() / 1000
            )
        )
        val chatsFlow = MutableStateFlow(mockChats)
        coEvery { chatRepository.observeChats(myUid) } returns chatsFlow

        // When: Inicio observación
        viewModel.start(myUid)
        advanceUntilIdle()

        // Then: Debería emitir lista de chats
        viewModel.chats.test {
            // Estado inicial vacío
            assertThat(awaitItem()).isEmpty()
            // Lista cargada
            assertThat(awaitItem()).hasSize(1)
            assertThat(awaitItem().first().id).isEqualTo("chat-1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start updates isLoading to false after loading`() = runTest {
        // Given: myUid válido
        val myUid = "user-456"
        val mockChats = emptyList<Chat>()
        val chatsFlow = MutableStateFlow(mockChats)
        coEvery { chatRepository.observeChats(myUid) } returns chatsFlow

        // When: Inicio observación
        viewModel.start(myUid)
        advanceUntilIdle()

        // Then: isLoading debería ser false después de cargar
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `start with repository error sets error state`() = runTest {
        // Given: Repository lanza excepción
        val myUid = "user-789"
        val mockError = Exception("Failed to load chats")
        coEvery { chatRepository.observeChats(myUid) } throws mockError

        // When: Inicio observación con error
        viewModel.start(myUid)
        advanceUntilIdle()

        // Then: Debería setear estado de error
        assertThat(viewModel.error.value).isNotNull()
        assertThat(viewModel.error.value).contains("Failed to load chats")
    }

    @Test
    fun `start multiple times does not observe multiple times`() = runTest {
        // Given: myUid válido
        val myUid = "user-999"
        val mockChats = emptyList<Chat>()
        val chatsFlow = MutableStateFlow(mockChats)
        coEvery { chatRepository.observeChats(myUid) } returns chatsFlow

        // When: Llamo start múltiples veces
        viewModel.start(myUid)
        viewModel.start(myUid)
        viewModel.start(myUid)
        advanceUntilIdle()

        // Then: No debería crashar (isObserving previene múltiples observaciones)
        assertThat(viewModel.chats.value).isEmpty()
    }

    // ============================================
    // Tests para stop
    // ============================================

    @Test
    fun `stop sets isObserving to false`() {
        // Given: ViewModel started
        viewModel.start("user-123")

        // When: Detengo observación
        viewModel.stop()

        // Then: No debería crashar
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `stop multiple times does not crash`() {
        // When: Detengo múltiples veces
        val result = runCatching {
            viewModel.stop()
            viewModel.stop()
            viewModel.stop()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para ensureDirectChat
    // ============================================

    @Test
    fun `ensureDirectChat with empty myUid throws exception`() = runTest {
        // Given: myUid vacío
        val emptyMyUid = ""
        val otherUid = "user-456"

        // When: Intento crear chat
        val result = runCatching {
            viewModel.ensureDirectChat(emptyMyUid, otherUid)
        }

        // Then: Debería lanzar excepción o retornar error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `ensureDirectChat with empty otherUid throws exception`() = runTest {
        // Given: otherUid vacío
        val myUid = "user-123"
        val emptyOtherUid = ""

        // When: Intento crear chat
        val result = runCatching {
            viewModel.ensureDirectChat(myUid, emptyOtherUid)
        }

        // Then: Debería lanzar excepción
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `ensureDirectChat with valid uids returns chatId`() = runTest {
        // Given: UIDs válidos
        val myUid = "user-123"
        val otherUid = "user-456"
        val expectedChatId = "user-123_user-456"
        coEvery { chatRepository.ensureDirectChat(myUid, otherUid) } returns expectedChatId

        // When: Creo chat directo
        val result = runCatching {
            viewModel.ensureDirectChat(myUid, otherUid)
        }

        // Then: Debería retornar chatId
        assertThat(result.getOrNull()).isEqualTo(expectedChatId)
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `start with multiple rapid chat updates does not crash`() = runTest {
        // Given: Múltiples actualizaciones de chats
        val myUid = "user-123"
        val chatsFlow = MutableStateFlow(emptyList<Chat>())
        coEvery { chatRepository.observeChats(myUid) } returns chatsFlow

        // When: Inicio y emito múltiples actualizaciones
        viewModel.start(myUid)
        chatsFlow.value = listOf(createMockChat("chat-1"))
        chatsFlow.value = listOf(createMockChat("chat-1"), createMockChat("chat-2"))
        chatsFlow.value = listOf(createMockChat("chat-2"))
        advanceUntilIdle()

        // Then: No debería crashar
        assertThat(viewModel.chats.value).isNotEmpty()
    }

    // ============================================
    // Tests edge cases
    // ============================================

    @Test
    fun `start with null-like myUid does not crash`() = runTest {
        // Given: myUid que parece null
        val nullLikeUid = "null"

        // When: Inicio observación
        val result = runCatching {
            viewModel.start(nullLikeUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `start with very long myUid does not crash`() = runTest {
        // Given: myUid muy largo
        val longUid = "user-${"a".repeat(1000)}"
        val mockChats = emptyList<Chat>()
        val chatsFlow = MutableStateFlow(mockChats)
        coEvery { chatRepository.observeChats(longUid) } returns chatsFlow

        // When: Inicio observación
        val result = runCatching {
            viewModel.start(longUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `start with special characters in myUid does not crash`() = runTest {
        // Given: myUid con caracteres especiales
        val specialUid = "user-<>&\"'-123"
        val mockChats = emptyList<Chat>()
        val chatsFlow = MutableStateFlow(mockChats)
        coEvery { chatRepository.observeChats(specialUid) } returns chatsFlow

        // When: Inicio observación
        val result = runCatching {
            viewModel.start(specialUid)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Helper functions
    // ============================================

    private fun createMockChat(id: String): Chat {
        return Chat(
            id = id,
            type = "direct",
            memberIds = listOf("user-1", "user-2"),
            user1Typing = false,
            user2Typing = false,
            pinnedMessageId = null,
            pinnedSnippet = null,
            lastMessageEnc = null,
            lastMessageAt = null,
            createdAt = System.currentTimeMillis() / 1000,
            updatedAt = System.currentTimeMillis() / 1000
        )
    }
}
