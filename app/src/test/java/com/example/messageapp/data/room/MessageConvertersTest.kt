package com.example.messageapp.data.room

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for MessageConverters (List<String> ↔ JSON serialization).
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class MessageConvertersTest {

    private val converters = MessageConverters()

    @Test
    fun `fromStringList converts list to JSON`() {
        val result = converters.fromStringList(listOf("user-1", "user-2", "user-3"))

        assertThat(result).isEqualTo("""["user-1","user-2","user-3"]""")
    }

    @Test
    fun `fromStringList converts empty list to JSON`() {
        val result = converters.fromStringList(emptyList())

        assertThat(result).isEqualTo("[]")
    }

    @Test
    fun `toStringList converts JSON to list`() {
        val result = converters.toStringList("""["user-1","user-2"]""")

        assertThat(result).containsExactly("user-1", "user-2")
    }

    @Test
    fun `toStringList returns empty list for invalid JSON`() {
        val result = converters.toStringList("not-valid-json")

        assertThat(result).isEmpty()
    }

    @Test
    fun `roundtrip preserves list contents`() {
        val original = listOf("user-a", "user-b", "user-c")

        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)

        assertThat(restored).containsExactly("user-a", "user-b", "user-c")
    }

    @Test
    fun `roundtrip preserves empty list`() {
        val original = emptyList<String>()

        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)

        assertThat(restored).isEmpty()
    }
}
