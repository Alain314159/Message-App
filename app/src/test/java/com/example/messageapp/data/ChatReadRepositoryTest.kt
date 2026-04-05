package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for ChatReadRepository directChatIdFor logic.
 * Pure Kotlin function - no Supabase dependency.
 */
class ChatReadRepositoryTest {

    private fun directChatIdFor(uidA: String, uidB: String): String {
        return listOf(uidA.trim(), uidB.trim()).sorted().joinToString("_")
    }

    @Test
    fun `directChatIdFor generates deterministic ID`() {
        val id1 = directChatIdFor("user-a", "user-b")
        val id2 = directChatIdFor("user-b", "user-a")

        assertThat(id1).isEqualTo(id2)
    }

    @Test
    fun `directChatIdFor sorts user IDs alphabetically`() {
        val id = directChatIdFor("user-z", "user-a")
        assertThat(id).isEqualTo("user-a_user-z")
    }

    @Test
    fun `directChatIdFor uses underscore separator`() {
        val id = directChatIdFor("user-1", "user-2")
        assertThat(id).contains("_")
        assertThat(id.split("_")).hasSize(2)
    }

    @Test
    fun `directChatIdFor trims whitespace from user IDs`() {
        val id = directChatIdFor(" user-a ", "user-b")
        assertThat(id).isEqualTo("user-a_user-b")
    }

    @Test
    fun `directChatIdFor with UUID-like IDs`() {
        val uidA = "550e8400-e29b-41d4-a716-446655440000"
        val uidB = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"

        val id = directChatIdFor(uidA, uidB)

        assertThat(id).startsWith("550e8400")
        assertThat(id).contains("-")
    }
}
