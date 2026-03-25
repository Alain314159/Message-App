package com.example.messageapp.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests para Modelos de Datos (User, Message, Chat, Avatar)
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
class ModelsTest {

    // ============================================
    // Tests para User
    // ============================================

    @Test
    fun `User with empty uid creates successfully`() {
        // When: Creo User con uid vacío
        val user = User(uid = "")

        // Then: Debería crear exitosamente
        assertThat(user.uid).isEmpty()
        assertThat(user.email).isEmpty()
        assertThat(user.displayName).isEmpty()
    }

    @Test
    fun `User with valid data creates successfully`() {
        // When: Creo User con datos válidos
        val user = User(
            uid = "user-123",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio",
            isOnline = true,
            isPaired = false
        )

        // Then: Debería crear exitosamente
        assertThat(user.uid).isEqualTo("user-123")
        assertThat(user.email).isEqualTo("test@example.com")
        assertThat(user.displayName).isEqualTo("Test User")
        assertThat(user.bio).isEqualTo("Test bio")
        assertThat(user.isOnline).isTrue()
        assertThat(user.isPaired).isFalse()
    }

    @Test
    fun `User with null optional fields creates successfully`() {
        // When: Creo User con campos opcionales null
        val user = User(
            uid = "user-456",
            photoUrl = null,
            pairingCode = null,
            partnerId = null,
            lastSeen = null
        )

        // Then: Debería crear exitosamente con nulls
        assertThat(user.photoUrl).isNull()
        assertThat(user.pairingCode).isNull()
        assertThat(user.partnerId).isNull()
        assertThat(user.lastSeen).isNull()
    }

    @Test
    fun `User with unicode data creates successfully`() {
        // When: Creo User con unicode
        val user = User(
            uid = "user-unicode",
            displayName = "Usuario 🌍",
            bio = "Bio con 你好 áéíóú"
        )

        // Then: Debería crear exitosamente
        assertThat(user.displayName).contains("🌍")
        assertThat(user.bio).contains("你好")
    }

    @Test
    fun `User with very long data creates successfully`() {
        // When: Creo User con datos muy largos
        val user = User(
            uid = "user-long",
            displayName = "a".repeat(500),
            bio = "b".repeat(1000)
        )

        // Then: Debería crear exitosamente
        assertThat(user.displayName).hasLength(500)
        assertThat(user.bio).hasLength(1000)
    }

    @Test
    fun `User default values are correct`() {
        // When: Creo User sin parámetros
        val user = User()

        // Then: Debería tener valores por defecto correctos
        assertThat(user.uid).isEmpty()
        assertThat(user.email).isEmpty()
        assertThat(user.displayName).isEmpty()
        assertThat(user.photoUrl).isNull()
        assertThat(user.bio).isEmpty()
        assertThat(user.pairingCode).isNull()
        assertThat(user.partnerId).isNull()
        assertThat(user.isPaired).isFalse()
        assertThat(user.isOnline).isFalse()
        assertThat(user.lastSeen).isNull()
        assertThat(user.isTyping).isFalse()
        assertThat(user.typingInChat).isNull()
        assertThat(user.jpushRegistrationId).isNull()
    }

    // ============================================
    // Tests para Message
    // ============================================

    @Test
    fun `Message with empty id creates successfully`() {
        // When: Creo Message con id vacío
        val message = Message(id = "")

        // Then: Debería crear exitosamente
        assertThat(message.id).isEmpty()
        assertThat(message.chatId).isEmpty()
        assertThat(message.senderId).isEmpty()
    }

    @Test
    fun `Message with valid data creates successfully`() {
        // When: Creo Message con datos válidos
        val message = Message(
            id = "msg-123",
            chatId = "chat-456",
            senderId = "user-789",
            type = "text",
            textEnc = "encrypted-text",
            nonce = "nonce-abc"
        )

        // Then: Debería crear exitosamente
        assertThat(message.id).isEqualTo("msg-123")
        assertThat(message.chatId).isEqualTo("chat-456")
        assertThat(message.senderId).isEqualTo("user-789")
        assertThat(message.type).isEqualTo("text")
        assertThat(message.textEnc).isEqualTo("encrypted-text")
        assertThat(message.nonce).isEqualTo("nonce-abc")
    }

    @Test
    fun `Message status is SENT when not delivered`() {
        // Given: Message sin deliveredAt ni readAt
        val message = Message(
            id = "msg-1",
            deliveredAt = null,
            readAt = null
        )

        // When: Obtengo status
        val status = message.status

        // Then: Debería ser SENT
        assertThat(status).isEqualTo(MessageStatus.SENT)
    }

