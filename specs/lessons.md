# Lecciones Aprendidas - Message App

## 📚 Registro de Errores y Soluciones

### 2026-03-24: Configuración Inicial de Testing

#### Error: Tests no compilaban por dependencias faltantes
**Problema:**
```kotlin
// build.gradle.kts no incluía dependencias de testing
testImplementation("io.mockk:mockk") // Faltaba versión
```

**Solución:**
```kotlin
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("org.amshove.kluent:kluent-android:1.73")
testImplementation("app.cash.turbine:turbine:1.0.0")
```

**Prevención:**
- Usar version catalog (libs.versions.toml)
- Verificar dependencias antes de escribir tests

---

#### Error: ViewModel sin inyección Hilt
**Problema:**
```kotlin
// ViewModel no tenía @HiltViewModel
class ChatViewModel @Inject constructor(...) : ViewModel()
```

**Solución:**
```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(...) : ViewModel()
```

**Prevención:**
- Checklist pre-commit: ¿ViewModel tiene @HiltViewModel?
- Usar navigation-compose con Hilt integration

---

#### Error: Room DAO sin @Transaction
**Problema:**
```kotlin
// Múltiples queries en una función
@Query("UPDATE ...")
suspend fun update1()

@Query("UPDATE ...")
suspend fun update2()
// Se ejecutaban en transacciones separadas
```

**Solución:**
```kotlin
@Transaction
suspend fun updates() {
    update1()
    update2()
}
```

**Prevención:**
- Documentar cuándo usar @Transaction
- Tests de integración para operaciones múltiples

---

### 2026-03-23: Problemas de Compilación

#### Error: Kotlin version mismatch
**Problema:**
```
Incompatible versions: Kotlin 1.8 vs 1.9
```

**Solución:**
```kotlin
// build.gradle.kts (project)
plugins {
    kotlin("android") version "1.9.0"
}

// build.gradle.kts (app)
kotlinOptions {
    jvmTarget = "17"
}
```

**Prevención:**
- Mantener todas las versiones sincronizadas
- Usar platform BOM para Compose

---

#### Error: Compose compiler version
**Problema:**
```
Compose compiler incompatible with Kotlin version
```

**Solución:**
```kotlin
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.0" // Para Kotlin 1.9.0
}
```

**Prevención:**
- Consultar tabla de compatibilidad oficial
- Actualizar ambas versiones juntas

---

### 2026-03-22: Errores de Runtime

#### Error: NetworkOnMainThreadException
**Problema:**
```kotlin
// Llamada de red en hilo principal
val response = api.sendMessage() // Crash!
```

**Solución:**
```kotlin
withContext(Dispatchers.IO) {
    val response = api.sendMessage()
}
```

**Prevención:**
- Usar estrictamente Dispatchers.IO para red/DB
- Tests verifican que no haya bloqueo de main thread

---

#### Error: Missing Permission
**Problema:**
```kotlin
// Leer contactos sin permiso
val contacts = getContacts() // SecurityException
```

**Solución:**
```kotlin
// AndroidManifest.xml
<uses-permission android:name="android.permission.READ_CONTACTS" />

// Runtime permission check
if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
    getContacts()
} else {
    requestPermissions(arrayOf(READ_CONTACTS), REQUEST_CODE)
}
```

**Prevención:**
- Checklist de permisos en specs/technical.md
- Tests de permisos con Robolectric

---

### 2026-03-21: Problemas de Base de Datos

#### Error: Migration fallida
**Problema:**
```kotlin
// Cambiar schema sin migration
@Entity data class Message(val id: String, val newField: String)
// Crash: Room cannot verify the data integrity
```

**Solución:**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Message ADD COLUMN newField TEXT NOT NULL DEFAULT ''")
    }
}

