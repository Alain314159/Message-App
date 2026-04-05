package com.example.messageapp.data

import com.example.messageapp.model.Chat
import com.example.messageapp.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * FACADE TEMPORAL - Para compatibilidad con código existente
 *
 * Este facade combina los repositorios split para mantener
 * la API antigua de ChatRepository mientras se actualiza el código.
 *
 * TODO: Eliminar este archivo cuando todo el código use los nuevos repositorios
 */
@Deprecated(
    "Usar ChatReadRepository, ChatWriteRepository, MessageReadRepository, MessageWriteRepository y MessageActionsRepository por separado",
    ReplaceWith("ChatReadRepository, ChatWriteRepository, MessageReadRepository, MessageWriteRepository, MessageActionsRepository")
)
class ChatRepository(
    private val chatReadRepository: ChatReadRepository = ChatReadRepository(),
    private val chatWriteRepository: ChatWriteRepository = ChatWriteRepository(),
    private val messageReadRepository: MessageReadRepository = MessageReadRepository(),
    private val messageWriteRepository: MessageWriteRepository = MessageWriteRepository(),
    private val messageActionsRepository: MessageActionsRepository = MessageActionsRepository()
) {

    // Delegados a ChatReadRepository
    fun directChatIdFor(uidA: String, uidB: String): String =
        chatReadRepository.directChatIdFor(uidA, uidB)

    suspend fun ensureDirectChat(uidA: String, uidB: String): String =
        chatWriteRepository.ensureDirectChat(uidA, uidB)

    fun observeChats(uid: String): Flow<List<Chat>> =
        chatReadRepository.observeChats(uid)

    fun observeChat(chatId: String): Flow<Chat?> =
        chatReadRepository.observeChat(chatId)

    // Delegados a MessageReadRepository
    fun observeMessages(chatId: String, myUid: String): Flow<List<Message>> =
        messageReadRepository.observeMessages(chatId, myUid)

    // Delegados a MessageWriteRepository
    suspend fun sendText(chatId: String, senderId: String, textEnc: String, iv: String) =
        messageWriteRepository.sendText(chatId, senderId, textEnc, iv)

    suspend fun markDelivered(chatId: String, messageId: String, uid: String) =
        messageWriteRepository.markDelivered(chatId, messageId, uid)

    suspend fun markAsRead(chatId: String, uid: String) =
        messageWriteRepository.markAsRead(chatId, uid)

    // Delegados a MessageActionsRepository
    suspend fun pinMessage(chatId: String, messageId: String, snippet: String) =
        messageActionsRepository.pinMessage(chatId, messageId, snippet)

    suspend fun unpinMessage(chatId: String) =
        messageActionsRepository.unpinMessage(chatId)

    suspend fun deleteMessageForUser(chatId: String, messageId: String, uid: String) =
        messageActionsRepository.deleteMessageForUser(chatId, messageId, uid)

    suspend fun deleteMessageForAll(chatId: String, messageId: String) =
        messageActionsRepository.deleteMessageForAll(chatId, messageId)

    suspend fun countUnreadMessages(chatId: String, uid: String): Int =
        messageReadRepository.countUnreadMessages(chatId, uid)
}
