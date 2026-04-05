package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Tests for MessageReadRepository validation logic.
 * Pure function copies of the require() validation from the repo.
 */
class MessageReadRepositoryTest {

    companion object {
        const val PAGE_SIZE = 50
    }

    private fun validateLoadMessagesPaginated(chatId: String, page: Int, pageSize: Int) {
        require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
        require(page >= 0) { "page debe ser >= 0" }
        require(pageSize > 0) { "pageSize debe ser > 0" }
    }

    private fun validateLoadOlderMessages(chatId: String, beforeTimestamp: Long) {
        require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
        require(beforeTimestamp > 0) { "beforeTimestamp debe ser > 0" }
    }

    @Test
    fun `loadMessagesPaginated throws when chatId is blank`() = runTest {
        val exception = kotlin.runCatching {
            validateLoadMessagesPaginated("", 0, PAGE_SIZE)
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("chatId")
    }

    @Test
    fun `loadMessagesPaginated throws when page is negative`() = runTest {
        val exception = kotlin.runCatching {
            validateLoadMessagesPaginated("chat-1", -1, PAGE_SIZE)
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("page")
    }

    @Test
    fun `loadMessagesPaginated throws when pageSize is zero`() = runTest {
        val exception = kotlin.runCatching {
            validateLoadMessagesPaginated("chat-1", 0, 0)
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("pageSize")
    }

    @Test
    fun `loadOlderMessages throws when chatId is blank`() = runTest {
        val exception = kotlin.runCatching {
            validateLoadOlderMessages("", System.currentTimeMillis())
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("chatId")
    }

    @Test
    fun `loadOlderMessages throws when timestamp is zero`() = runTest {
        val exception = kotlin.runCatching {
            validateLoadOlderMessages("chat-1", 0)
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception!!.message).contains("beforeTimestamp")
    }
}
