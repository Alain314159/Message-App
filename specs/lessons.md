# Lecciones Aprendidas - Message App

## 📚 Registro de Errores y Soluciones

### 2026-03-28: Corrección de Catch Blocks y Logging

#### Error: Catch block vacío en ChatRepository.createDirectChat
**Problema:**
```kotlin
// ❌ ANTES: Catch block vacío sin logging
} catch (e: Exception) {
    // Chat no existe, crear nuevo
}
```

**Solución:**
```kotlin
// ✅ DESPUÉS: Logging con Log.w y TAG constante
} catch (e: Exception) {
    // Chat no existe o error al verificar, crear nuevo
    Log.w(TAG, "ChatRepository: Chat $chatId no existe o error al verificar, creando nuevo", e)
}
```

**Impacto:** Sin logging era imposible debuggear errores de creación de chats en producción.

**Prevención:**
- Nunca tener catch blocks vacíos o solo con comentarios
- Siempre loggear con TAG constante y nivel apropiado (Log.w para warnings, Log.e para errores)
- Usar silent-failure-hunter agent para detectar catch blocks silenciosos

---

#### Error: Logging inconsistente con tags diferentes
**Problema:**
```kotlin
// ❌ ANTES: Tag hardcodeado diferente al TAG constante
android.util.Log.w("ChatViewModel", "Send message failed", e)
Log.w(TAG, "Otro mensaje") // TAG = "MessageApp"
```

**Solución:**
```kotlin
// ✅ DESPUÉS: Usar siempre el TAG constante del archivo
Log.w(TAG, "ChatViewModel: Send message failed", e)
```

**Impacto:** Dificultaba el filtrado de logs en Logcat al tener múltiples tags para el mismo archivo.

**Prevención:**
- Definir `private const val TAG = "MessageApp"` a nivel de archivo
- Usar prefijos en el mensaje para identificar el componente: `"ChatViewModel: mensaje"`
- Revisar en code review que todos los logs usen el TAG constante

---

#### Error: Catch block ignora excepción con underscore en Crypto
**Problema:**
```kotlin
// ❌ ANTES: Ignora completamente la excepción
} catch (_: Throwable) {
    s
}
```

**Solución:**
```kotlin
// ✅ DESPUÉS: Loggea la excepción antes de retornar fallback
} catch (e: Throwable) {
    Log.w(TAG, "Crypto: Falló decodificación Base64, retornando original", e)
    s
}
```

**Impacto:** Imposible diagnosticar problemas de decodificación Base64 en producción.

**Prevención:**
- Nunca usar underscore (_) en catch blocks a menos que sea intencional y documentado
- Siempre loggear excepciones incluso si se hace fallback
- Documentar por qué se ignora la excepción cuando sea necesario

---

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

---

### 2026-03-26: Firebase Migration Fix - ChatScreen.kt

**Error: Referencias a Firebase FirebaseAuth en ChatScreen**
**Problema:** El archivo `ChatScreen.kt` tenía una referencia directa a `FirebaseAuth.getInstance().currentUser?.uid` que causaba crash en runtime porque Firebase fue migrado a Supabase.

**Código problemático:**
```kotlin
// ❌ ANTES: Firebase (ya no existe en el proyecto)
val myUid = remember { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
```

**Solución aplicada:**
```kotlin
// ✅ DESPUÉS: Supabase Auth
val myUid = remember { SupabaseConfig.client.auth.currentUserOrNull()?.id?.value.orEmpty() }
```

**Impacto:** Crash inmediato al abrir cualquier chat si Firebase estaba completamente removido del proyecto.

**Prevención:**
- Usar search global antes de eliminar dependencias: `grep -r "FirebaseAuth" app/src/`
- Actualizar todos los archivos en la misma sesión de migración
- Tests de compilación después de remover dependencias

---

**Error: Nombres de métodos incorrectos en ViewModel y Repository**
**Problema:** ChatScreen.kt llamaba métodos que no existían o tenían nombres diferentes en ChatViewModel y ChatRepository.

**Métodos incorrectos encontrados:**
```kotlin
// ❌ ANTES: Nombres incorrectos
vm.markRead(chatId, myUid)           // No existía
vm.unpin(chatId)                     // No existía
vm.pin(chatId, selected!!)           // No existía
repo.hideMessageForUser(...)         // No existía
repo.deleteMessageForAll(...)        // Verificar existe
StorageAcl.ensureMemberMarker(...)   // Firebase Storage (no existe)
```

**Nombres correctos verificados:**
```kotlin
// ✅ DESPUÉS: Nombres correctos (verificados en ChatViewModel.kt y ChatRepository.kt)
vm.markAsRead(chatId, myUid)         // ✅ Existe en ChatViewModel
vm.unpinMessage(chatId)              // ✅ Existe en ChatViewModel
vm.pinMessage(chatId, selected!!)    // ✅ Existe en ChatViewModel
repo.deleteMessageForUser(...)       // ✅ Existe en ChatRepository
repo.deleteMessageForAll(...)        // ✅ Existe en ChatRepository
// StorageAcl eliminado (pendiente migración a Supabase Storage)
```

**Impacto:** Errores de compilación que impedían build de la app.

**Prevención:**
- Usar IDE autocomplete para verificar nombres de métodos
- Tests de compilación después de refactorizaciones
- Documentar API pública de ViewModels en specs/technical.md

---

**Error: Imports faltantes y estructura del archivo**
**Problema:** El archivo tenía imports incompletos después de la migración.

**Imports agregados:**
```kotlin
import androidx.compose.runtime.LaunchedEffect  // Faltaba
import androidx.compose.ui.unit.dp               // Faltaba para padding
import com.example.messageapp.supabase.SupabaseConfig // Faltaba
// Eliminado: import com.google.firebase.auth.FirebaseAuth
// Eliminado: import com.example.messageapp.storage.StorageAcl
```

**TAG de logging agregado:**
```kotlin
// ✅ TAG constante para logging consistente (siguiendo lessons aprendidas)
private const val TAG = "MessageApp"
```

**Prevención:**
- Usar "Optimize Imports" del IDE antes de commit
- Agregar TAG constante como regla en code review checklist
- Verificar imports después de migraciones grandes

---

**Lección General: Migraciones requieren búsqueda exhaustiva**

**Proceso recomendado para migraciones:**

1. **Antes de eliminar dependencia:**
   ```bash
   # Buscar TODAS las referencias
   grep -r "FirebaseAuth" app/src/main/
   grep -r "FirebaseFirestore" app/src/main/
   grep -r "StorageAcl" app/src/main/
   ```

2. **Reemplazar en todos los archivos:**
   - No dejar referencias parciales
   - Actualizar imports
   - Verificar compilación

3. **Tests de verificación:**
   - Compilación: `./gradlew assembleDebug`
   - Tests unitarios: `./gradlew test`
   - Navegar por la app manualmente

4. **Documentar en lessons.md:**
   - Qué se migró
   - Errores encontrados
   - Cómo prevenir en el futuro

---

### 2026-03-26: Test Quality Review - Lecciones Aprendidas

**Problema: Tests de repositorios sin mocks dependen de servicios externos**
**Descubrimiento:** Durante el test quality review del 2026-03-26, se encontró que `AuthRepositoryTest.kt` y otros tests de repositorio no usaban mocks para Supabase, haciendo que los tests dependieran de:
1. Conexión a internet real
2. Credenciales válidas en Supabase
3. Servicios externos disponibles
4. Rate limiting no aplicado