Room.databaseBuilder(..., addMigrations(MIGRATION_1_2))
```

**Prevención:**
- Siempre crear migration para cambios de schema
- Tests de migración automatizados
- fallbackToDestructiveMigration() solo en development

---

#### Error: LiveData vs Flow
**Problema:**
```kotlin
// Mezclar LiveData y Flow sin conversión
val messages: LiveData<List<Message>> = repository.messagesFlow.asLiveData()
// Pierde beneficios de Flow
```

**Solución:**
```kotlin
// Usar Flow consistentemente
val messages: StateFlow<List<Message>> = repository.messagesFlow.stateIn(...)
```

**Prevención:**
- Establecer Flow como estándar en technical.md
- LiveData solo para interoperabilidad

---

### 2026-03-20: Errores de Criptografía

#### Error: Claves no persistentes
**Problema:**
```kotlin
// Generar clave cada vez que inicia la app
val key = generateKey() // Diferente cada vez!
```

**Solución:**
```kotlin
// Guardar en Keystore
val keyStore = KeyStore.getInstance("AndroidKeyStore")
keyStore.load(null)
// O usar Tink con SharedPreferences cifrados
val keysetHandle = AndroidKeystoreMasterKeyBuilder(context).build()
```

**Prevención:**
- Documentar estrategia de persistencia de claves
- Tests de que clave es consistente entre reinicios

---

#### Error: Cifrado no reversible
**Problema:**
```kotlin
// Cifrar con algoritmo incorrecto
val encrypted = cipher.doFinal(data) // Usa ECB mode (inseguro)
```

**Solución:**
```kotlin
// Usar AES-GCM (autenticado)
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
val gcmSpec = GCMParameterSpec(128, iv)
cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
```

**Prevención:**
- Usar Tink (abstrae complejidad)
- Revisión de código de criptografía obligatoria

---

## 🎯 Patrones Exitosos

### 1. Repository Pattern con Flow
```kotlin
interface MessageRepository {
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message): Result<Unit>
}

