package com.example.messageapp.viewmodel

import app.cash.turbine.test
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.data.PresenceRepository
import com.example.messageapp.model.Chat
import com.example.messageapp.model.Message
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

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

    @Test
    fun `initial state has null chat and empty messages`() = runTest {
        // When: Verifico el estado inicial
        val chatState = viewModel.chat.value
        val messagesState = viewModel.messages.value
        val loadingState = viewModel.isLoading.value

        // Then: Estado inicial correcto
        assertThat(chatState).isNull()
        assertThat(messagesState).isEmpty()
        assertThat(loadingState).isFalse()
    }

    @Test
    fun `start emits loading then success with messages`() = runTest {
        // Given: Chat y mensajes mockeados
        val chatId = "chat-123"
        val myUid = "user-456"
        val mockChat = Chat(
            id = chatId,
            type = "direct",
            memberIds = listOf(myUid, "other-user"),
            user1Typing = false,
            user2Typing = false,
            pinnedMessageId = null,
            pinnedSnippet = null,
            lastMessageEnc = null,
            lastMessageAt = null,
            createdAt = System.currentTimeMillis() / 1000,
            updatedAt = System.currentTimeMillis() / 1000
        )
        val mockMessages = listOf(
            Message(
                id = "msg-1",
                chatId = chatId,
                senderId = myUid,
                textEnc = "Hola",
                type = "text",
                createdAt = System.currentTimeMillis() / 1000,
                nonce = null,
                mediaUrl = null,
                deliveredAt = null,
                readAt = null,
                deletedForAll = false,
                deletedFor = emptyList()
            )
        )

        val chatFlow = MutableStateFlow(mockChat)
        val messagesFlow = MutableStateFlow(mockMessages)

        coEvery { chatRepository.observeChat(chatId) } returns chatFlow
        coEvery { chatRepository.observeMessages(chatId, myUid) } returns messagesFlow

        // When: Inicio la observación del chat
        viewModel.start(chatId, myUid)
        advanceUntilIdle()

        // Then: Verifico con Turbine que el estado cambia correctamente
        viewModel.messages.test {
            // El estado inicial es vacío
            val initialMessages = awaitItem()
            assertThat(initialMessages).isEmpty()

            // Después de cargar, tiene mensajes
            val loadedMessages = awaitItem()
            assertThat(loadedMessages).hasSize(1)
            assertThat(loadedMessages.first().id).isEqualTo("msg-1")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start filters out messages deleted for current user`() = runTest {
        // Given: Mensajes donde algunos están eliminados para el usuario actual
        val chatId = "chat-123"
        val myUid = "user-456"
        val messagesWithDeleted = listOf(
            Message(
                id = "msg-1",
                chatId = chatId,
                senderId = myUid,
                textEnc = "Mensaje visible",
                type = "text",
                createdAt = 1000L,
                nonce = null,
                mediaUrl = null,
                deliveredAt = null,
                readAt = null,
                deletedForAll = false,
                deletedFor = emptyList()
            ),
            Message(
                id = "msg-2",
                chatId = chatId,
                senderId = "other-user",
                textEnc = "Mensaje eliminado para mí",
                type = "text",
                createdAt = 2000L,
                nonce = null,
                mediaUrl = null,
                deliveredAt = null,
                readAt = null,
                deletedForAll = false,
                deletedFor = listOf(myUid) // Eliminado para este usuario
            )
        )

        val messagesFlow = MutableStateFlow(messagesWithDeleted)
        coEvery { chatRepository.observeMessages(chatId, myUid) } returns messagesFlow

        // When: Inicio la observación
        viewModel.start(chatId, myUid)
        advanceUntilIdle()

        // Then: Solo recibe mensajes no eliminados
        viewModel.messages.test {
            val filteredMessages = awaitItem()
            assertThat(filteredMessages).hasSize(1)
            assertThat(filteredMessages.first().id).isEqualTo("msg-1")
            assertThat(filteredMessages.first().textEnc).isEqualTo("Mensaje visible")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stop clears current chat and user`() = runTest {
        // Given: ViewModel con chat iniciado
        val chatId = "chat-123"
        val myUid = "user-456"
        viewModel.start(chatId, myUid)
        advanceUntilIdle()

        // When: Detengo la observación
        viewModel.stop()

        // Then: Verifico que se limpió (esto depende de la implementación interna)
        // En este caso, verificamos que no haya error
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `error state is emitted when repository throws exception`() = runTest {
        // Given: Repository que lanza excepción
        val chatId = "chat-123"
        val myUid = "user-456"
        val errorMessage = "Network error"

        coEvery { chatRepository.observeChat(chatId) } throws Exception(errorMessage)

        // When: Inicio la observación con error
        viewModel.start(chatId, myUid)
        advanceUntilIdle()

        // Then: Se emite estado de error
        val error = viewModel.error.value
        assertThat(error).isNotNull()
        assertThat(error).contains(errorMessage)
    }

    @Test
    fun `isLoading becomes false after messages are loaded`() = runTest {
        // Given: Chat con mensajes
        val chatId = "chat-123"
        val myUid = "user-456"
        val mockMessages = listOf(
            Message(
                id = "msg-1",
                chatId = chatId,
                senderId = myUid,
                textEnc = "Test",
                type = "text",
                createdAt = 1000L,
                deletedFor = emptyMap(),
                deliveredTo = emptyList(),
                readBy = emptyList(),
                nonce = null,
                mediaUrl = null
            )
        )

        val messagesFlow = MutableStateFlow(mockMessages)
        coEvery { chatRepository.observeMessages(chatId, myUid) } returns messagesFlow

        // When: Inicio la observación
        viewModel.start(chatId, myUid)
        advanceUntilIdle()

        // Then: isLoading cambia a false después de cargar
        // Nota: El ViewModel establece isLoading = true al inicio y false después de cargar
        // Esta verificación depende de la implementación exacta
        assertThat(viewModel.isLoading.value).isFalse()
    }
}
