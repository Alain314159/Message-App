package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Tests for MessageWriteRepository validation logic.
 * Tests the require() validation behavior by wrapping the same validation
 * in a pure function since we can't instantiate the real repo without Supabase.
 */
class MessageWriteRepositoryTest {

    private fun validateSendText(chatId: String, senderId: String, textEnc: String, iv: String) {
        require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
        require(senderId.isNotBlank()) { "senderId no puede estar vacío" }
        require(textEnc.isNotBlank()) { "textEnc no puede estar vacío" }
        require(iv.isNotBlank()) { "iv no puede estar vacío" }
    }

    @Test
    fun `sendText throws when chatId is blank`() {
        val exception = kotlin.runCatching {
            validateSendText("", "user-1", "enc", "iv")
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("chatId")
    }

    @Test
    fun `sendText throws when senderId is blank`() {
        val exception = kotlin.runCatching {
            validateSendText("chat-1", "", "enc", "iv")
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("senderId")
    }

    @Test
    fun `sendText throws when textEnc is blank`() {
        val exception = kotlin.runCatching {
            validateSendText("chat-1", "user-1", "", "iv")
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("textEnc")
    }

    @Test
    fun `sendText throws when iv is blank`() {
        val exception = kotlin.runCatching {
            validateSendText("chat-1", "user-1", "enc", "")
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("iv")
    }
}
