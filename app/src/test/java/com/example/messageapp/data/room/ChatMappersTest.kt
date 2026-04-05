package com.example.messageapp.data.room

import com.example.messageapp.model.Chat
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for Chat ↔ ChatEntity mapper roundtrip conversions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class ChatMappersTest {

    @Test
    fun `toEntity and toDomain roundtrip preserves all fields`() {
        val chat = Chat(
            id = "chat-123",
            type = "direct",
            memberIds = listOf("user-a", "user-b"),
            user1Typing = true,
            user2Typing = false,
            pinnedMessageId = "msg-456",
            pinnedSnippet = "See you later",
            lastMessageEnc = "encrypted:last:msg",
            lastMessageAt = 9876543210L,
            createdAt = 1000000000L,
            updatedAt = 9876543210L
        )

        val entity = chat.toEntity()
        val restored = entity.toDomain()

        assertThat(restored.id).isEqualTo(chat.id)
        assertThat(restored.type).isEqualTo(chat.type)
        assertThat(restored.memberIds).containsExactlyElementsIn(chat.memberIds)
        assertThat(restored.user1Typing).isEqualTo(chat.user1Typing)
        assertThat(restored.user2Typing).isEqualTo(chat.user2Typing)
        assertThat(restored.pinnedMessageId).isEqualTo(chat.pinnedMessageId)
        assertThat(restored.pinnedSnippet).isEqualTo(chat.pinnedSnippet)
        assertThat(restored.lastMessageEnc).isEqualTo(chat.lastMessageEnc)
        assertThat(restored.lastMessageAt).isEqualTo(chat.lastMessageAt)
        assertThat(restored.createdAt).isEqualTo(chat.createdAt)
        assertThat(restored.updatedAt).isEqualTo(chat.updatedAt)
    }

    @Test
    fun `toEntity sets synced to false`() {
        val chat = Chat(id = "chat-1", memberIds = listOf("user-1"))
        val entity = chat.toEntity()

        assertThat(entity.synced).isFalse()
    }

    @Test
    fun `roundtrip preserves null pinnedMessageId`() {
        val chat = Chat(
            id = "chat-1",
            memberIds = listOf("user-1"),
            pinnedMessageId = null,
            pinnedSnippet = null
        )

        val entity = chat.toEntity()
        val restored = entity.toDomain()

        assertThat(restored.pinnedMessageId).isNull()
        assertThat(restored.pinnedSnippet).isNull()
    }

    @Test
    fun `roundtrip preserves memberIds order`() {
        val chat = Chat(
            id = "chat-1",
            memberIds = listOf("user-z", "user-a", "user-m")
        )

        val entity = chat.toEntity()
        val restored = entity.toDomain()

        assertThat(restored.memberIds).containsExactly("user-z", "user-a", "user-m").inOrder()
    }
}