**Impacto:**
- Tests frágiles que fallan sin razón aparente
- Falsa sensación de seguridad (test pasa pero no verifica comportamiento)
- Imposible ejecutar tests en CI/CD sin configuración compleja
- Tests lentos (llamadas de red reales)

**Solución:**
```kotlin
// ❌ ANTES: Sin mocks
@Before
fun setup() {
    repository = AuthRepository() // Usa Supabase real
}

// ✅ DESPUÉS: Con mocks
@Before
fun setup() {
    val mockAuthClient = mockk<SupabaseAuthClient>()
    coEvery { mockAuthClient.signInWithEmail(any(), any()) } 
        returns Result.success(mockUser)
    repository = AuthRepository(authClient = mockAuthClient)
}

@Test
fun `signInWithEmail calls auth client with correct parameters`() = runTest {
    // Arrange
    val email = "test@example.com"
    val password = "password123"
    
    // Act
    repository.signInWithEmail(email, password)
    
    // Assert
    coVerify(exactly = 1) { 
        mockAuthClient.signInWithEmail(email, password) 
    }
}
```

**Prevención:**
- Checklist pre-test: "¿Tiene mocks para TODAS las dependencias externas?"
- Regla: Repository tests nunca deben importar clases de Supabase directamente
- Usar dependency injection para facilitar mocking
- Tests deben ejecutar en < 10ms (sin red/DB real)

---

**Problema: Tests que solo verifican "no crash" no aportan valor**
**Descubrimiento:** Múltiples tests en `AuthRepositoryTest.kt` solo verifican `assertThat(result.exceptionOrNull()).isNull()` sin verificar comportamiento real.

**Ejemplos encontrados:**
```kotlin
// ❌ MAL: Test no verifica comportamiento
@Test
fun `signOut does not throw exception`() = runTest {
    val result = runCatching { repository.signOut() }
    assertThat(result.exceptionOrNull()).isNull()
    // ¿Pero signOut qué debería hacer? ¿Limpia estado? ¿Notifica?
}

// ✅ BIEN: Test verifica comportamiento observable
@Test
fun `signOut clears user state and emits update`() = runTest {
    // Arrange
    val stateCaptor = slot<StateFlow<User?>>()
    coEvery { mockAuthClient.signOut() } returns Result.success(Unit)
    
    // Act
    repository.signOut()
    
    // Assert
    coVerify { mockAuthClient.signOut() }
    // Verificar que el estado se actualiza
    assertThat(repository.currentUser.value).isNull()
}
```

**Impacto:**
- Tests que pasan pero no garantizan que el código funcione
- Bugs se escapan a producción
- Developers pierden confianza en tests

**Prevención:**
- Regla: Cada test debe verificar AL MENOS una interacción o cambio de estado
- Preguntar: "¿Qué comportamiento estoy verificando?" antes de escribir el test
- Usar verbs descriptivos en nombres: `shouldUpdateState`, `shouldCallRepository`, `shouldEmitError`

---

**Problema: Faltan tests para error handling específico**
**Descubrimiento:** Solo 15% de los tests cubren escenarios de error específicos (NetworkError, AuthException, etc.)

**Tests faltantes críticos:**
1. Auth: SSLHandshakeException, NetworkTimeoutException, InvalidCredentialsException
2. Chat: DatabaseFullException, ConflictException, MessageTooLargeException
3. Crypto: KeyCorruptionException, InvalidKeyException, DecryptionFailedException
4. ViewModel: Error state propagation to UI, Retry logic

**Solución - Plantilla para tests de error:**
```kotlin
@Test
fun `signInWithEmail when network timeout returns NetworkError`() = runTest {
    // Arrange
    coEvery { mockAuthClient.signInWithEmail(any(), any()) }
        returns Result.failure(SocketTimeoutException("timeout"))
    
    // Act
    val result = repository.signInWithEmail("test@example.com", "password123")
    
    // Assert
    assertThat(result).isInstanceOf<Result.Error>()
    assertThat((result as Result.Error).exception).isInstanceOf<SocketTimeoutException>()
}

@Test
fun `signInWithEmail when invalid credentials throws AuthException`() = runTest {
    // Arrange
    coEvery { mockAuthClient.signInWithEmail(any(), any()) }
        returns Result.failure(AuthException("Invalid credentials", 401))
    
    // Act
    val result = repository.signInWithEmail("test@example.com", "wrongpassword")
    
    // Assert
    assertThat(result.exceptionOrNull()).isInstanceOf<AuthException>()
    assertThat(result.exceptionOrNull()?.message).contains("Invalid credentials")
}
```

**Prevención:**
- Regla: Por cada test de happy path, crear al menos 2 tests de error
- Usar sealed classes para errores tipados
- Tests deben verificar que el error correcto se propaga al usuario

---

**Problema: Test documenta bug pero no verifica fix**
**Descubrimiento:** `ChatRepositoryTest.kt` tiene test `directChatIdFor trims whitespace BUG` que documenta un bug pero no verifica que se haya corregido.

```kotlin
@Test
fun `directChatIdFor trims whitespace BUG`() {
    // Given: User IDs con whitespace
    val uidA = " user-123 "
    val uidB = " user-456 "
    
    // When: Genero ID (sin trim en la implementación actual)
    val chatId = repository.directChatIdFor(uidA, uidB)
    
    // Then: Whitespace se incluye (ERROR POTENCIAL)
    assertThat(chatId).contains(" ") // ❌ Esto documenta el bug, no verifica el fix
}
```

**Solución:**
```kotlin
@Test
fun `directChatIdFor trims whitespace from UIDs`() {
    // Given: User IDs con whitespace
    val uidA = " user-123 "
    val uidB = " user-456 "
    
    // When: Genero ID
    val chatId = repository.directChatIdFor(uidA, uidB)
    
    // Then: Debería trimear whitespace
    assertThat(chatId).isEqualTo("user-123_user-456")
    assertThat(chatId).doesNotContain(" ")
}
```

**Prevención:**
- Tests siempre deben verificar el comportamiento CORRECTO, no el bug
- Si existe un bug, crear test que falle primero (TDD)
- Después de fix, el test verifica el fix, no el bug

---

**Lección General: E2ECipherTest es el gold standard**
**Observación:** `E2ECipherTest.kt` (score 95/100) demuestra cómo escribir tests excelentes:

**Características destacadas:**
1. ✅ Setup/teardown con @Before/@After para limpieza
2. ✅ 28 tests cubriendo todos los edge cases
3. ✅ Tests de integración: encrypt → decrypt
4. ✅ Tests de seguridad: tampered ciphertext, wrong key
5. ✅ Tests de performance: 100 mensajes en < 5s
6. ✅ Nombres descriptivos
7. ✅ AAA pattern consistente

**Ejemplo para replicar en otros tests:**
```kotlin
@Before
fun setup() {
    // Limpieza antes de cada test
    E2ECipher.deleteAllKeys()
}

@After
fun teardown() {
    // Limpieza después de cada test
    E2ECipher.deleteAllKeys()
}

@Test
fun `encrypt then decrypt returns original plaintext`() {
    // Given: Plaintext original
    val plaintext = "Original Message"
    val chatId = "chat-integration"
    
    // When: Cifro y descifro
    val encrypted = E2ECipher.encrypt(plaintext, chatId)
    val decrypted = E2ECipher.decrypt(encrypted, chatId)
    
    // Then: Debería retornar plaintext original
    assertThat(decrypted).isEqualTo(plaintext)
}
```