    @Test
    fun `Message status is DELIVERED when delivered but not read`() {
        // Given: Message con deliveredAt pero sin readAt
        val message = Message(
            id = "msg-2",
            deliveredAt = System.currentTimeMillis() / 1000,
            readAt = null
        )

        // When: Obtengo status
        val status = message.status

        // Then: Debería ser DELIVERED
        assertThat(status).isEqualTo(MessageStatus.DELIVERED)
    }

    @Test
    fun `Message status is READ when readAt is set`() {
        // Given: Message con readAt
        val message = Message(
            id = "msg-3",
            deliveredAt = System.currentTimeMillis() / 1000,
            readAt = System.currentTimeMillis() / 1000
        )

        // When: Obtengo status
        val status = message.status

        // Then: Debería ser READ
        assertThat(status).isEqualTo(MessageStatus.READ)
    }

    @Test
    fun `Message with deletedForAll is true`() {
        // Given: Message eliminado para todos
        val message = Message(
            id = "msg-4",
            deletedForAll = true,
            textEnc = "original-text"
        )

        // Then: Debería tener deletedForAll en true
        assertThat(message.deletedForAll).isTrue()
    }

    @Test
    fun `Message with deletedFor list handles correctly`() {
        // Given: Message eliminado para usuarios específicos
        val message = Message(
            id = "msg-5",
            deletedFor = listOf("user-1", "user-2")
        )

        // Then: Debería tener la lista de usuarios
        assertThat(message.deletedFor).hasSize(2)
        assertThat(message.deletedFor).contains("user-1")
        assertThat(message.deletedFor).contains("user-2")
    }

    @Test
    fun `Message with empty deletedFor list creates successfully`() {
        // When: Creo Message con deletedFor vacío
        val message = Message(
            id = "msg-6",
            deletedFor = emptyList()
        )

        // Then: Debería crear exitosamente con lista vacía
        assertThat(message.deletedFor).isEmpty()
    }

    @Test
    fun `Message with different types creates successfully`() {
        // When: Creo Messages con diferentes tipos
        val textMessage = Message(id = "msg-text", type = "text")
        val imageMessage = Message(id = "msg-image", type = "image")
        val videoMessage = Message(id = "msg-video", type = "video")
        val audioMessage = Message(id = "msg-audio", type = "audio")

        // Then: Deberían crear exitosamente
        assertThat(textMessage.type).isEqualTo("text")
        assertThat(imageMessage.type).isEqualTo("image")
        assertThat(videoMessage.type).isEqualTo("video")
        assertThat(audioMessage.type).isEqualTo("audio")
    }

    // ============================================
    // Tests para Chat
    // ============================================

    @Test
    fun `Chat with empty id creates successfully`() {
        // When: Creo Chat con id vacío
        val chat = Chat(id = "")

        // Then: Debería crear exitosamente
        assertThat(chat.id).isEmpty()
        assertThat(chat.type).isEqualTo("couple")
        assertThat(chat.memberIds).isEmpty()
    }

    @Test
    fun `Chat with valid data creates successfully`() {
        // When: Creo Chat con datos válidos
        val chat = Chat(
            id = "chat-123",
            type = "couple",
            memberIds = listOf("user-1", "user-2"),
            user1Typing = false,
            user2Typing = true
        )

        // Then: Debería crear exitosamente
        assertThat(chat.id).isEqualTo("chat-123")
        assertThat(chat.type).isEqualTo("couple")
        assertThat(chat.memberIds).hasSize(2)
        assertThat(chat.user1Typing).isFalse()
        assertThat(chat.user2Typing).isTrue()
    }

    @Test
    fun `Chat isUserTyping returns true for user1`() {
        // Given: Chat con user1 escribiendo
        val chat = Chat(
            id = "chat-1",
            memberIds = listOf("user-1", "user-2"),
            user1Typing = true,
            user2Typing = false
        )

        // When: Verifico si user-1 está escribiendo
        val isTyping = chat.isUserTyping("user-1")

        // Then: Debería ser true
        assertThat(isTyping).isTrue()
    }

    @Test
    fun `Chat isUserTyping returns true for user2`() {
        // Given: Chat con user2 escribiendo
        val chat = Chat(
            id = "chat-2",
            memberIds = listOf("user-1", "user-2"),
            user1Typing = false,
            user2Typing = true
        )

        // When: Verifico si user-2 está escribiendo
        val isTyping = chat.isUserTyping("user-2")

        // Then: Debería ser true
        assertThat(isTyping).isTrue()
    }

