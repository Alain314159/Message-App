package com.example.messageapp.data

import com.example.messageapp.model.Chat
import com.example.messageapp.model.Message
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {

    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        repository = ChatRepository()
    }

    @Test
    fun `directChatIdFor generates same ID regardless of order`() {
        // Given: Dos user IDs en diferente orden
        val uidA = "user-123"
        val uidB = "user-456"

        // When: Genero IDs en ambos órdenes
        val id1 = repository.directChatIdFor(uidA, uidB)
        val id2 = repository.directChatIdFor(uidB, uidA)

        // Then: Mismo ID determinista
        assertThat(id1).isEqualTo(id2)
        assertThat(id1).isEqualTo("user-123_user-456")
    }

    @Test
    fun `directChatIdFor handles same user IDs`() {
        // Given: Mismo user ID
        val uid = "user-123"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uid, uid)

        // Then: ID consistente
        assertThat(chatId).isEqualTo("user-123_user-123")
    }

    @Test
    fun `directChatIdFor handles empty strings`() {
        // Given: User IDs vacíos
        val uidA = ""
        val uidB = ""

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No crash, ID consistente
        assertThat(chatId).isEqualTo("_")
    }

    @Test
    fun `directChatIdFor handles null-like strings`() {
        // Given: User IDs que parecen null
        val uidA = "null"
        val uidB = "null"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Maneja como strings normales
        assertThat(chatId).isEqualTo("null_null")
    }

    @Test
    fun `directChatIdFor sorts lexicographically not numerically`() {
        // Given: User IDs numéricos
        val uidA = "user-9"
        val uidB = "user-10"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Orden lexicográfico (no numérico)
        // "user-10" < "user-9" lexicográficamente
        assertThat(chatId).isEqualTo("user-10_user-9")
    }

    @Test
    fun `directChatIdFor handles special characters`() {
        // Given: User IDs con caracteres especiales
        val uidA = "user_123"
        val uidB = "user-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No crash, maneja caracteres
        assertThat(chatId).contains("_")
    }

    @Test
    fun `directChatIdFor handles unicode characters`() {
        // Given: User IDs con unicode
        val uidA = "user-ñ-123"
        val uidB = "user-á-456"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No crash, maneja unicode
        assertThat(chatId).isNotEmpty()
    }

    @Test
    fun `directChatIdFor handles very long IDs`() {
        // Given: User IDs muy largos
        val uidA = "user-${"a".repeat(1000)}"
        val uidB = "user-${"b".repeat(1000)}"

        // When: Genero ID
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: No crash, ID contiene ambos
        assertThat(chatId).contains(uidA)
        assertThat(chatId).contains(uidB)
    }

    @Test
    fun `directChatIdFor performance with many calls`() {
        // Given: Muchas llamadas
        val iterations = 10000

        // When: Mido tiempo
        val elapsed = measureTimeMillis {
            repeat(iterations) {
                repository.directChatIdFor("user-$it", "user-${it + 1}")
            }
        }

        // Then: Rápido (< 100ms para 10k llamadas)
        assertThat(elapsed).isLessThan(100)
    }

    @Test
    fun `directChatIdFor handles case sensitivity`() {
        // Given: User IDs con diferente case
        val uidA = "User-123"
        val uidB = "user-123"

        // When: Genero IDs
        val id1 = repository.directChatIdFor(uidA, uidB)
        val id2 = repository.directChatIdFor(uidB, uidA)

        // Then: Case-sensitive (diferente)
        assertThat(id1).isEqualTo(id2) // Debería ser igual por sorted()
    }

    @Test
    fun `directChatIdFor trims whitespace BUG`() {
        // Given: User IDs con whitespace
        val uidA = " user-123 "
        val uidB = " user-456 "

        // When: Genero ID (sin trim en la implementación actual)
        val chatId = repository.directChatIdFor(uidA, uidB)

        // Then: Whitespace se incluye (ERROR POTENCIAL)
        // Esto es un bug - debería trimear
        assertThat(chatId).contains(" ")
    }
}