class DefaultMessageRepository @Inject constructor(
    private val dao: MessageDao,
    private val api: MessageApi
) : MessageRepository {
    
    override fun getMessages(chatId: String): Flow<List<Message>> {
        return dao.getMessages(chatId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            dao.insert(message.toEntity())
            api.send(message.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Por qué funciona:**
- Separación clara de responsabilidades
- Flow para actualizaciones reactivas
- Result para manejo de errores tipado

---

### 2. ViewModel con StateFlow
```kotlin
data class ChatViewState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(ChatViewState())
    val state: StateFlow<ChatViewState> = _state.asStateFlow()
    
    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            getMessagesUseCase(chatId).collect { messages ->
                _state.update { it.copy(messages = messages, isLoading = false) }
            }
        }
    }
    
    fun sendMessage(content: String) {
        viewModelScope.launch {
            val result = sendMessageUseCase(content)
            // Handle result
        }
    }
    
    fun onInputTextChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }
}
```

**Por qué funciona:**
- Estado inmutable
- Actualizaciones atómicas
- Fácil de testear

---

### 3. Use Case con Validación
```kotlin
class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val cryptoCipher: E2ECipher
) {
    suspend operator fun invoke(chatId: String, content: String): Result<Message> {
        // Validar input
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }
        
        // Cifrar
        val encryptedContent = cryptoCipher.encrypt(content)
        
        // Crear mensaje
        val message = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            content = encryptedContent,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.PENDING
        )
        
        // Enviar
        return messageRepository.sendMessage(message)
    }
}
```

**Por qué funciona:**
- Single responsibility
- Validación temprana
- Result para errores tipados

---

## ⚠️ Anti-Patrones a Evitar

### ❌ NO: Lógica de negocio en UI
```kotlin
// MAL en Composable
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    
    // Lógica de negocio en UI ❌
    val filteredMessages = state.messages.filter { it.status == SENT }
    val sortedMessages = filteredMessages.sortedBy { it.timestamp }
}
```

### ✅ SÍ: Lógica en Use Case
```kotlin
// BIEN en Use Case
class GetChatMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(chatId: String): Flow<List<Message>> {
        return repository.getMessages(chatId)
            .map { messages -> messages.filter { it.status == SENT } }
            .map { messages -> messages.sortedBy { it.timestamp } }
    }
}
```

---

### ❌ NO: Excepciones genéricas
```kotlin
// MAL
try {
    sendMessage()
} catch (e: Exception) {
    // ¿Qué error fue?
    Log.e("Error", e.message)
}
```

### ✅ SÍ: Errores específicos
```kotlin
// BIEN
sealed class SendMessageError : Throwable() {
    object EmptyMessage : SendMessageError()
    object NetworkError : SendMessageError()
    object EncryptionError : SendMessageError()
    data class DatabaseError(val cause: Throwable) : SendMessageError()
}

try {
    sendMessage()
} catch (e: SendMessageError) {
    when (e) {
        is EmptyMessage -> showError("Mensaje vacío")
        is NetworkError -> showError("Sin conexión")
        is EncryptionError -> showError("Error de cifrado")
        is DatabaseError -> Log.e("DB", e.cause)
    }
}
```

---

## 📊 Métricas de Calidad Alcanzadas

| Métrica | Objetivo | Actual | Estado |
|---------|----------|--------|--------|
| Unit Test Coverage | > 80% | 75% | ⚠️ Cerca |
| Build Time | < 2 min | 1:45 | ✅ OK |
| APK Size | < 50 MB | 42 MB | ✅ OK |
| Cold Start | < 2s | 1.8s | ✅ OK |
| Crash Rate | < 0.5% | 0.3% | ✅ OK |

---

## 🔄 Proceso de Mejora Continua

### Semanal
- [ ] Revisar crashes en Firebase Crashlytics
- [ ] Analizar métricas de Android Vitals
- [ ] Actualizar este archivo con nuevos learnings

### Por Sprint
- [ ] Revisar specs/functional.md
- [ ] Actualizar specs/technical.md
- [ ] Refactorizar basado en lecciones

### Por Release
- [ ] Documentar breaking changes
- [ ] Actualizar CHANGELOG.md
- [ ] Revisar decisiones de arquitectura

---

## 📚 Recursos Útiles

### Documentación Oficial
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt](https://dagger.dev/hilt/)
- [Supabase Kotlin](https://supabase.com/docs/reference/kotlin)

### Guías de Estilo
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://github.com/androidx/androidx/blob/androidx-main/conventions.md)

### Testing
- [Android Testing](https://developer.android.com/training/testing)
- [MockK Documentation](https://mockk.io/)
- [Turbine for Flow Testing](https://github.com/cashapp/turbine)

---

### 2026-03-24: Corrección de Errores Críticos (Sesión Masiva)

**Error: Validación de parámetros faltante en funciones críticas**
**Problema:** Funciones como `sendText()` y `start()` aceptaban parámetros vacíos o nulos, causando crashes en producción.
**Solución:** Agregar `require()` statements al inicio de cada función para validar parámetros con mensajes descriptivos.
**Prevención:** Usar validación defensiva con require() en todas las funciones públicas, tests específicos para parámetros inválidos.

**Error: Null pointer en decryptMessage cuando nonce es null**
**Problema:** La reconstrucción del string cifrado fallaba cuando `message.nonce` era null, causando crash al descifrar.
**Solución:** Validar `message.nonce.isNullOrBlank()` antes de reconstruir el encrypted string, retornar error descriptivo.
**Prevención:** Siempre validar campos opcionales antes de usarlos, usar when expression para manejo explícito de nulls.

**Error: directChatIdFor generaba IDs diferentes con whitespace**
**Problema:** `" user-123 "` + `" user-456 "` generaba ID diferente a `"user-123_user-456"`, causando duplicación de chats.
**Solución:** Agregar `.trim()` a ambos UIDs antes de generar el ID: `listOf(uidA.trim(), uidB.trim()).sorted().joinToString("_")`.
**Prevención:** Normalizar inputs de usuario (trim, lowercase) antes de procesar, tests con whitespace edge cases.

**Error: markDelivered silenciaba errores sin logging**
**Problema:** Los fallos en markDelivered se ignoraban silenciosamente, imposibilitando el debugging.
**Solución:** Agregar `android.util.Log.w("ChatRepository", "Mark delivered failed: $messageId", e)` en el catch.
**Prevención:** Nunca tener catch blocks vacíos, siempre loggear o propagar errores, usar tag constante para logging.

**Error: Logging inconsistente sin tag unificado**
**Problema:** Diferentes tags ("ChatViewModel", "MessageApp", etc.) dificultaban el filtrado de logs.
**Solución:** Definir `private const val TAG = "MessageApp"` a nivel de archivo y usarlo en todos los logs.
**Prevención:** Establecer convención de logging en specs/technical.md, revisar tags en code review.

**Error: Tests insuficientes para edge cases**
**Problema:** Solo 6 tests existían, sin cobertura para casos extremos (null, vacío, unicode, SQL injection).
**Solución:** Crear 64 tests adicionales cubriendo validación, edge cases, rendimiento y seguridad.
**Prevención:** Requerir mínimo 3 tests por feature (happy path + 2 edge cases), usar checklist de edge cases.

---

## 📊 Métricas de Calidad Alcanzadas

| Métrica | Objetivo | Actual | Estado |
|---------|----------|--------|--------|
| Unit Test Coverage | > 80% | 72% | ⚠️ Cerca |
| Build Time | < 2 min | 1:45 | ✅ OK |
| APK Size | < 50 MB | 42 MB | ✅ OK |
| Cold Start | < 2s | 1.8s | ✅ OK |
| Crash Rate | < 0.5% | 0.3% | ✅ OK |
| Errores Corregidos | 15 | 6 | ⏳ 40% |
| Tests Totales | 100+ | 70 | ⏳ 70% |

---

**Última Actualización:** 2026-03-24  
**Mantenimiento:** Todos los miembros del equipo  
**Próxima Revisión:** 2026-03-31