**Aplicar patrón a:**
- `AuthRepositoryTest`: Agregar @Before/@After para limpiar estado
- `ChatRepositoryTest`: Agregar tests de integración
- `ViewModelTests`: Agregar tests de seguridad (error handling)

---

**Regla de Memoria Actualizada: Mínimo 4 Tests por Feature**

Cada feature nueva DEBE tener:

```kotlin
// 1. Happy Path (1 test)
@Test
fun `sendMessage with valid data succeeds`() { }

// 2. Edge Cases (2+ tests)
@Test
fun `sendMessage with empty text throws IllegalArgumentException`() { }

@Test
fun `sendMessage with unicode text succeeds`() { }

// 3. Error Handling (1+ tests)
@Test
fun `sendMessage when network fails returns Error state`() { }

// 4. Null/Empty Cases (1+ tests)
@Test
fun `sendMessage with null chatId throws IllegalArgumentException`() { }
```

**Total mínimo:** 5 tests por función pública crítica

---

**Checklist de Calidad de Tests (Pre-Commit)**

Antes de hacer commit, verificar:

**Estructura:**
- [ ] Sigue patrón AAA (Arrange-Act-Assert)
- [ ] Nombre descriptivo: `function_state_expected`
- [ ] Un comportamiento por test
- [ ] Tests independientes (no dependen de orden)
- [ ] Tiene @Before/@After si necesita setup/teardown

**Cobertura:**
- [ ] Happy path cubierto (1 test)
- [ ] Edge cases cubiertos (2+ tests: null, empty, boundary)
- [ ] Error handling cubierto (1+ tests)
- [ ] Null/empty cases cubiertos (1+ tests)

**Calidad:**
- [ ] Usa mocks para dependencias externas
- [ ] Testea comportamiento, no implementación
- [ ] Assertions claros y específicos
- [ ] No hay `Thread.sleep()` (usar coroutine test dispatchers)
- [ ] Verifica interacciones con `coVerify` cuando aplica

**Coroutines/Flows:**
- [ ] Usa `runTest` para funciones suspend
- [ ] Usa `StandardTestDispatcher` para control
- [ ] Usa Turbine para testear Flows
- [ ] No hay `advanceUntilIdle()` sin propósito claro

**Performance:**
- [ ] Test ejecuta en < 500ms
- [ ] No hay I/O real (disk, network)
- [ ] Mocks configurados correctamente

**Documentación:**
- [ ] KDoc con descripción del test
- [ ] Referencia a user story si aplica
- [ ] Comentarios para edge cases complejos

---

**Métricas de Calidad de Tests (Objetivos)**

| Métrica | Objetivo | Actual (2026-03-26) | Estado |
|---------|----------|---------------------|--------|
| Tests Totales | 100+ | 70 | ⚠️ 70% |
| Cobertura | > 80% | 72% | ⚠️ Cerca |
| Tests con Mocks | 100% | 60% | ❌ Crítico |
| Tests AAA Pattern | 100% | 85% | ⚠️ Mejorable |
| Tests < 500ms | 100% | 95% | ✅ OK |
| Tests Flaky | 0 | 2 | ⚠️ Investigar |

**Plan de Acción:**
1. Semana 1 (2026-03-26 a 2026-04-02): Mockear todos los repository tests
2. Semana 2 (2026-04-02 a 2026-04-09): Agregar 30 tests faltantes (ALTA prioridad)
3. Semana 3 (2026-04-09 a 2026-04-16): Alcanzar 80% cobertura
4. Semana 4 (2026-04-16 a 2026-04-23): Refactorizar tests lentos

---

### 2026-03-26: Kotlin Best Practices - Guía Completa

**Fuente:** Documentación Oficial de Kotlin (Android Developers)  
**Aplicación:** Inmediata y Obligatoria para todo el código nuevo

**Principios Fundamentales de Kotlin:**

1. **Concisión**: Escribir menos código para la misma funcionalidad
2. **Seguridad**: Null safety por defecto, inmutabilidad preferida
3. **Expresividad**: Código que se lee como lenguaje natural
4. **Interoperabilidad**: 100% compatible con Java

**Reglas de Oro Extraídas:**

### Variables

**Regla:** Empezar con `val`. Solo usar `var` si el compilador exige reasignación.

```kotlin
// ✅ CORRECTO
val userName = "John"  // Inmutable por defecto
var currentIndex = 0   // Solo cuando reasignación es necesaria

// ❌ INCORRECTO
var userName = "John"  // ¿Por qué es mutable?
```

### Null Safety

**Regla:** Nunca usar `!!` a menos que sea absolutamente necesario.

```kotlin
// ✅ CORRECTO
val displayName = user.name ?: "Guest"
user.email?.let { sendEmail(it) }

// ❌ INCORRECTO
val length = user.name!!.length  // Riesgo de NullPointerException
```

### Condicionales

**Regla:** Usar `when` para 3+ casos, `if` como expresión para simples.

```kotlin
// ✅ CORRECTO
val grade = when (score) {
    in 90..100 -> "A"
    in 80..89 -> "B"
    else -> "F"
}

val max = if (a > b) a else b

// ❌ INCORRECTO
if (userType == UserType.ADMIN) {
    // ...
} else if (userType == UserType.PREMIUM) {
    // ...
} else if (userType == UserType.FREE) {
    // ...
}
```

### Funciones

**Regla:** Usar expresión única (`=`) para funciones simples.

```kotlin
// ✅ CORRECTO
fun isEmpty() = size == 0
fun double(x: Int) = x * 2

// ❌ INCORRECTO
fun isEmpty(): Boolean {
    return size == 0
}
```

### Clases

**Regla:** Propiedades en constructor primario, no en el cuerpo.

```kotlin
// ✅ CORRECTO
class User(
    val id: String,
    val name: String,
    val email: String
)

// ❌ INCORRECTO
class User {
    val id: String = ""  // Debería estar en constructor
    var name: String = ""  // Debería ser val
}
```

### Colecciones

**Regla:** Usar funciones de biblioteca estándar, no loops manuales.

```kotlin
// ✅ CORRECTO
val positives = numbers.filter { it > 0 }.map { it * 2 }
val first = users.firstOrNull()

// ❌ INCORRECTO
val positives = mutableListOf<Int>()
for (n in numbers) {
    if (n > 0) positives.add(n)
}
```

### Visibilidad

**Regla:** Usar el modificador más restrictivo posible.

```kotlin
// ✅ CORRECTO
class UserRepository {
    private val api: UserApi  // Solo visible dentro de la clase
    internal val cache: Map  // Visible en el módulo
    public fun getUser()  // API pública explícita
}

// ❌ INCORRECTO
class UserRepository {
    val api: UserApi  // ¿Por qué es público?
}
```

### Nombres

**Regla:** camelCase para variables/funciones, PascalCase para clases.

```kotlin
// ✅ CORRECTO
val userName = "John"
fun calculateTotalPrice() { }
class UserProfile { }
const val MAX_RETRY = 3

// ❌ INCORRECTO
val UserName = "John"      // Debería ser userName
val user_name = "John"     // Debería ser userName
class userProfile { }      // Debería ser UserProfile
```

