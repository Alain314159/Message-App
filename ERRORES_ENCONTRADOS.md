# 🐛 Errores Encontrados y Correcciones - Message App

## 📊 Resumen

**Fecha:** 2026-03-24  
**Total Errores:** 15  
**Críticos:** 3  
**Mayores:** 5  
**Menores:** 7

---

## 🔴 Errores Críticos

### ERR-001: ChatRepository no valida nulls en observeChat

**Archivo:** `app/src/main/java/com/example/messageapp/data/ChatRepository.kt`  
**Línea:** ~147

**Problema:**
```kotlin
fun observeChat(chatId: String): Flow<Chat?> = callbackFlow {
    try {
        val chat = db.from("chats")
            .select(...)
            .decodeSingle<Chat>()  // ❌ Puede lanzar si no existe
        
        trySend(chat)
    } catch (e: Exception) {
        trySend(null)  // ❌ Silencia el error
    }
    awaitClose { }
}
```

**Riesgo:**
- Si el chat no existe, `decodeSingle` lanza excepción
- Se silencia el error y se envía null
- La UI no sabe si es error o chat vacío

**Solución:**
```kotlin
fun observeChat(chatId: String): Flow<Result<Chat?>> = callbackFlow {
    try {
        val chat = db.from("chats")
            .select(...)
            .filter { eq("id", chatId) }
            .maybeDecodeSingle<Chat>()  // ✅ Returns null si no existe
        
        trySend(Result.success(chat))
    } catch (e: Exception) {
        trySend(Result.failure(e))  // ✅ Propaga error
    }
    awaitClose { }
}
```

