package com.example.messageapp.ui.chat

import com.example.messageapp.model.Message
import com.example.messageapp.utils.Crypto
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for chat helper pure logic functions.
 * These are pure function copies of the logic inside remember blocks.
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class ChatHelpersTest {

    // Pure copy of the search filtering logic from ChatHelpers.kt
    private fun searchMatches(msgs: List<Message>, queryText: String): List<String> {
        if (queryText.isBlank()) return emptyList()
        return msgs.filter { msg ->
            msg.type == "text" && msg.textEnc != null &&
                runCatching { Crypto.decrypt(msg.textEnc!!) }.getOrElse { "" }
                    .contains(queryText, ignoreCase = true)
        }.map { it.id }
    }

    @Test
    fun `searchMatches returns empty for blank query`() {
        val msgs = listOf(
            Message(id = "msg-1", chatId = "chat-1", senderId = "user-1",
                type = "text", textEnc = "SGVsbG8=", nonce = "n")
        )

        assertThat(searchMatches(msgs, "")).isEmpty()
        assertThat(searchMatches(msgs, "   ")).isEmpty()
    }

    @Test
    fun `searchMatches returns empty when no text messages`() {
        val msgs = listOf(
            Message(id = "msg-1", chatId = "chat-1", senderId = "user-1",
                type = "image", mediaUrl = "http://img.jpg")
        )

        assertThat(searchMatches(msgs, "hello")).isEmpty()
    }

    @Test
    fun `searchMatches finds messages containing query case insensitive`() {
        val encoded = Crypto.encrypt("Hello World")
        val msgs = listOf(
            Message(id = "msg-1", chatId = "chat-1", senderId = "user-1",
                type = "text", textEnc = encoded, nonce = "n")
        )

        val results = searchMatches(msgs, "hello")

        assertThat(results).contains("msg-1")
    }

    @Test
    fun `searchMatches does not find messages without match`() {
        val encoded = Crypto.encrypt("Hello World")
        val msgs = listOf(
            Message(id = "msg-1", chatId = "chat-1", senderId = "user-1",
                type = "text", textEnc = encoded, nonce = "n")
        )

        val results = searchMatches(msgs, "notfound")

        assertThat(results).isEmpty()
    }

    @Test
    fun `searchMatches handles invalid base64 gracefully`() {
        val msgs = listOf(
            Message(id = "msg-1", chatId = "chat-1", senderId = "user-1",
                type = "text", textEnc = "not-valid-base64!!", nonce = "n")
        )

        // Should not throw, returns empty list
        val results = searchMatches(msgs, "hello")
        assertThat(results).isEmpty()
    }

    @Test
    fun `searchMatches returns multiple matches across messages`() {
        val hello = Crypto.encrypt("hello")
        val hi = Crypto.encrypt("hi there")
        val msgs = listOf(
            Message(id = "msg-1", chatId = "chat-1", senderId = "user-1",
                type = "text", textEnc = hello, nonce = "n"),
            Message(id = "msg-2", chatId = "chat-1", senderId = "user-1",
                type = "text", textEnc = hi, nonce = "n"),
            Message(id = "msg-3", chatId = "chat-1", senderId = "user-1",
                type = "text", textEnc = Crypto.encrypt("goodbye"), nonce = "n")
        )

        val results = searchMatches(msgs, "he")

        assertThat(results).containsAtLeast("msg-1", "msg-2")
        assertThat(results).doesNotContain("msg-3")
    }
}