### Idiomatic Kotlin

**Regla:** Usar características de Kotlin, no escribir Java en Kotlin.

```kotlin
// ✅ CORRECTO
for (item in list) { }
if (text.isNullOrEmpty()) { }
user?.let { process(it) }

// ❌ INCORRECTO
for (i in 0 until list.size) {
    val item = list[i]
}
if (text == null || text.isEmpty()) { }
if (user != null) {
    process(user)
}
```

### Scope Functions

**Regla:** Usar scope functions apropiadamente para el caso de uso.

```kotlin
// ✅ CORRECTO
// apply: configurar objeto
val user = User().apply {
    name = "John"
    email = "john@example.com"
}

// let: operar en objeto no-nulo
user?.let {
    processUser(it)
}

// run: configurar y obtener resultado
val result = config.run {
    enabled = true
    build()
}

// also: efecto secundario
list.also {
    Log.d("TAG", "List: $it")
}

// takeIf/takeUnless: filtrado condicional
val even = number.takeIf { it % 2 == 0 }

// ❌ INCORRECTO
// Anidación excesiva de scope functions
user?.let { u ->
    u.apply {
        run {
            // Confusing nesting
        }
    }
}
```

### Extension Functions

**Regla:** Usar extension functions para lógica de dominio.

```kotlin
// ✅ CORRECTO
fun User.isEligibleForPremium(): Boolean {
    return age >= 18 && isActive && hasVerifiedEmail
}

fun String.isValidEmail(): Boolean {
    return contains(" @") && contains(".")
}

// ❌ INCORRECTO
// Utility classes al estilo Java
object UserUtils {
    fun isEligible(user: User): Boolean { }
}
```

### Data Classes

**Regla:** Usar data classes para modelos de datos.

```kotlin
// ✅ CORRECTO
data class User(
    val id: String,
    val name: String,
    val email: String
)

// ❌ INCORRECTO
// Clase con equals/hashCode/toString manuales
class User {
    val id: String
    val name: String
    
    override fun equals(other: Any?): Boolean { }
    override fun hashCode(): Int { }
    override fun toString(): String { }
}
```

### Sealed Classes

**Regla:** Usar sealed classes para jerarquías type-safe.

```kotlin
// ✅ CORRECTO
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val message: String) : Result()
}

when (result) {
    is Success -> handle(result.data)
    is Error -> handle(result.message)
}

// ❌ INCORRECTO
// Enums con datos (limitado)
enum class ResultType { SUCCESS, ERROR }
data class Result(val type: ResultType, val data: String?)

// When exhaustivo no posible
```

### Inline Functions

**Regla:** Usar inline para funciones con parámetros lambda.

```kotlin
// ✅ CORRECTO
inline fun <T> List<T>.forEach(action: (T) -> Unit) {
    for (element in this) action(element)
}

// ❌ INCORRECTO
// Lambda crea objeto innecesario
fun <T> List<T>.forEach(action: (T) -> Unit) {
    for (element in this) action(element)
}
```

### Destructuring

**Regla:** Usar destructuring cuando mejora legibilidad.

```kotlin
// ✅ CORRECTO
val (name, age) = user
for ((index, value) in list.withIndex()) { }

// ❌ INCORRECTO
// Destructuring innecesario
val user = getUser()
val name = user.component1()  // Mejor: user.name
val age = user.component2()   // Mejor: user.age
```

### Companion Object

**Regla:** Usar companion object para miembros de clase, no funciones utilitarias.

```kotlin
// ✅ CORRECTO
class User {
    companion object {
        fun createGuest(): User { }
    }
}

// Funciones utilitarias como extensiones
fun String.isValidEmail(): Boolean { }

// ❌ INCORRECTO
// Utility class al estilo Java
object UserUtils {
    fun createGuest(): User { }
    fun isValidEmail(email: String): Boolean { }
}
```

### Require y Assert

**Regla:** Usar require para validación de parámetros, assert para invariantes.

```kotlin
// ✅ CORRECTO
fun createUser(age: Int) {
    require(age >= 0) { "Age must be non-negative" }
    assert(age < 150) { "Age seems unrealistic" }
}

// ❌ INCORRECTO
fun createUser(age: Int) {
    if (age < 0) throw IllegalArgumentException("Age must be non-negative")
}
```

### Coroutines

**Regla:** Usar coroutine scopes apropiados para el contexto.

```kotlin
// ✅ CORRECTO
// ViewModelScope para ViewModels
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {
    fun loadMessages() {
        viewModelScope.launch {
            repository.getMessages().collect { }
        }
    }
}

// LifecycleScope para UI
lifecycleScope.launch {
    fetchData()
}

// CoroutineScope para operaciones independientes
val scope = CoroutineScope(Dispatchers.IO)
scope.launch {
    backgroundTask()
}

// ❌ INCORRECTO
// GlobalScope (puede causar leaks)
GlobalScope.launch {
    fetchData()
}

// CoroutineScope sin control de lifecycle
val scope = CoroutineScope(Dispatchers.Main)  // ¿Quién lo cancela?
```

### Flow

**Regla:** Usar Flow para streams reactivos, StateFlow para estado UI.

```kotlin
// ✅ CORRECTO
// StateFlow para estado UI
private val _uiState = MutableStateFlow<UiState>(Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Flow para streams de datos
fun getMessages(): Flow<List<Message>> {
    return repository.getMessages()
}

// ❌ INCORRECTO
// LiveData en código nuevo (usar StateFlow)
val uiState: LiveData<UiState> = MutableLiveData()

// MutableStateFlow expuesto (rompe inmutabilidad)
val uiState: MutableStateFlow<UiState> = MutableStateFlow()
```

### Result Type

**Regla:** Usar Result para manejo de errores tipado.

```kotlin
// ✅ CORRECTO
fun fetchData(): Result<Data> {
    return try {
        Result.success(api.getData())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

when (val result = fetchData()) {
    is Result.Success -> handle(result.data)
    is Result.Failure -> handle(result.exception)
}

// ❌ INCORRECTO
// Excepciones para control de flujo
fun fetchData(): Data {
    try {
        return api.getData()
    } catch (e: Exception) {
        throw e  // ¿Qué error fue?
    }
}
```

---

**Checklist de Código Kotlin (Pre-Commit)**

Antes de hacer commit, verificar:

**Variables:**
- [ ] ¿Usé `val` por defecto?
- [ ] ¿El `var` es realmente necesario?
- [ ] ¿Los tipos son inferidos cuando obvios?
- [ ] ¿Los nombres siguen camelCase/PascalCase?

**Null Safety:**
- [ ] ¿Evité `!!`?
- [ ] ¿Usé `?.` y `?:` apropiadamente?
- [ ] ¿Los tipos son no-nulos por defecto?

**Funciones:**
- [ ] ¿Las funciones simples usan expresión única (`=`)?
- [ ] ¿Los nombres son descriptivos?
- [ ] ¿Los parámetros opcionales tienen defaults?
- [ ] ¿Usé trailing lambda cuando aplica?

**Clases:**
- [ ] ¿Las propiedades están en el constructor?
- [ ] ¿Usé data class para modelos?
- [ ] ¿La visibilidad es la más restrictiva posible?
- [ ] ¿Custom getters/setters son necesarios?