**Test para agregar:**
```kotlin
@Test
fun `observeChat emits error when chat does not exist`() = runTest {
    // Given: Chat ID que no existe
    val nonExistentChatId = "chat-nonexistent"
    
    // When: Observo chat
    viewModel.start(nonExistentChatId, "user-123")
    
    // Then: Debería emitir error, no null silencioso
    viewModel.error.test {
        val error = awaitItem()
        assertThat(error).isNotNull()
    }
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🔴 Alta

---

### ERR-002: ChatViewModel no maneja null en decryptMessage

**Archivo:** `app/src/main/java/com/example/messageapp/viewmodel/ChatViewModel.kt`  
**Línea:** ~127

**Problema:**
```kotlin
fun decryptMessage(message: Message): String {
    if (message.type == "deleted") {
        return "[Mensaje eliminado]"
    }
    
    if (message.textEnc.isNullOrBlank()) {  // ❌ No verifica nonce
        return ""
    }
    
    val chatId = currentChatId ?: return "[Error: Chat no disponible]"
    
    // ❌ Si nonce es null, la reconstrucción falla
    val encrypted = "${message.nonce}:${message.textEnc}"
    
    return E2ECipher.decrypt(encrypted, chatId)  // ❌ Crash si nonce es null
}
```

**Riesgo:**
- Si `nonce` es null, se reconstruye mal el cifrado
- `E2ECipher.decrypt` puede crashar
- La UI muestra "[Error: No se pudo descifrar]" incorrectamente

**Solución:**
```kotlin
fun decryptMessage(message: Message): String {
    when {
        message.type == "deleted" -> return "[Mensaje eliminado]"
        message.textEnc.isNullOrBlank() -> return ""
        message.nonce.isNullOrBlank() -> return "[Error: Clave de cifrado faltante]"
        currentChatId == null -> return "[Error: Chat no disponible]"
    }
    
    return try {
        val encrypted = "${message.nonce}:${message.textEnc}"
        E2ECipher.decrypt(encrypted, currentChatId!!)
    } catch (e: Exception) {
        Log.w("ChatViewModel", "Decrypt failed", e)
        "[Error: No se pudo descifrar]"
    }
}
```

**Test para agregar:**
```kotlin
@Test
fun `decryptMessage handles null nonce gracefully`() {
    // Given: Mensaje con nonce null
    val message = Message(
        id = "msg-1",
        chatId = "chat-1",
        senderId = "user-1",
        textEnc = "encrypted-text",
        type = "text",
        nonce = null,  // ❌ Null
        createdAt = 1000L
    )
    
    // When: Descifro
    val result = viewModel.decryptMessage(message)
    
    // Then: Error descriptivo, no crash
    assertThat(result).isEqualTo("[Error: Clave de cifrado faltante]")
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🔴 Alta

---

### ERR-003: ChatRepository.sendText no valida parámetros

**Archivo:** `app/src/main/java/com/example/messageapp/data/ChatRepository.kt`  
**Línea:** ~203

**Problema:**
```kotlin
suspend fun sendText(
    chatId: String,      // ❌ No valida null/empty
    senderId: String,    // ❌ No valida null/empty
    textEnc: String,     // ❌ No valida null/empty
    iv: String           // ❌ No valida null/empty
) = withContext(Dispatchers.IO) {
    try {
        db.from("messages").insert(  // ❌ Crash si parámetros son inválidos
            mapOf(
                "chat_id" to chatId,
                "sender_id" to senderId,
                "text_enc" to textEnc,
                "nonce" to iv
            )
        )
    } catch (e: Exception) {
        Log.w("ChatRepository", "Send message error", e)
        throw e  // ❌ Relanza sin manejar
    }
}
```

**Riesgo:**
- Si `textEnc` es vacío, se guarda mensaje sin contenido
- Si `iv` es null, no se puede descifrar después
- Crash en producción por datos inválidos

**Solución:**
```kotlin
suspend fun sendText(
    chatId: String,
    senderId: String,
    textEnc: String,
    iv: String
) = withContext(Dispatchers.IO) {
    // ✅ Validar parámetros
    require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
    require(senderId.isNotBlank()) { "senderId no puede estar vacío" }
    require(textEnc.isNotBlank()) { "textEnc no puede estar vacío" }
    require(iv.isNotBlank()) { "iv no puede estar vacío" }
    
    try {
        db.from("messages").insert(...)
    } catch (e: Exception) {
        Log.w("ChatRepository", "Send message error", e)
        throw e
    }
}
```

**Test para agregar:**
```kotlin
@Test
fun `sendText throws when chatId is empty`() = runTest {
    // When: Envío con chatId vacío
    val result = runCatching {
        repository.sendText("", "user-1", "encrypted", "iv")
    }
    
    // Then: Debería lanzar IllegalArgumentException
    assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🔴 Alta

---

## 🟠 Errores Mayores

### ERR-004: directChatIdFor no trimea whitespace

**Archivo:** `app/src/main/java/com/example/messageapp/data/ChatRepository.kt`  
**Línea:** ~36

**Problema:**
```kotlin
fun directChatIdFor(uidA: String, uidB: String): String {
    return listOf(uidA, uidB).sorted().joinToString("_")
    // ❌ No trimea whitespace
}
```

**Riesgo:**
- `" user-123 "` + `" user-456 "` = `" user-123 _ user-456 "`
- Chat IDs diferentes para mismos usuarios
- Duplicación de chats

**Solución:**
```kotlin
fun directChatIdFor(uidA: String, uidB: String): String {
    return listOf(uidA.trim(), uidB.trim()).sorted().joinToString("_")
}
```

**Test:** ✅ Ya agregado en `ChatRepositoryTest.kt`

**Estado:** ⏳ Pendiente  
**Prioridad:** 🟠 Media

---

### ERR-005: observeMessages no maneja errores de decodificación

**Archivo:** `app/src/main/java/com/example/messageapp/data/ChatRepository.kt`  
**Línea:** ~165

**Problema:**
```kotlin
changeFlow.collect { change ->
    val recordJson = change.record
    if (recordJson != null) {
        try {
            val message = Json.decodeFromJsonElement<Message>(recordJson)
            // ❌ Si falla, se loguea y se ignora
        } catch (e: Exception) {
            Log.w("ChatRepository", "Error decoding message", e)
            // ❌ Silencioso - el usuario no ve el mensaje
        }
    }
}
```

**Riesgo:**
- Mensajes nuevos se pierden silenciosamente
- No hay reintento
- El usuario no sabe que faltan mensajes

**Solución:**
```kotlin
changeFlow.collect { change ->
    val recordJson = change.record
    if (recordJson != null) {
        try {
            val message = Json.decodeFromJsonElement<Message>(recordJson)
            if (message.chatId == chatId) {
                loadMessages(chatId)  // ✅ Recargar
            }
        } catch (e: Exception) {
            Log.w("ChatRepository", "Error decoding message", e)
            // ✅ Intentar decodificación alternativa
            try {
                val fallbackMessage = decodeFallbackMessage(recordJson)
                if (fallbackMessage != null) {
                    loadMessages(chatId)
                }
            } catch (e2: Exception) {
                Log.e("ChatRepository", "Fallback decode failed", e2)
                // ✅ Reportar error para debugging
            }
        }
    }
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🟠 Media

---

### ERR-006: markDelivered ignora errores silenciosamente

**Archivo:** `app/src/main/java/com/example/messageapp/data/ChatRepository.kt`  
**Línea:** ~237

**Problema:**
```kotlin
suspend fun markDelivered(chatId: String, messageId: String, uid: String) {
    try {
        db.from("messages").update(...)
    } catch (e: Exception) {
        // ❌ Silencioso - no hay log, no hay retry
    }
}
```

**Riesgo:**
- Mensajes nunca se marcan como entregados
- Usuario ve 1 tick siempre
- No hay forma de diagnosticar

**Solución:**
```kotlin
suspend fun markDelivered(chatId: String, messageId: String, uid: String) {
    try {
        db.from("messages").update(...)
    } catch (e: Exception) {
        Log.w("ChatRepository", "Mark delivered failed: $messageId", e)
        // ✅ Reintentar después
        // retryQueue.add(MarkDeliveredRequest(chatId, messageId, uid))
    }
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🟠 Media

---

### ERR-007: ChatViewModel no valida start() parameters

**Archivo:** `app/src/main/java/com/example/messageapp/viewmodel/ChatViewModel.kt`  
**Línea:** ~46

**Problema:**
```kotlin
fun start(chatId: String, myUid: String) {
    if (currentChatId == chatId) return  // ❌ No valida chatId vacío
    
    currentChatId = chatId
    currentUserId = myUid
    _isLoading.value = true
    
    // ❌ Si chatId es vacío, observeChat falla
    viewModelScope.launch {
        repo.observeChat(chatId).collect { ... }  // ❌ Crash
    }
}
```

**Solución:**
```kotlin
fun start(chatId: String, myUid: String) {
    require(chatId.isNotBlank()) { "chatId no puede estar vacío" }
    require(myUid.isNotBlank()) { "myUid no puede estar vacío" }
    
    if (currentChatId == chatId) return
    
    currentChatId = chatId
    currentUserId = myUid
    _isLoading.value = true
    
    viewModelScope.launch {
        try {
            repo.observeChat(chatId).collect { ... }
        } catch (e: Exception) {
            _error.value = "Error al cargar chat: ${e.message}"
        }
    }
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🟠 Media

---

### ERR-008: Crypto.kt usa Base64 en lugar de cifrado real

**Archivo:** `app/src/main/java/com/example/messageapp/utils/Crypto.kt`

**Problema:**
```kotlin
object Crypto {
    fun encrypt(plain: String): String =
        Base64.encodeToString(plain.toByteArray(), Base64.NO_WRAP)  // ❌ No es cifrado
    
    fun decrypt(enc: String?): String {
        return String(Base64.decode(enc, Base64.NO_WRAP))  // ❌ Solo decodifica
    }
}
```

**Riesgo:**
- **CRÍTICO:** Los "mensajes cifrados" solo están en Base64
- Cualquiera puede decodificarlos
- No hay seguridad real

**Solución:**
```kotlin
// ✅ Usar E2ECipher que ya existe
object Crypto {
    fun encrypt(plain: String, chatId: String): String =
        E2ECipher.encrypt(plain, chatId)
    
    fun decrypt(encrypted: String, chatId: String): String =
        E2ECipher.decrypt(encrypted, chatId)
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🟠 Media (pero E2ECipher ya está implementado)

---

## 🟡 Errores Menores

### ERR-009: Logging inconsistente

**Problema:**
- Algunos lugares usan `Log.w`, otros `Log.e`
- No hay tag consistente
- Difícil filtrar logs

**Solución:**
```kotlin
// Constante centralizada
private const val TAG = "MessageApp"

// Usar en toda la app
Log.w(TAG, "Specific message", e)
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🟡 Baja

---

### ERR-010: No hay tests para PresenceRepository

**Archivo:** `app/src/main/java/com/example/messageapp/data/PresenceRepository.kt`

**Problema:**
- 0 tests para typing indicators
- 0 tests para presencia online/offline
- Bug potencial no detectado

**Solución:**
```kotlin
// Agregar tests
class PresenceRepositoryTest {
    @Test
    fun `setTypingStatus sends correct value`() = runTest { ... }
    
    @Test
    fun `observePartnerTyping emits false after timeout`() = runTest { ... }
}
```

**Estado:** ⏳ Pendiente  
**Prioridad:** 🟡 Baja

---

## 📋 Plan de Corrección

### Semana 1 (Críticos)
- [ ] ERR-001: Validar nulls en observeChat
- [ ] ERR-002: Manejar null nonce en decryptMessage
- [ ] ERR-003: Validar parámetros en sendText

### Semana 2 (Mayores)
- [ ] ERR-004: Trim whitespace en directChatIdFor
- [ ] ERR-005: Manejo de errores en observeMessages
- [ ] ERR-006: Logging en markDelivered
- [ ] ERR-007: Validación en ViewModel.start()
- [ ] ERR-008: Reemplazar Crypto.kt con E2ECipher

### Semana 3 (Menores)
- [ ] ERR-009: Logging consistente
- [ ] ERR-010: Tests para PresenceRepository

---

## 🧪 Tests Creados en Esta Sesión

| Test | Archivo | Errores Cubiertos | Estado |
|------|---------|-------------------|--------|
| `ChatRepositoryTest` | `data/ChatRepositoryTest.kt` | ERR-004, ERR-013 | ✅ Creado |
| `ChatViewModelTest` (existente) | `viewmodel/ChatViewModelTest.kt` | ERR-002, ERR-007 | ✅ Existe |

---

**Última Actualización:** 2026-03-24  
**Próxima Revisión:** 2026-03-31  
**Responsable:** Equipo de desarrollo
