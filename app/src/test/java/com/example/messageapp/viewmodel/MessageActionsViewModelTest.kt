package com.example.messageapp.viewmodel

import com.example.messageapp.crypto.MessageDecryptor
import com.example.messageapp.data.ChatRepository
import com.example.messageapp.model.Message
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for MessageActionsViewModel.
 * Uses mocked ChatRepository and MessageDecryptor.
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class MessageActionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<ChatRepository>(relaxed = true)
    private val mockDecryptor = mockk<MessageDecryptor>(relaxed = true)
    private lateinit var viewModel: MessageActionsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MessageActionsViewModel(mockRepo, mockDecryptor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no error`() {
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `pinMessage for text message decrypts and pins with snippet`() = runTest {
        val msg = Message(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            type = "text",
            textEnc = "encrypted",
            nonce = "nonce"
        )
        every { mockDecryptor.decrypt(msg, "chat-1") } returns "Hello this is a test message that is quite long"

        viewModel.pinMessage("chat-1", msg)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.pinMessage("chat-1", "msg-1", "Hello this is a test message that is quite long") }
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `pinMessage for image message uses type as snippet`() = runTest {
        val msg = Message(
            id = "msg-2",
            chatId = "chat-1",
            senderId = "user-1",
            type = "image",
            mediaUrl = "http://img.jpg"
        )

        viewModel.pinMessage("chat-1", msg)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.pinMessage("chat-1", "msg-2", "[image]") }
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `unpinMessage calls repo without error`() = runTest {
        viewModel.unpinMessage("chat-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.unpinMessage("chat-1") }
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `deleteMessageForUser calls repo without error`() = runTest {
        viewModel.deleteMessageForUser("chat-1", "msg-1", "user-2")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.deleteMessageForUser("chat-1", "msg-1", "user-2") }
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `deleteMessageForAll calls repo without error`() = runTest {
        viewModel.deleteMessageForAll("chat-1", "msg-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.deleteMessageForAll("chat-1", "msg-1") }
        assertThat(viewModel.error.value).isNull()
    }
}