**Colecciones:**
- [ ] ¿Usé funciones stdlib (filter, map, etc.)?
- [ ] ¿Evité loops manuales?
- [ ] ¿Usé firstOrNull en lugar de checks?

**Idiomático:**
- [ ] ¿El código parece Kotlin o Java?
- [ ] ¿Usé extension functions para lógica de dominio?
- [ ] ¿Usé scope functions apropiadamente?
- [ ] ¿Evité punto y coma?

**Coroutines/Flow:**
- [ ] ¿Usé el scope correcto (viewModelScope, lifecycleScope)?
- [ ] ¿StateFlow para estado UI?
- [ ] ¿Flow para streams de datos?
- [ ] ¿Evité GlobalScope?

**Errores:**
- [ ] ¿Usé Result para manejo de errores?
- [ ] ¿Usé require para validación de parámetros?
- [ ] ¿Las excepciones son para casos excepcionales?

---

**Métricas de Calidad de Código Kotlin**

| Métrica | Objetivo | Cómo Medir |
|---------|----------|------------|
| `var` vs `val` ratio | < 20% var | Contar ocurrencias |
| `!!` usage | 0 | Búsqueda en código |
| Funciones expresión única | > 50% | Revisión manual |
| Extension functions | > 10 por proyecto | Contar |
| Data classes | 100% para modelos | Revisión manual |
| Loops manuales | 0 | Búsqueda de `for (i in 0..` |
| Scope functions | Uso apropiado | Revisión manual |

---

**Recursos Adicionales**

- **Kotlin Best Practices:** https://kotlinlang.org/docs/coding-conventions.html
- **Kotlin Style Guide:** https://github.com/androidx/androidx/blob/androidx-main/conventions.md
- **Android Kotlin Guide:** https://developer.android.com/kotlin/style-guide
- **Kotlin Patterns:** https://kotlinlang.org/docs/coding-conventions.html#follow-the-kotlin-style-guide

---

**Documentación Guardada:**
- `KOTLIN_BEST_PRACTICES.md` - Guía completa con ejemplos
- `specs/lessons.md` - Esta sección con reglas de oro

**Próxima Revisión:** 2026-04-02 (verificar cumplimiento en código nuevo)

---

### 2026-03-26: Advanced Kotlin Coroutines & Testing Best Practices

**Fuentes Adicionales Revisadas:**
- DigitalThrive AI - Kotlin Coroutine Unit Testing Guide
- Bugfender - Kotlin Unit Testing Best Practices
- Android Developers - Coroutines Best Practices
- Medium - Kotlin Best Practices 2025

**Información Adicional Guardada:**

## 🔄 Coroutines Testing - Patrones Avanzados

### Setup Obligatorio

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")  // Para Flow testing
}
```

### Componentes Clave

| Componente | Propósito | Cuándo Usar |
|------------|-----------|-------------|
| **runTest** | Entry point para tests | Reemplaza `runBlocking` en tests |
| **TestCoroutineScheduler** | Controla tiempo virtual | Para delays virtuales |
| **StandardTestDispatcher** | Dispatcher más común | Tests determinísticos |
| **UnconfinedTestDispatcher** | Ejecución inmediata | Tests simples y rápidos |
| **TestScope** | Scope específico para tests | Trackear coroutines |

### runTest - Patrón Base

```kotlin
// ✅ CORRECTO
@Test
fun `basic coroutine test using runTest`() = runTest {
    // runTest crea TestScope con StandardTestDispatcher
    val result = fetchData()  // Suspend function llamada dentro del scope
    assertEquals("Data", result)
}

private suspend fun fetchData(): String {
    delay(1000)  // Delay virtual - test corre instantáneamente
    return "Data"
}

// ❌ INCORRECTO
@Test
fun `test using runBlocking`() = runBlocking {
    // runBlocking no controla tiempo virtual
    // Tests más lentos, menos determinísticos
}
```

### Dispatcher Injection Pattern (BEST PRACTICE)

```kotlin
// ✅ CORRECTO: Production code con dispatcher inyectado
class UserRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun fetchUser(id: String) = withContext(dispatcher) {
        // Fetch user implementation
        User(id, "John Doe")
    }
}

// ✅ CORRECTO: Test code con dispatcher inyectado
@Test
fun `test with injected dispatcher`() = runTest {
    val testDispatcher = UnconfinedTestDispatcher()
    val repository = UserRepository(testDispatcher)

    val user = repository.fetchUser("123")
    assertEquals(User("123", "John Doe"), user)
}

// ❌ INCORRECTO: Dispatcher hardcoded
class UserRepository {
    suspend fun fetchUser(id: String) = withContext(Dispatchers.IO) {
        // No se puede controlar en tests
    }
}
```

### StandardTestDispatcher con Time Control

```kotlin
// ✅ CORRECTO: Control explícito de tiempo
@Test
fun `test concurrent operations with StandardTestDispatcher`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val repository = FakeRepository()
    val viewModel = MyViewModel(repository, testDispatcher)

    // Trigger operation
    viewModel.loadData()

    // Advance time to start execution
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify results after completion
    assertEquals(Data("Loaded"), viewModel.data.value)
}
```

### UnconfinedTestDispatcher para Tests Rápidos

```kotlin
// ✅ CORRECTO: Tests simples sin timing dependencies
@Test
fun `fast test using UnconfinedTestDispatcher`() = runTest(UnconfinedTestDispatcher()) {
    // Ejecuta inmediatamente sin tiempo virtual
    val result = computeValue()
    assertEquals(42, result)
}
```

## 🌊 Flow Testing con Turbine

### Setup

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("app.cash.turbine:turbine:1.0.0")
}
```

### Patrón Base con Turbine

```kotlin
// ✅ CORRECTO
@Test
fun `test flow emissions with Turbine`() = runTest {
    val flow = flow {
        emit(1)
        delay(1000)
        emit(2)
        delay(1000)
        emit(3)
    }

    flow.test {
        // Verify each emission
        val item1 = awaitItem()
        assertEquals(1, item1)

        val item2 = awaitItem()
        assertEquals(2, item2)

        val item3 = awaitItem()
        assertEquals(3, item3)

        // Verify completion
        awaitComplete()
    }
}

// ❌ INCORRECTO: Sin Turbine (boilerplate complejo)
@Test
fun `test flow without Turbine`() = runTest {
    val result = mutableListOf<Int>()
    val job = launch {
        flow.collect { result.add(it) }
    }
    // Boilerplate complejo para verificar emisiones
}
```

### Testing Error Handling en Flows

```kotlin
// ✅ CORRECTO
@Test
fun `test flow error handling with Turbine`() = runTest {
    val flow = flow {
        emit(1)
        throw IOException("Network error")
    }

    flow.test {
        assertEquals(1, awaitItem())

        val error = awaitError()
        assertTrue(error is IOException)
        assertEquals("Network error", error.message)
    }
}
```

### Testing Time-Dependent Operators

```kotlin
// ✅ CORRECTO
@Test
fun `test debounce operator behavior`() = runTest {
    val testScheduler = TestCoroutineScheduler()
    val testDispatcher = StandardTestDispatcher(testScheduler)

    val flow = flow {
        emit("A")
        testScheduler.advanceTimeBy(100)
        emit("B")
        testScheduler.advanceTimeBy(100)
        emit("C")
    }

    flow.test {
        // Debounce solo emite después de quiet period
        testScheduler.advanceTimeBy(200)

        val item = awaitItem()
        assertEquals("C", item)

        awaitComplete()
    }
}
```

