package com.example.messageapp.data.room

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for Room entity data classes default values and copy behavior.
 */
class RoomEntitiesTest {

    // === MessageEntity Tests ===

    @Test
    fun `MessageEntity has correct defaults`() {
        val entity = MessageEntity(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            textEnc = "encrypted",
            createdAt = 1000L
        )

        assertThat(entity.type).isEqualTo("text")
        assertThat(entity.nonce).isNull()
        assertThat(entity.mediaUrl).isNull()
        assertThat(entity.deliveredAt).isNull()
        assertThat(entity.readAt).isNull()
        assertThat(entity.deletedForAll).isFalse()
        assertThat(entity.deletedFor).isEmpty()
        assertThat(entity.synced).isFalse()
    }

    @Test
    fun `MessageEntity copy creates modified version`() {
        val entity = MessageEntity(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            textEnc = "enc",
            createdAt = 1000L
        )

        val modified = entity.copy(synced = true, readAt = 12345L)

        assertThat(modified.synced).isTrue()
        assertThat(modified.readAt).isEqualTo(12345L)
        assertThat(modified.id).isEqualTo("msg-1") // unchanged
    }

    // === UserEntity Tests ===

    @Test
    fun `UserEntity has correct defaults`() {
        val entity = UserEntity(
            id = "user-1",
            email = "",
            displayName = "Test",
            createdAt = 0L,
            updatedAt = 0L
        )

        assertThat(entity.photoUrl).isNull()
        assertThat(entity.bio).isEqualTo("")
        assertThat(entity.pairingCode).isNull()
        assertThat(entity.partnerId).isNull()
        assertThat(entity.isPaired).isFalse()
        assertThat(entity.isOnline).isFalse()
        assertThat(entity.lastSeen).isNull()
        assertThat(entity.isTyping).isFalse()
        assertThat(entity.typingInChat).isNull()
        assertThat(entity.fcmToken).isNull()
        assertThat(entity.synced).isFalse()
    }

    @Test
    fun `UserEntity copy preserves all fields`() {
        val entity = UserEntity(
            id = "user-1",
            email = "test@example.com",
            displayName = "Test",
            isPaired = true,
            partnerId = "partner-1",
            synced = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val modified = entity.copy(isOnline = true)

        assertThat(modified.isOnline).isTrue()
        assertThat(modified.isPaired).isTrue()
        assertThat(modified.partnerId).isEqualTo("partner-1")
    }

    // === ChatEntity Tests ===

    @Test
    fun `ChatEntity has correct defaults`() {
        val entity = ChatEntity(
            id = "chat-1",
            memberIds = listOf("user-1"),
            createdAt = 0L,
            updatedAt = 0L
        )

        assertThat(entity.type).isEqualTo("couple")
        assertThat(entity.user1Typing).isFalse()
        assertThat(entity.user2Typing).isFalse()
        assertThat(entity.pinnedMessageId).isNull()
        assertThat(entity.pinnedSnippet).isNull()
        assertThat(entity.lastMessageEnc).isNull()
        assertThat(entity.lastMessageAt).isNull()
        assertThat(entity.synced).isFalse()
    }

    @Test
    fun `ChatEntity copy preserves memberIds`() {
        val entity = ChatEntity(
            id = "chat-1",
            memberIds = listOf("user-a", "user-b"),
            type = "direct",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val modified = entity.copy(updatedAt = 12345L)

        assertThat(modified.memberIds).containsExactly("user-a", "user-b")
        assertThat(modified.type).isEqualTo("direct")
    }
}