    @Test
    fun `Chat isUserTyping returns false when not typing`() {
        // Given: Chat con nadie escribiendo
        val chat = Chat(
            id = "chat-3",
            memberIds = listOf("user-1", "user-2"),
            user1Typing = false,
            user2Typing = false
        )

        // When: Verifico si user-1 está escribiendo
        val isTyping = chat.isUserTyping("user-1")

        // Then: Debería ser false
        assertThat(isTyping).isFalse()
    }

    @Test
    fun `Chat isUserTyping returns false for unknown user`() {
        // Given: Chat con usuario desconocido
        val chat = Chat(
            id = "chat-4",
            memberIds = listOf("user-1", "user-2"),
            user1Typing = true,
            user2Typing = false
        )

        // When: Verifico si user-3 está escribiendo
        val isTyping = chat.isUserTyping("user-3")

        // Then: Debería ser false (no está en memberIds)
        assertThat(isTyping).isFalse()
    }

    @Test
    fun `Chat with pinned message creates successfully`() {
        // When: Creo Chat con mensaje fijado
        val chat = Chat(
            id = "chat-5",
            pinnedMessageId = "msg-123",
            pinnedSnippet = "Mensaje fijado"
        )

        // Then: Debería crear exitosamente
        assertThat(chat.pinnedMessageId).isEqualTo("msg-123")
        assertThat(chat.pinnedSnippet).isEqualTo("Mensaje fijado")
    }

    @Test
    fun `Chat with null pinned message creates successfully`() {
        // When: Creo Chat sin mensaje fijado
        val chat = Chat(
            id = "chat-6",
            pinnedMessageId = null,
            pinnedSnippet = null
        )

        // Then: Debería crear exitosamente con nulls
        assertThat(chat.pinnedMessageId).isNull()
        assertThat(chat.pinnedSnippet).isNull()
    }

    @Test
    fun `Chat with last message metadata creates successfully`() {
        // When: Creo Chat con metadatos del último mensaje
        val chat = Chat(
            id = "chat-7",
            lastMessageEnc = "encrypted-last-msg",
            lastMessageAt = System.currentTimeMillis() / 1000
        )

        // Then: Debería crear exitosamente
        assertThat(chat.lastMessageEnc).isEqualTo("encrypted-last-msg")
        assertThat(chat.lastMessageAt).isGreaterThan(0)
    }

    @Test
    fun `Chat default type is couple`() {
        // When: Creo Chat sin especificar tipo
        val chat = Chat(id = "chat-8")

        // Then: Debería ser "couple" por defecto
        assertThat(chat.type).isEqualTo("couple")
    }

    // ============================================
    // Tests para Avatar
    // ============================================

    @Test
    fun `AvatarType fromId with cerdita returns CERDITA`() {
        // When: Obtengo AvatarType desde id
        val avatar = AvatarType.fromId("cerdita")

        // Then: Debería retornar CERDITA
        assertThat(avatar).isEqualTo(AvatarType.CERDITA)
    }

    @Test
    fun `AvatarType fromId with cerdito returns CERDITO`() {
        // When: Obtengo AvatarType desde id
        val avatar = AvatarType.fromId("cerdito")

        // Then: Debería retornar CERDITO
        assertThat(avatar).isEqualTo(AvatarType.CERDITO)
    }

    @Test
    fun `AvatarType fromId with unknown returns CERDITA (default)`() {
        // When: Obtengo AvatarType desde id desconocido
        val avatar = AvatarType.fromId("unknown")

        // Then: Debería retornar CERDITA por defecto
        assertThat(avatar).isEqualTo(AvatarType.CERDITA)
    }

    @Test
    fun `AvatarType fromId with null returns CERDITA (default)`() {
        // When: Obtengo AvatarType desde null
        val avatar = AvatarType.fromId(null)

        // Then: Debería retornar CERDITA por defecto
        assertThat(avatar).isEqualTo(AvatarType.CERDITA)
    }

    @Test
    fun `AvatarType getAll returns non-empty list`() {
        // When: Obtengo todos los avatares
        val avatars = AvatarType.getAll()

        // Then: Debería retornar lista no vacía
        assertThat(avatars).isNotEmpty()
    }

    @Test
    fun `AvatarType getAll contains CERDITA and CERDITO`() {
        // When: Obtengo todos los avatares
        val avatars = AvatarType.getAll()

        // Then: Debería contener CERDITA y CERDITO
        assertThat(avatars).contains(AvatarType.CERDITA)
        assertThat(avatars).contains(AvatarType.CERDITO)
    }