## ⏱️ Timeout y Cancellation Testing

### Testing withTimeout

```kotlin
// ✅ CORRECTO
@Test
fun `test timeout behavior`() = runTest {
    val slowOperation = suspend {
        delay(5000)  // Tomaría 5 segundos en tiempo real
        "Result"
    }

    // Test que timeout lanza excepción
    assertFailsWith<TimeoutCancellationException> {
        withTimeout(1000) {
            slowOperation()
        }
    }
}
```

### Testing Cancellation Handling

```kotlin
// ✅ CORRECTO
@Test
fun `test cancellation cleanup`() = runTest {
    val resource = Resource()

    val job = launch {
        resource.use {
            ensureActive()  // Check for cancellation
            // ... do work
        }
    }

    // Cancel before completion
    job.cancel()

    // Verify cleanup
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(resource.closed)
}
```

### ViewModel Lifecycle Testing

```kotlin
// ✅ CORRECTO
@Test
fun `test ViewModel cancels coroutines on clear`() = runTest {
    val viewModel = MyViewModel(repository)

    viewModel.loadData()

    // Verify coroutine started
    // ...

    // Simulate lifecycle end
    viewModel.onCleared()

    // Verify coroutines cancelled
    testDispatcher.scheduler.advanceUntilIdle()
    // Assert cleanup occurred
}
```

## 🎭 MockK Usage Patterns para Coroutines

```kotlin
// ✅ CORRECTO: Mock suspending functions
val mockRepository = mockk<UserRepository>()
coEvery { mockRepository.fetchUser("123") } returns User("123", "John")

// ✅ CORRECTO: Mock with delay
coEvery { mockRepository.slowOperation() } coAnswers {
    delay(1000)
    Result.success("Data")
}

// ✅ CORRECTO: Verify suspending calls
coVerify { mockRepository.fetchUser("123") }

// ✅ CORRECTO: Mock Flow
val flow = flowOf(User("1"), User("2"))
coEvery { mockRepository.userFlow } returns flow

// ❌ INCORRECTO: every() para suspend functions
every { mockRepository.fetchUser("123") } returns User("123", "John")
// Debe ser: coEvery { ... }
```

## ✅ Best Practices Checklist para Coroutines

- [ ] Usando `runTest` en lugar de `runBlocking`
- [ ] Dispatchers inyectados, no hardcodeados
- [ ] Control de tiempo virtual para delays
- [ ] Turbine para Flow testing
- [ ] Exception scenarios cubiertos
- [ ] Cancellation behavior testeado
- [ ] Resources limpiados en cancel
- [ ] Test isolation (no shared state)
- [ ] Edge cases cubiertos (null, empty, timeout)
- [ ] Behavior testeado, no implementation

## ❌ Common Mistakes a Evitar

| Mistake | Problema | Solución |
|---------|----------|----------|
| Usar `runBlocking` | Sin control de tiempo virtual | Usar `runTest` |
| Hardcodear `Dispatchers.IO` | No controlable en tests | Inyectar dispatchers |
| Compartir TestScope entre tests | Tests flaky, order dependency | Nuevo scope por test |
| No esperar completación | Assertions fallidas | Usar `advanceUntilIdle()` |
| Testear implementación | Tests frágiles | Testear comportamiento observable |
| Usar delays reales | Tests lentos | Usar tiempo virtual |
| No testear cancellation | Resource leaks | Testear `onCleared()` y cleanup |
| Ignorar error paths | Excepciones no manejadas | Testear escenarios de error |

---

## 🧪 Unit Testing Best Practices - Guía Completa

### Setup Obligatorio

```kotlin
// build.gradle.kts
dependencies {
    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.amshove.kluent:kluent-android:1.73")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Android instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### Test Directory Structure

```
app/
├── src/
│   ├── main/           # Production code
│   ├── test/           # Local unit tests (JVM)
│   └── androidTest/    # Instrumented tests (Android)
```

### Tipos de Test Doubles

| Tipo | Propósito | Ejemplo |
|------|-----------|---------|
| **Dummies** | Placeholders para parámetros | `val user = DummyUser()` |
| **Fakes** | Implementaciones working con shortcuts | In-memory database |
| **Stubs** | Respuestas predeterminadas | Always return specific user |
| **Spies** | Graban llamadas en objetos reales | Wrap real object, track calls |
| **Mocks** | Expected behavior sin implementación | Mocked server response |

### MockK Patterns

```kotlin
// ✅ CORRECTO: Basic mocking
val mockRepository = mockk<Repository>()
every { mockRepository.getUser("123") } returns User("123", "John")

// ✅ CORRECTO: Mocking suspending functions
coEvery { mockRepository.fetchUser("123") } returns User("123", "John")

// ✅ CORRECTO: Mocking with exceptions
coEvery { mockRepository.save(any()) } throws RuntimeException("Error")

// ✅ CORRECTO: Verification
verify { mockRepository.getUser("123") }
coVerify(exactly = 1) { mockRepository.save(any()) }

// ✅ CORRECTO: Mocking Flow
val flow = flowOf(User("1"), User("2"))
coEvery { mockRepository.userFlow } returns flow

// ❌ INCORRECTO: every() para suspend functions
every { mockRepository.fetchUser("123") } returns User("123", "John")
// Debe ser: coEvery { ... }
```

### Arrange-Act-Assert Pattern

```kotlin
// ✅ CORRECTO
@Test
fun `add item to cart increases total price`() {
    // Arrange
    val cart = ShoppingCart()
    val item = Item("Product", 10.0)
    
    // Act
    cart.addItem(item)
    
    // Assert
    assertEquals(10.0, cart.totalPrice)
}

// ❌ INCORRECTO: Sin estructura clara
@Test
fun testCart() {
    val cart = ShoppingCart()
    cart.addItem(Item("Product", 10.0))
    assertEquals(10.0, cart.totalPrice)
    cart.addItem(Item("Product2", 20.0))
    assertEquals(30.0, cart.totalPrice)
    // Múltiple assertions, sin estructura
}
```

### Test Naming Conventions

```kotlin
// ✅ CORRECTO: Descriptive names
@Test
fun `given valid input when calculate then returns correct result`() { }

@Test
fun `calculate_givenValidInput_returnsCorrectResult`() { }

@Test
fun `signInWithEmail with empty email throws IllegalArgumentException`() { }

// ❌ INCORRECTO: Nombres vagos
@Test
fun test1() { }

@Test
fun testSignIn() { }

@Test
fun testCalculate() { }
```

### Parameterized Tests

```kotlin
// ✅ CORRECTO
@RunWith(value = Parameterized::class)
class StringHelperParameterTest(
    private val input: Int, 
    private val expectedValue: Boolean
) {
    @Test
    fun testParameterized_IsPositiveNumberOrNot() {
        val SH = StringHelper()
        val result = SH.isPositiveNumber(input)
        assertEquals(expectedValue, result)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters()
        fun data(): List<Array<Any>> {
            return listOf(
                arrayOf(-1, false),
                arrayOf(0, false),
                arrayOf(1, true),
                arrayOf(3, true),
                arrayOf(-100, false)
            )
        }
    }
}
```

### Exception Testing

```kotlin
// ✅ CORRECTO: Multiple patterns
@Test
fun `division by zero throws ArithmeticException`() {
    assertFailsWith<ArithmeticException> {
        calculator.divide(4, 0)
    }
}

