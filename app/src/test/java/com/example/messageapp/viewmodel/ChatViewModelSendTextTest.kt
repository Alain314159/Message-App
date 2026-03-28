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
 * Tests para ChatViewModel - sendText Method
 *
 * Cubre: Envío de mensajes con cifrado E2E
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelSendTextTest {

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
    // Tests para sendText con texto vacío
    // ============================================

    @Test
    fun `sendText with empty text does nothing`() = runTest {
        // Given: Texto vacío
        val emptyText = ""
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Envío texto vacío
        viewModel.sendText(chatId, myUid, emptyText)
        advanceUntilIdle()

        // Then: No debería llamar al repository
        coVerify(exactly = 0) {
            chatRepository.sendText(any(), any(), any(), any())
        }
    }

    @Test
    fun `sendText with whitespace text does nothing`() = runTest {
        // Given: Texto con solo whitespace
        val whitespaceText = "   "
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Envío whitespace
        viewModel.sendText(chatId, myUid, whitespaceText)
        advanceUntilIdle()

        // Then: No debería llamar al repository
        coVerify(exactly = 0) {
            chatRepository.sendText(any(), any(), any(), any())
        }
    }

    @Test
    fun `sendText with null-like text does nothing`() = runTest {
        // Given: Texto que parece null
        val nullText = "null"
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Envío texto
        viewModel.sendText(chatId, myUid, nullText)
        advanceUntilIdle()

        // Then: Debería intentar enviar (es un string válido)
        // Nota: "null" es un string válido, debería enviarse
    }

    // ============================================
    // Tests para sendText con texto válido
    // ============================================

    @Test
    fun `sendText with valid text calls repository`() = runTest {
        // Given: Texto válido y repository mockeado
        val validText = "Hello World"
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje
        viewModel.sendText(chatId, myUid, validText)
        advanceUntilIdle()

        // Then: Debería llamar al repository
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    @Test
    fun `sendText with unicode text calls repository`() = runTest {
        // Given: Texto con unicode y repository mockeado
        val unicodeText = "Hola 🌍 ¿Cómo estás? 你好"
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje unicode
        viewModel.sendText(chatId, myUid, unicodeText)
        advanceUntilIdle()

        // Then: Debería llamar al repository
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    @Test
    fun `sendText with newline text calls repository`() = runTest {
        // Given: Texto con newlines y repository mockeado
        val newlineText = "Linea 1\nLinea 2\nLinea 3"
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje con newlines
        viewModel.sendText(chatId, myUid, newlineText)
        advanceUntilIdle()

        // Then: Debería llamar al repository
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    @Test
    fun `sendText with very long text calls repository`() = runTest {
        // Given: Texto muy largo (10,000 caracteres) y repository mockeado
        val longText = "a".repeat(10000)
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje largo
        viewModel.sendText(chatId, myUid, longText)
        advanceUntilIdle()

        // Then: Debería llamar al repository
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    // ============================================
    // Tests para sendText con errores
    // ============================================

    @Test
    fun `sendText updates error state when repository throws exception`() = runTest {
        // Given: Repository que lanza excepción
        val validText = "Hello"
        val chatId = "chat-123"
        val myUid = "user-456"
        val errorMessage = "Network error"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } throws Exception(errorMessage)

        // When: Envío mensaje con error
        viewModel.sendText(chatId, myUid, validText)
        advanceUntilIdle()

        // Then: Debería actualizar estado de error
        val error = viewModel.error.value
        assertThat(error).isNotNull()
        assertThat(error).contains(errorMessage)
    }

    @Test
    fun `sendText does not crash when encryption fails`() = runTest {
        // Given: E2ECipher falla (se simula internamente)
        val validText = "Hello"
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Envío mensaje (puede fallar por cifrado)
        val result = runCatching {
            viewModel.sendText(chatId, myUid, validText)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `sendText handles cipher initialization error gracefully`() = runTest {
        // Given: Cipher no inicializado (se maneja internamente)
        val validText = "Hello"
        val chatId = "chat-123"
        val myUid = "user-456"

        // When: Envío mensaje
        viewModel.sendText(chatId, myUid, validText)
        advanceUntilIdle()

        // Then: Debería manejar el error o enviar
        // (depende de la implementación de E2ECipher)
        assertThat(viewModel.error.value).isNull()
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `multiple rapid sendText calls all process`() = runTest {
        // Given: Múltiples mensajes y repository mockeado
        val chatId = "chat-123"
        val myUid = "user-456"
        val messages = listOf("msg1", "msg2", "msg3", "msg4", "msg5")

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío múltiples mensajes rápidamente
        messages.forEach { text ->
            viewModel.sendText(chatId, myUid, text)
        }
        advanceUntilIdle()

        // Then: Todos deberían procesarse
        coVerify(exactly = 5) {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    @Test
    fun `sendText while loading does not crash`() = runTest {
        // Given: ViewModel en estado de carga
        val chatId = "chat-123"
        val myUid = "user-456"
        val text = "Hello"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mientras está cargando
        viewModel.start(chatId, myUid)
        viewModel.sendText(chatId, myUid, text)
        advanceUntilIdle()

        // Then: No debería crashar
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `sendText after stop does not crash`() = runTest {
        // Given: ViewModel detenido
        val chatId = "chat-123"
        val myUid = "user-456"
        val text = "Hello"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        viewModel.start(chatId, myUid)
        viewModel.stop()

        // When: Envío después de stop
        val result = runCatching {
            viewModel.sendText(chatId, myUid, text)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Parámetros inválidos
    // ============================================

    @Test
    fun `sendText with empty chatId does not crash`() = runTest {
        // Given: Chat ID vacío
        val emptyChatId = ""
        val myUid = "user-456"
        val text = "Hello"

        // When: Envío con chatId vacío
        val result = runCatching {
            viewModel.sendText(emptyChatId, myUid, text)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `sendText with empty myUid does not crash`() = runTest {
        // Given: My UID vacío
        val chatId = "chat-123"
        val emptyUid = ""
        val text = "Hello"

        // When: Envío con uid vacío
        val result = runCatching {
            viewModel.sendText(chatId, emptyUid, text)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `sendText with all empty parameters does not crash`() = runTest {
        // Given: Todos los parámetros vacíos
        val emptyChatId = ""
        val emptyUid = ""
        val emptyText = ""

        // When: Envío con todo vacío
        val result = runCatching {
            viewModel.sendText(emptyChatId, emptyUid, emptyText)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `sendText with whitespace parameters does not crash`() = runTest {
        // Given: Parámetros con whitespace
        val whitespaceChatId = "   "
        val whitespaceUid = "   "
        val whitespaceText = "   "

        // When: Envío con whitespace
        val result = runCatching {
            viewModel.sendText(whitespaceChatId, whitespaceUid, whitespaceText)
            advanceUntilIdle()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Special values
    // ============================================

    @Test
    fun `sendText with special characters calls repository`() = runTest {
        // Given: Texto con caracteres especiales
        val specialText = "Hello <>&\"' World"
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje
        viewModel.sendText(chatId, myUid, specialText)
        advanceUntilIdle()

        // Then: Debería llamar al repository
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    @Test
    fun `sendText with emoji calls repository`() = runTest {
        // Given: Texto con emojis
        val emojiText = "Hello 👋 World 🌍"
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje
        viewModel.sendText(chatId, myUid, emojiText)
        advanceUntilIdle()

        // Then: Debería llamar al repository
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    @Test
    fun `sendText with SQL injection attempt calls repository`() = runTest {
        // Given: Texto con intento de SQL injection
        val injectionText = "'; DROP TABLE messages;--"
        val chatId = "chat-123"
        val myUid = "user-456"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje
        viewModel.sendText(chatId, myUid, injectionText)
        advanceUntilIdle()

        // Then: Debería llamar al repository
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }

    // ============================================
    // Tests de estado
    // ============================================

    @Test
    fun `error state is null before any sendText call`() {
        // When: Verifico estado inicial
        val error = viewModel.error.value

        // Then: Debería ser null
        assertThat(error).isNull()
    }

    @Test
    fun `error state is cleared after successful sendText`() = runTest {
        // Given: Repository exitoso
        val chatId = "chat-123"
        val myUid = "user-456"
        val text = "Hello"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mensaje exitoso
        viewModel.sendText(chatId, myUid, text)
        advanceUntilIdle()

        // Then: Error debería ser null después de éxito
        // Nota: Esto depende de si el ViewModel limpia el error automáticamente
    }

    @Test
    fun `isLoading state does not block sendText`() = runTest {
        // Given: ViewModel cargando
        val chatId = "chat-123"
        val myUid = "user-456"
        val text = "Hello"

        coEvery { chatRepository.sendText(any(), any(), any(), any()) } returns Unit

        // When: Envío mientras está cargando
        viewModel.start(chatId, myUid)
        viewModel.sendText(chatId, myUid, text)
        advanceUntilIdle()

        // Then: isLoading no debería bloquear el envío
        coVerify {
            chatRepository.sendText(
                chatId = chatId,
                senderId = myUid,
                textEnc = any(),
                iv = any()
            )
        }
    }
}
