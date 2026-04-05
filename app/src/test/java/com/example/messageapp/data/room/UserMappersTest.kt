package com.example.messageapp.data.room

import com.example.messageapp.model.User
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for User ↔ UserEntity mapper roundtrip conversions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class UserMappersTest {

    @Test
    fun `toEntity and toDomain roundtrip preserves all fields`() {
        val user = User(
            id = "user-123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg",
            bio = "Hello world",
            pairingCode = "ABC123",
            partnerId = "partner-456",
            isPaired = true,
            isOnline = true,
            lastSeen = 1234567890L,
            isTyping = false,
            typingInChat = null,
            fcmToken = "fcm-token-789",
            createdAt = 1000000000L,
            updatedAt = 1234567890L
        )

        val entity = user.toEntity()
        val restored = entity.toDomain()

        assertThat(restored.id).isEqualTo(user.id)
        assertThat(restored.email).isEqualTo(user.email)
        assertThat(restored.displayName).isEqualTo(user.displayName)
        assertThat(restored.photoUrl).isEqualTo(user.photoUrl)
        assertThat(restored.bio).isEqualTo(user.bio)
        assertThat(restored.pairingCode).isEqualTo(user.pairingCode)
        assertThat(restored.partnerId).isEqualTo(user.partnerId)
        assertThat(restored.isPaired).isEqualTo(user.isPaired)
        assertThat(restored.isOnline).isEqualTo(user.isOnline)
        assertThat(restored.lastSeen).isEqualTo(user.lastSeen)
        assertThat(restored.isTyping).isEqualTo(user.isTyping)
        assertThat(restored.typingInChat).isEqualTo(user.typingInChat)
        assertThat(restored.fcmToken).isEqualTo(user.fcmToken)
        assertThat(restored.createdAt).isEqualTo(user.createdAt)
        assertThat(restored.updatedAt).isEqualTo(user.updatedAt)
    }

    @Test
    fun `toEntity sets synced to false`() {
        val user = User(id = "user-1", displayName = "Test")
        val entity = user.toEntity()

        assertThat(entity.synced).isFalse()
    }

    @Test
    fun `roundtrip preserves null fields`() {
        val user = User(
            displayName = "Test",
            photoUrl = null,
            bio = "",
            pairingCode = null,
            partnerId = null,
            lastSeen = null,
            typingInChat = null,
            fcmToken = null
        )

        val entity = user.toEntity()
        val restored = entity.toDomain()

        assertThat(restored.photoUrl).isNull()
        assertThat(restored.pairingCode).isNull()
        assertThat(restored.partnerId).isNull()
        assertThat(restored.lastSeen).isNull()
        assertThat(restored.typingInChat).isNull()
        assertThat(restored.fcmToken).isNull()
    }

    @Test
    fun `toDomain does not include synced field`() {
        val entity = UserEntity(
            id = "user-1",
            email = "test@example.com",
            displayName = "Test",
            synced = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val restored = entity.toDomain()

        // Domain User has no synced field, verify it doesn't affect other fields
        assertThat(restored.id).isEqualTo("user-1")
        assertThat(restored.displayName).isEqualTo("Test")
    }
}