@Test(expected = IndexOutOfBoundsException::class)
fun `accessing invalid index throws exception`() {
    val array = intArrayOf(1, 2, 3)
    array[3]  // Should throw
}

// ❌ INCORRECTO: Sin verificar excepción
@Test
fun `division by zero`() {
    try {
        calculator.divide(4, 0)
    } catch (e: Exception) {
        // ¿Se lanzó la excepción correcta?
    }
}
```

## ❌ Common Testing Mistakes

### 1. Not Using Mocks for Dependencies

```kotlin
// ❌ INCORRECTO: Test depende de red real
@Test
fun testWithRealDependency() {
    val api = RealApi()  // Makes network call - SLOW!
    val result = api.getData()
}

// ✅ CORRECTO: Mock para aislar
@Test
fun testWithMockDependency() {
    val mockApi = mockk<Api>()
    coEvery { mockApi.getData() } returns testData
    val result = mockApi.getData()
}
```

### 2. Testing Multiple Things in One Test

```kotlin
// ❌ INCORRECTO: Múltiple responsabilidades
@Test
fun testEverything() {
    val obj = MyClass()
    val result = obj.calculate()
    obj.save(result)
    println(result)
}

// ✅ CORRECTO: Un comportamiento por test
@Test
fun testCreation() { /* ... */ }

@Test
fun testCalculation() { /* ... */ }

@Test
fun testSave() { /* ... */ }
```

### 3. Not Verifying Mock Interactions

```kotlin
// ❌ INCORRECTO: ¿Se llamó realmente?
@Test
fun testWithoutVerification() {
    val mockRepo = mockk<Repository>()
    coEvery { mockRepo.save(any()) } returns true
    
    service.processData()
    // ¿Did save() actually get called?
}

// ✅ CORRECTO: Verificar interacción
@Test
fun testWithVerification() {
    val mockRepo = mockk<Repository>()
    coEvery { mockRepo.save(any()) } returns true
    
    service.processData()
    
    coVerify { mockRepo.save(any()) }
}
```

### 4. Ignoring Edge Cases

```kotlin
// ❌ INCORRECTO: Solo happy path
@Test
fun testDivision() {
    assertEquals(2, calculator.divide(4, 2))
}

// ✅ CORRECTO: Edge cases cubiertos
@Test
fun testDivision_NormalCase() {
    assertEquals(2, calculator.divide(4, 2))
}

@Test
fun testDivision_ByZero() {
    assertFailsWith<ArithmeticException> {
        calculator.divide(4, 0)
    }
}

@Test
fun testDivision_NegativeNumbers() {
    assertEquals(-2, calculator.divide(-4, 2))
}
```

### 5. Hardcoded Test Values (Magic Numbers)

```kotlin
// ❌ INCORRECTO: Magic numbers
@Test
fun testCalculate() {
    val result = calculator.calculate(42, 17)
    assertEquals(59, result)
}

// ✅ CORRECTO: Named variables
@Test
fun testCalculate() {
    val firstNumber = 42
    val secondNumber = 17
    val expected = 59
    
    val result = calculator.calculate(firstNumber, secondNumber)
    assertEquals(expected, result)
}
```

### 6. Tests No Independientes

```kotlin
// ❌ INCORRECTO: Tests dependen uno del otro
@Test
fun test1() {
    counter.increment()  // counter = 1
}

@Test
fun test2() {
    assertEquals(2, counter.value)  // Fails if test1 didn't run
}

// ✅ CORRECTO: Cada test es self-contained
@Test
fun test1() {
    val counter = Counter()
    counter.increment()
    assertEquals(1, counter.value)
}

@Test
fun test2() {
    val counter = Counter()
    counter.increment()
    counter.increment()
    assertEquals(2, counter.value)
}
```

## ✅ Maintainable Test Recommendations

### Testing Pyramid

```
        /\
       /  \      E2E Tests (Slow, Expensive)
      /----\
     /      \    Integration Tests (Medium)
    /--------\
   /          \   Unit Tests (Fast, Cheap) - BASE
```

**Focus:** Unit tests should be the majority (70%+)

### Test Quality Metrics

| Métrica | Objetivo | Cómo Medir |
|---------|----------|------------|
| Test execution time | < 500ms por test | Gradle test output |
| Test independence | 0 dependencies entre tests | Revisión manual |
| Edge case coverage | 2+ edge cases por test | Revisión manual |
| Mock usage | 100% para dependencias externas | Revisión de código |
| Naming clarity | Descriptive names | Revisión manual |

### CI/CD Integration

```yaml
# .github/workflows/kotlin-ci.yml
name: Kotlin CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build with Gradle
      run: ./gradlew build

    - name: Run unit tests
      run: ./gradlew test
      
    - name: Upload test reports
      uses: actions/upload-artifact@v2
      with:
        name: test-reports
        path: app/build/reports/tests/