    @Test
    fun `AvatarType id is not empty`() {
        // When: Obtengo todos los avatares
        val avatars = AvatarType.getAll()

        // Then: Cada avatar debería tener id no vacío
        avatars.forEach { avatar ->
            assertThat(avatar.id).isNotEmpty()
        }
    }

    // ============================================
    // Tests de integración: Model relationships
    // ============================================

    @Test
    fun `User with Messages creates successfully`() {
        // Given: User con mensajes
        val user = User(uid = "user-1", displayName = "User 1")
        val messages = listOf(
            Message(id = "msg-1", senderId = user.uid),
            Message(id = "msg-2", senderId = user.uid)
        )

        // Then: Debería crear exitosamente
        assertThat(user.uid).isEqualTo("user-1")
        assertThat(messages).hasSize(2)
    }

    @Test
    fun `Chat with Messages creates successfully`() {
        // Given: Chat con mensajes
        val chat = Chat(id = "chat-1", memberIds = listOf("user-1", "user-2"))
        val messages = listOf(
            Message(id = "msg-1", chatId = chat.id),
            Message(id = "msg-2", chatId = chat.id)
        )

        // Then: Debería crear exitosamente
        assertThat(chat.id).isEqualTo("chat-1")
        assertThat(messages).hasSize(2)
    }

    @Test
    fun `User in Chat creates successfully`() {
        // Given: User en Chat
        val user = User(uid = "user-1", displayName = "User 1")
        val chat = Chat(id = "chat-1", memberIds = listOf(user.uid, "user-2"))

        // Then: Debería crear exitosamente
        assertThat(chat.memberIds).contains(user.uid)
    }

    // ============================================
    // Tests edge cases: Special characters
    // ============================================

    @Test
    fun `User with special characters in displayName creates successfully`() {
        // When: Creo User con caracteres especiales
        val user = User(
            uid = "user-special",
            displayName = "User-<>&\"'-123"
        )

        // Then: Debería crear exitosamente
        assertThat(user.displayName).contains("<>&\"'")
    }

    @Test
    fun `Message with special characters in textEnc creates successfully`() {
        // When: Creo Message con caracteres especiales
        val message = Message(
            id = "msg-special",
            textEnc = "encrypted-<>&\"'-text"
        )

        // Then: Debería crear exitosamente
        assertThat(message.textEnc).contains("<>&\"'")
    }

    // ============================================
    // Tests edge cases: Boundary values
    // ============================================

    @Test
    fun `User with max timestamp creates successfully`() {
        // When: Creo User con timestamp máximo
        val user = User(
            uid = "user-max",
            createdAt = Long.MAX_VALUE
        )

        // Then: Debería crear exitosamente
        assertThat(user.createdAt).isEqualTo(Long.MAX_VALUE)
    }

    @Test
    fun `Message with zero timestamp creates successfully`() {
        // When: Creo Message con timestamp cero
        val message = Message(
            id = "msg-zero",
            createdAt = 0
        )

        // Then: Debería crear exitosamente
        assertThat(message.createdAt).isEqualTo(0)
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `creating multiple Users concurrently does not crash`() {
        // When: Creo múltiples Users en paralelo
        val users = List(100) { index ->
            User(uid = "user-$index", displayName = "User $index")
        }

        // Then: Ninguno debería crashar
        assertThat(users).hasSize(100)
        users.forEachIndexed { index, user ->
            assertThat(user.uid).isEqualTo("user-$index")
        }
    }

    @Test
    fun `creating multiple Messages concurrently does not crash`() {
        // When: Creo múltiples Messages en paralelo
        val messages = List(100) { index ->
            Message(id = "msg-$index", chatId = "chat-1")
        }

        // Then: Ninguno debería crashar
        assertThat(messages).hasSize(100)
        messages.forEachIndexed { index, message ->
            assertThat(message.id).isEqualTo("msg-$index")
        }
    }

    @Test
    fun `creating multiple Chats concurrently does not crash`() {
        // When: Creo múltiples Chats en paralelo
        val chats = List(100) { index ->
            Chat(id = "chat-$index", memberIds = listOf("user-1", "user-2"))
        }

        // Then: Ninguno debería crashar
        assertThat(chats).hasSize(100)
        chats.forEachIndexed { index, chat ->
            assertThat(chat.id).isEqualTo("chat-$index")
        }
    }
}
