package com.example.messageapp.data.room

import com.example.messageapp.model.Message
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for Message ↔ MessageEntity mapper roundtrip conversions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class MessageMappersTest {

    @Test
    fun `toEntity and toDomain roundtrip preserves text message fields`() {
        val msg = Message(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            type = "text",
            textEnc = "encrypted-content",
            nonce = "nonce-123",
            createdAt = 1000L,
            deliveredAt = 2000L,
            readAt = 3000L,
            deletedForAll = false,
            deletedFor = listOf("user-2")
        )

        val entity = msg.toEntity()
        val restored = entity.toDomain()

        assertThat(restored.id).isEqualTo(msg.id)
        assertThat(restored.chatId).isEqualTo(msg.chatId)
        assertThat(restored.senderId).isEqualTo(msg.senderId)
        assertThat(restored.type).isEqualTo(msg.type)
        assertThat(restored.textEnc).isEqualTo(msg.textEnc)
        assertThat(restored.nonce).isEqualTo(msg.nonce)
        assertThat(restored.createdAt).isEqualTo(msg.createdAt)
        assertThat(restored.deliveredAt).isEqualTo(msg.deliveredAt)
        assertThat(restored.readAt).isEqualTo(msg.readAt)
        assertThat(restored.deletedForAll).isEqualTo(msg.deletedForAll)
        assertThat(restored.deletedFor).containsExactlyElementsIn(msg.deletedFor)
    }

    @Test
    fun `toEntity converts null textEnc to empty string`() {
        val msg = Message(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            type = "image",
            mediaUrl = "https://example.com/image.jpg",
            textEnc = null,
            nonce = null
        )

        val entity = msg.toEntity()

        assertThat(entity.textEnc).isEqualTo("")
        assertThat(entity.nonce).isNull()
    }

    @Test
    fun `toDomain converts empty textEnc back to null`() {
        val entity = MessageEntity(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            type = "image",
            textEnc = "",
            nonce = null,
            mediaUrl = "https://example.com/image.jpg",
            createdAt = 1000L,
            deliveredAt = null,
            readAt = null,
            deletedForAll = false,
            deletedFor = emptyList(),
            synced = false
        )

        val restored = entity.toDomain()

        assertThat(restored.textEnc).isNull()
    }

    @Test
    fun `toEntity sets synced to false`() {
        val msg = Message(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            type = "text",
            textEnc = "enc",
            nonce = "nonce"
        )

        val entity = msg.toEntity()

        assertThat(entity.synced).isFalse()
    }

    @Test
    fun `roundtrip preserves deletedFor list with multiple users`() {
        val msg = Message(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            type = "text",
            textEnc = "enc",
            nonce = "nonce",
            deletedFor = listOf("user-2", "user-3", "user-4")
        )

        val entity = msg.toEntity()
        val restored = entity.toDomain()

        assertThat(restored.deletedFor).containsExactly("user-2", "user-3", "user-4")
    }
}
