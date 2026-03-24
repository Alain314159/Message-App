package com.example.messageapp.data.room

import com.example.messageapp.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fake implementation of MessageDao for unit tests.
 * Uses in-memory storage with StateFlow for reactive streams.
 */
class FakeMessageDao : MessageDao {

    private val messagesStateFlow = MutableStateFlow<List<MessageEntity>>(emptyList())
    override fun getAll(): Flow<List<MessageEntity>> = messagesStateFlow.asStateFlow()

    private val messages = mutableListOf<MessageEntity>()

    override suspend fun insert(message: MessageEntity) {
        val existingIndex = messages.indexOfFirst { it.id == message.id }
        if (existingIndex >= 0) {
            messages[existingIndex] = message
        } else {
            messages.add(message)
        }
        messagesStateFlow.update { messages.toList() }
    }

    override suspend fun getById(messageId: String): MessageEntity? {
        return messages.find { it.id == messageId }
    }

    override fun getByChatId(chatId: String): Flow<List<MessageEntity>> {
        return MutableStateFlow(messages.filter { it.chatId == chatId }.sortedBy { it.createdAt }).asStateFlow()
    }

    override suspend fun markAsSynced(messageId: String) {
        messages.find { it.id == messageId }?.let {
            val updated = it.copy(synced = true)
            val index = messages.indexOf(it)
            if (index >= 0) messages[index] = updated
            messagesStateFlow.update { messages.toList() }
        }
    }

    override suspend fun deleteById(messageId: String) {
        messages.removeAll { it.id == messageId }
        messagesStateFlow.update { messages.toList() }
    }

    override suspend fun deleteByChatId(chatId: String) {
        messages.removeAll { it.chatId == chatId }
        messagesStateFlow.update { messages.toList() }
    }

    /**
     * Helper method for tests: clear all data
     */
    fun clear() {
        messages.clear()
        messagesStateFlow.update { emptyList() }
    }

    /**
     * Helper method for tests: insert multiple messages
     */
    fun insertAll(vararg messageEntities: MessageEntity) {
        messageEntities.forEach { insert(it) }
    }

    /**
     * Helper method for tests: get current message count
     */
    fun count(): Int = messages.size

    /**
     * Helper method for tests: get all messages synchronously
     */
    fun getAllSync(): List<MessageEntity> = messages.toList()
}