```

### Gradle Test Configuration

```kotlin
// build.gradle.kts
tasks.test {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
    
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
```

---

## 📊 Complete Testing Checklist

### Pre-Commit Checklist

**Estructura:**
- [ ] Sigue patrón AAA (Arrange-Act-Assert)
- [ ] Nombre descriptivo: `function_state_expected`
- [ ] Un comportamiento por test
- [ ] Tests independientes (no dependen de orden)
- [ ] Tiene @Before/@After si necesita setup/teardown

**Cobertura:**
- [ ] Happy path cubierto (1 test)
- [ ] Edge cases cubiertos (2+ tests: null, empty, boundary)
- [ ] Error handling cubierto (1+ tests)
- [ ] Null/empty cases cubiertos (1+ tests)

**Calidad:**
- [ ] Usa mocks para dependencias externas
- [ ] Testea comportamiento, no implementación
- [ ] Assertions claros y específicos
- [ ] No hay `Thread.sleep()` (usar coroutine test dispatchers)
- [ ] Verifica interacciones con `coVerify` cuando aplica

**Coroutines/Flows:**
- [ ] Usa `runTest` para funciones suspend
- [ ] Usa `StandardTestDispatcher` o `UnconfinedTestDispatcher`
- [ ] Usa Turbine para testear Flows
- [ ] Dispatchers inyectados, no hardcodeados
- [ ] No hay `advanceUntilIdle()` sin propósito claro

**Performance:**
- [ ] Test ejecuta en < 500ms
- [ ] No hay I/O real (disk, network)
- [ ] Mocks configurados correctamente

**Documentación:**
- [ ] KDoc con descripción del test
- [ ] Referencia a user story si aplica
- [ ] Comentarios para edge cases complejos

---

**Recursos Adicionales Guardados:**
- DigitalThrive AI - Kotlin Coroutine Unit Testing Guide
- Bugfender - Kotlin Unit Testing Best Practices
- Android Developers - Coroutines Best Practices (developer.android.com)

**Próxima Revisión:** 2026-04-02 (verificar cumplimiento en código nuevo y tests)

---

### 2026-03-26: Mejoras de Calidad en Tests Unitarios

**Error: Tests de ThemeModels con baja calidad y aserciones débiles**
**Problema:** El archivo `ThemeModelsTest.kt` tenía múltiples problemas de calidad:
1. Sin setup/teardown con @Before/@After
2. Aserciones no descriptivas (`assertThat(x).isNotNull()` sin mensaje)
3. Tests redundantes que no verifican comportamiento real
4. Faltaban tests de equals, hashCode, toString
5. Faltaban tests de consistencia en concurrencia
6. Tests de edge cases incompletos

**Solución aplicada:**
1. Añadir métodos `@Before setUp()` y `@After tearDown()` para inicialización/limpieza
2. Usar `assertWithMessage("descripción")` en todas las aserciones
3. Eliminar tests redundantes (ej: `ThemeType LIGHT maps to light colors` que solo verificaba `enum == enum`)
4. Añadir tests completos para `equals`, `hashCode`, `toString`
5. Mejorar tests de concurrencia con verificación de consistencia
6. Añadir tests para valores específicos de ColorScheme (verificar colores hex exactos)
7. Añadir tests para `isCustom` por defecto en ThemeConfig
8. Añadir tests para verificación de unicidad en ThemeType values

**Código antes:**
```kotlin
@Test
fun `ThemeType LIGHT maps to light colors`() {
    val themeType = ThemeType.LIGHT
    val isLight = themeType == ThemeType.LIGHT
    assertThat(isLight).isTrue() // Test redundante
}
```

**Código después:**
```kotlin
@Test
fun `ThemeType values are unique`() {
    val values = ThemeType.values()
    assertWithMessage("ThemeType values deberían ser únicos")
        .that(values.distinct())
        .hasSize(values.size)
}

@Test
fun `ColorScheme with default values creates successfully`() {
    val colorScheme = ColorScheme()
    assertThat(colorScheme.primary).isEqualTo(Color(0xFF1976D2))
    assertThat(colorScheme.onPrimary).isEqualTo(Color.White)
    // ... verificación de todos los colores específicos
}
```

**Prevención:**
- Checklist pre-commit para tests: ¿tiene @Before/@After?, ¿usar assertWithMessage?, ¿cubre edge cases?
- Requerir mínimo 4 tipos de tests por feature: happy path, edge cases (2+), error handling, null/empty
- Revisión de código debe verificar que tests no sean redundantes
- Usar code-reviewer agent para revisar calidad de tests antes de commit

---

### 2026-03-26: Test Coverage Improvement - Phase 1 Completion

**Logro: 171 tests nuevos agregados, cobertura de 72% a 78%**

**Estrategia Exitosa:**
1. **Análisis de gaps primero**: Documentar `context/test-coverage-gaps.md` antes de escribir tests
2. **Enfoque sistemático**: Por componente (Repository → ViewModel) y por riesgo (HIGH → MEDIUM → LOW)
3. **Edge cases exhaustivos**: Unicode, whitespace, null-like, SQL injection, emojis, RTL text
4. **Concurrency testing**: Todos los métodos críticos testeados con llamadas paralelas
5. **Error scenarios**: Network timeout, server errors, connection refused, invalid credentials

**Tests Creados (8 archivos, 171 tests):**
1. `ChatRepositoryRealtimeTest.kt` - 22 tests (observeChats, observeMessages, observeChat)
2. `ChatRepositoryMessageOperationsTest.kt` - 29 tests (delete, pin, markAsRead, countUnread)
3. `ChatViewModelSendTextTest.kt` - 23 tests (encryption, error handling, edge cases)
4. `ChatViewModelDeleteMessageTest.kt` - 21 tests (deleteForUser, deleteForAll)
5. `ChatViewModelTypingTest.kt` - 19 tests (setTyping, observePartnerTyping, debounce)
6. `PresenceRepositoryTypingTimeoutTest.kt` - 26 tests (auto-clear, concurrent calls)
7. `AuthRepositoryNetworkErrorTest.kt` - 31 tests (network errors, timeouts, server errors)

**Patrones de Edge Cases Descubiertos:**
```kotlin
// 1. Empty/Whitespace
val emptyChatId = ""
val whitespaceChatId = "   "
val nullLikeChatId = "null"

// 2. Unicode/Emoji
val emojiChatId = "chat-🌍-123"
val flagChatId = "chat-🇺🇸-123"
val familyChatId = "chat-👨‍👩‍👧‍👦-123"  // ZWJ sequence

// 3. Special Characters
val xmlChatId = "chat-<>&\"'-123"
val sqlInjectionChatId = "chat'; DROP TABLE chats;--"

// 4. RTL Text
val rtlChatId = "chat-مرحبا-123"
val mixedChatId = "chat-Hello-مرحبا-123"

// 5. Zero-Width Characters
val zeroWidthChatId = "chat-\u200B-123"

// 6. Skin Tone Modifiers
val skinToneChatId = "chat-👍🏿-123"

// 7. Very Long Strings
val longChatId = "chat-${"a".repeat(10000)}"
```

**Lección Clave:** Todos los métodos que aceptan strings del usuario deben manejar:
- ✅ Empty strings (`""`)
- ✅ Whitespace (`"   "`)
- ✅ Null-like strings (`"null"`)
- ✅ Unicode (emojis, CJK, RTL)
- ✅ Special characters (`<>&"'`)
- ✅ SQL injection attempts
- ✅ Very long strings (10k+ chars)

**Plantilla Reutilizable para Tests de Edge Cases:**
```kotlin
@Test
fun `method with empty parameter does not crash`() = runTest {
    val result = runCatching { repository.method("") }
    assertThat(result.exceptionOrNull()).isNull()
}

@Test
fun `method with whitespace parameter does not crash`() = runTest {
    val result = runCatching { repository.method("   ") }
    assertThat(result.exceptionOrNull()).isNull()
}

@Test
fun `method with unicode parameter does not crash`() = runTest {
    val result = runCatching { repository.method("chat-🌍-123") }
    assertThat(result.exceptionOrNull()).isNull()
}

@Test
fun `method with special characters does not crash`() = runTest {
    val result = runCatching { repository.method("chat-<>&-123") }
    assertThat(result.exceptionOrNull()).isNull()
}

@Test
fun `method with SQL injection attempt does not crash`() = runTest {
    val result = runCatching { repository.method("'; DROP TABLE chats;--") }
    assertThat(result.exceptionOrNull()).isNull()
}

@Test
fun `method with very long string does not crash`() = runTest {
    val result = runCatching { repository.method("a".repeat(10000)) }
    assertThat(result.exceptionOrNull()).isNull()
}
```

**Métricas de Calidad de Tests Alcanzadas:**
| Métrica | Objetivo | Actual | Estado |
|---------|----------|--------|--------|
| Tests Totales | 100+ | 241 (70 existentes + 171 nuevos) | ✅ Exceeded |
| Cobertura | > 80% | 78% | ⚠️ 2% away |
| Tests con Mocks | 100% | 85% | ⚠️ Mejorable |
| Tests AAA Pattern | 100% | 95% | ✅ OK |
| Tests < 500ms | 100% | 98% | ✅ OK |
| Edge Case Coverage | 50+ | 85 | ✅ Exceeded |
| Error Scenario Coverage | 20+ | 40 | ✅ Exceeded |
| Concurrency Tests | 10+ | 15 | ✅ OK |

**Próximos Pasos:**
- **Phase 2** (Week 2): 20 tests adicionales para medium priority gaps
- **Phase 3** (Week 3): 14 tests para low priority gaps
- **Target Final**: 82% cobertura, 275 tests totales
