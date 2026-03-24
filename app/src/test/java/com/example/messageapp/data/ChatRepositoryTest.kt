package com.example.messageapp.data

import com.example.messageapp.data.room.FakeMessageDao
import com.example.messageapp.data.room.MessageEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var fakeMessageDao: FakeMessageDao
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        fakeMessageDao = FakeMessageDao()
        // Nota: ChatRepository actualmente usa Supabase directamente
        // Para tests completos, necesitaríamos mockear Supabase
        // Este test se enfoca en funciones puras y lógica que no depende de Supabase
    }

    @Test
    fun `directChatIdFor generates deterministic ID`() {
        // Given: Dos user IDs
        val userA = "user-123"
        val userB = "user-456"

        // When: Genero el ID dos veces (orden diferente)
        val chatId1 = chatRepository.directChatIdFor(userA, userB)
        val chatId2 = chatRepository.directChatIdFor(userB, userA)

        // Then: El ID es el mismo (determinista)
        assertThat(chatId1).isEqualTo(chatId2)
        assertThat(chatId1).contains(userA)
        assertThat(chatId1).contains(userB)
    }

    @Test
    fun `directChatIdFor orders IDs consistently`() {
        // Given: User IDs con diferente orden alfabético
        val userA = "zzz-user"
        val userB = "aaa-user"

        // When: Genero el ID
        val chatId = chatRepository.directChatIdFor(userA, userB)

        // Then: El ID siempre ordena alfabéticamente
        assertThat(chatId).isEqualTo("aaa-user_zzz-user")
    }

    @Test
    fun `directChatIdFor same user returns same ID`() {
        // Given: El mismo user ID
        val user = "user-123"

        // When: Genero el ID consigo mismo
        val chatId = chatRepository.directChatIdFor(user, user)

        // Then: El ID es consistente
        assertThat(chatId).isEqualTo("user-123_user-123")
    }
}
