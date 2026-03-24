package com.example.messageapp.data.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.messageapp.model.Message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MessageDaoTest {

    private lateinit var messageDao: MessageDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        messageDao = db.messageDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertMessage_verifiesDataInDatabase() = runTest {
        // Given: Un mensaje válido
        val message = MessageEntity(
            id = "msg-001",
            chatId = "chat-001",
            senderId = "user-123",
            textEnc = "Hola mundo",
            nonce = "nonce-abc",
            createdAt = System.currentTimeMillis(),
            synced = false
        )

        // When: Inserto el mensaje
        messageDao.insert(message)

        // Then: Verifico que está en la base de datos
        val allMessages = messageDao.getAll().first()
        assertEquals(1, allMessages.size)
        assertEquals(message.id, allMessages.first().id)
        assertEquals(message.textEnc, allMessages.first().textEnc)
    }

    @Test
    fun insertMessage_withReplace_updatesExistingMessage() = runTest {
        // Given: Un mensaje existente
        val originalMessage = MessageEntity(
            id = "msg-001",
            chatId = "chat-001",
            senderId = "user-123",
            textEnc = "Texto original",
            createdAt = 1000L,
            synced = false
        )
        messageDao.insert(originalMessage)

        // When: Inserto otro mensaje con el mismo ID (REPLACE)
        val updatedMessage = MessageEntity(
            id = "msg-001",
            chatId = "chat-001",
            senderId = "user-123",
            textEnc = "Texto actualizado",
            createdAt = 2000L,
            synced = true
        )
        messageDao.insert(updatedMessage)

        // Then: Verifico que se actualizó
        val retrieved = messageDao.getById("msg-001")
        assertNotNull(retrieved)
        assertEquals("Texto actualizado", retrieved?.textEnc)
        assertTrue(retrieved?.synced == true)
    }

    @Test
    fun getById_whenMessageExists_returnsMessage() = runTest {
        // Given: Mensaje insertado
        val message = MessageEntity(
            id = "msg-002",
            chatId = "chat-001",
            senderId = "user-456",
            textEnc = "Mensaje específico",
            createdAt = System.currentTimeMillis(),
            synced = false
        )
        messageDao.insert(message)

        // When: Busco por ID
        val result = messageDao.getById("msg-002")

        // Then: Encuentro el mensaje
        assertNotNull(result)
        assertEquals("msg-002", result?.id)
        assertEquals("Mensaje específico", result?.textEnc)
    }

    @Test
    fun getById_whenMessageNotExists_returnsNull() = runTest {
        // When: Busco un ID que no existe
        val result = messageDao.getById("msg-inexistente")

        // Then: Retorna null
        assertNull(result)
    }

    @Test
    fun getByChatId_returnsOnlyMessagesFromThatChat() = runTest {
        // Given: Múltiples mensajes en diferentes chats
        messageDao.insert(
            MessageEntity("msg-1", "chat-A", "user-1", "Chat A msg 1", null, 1000L, false)
        )
        messageDao.insert(
            MessageEntity("msg-2", "chat-A", "user-2", "Chat A msg 2", null, 2000L, false)
        )
        messageDao.insert(
            MessageEntity("msg-3", "chat-B", "user-1", "Chat B msg 1", null, 3000L, false)
        )

        // When: Obtengo mensajes de chat-A
        val chatAMessages = messageDao.getByChatId("chat-A").first()

        // Then: Solo obtengo mensajes de chat-A
        assertEquals(2, chatAMessages.size)
        assertTrue(chatAMessages.all { it.chatId == "chat-A" })
    }

    @Test
    fun markAsSynced_updatesMessageSyncStatus() = runTest {
        // Given: Mensaje no sincronizado
        val message = MessageEntity(
            id = "msg-003",
            chatId = "chat-001",
            senderId = "user-789",
            textEnc = "Sin sincronizar",
            createdAt = System.currentTimeMillis(),
            synced = false
        )
        messageDao.insert(message)

        // When: Marco como sincronizado
        messageDao.markAsSynced("msg-003")

        // Then: Verifico que está sincronizado
        val updated = messageDao.getById("msg-003")
        assertNotNull(updated)
        assertTrue(updated?.synced == true)
    }

    @Test
    fun deleteById_removesMessageFromDatabase() = runTest {
        // Given: Mensaje existente
        val message = MessageEntity(
            id = "msg-delete",
            chatId = "chat-001",
            senderId = "user-123",
            textEnc = "Para eliminar",
            createdAt = System.currentTimeMillis(),
            synced = false
        )
        messageDao.insert(message)

        // When: Elimino el mensaje
        messageDao.deleteById("msg-delete")

        // Then: Ya no existe
        val result = messageDao.getById("msg-delete")
        assertNull(result)
    }

    @Test
    fun deleteByChatId_removesAllMessagesFromChat() = runTest {
        // Given: Múltiples mensajes en un chat
        messageDao.insert(MessageEntity("msg-1", "chat-delete", "user-1", "M1", null, 1000L, false))
        messageDao.insert(MessageEntity("msg-2", "chat-delete", "user-2", "M2", null, 2000L, false))
        messageDao.insert(MessageEntity("msg-3", "chat-delete", "user-1", "M3", null, 3000L, false))
        messageDao.insert(MessageEntity("msg-4", "chat-other", "user-1", "Other", null, 4000L, false))

        // When: Elimino todos los mensajes de chat-delete
        messageDao.deleteByChatId("chat-delete")

        // Then: Solo queda el mensaje de chat-other
        val allMessages = messageDao.getAll().first()
        assertEquals(1, allMessages.size)
        assertEquals("chat-other", allMessages.first().chatId)
    }

    @Test
    fun getAll_returnsMessagesOrderedByCreatedAt() = runTest {
        // Given: Mensajes con diferentes timestamps (desordenados)
        messageDao.insert(MessageEntity("msg-3", "chat-1", "user-1", "Tercero", null, 3000L, false))
        messageDao.insert(MessageEntity("msg-1", "chat-1", "user-1", "Primero", null, 1000L, false))
        messageDao.insert(MessageEntity("msg-2", "chat-1", "user-1", "Segundo", null, 2000L, false))

        // When: Obtengo todos los mensajes
        val allMessages = messageDao.getAll().first()

        // Then: Están ordenados por createdAt ASC
        assertEquals(3, allMessages.size)
        assertEquals("Primero", allMessages[0].textEnc)
        assertEquals("Segundo", allMessages[1].textEnc)
        assertEquals("Tercero", allMessages[2].textEnc)
    }
}
