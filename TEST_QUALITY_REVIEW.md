# Test Quality Review - Message App

**Fecha:** 2026-03-26  
**Revisado por:** Qwen Code  
**Basado en:** "How to Write Unit Tests" de TestRail + QWEN.md

---

## 📊 Resumen Ejecutivo

### Overall Score: **78/100** ✅ Bueno (con margen de mejora)

**Fortalezas Principales:**
- ✅ Excelente cobertura de edge cases en la mayoría de tests
- ✅ Uso consistente de AAA pattern (Arrange-Act-Assert)
- ✅ Buenos tests de criptografía (E2ECipherTest)
- ✅ Testing de concurrencia y performance incluido
- ✅ Uso apropiado de MockK, Turbine, y Truth

**Áreas de Mejora Críticas:**
- ❌ Tests de repositorios sin mocks de dependencias externas (Supabase)
- ❌ Algunos tests no verifican comportamiento, solo "no crash"
- ❌ Faltan tests para casos de error específicos (excepciones, errores de red)
- ❌ Tests instrumentados mezclados con unit tests puros
- ❌ Cobertura insuficiente en ViewModels para estados de error

---

## 📁 Análisis Detallado por Archivo

### 1. AuthRepositoryTest.kt
**Score: 65/100** ⚠️ Regular

**Fortalezas:**
- ✅ Cubre múltiples edge cases para validación de email
- ✅ Tests para null/empty cases
- ✅ Usa `runCatching` para verificar que no crashea

**Debilidades Críticas:**
- ❌ **PROBLEMA MAYOR**: No usa mocks para Supabase - los tests dependen de red
- ❌ Tests como `signInWithEmail with valid credentials should work` no verifican resultado real
- ❌ Muchos tests solo verifican "no debería crashar" en lugar de comportamiento esperado
- ❌ No hay tests para `isUserLoggedIn()`
- ❌ No verifica interacciones con dependencias

**Issues Específicos:**
```kotlin
// ❌ MAL: Test no verifica comportamiento, solo "no crash"
@Test
fun `signUpWithEmail with empty email does not crash`() = runTest {
    val result = runCatching { repository.signUpWithEmail(email, password) }
    assertThat(result.exceptionOrNull()).isNull() // ¿Por qué no debería crashar?
}

// ✅ BIEN: Test verifica validación específica
@Test
fun `signUpWithEmail rejects email without @ symbol`() = runTest {
    val result = runCatching { repository.signUpWithEmail(email, password) }
    assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
}
```

**Recomendaciones:**
1. Mockear SupabaseAuthClient para aislar repository
2. Verificar que se llama a `signInWithEmail` del cliente con parámetros correctos
3. Agregar tests para errores de red (timeout, SSL, etc.)
4. Testear refresh token flow

---

### 2. ChatRepositoryTest.kt
**Score: 85/100** ✅ Muy Bueno

**Fortalezas:**
- ✅ Excelente testing de `directChatIdFor` con múltiples edge cases
- ✅ Tests de performance (10k llamadas en < 100ms)
- ✅ Cubre unicode, special characters, boundary values
- ✅ Test que documenta bug conocido (whitespace sin trim)

**Debilidades:**
- ⚠️ Solo tiene tests para `directChatIdFor` - faltan tests para otras funciones
- ⚠️ El test `directChatIdFor trims whitespace BUG` documenta un bug pero no hay test que verifique el fix

**Recomendaciones:**
1. Agregar tests para: `observeChat`, `observeMessages`, `sendMessage`
2. Convertir el test del bug en verificación del fix después de corregir
3. Agregar tests de concurrencia para operaciones de DB

---

### 3. AuthViewModelTest.kt
**Score: 88/100** ✅ Muy Bueno

**Fortalezas:**
- ✅ Usa MockK correctamente para aislar dependencias
- ✅ Tests para validación de inputs (email vacío, password corto)
- ✅ Usa Turbine para testear StateFlows
- ✅ Cubre happy path + edge cases + error handling
- ✅ Tests de concurrencia (múltiples llamadas rápidas)

**Debilidades:**
- ⚠️ Test `signOut updates state to logged out` está comentado/incompleto
- ⚠️ Faltan tests para `init()` con diferentes estados de repository

**Recomendaciones:**
1. Completar test de `signOut` o eliminarlo
2. Agregar tests para estado de error en `signInWithEmail`
3. Testear que `onCleared` hace cleanup correcto

---

### 4. ChatViewModelTest.kt
**Score: 82/100** ✅ Bueno

**Fortalezas:**
- ✅ Usa MockK y Turbine apropiadamente
- ✅ Tests para filtering de mensajes eliminados
- ✅ Verifica loading states

**Debilidades:**
- ❌ Test `stop clears current chat and user` no verifica nada realmente
- ⚠️ Faltan tests para `sendMessage`, `deleteMessage`, `markAsRead`
- ⚠️ No hay tests para typing indicators

**Recomendaciones:**
1. Agregar tests para todas las funciones públicas del ViewModel
2. Testear que errores de red se propagan al estado de error
3. Agregar tests para reconexión de WebSocket

---

### 5. E2ECipherTest.kt
**Score: 95/100** ✅ Excelente

**Fortalezas:**
- ✅ **Gold standard** para tests de criptografía
- ✅ Cubre todos los edge cases: empty, null, unicode, special chars
- ✅ Tests de integración: encrypt → decrypt
- ✅ Tests de seguridad: tampered ciphertext, wrong chatId
- ✅ Tests de performance (100 mensajes en < 5s)
- ✅ Setup/teardown para limpieza de claves
- ✅ 28 tests bien estructurados

**Ejemplo a seguir:**
```kotlin
@Test
fun `decrypt with tampered ciphertext returns error`() {
    // Given: Ciphertext alterado
    val plaintext = "Secret"
    val chatId = "chat-123"
    val encrypted = E2ECipher.encrypt(plaintext, chatId)
    val tampered = encrypted.replaceRange(0, 5, "XXXXX")

    // When: Descifro ciphertext alterado
    val result = E2ECipher.decrypt(tampered, chatId)

    // Then: Debería retornar error (auth tag inválido)
    assertThat(result).contains("Error")
}
```

**Recomendaciones:**
1. Agregar test para key rotation
2. Testear resistencia a replay attacks

---

### 6. ModelsTest.kt
**Score: 90/100** ✅ Excelente

**Fortalezas:**
- ✅ Cubre todos los modelos: User, Message, Chat, Avatar
- ✅ Tests para propiedades computadas (Message.status)
- ✅ Tests de integración entre modelos
- ✅ Edge cases: unicode, special chars, boundary values
- ✅ Tests de concurrencia (100 objetos concurrentes)

**Recomendaciones:**
1. Agregar tests para métodos de validación si existen
2. Testear equals/hashCode si están override

---

### 7. TimeUtilsTest.kt
**Score: 92/100** ✅ Excelente

**Fortalezas:**
- ✅ Cubre todas las funciones: toFormattedDate, toRelativeTime, isToday, etc.
- ✅ Tests de performance (1000 calls en < 500ms)
- ✅ Boundary values: zero, negative, Long.MAX_VALUE
- ✅ Tests de precisión (exactamente 60 segundos, 1 hora, 1 día)

**Recomendaciones:**
1. Agregar tests para timezones diferentes
2. Testear daylight saving time edge cases

---

### 8. MessageDaoTest.kt
**Score: 88/100** ✅ Muy Bueno

**Fortalezas:**
- ✅ Usa Room in-memory database
- ✅ Tests para todas las operaciones CRUD
- ✅ Verifica ordenamiento (orderBy createdAt)
- ✅ Tests para REPLACE behavior

**Debilidades:**
- ❌ Requiere Robolectric (más lento, más frágil)
- ⚠️ Podría ser más rápido usando FakeDao en tests unitarios

**Recomendaciones:**
1. Considerar usar FakeDao para tests unitarios puros
2. Mover a androidTest si necesita Room real

---

## 🚨 Issues Críticos Encontrados

### 1. **Tests de Repositorio sin Mocks** (ALTA PRIORIDAD)
**Impacto:** Tests frágiles que dependen de red y servicios externos

**Archivos afectados:**
- `AuthRepositoryTest.kt`
- `ChatRepositoryTest.kt` (parcialmente)
- `PresenceRepositoryTest.kt`
- `ProfileRepositoryTest.kt`

**Problema:**
```kotlin
// ❌ MAL: Test depende de Supabase real
@Before
fun setup() {
    repository = AuthRepository() // Usa Supabase real
}

@Test
fun `signInWithEmail with valid credentials should work`() = runTest {
    // Esto puede fallar por:
    // - Sin conexión
    // - Credenciales inválidas en Supabase
    // - Rate limiting
    val result = repository.signInWithEmail(email, password)
}
```

**Solución:**
```kotlin
// ✅ BIEN: Mockear dependencias
@Before
fun setup() {
    val mockAuthClient = mockk<SupabaseAuthClient>()
    coEvery { mockAuthClient.signInWithEmail(...) } returns Result.success(mockUser)
    repository = AuthRepository(authClient = mockAuthClient)
}
```

---

### 2. **Tests que no Verifican Comportamiento** (MEDIA PRIORIDAD)
**Impacto:** Falsa sensación de seguridad

**Ejemplo:**
```kotlin
@Test
fun `signOut does not throw exception`() = runTest {
    val result = runCatching { repository.signOut() }
    assertThat(result.exceptionOrNull()).isNull()
    // ❌ ¿Pero qué debería hacer signOut? ¿Limpia estado? ¿Notifica?
}
```

**Solución:**
```kotlin
@Test
fun `signOut clears user state and notifies listeners`() = runTest {
    // Given: Usuario logueado
    val stateCaptor = slot<StateFlow<User?>>()
    
    // When: Cierro sesión
    repository.signOut()
    
    // Then: Estado actualizado a null
    verify { mockAuthClient.signOut() }
    assertThat(capturedState.value).isNull()
}
```

---

### 3. **Faltan Tests para Error Handling** (ALTA PRIORIDAD)
**Impacto:** Errores en producción no detectados

**Tests faltantes:**
- [ ] `AuthRepository`: SSLHandshakeException, NetworkTimeoutException
- [ ] `ChatRepository`: DatabaseFullException, ConflictException
- [ ] `ViewModels`: Error states propagation to UI
- [ ] `E2ECipher`: KeyCorruptionException, InvalidKeyException

---

### 4. **Tests Acoplados a Implementación** (BAJA PRIORIDAD)
**Impacto:** Tests se rompen con refactors

**Ejemplo:**
```kotlin
// ❌ MAL: Test depende de implementación interna
@Test
fun `encrypt uses AES-GCM mode`() {
    // Esto prueba implementación, no comportamiento
}

// ✅ BIEN: Test verifica comportamiento observable
@Test
fun `decrypt with wrong key returns error`() {
    // Esto prueba comportamiento esperado por el usuario
}
```

---

## 📋 Tests Faltantes (Priorizados)

### **ALTA PRIORIDAD** (Seguridad, Auth, Core Features)

#### Auth Layer
- [ ] `AuthRepositoryTest`: `signInWithEmail with network timeout throws NetworkException`
- [ ] `AuthRepositoryTest`: `signInWithEmail with invalid credentials throws AuthException`
- [ ] `AuthRepositoryTest`: `refreshToken automatically when expired`
- [ ] `AuthViewModelTest`: `error state propagates to UI on login failure`

#### Crypto Layer
- [ ] `E2ECipherTest`: `key rotation invalidates old keys`
- [ ] `E2ECipherTest`: `decrypt with corrupted key returns specific error`
- [ ] `KeyManagerTest`: (archivo nuevo) tests para gestión de claves

#### Chat Core
- [ ] `ChatRepositoryTest`: `sendMessage encrypts before sending`
- [ ] `ChatRepositoryTest`: `receiveMessage decrypts and validates signature`
- [ ] `ChatRepositoryTest`: `markAsRead updates local and remote state`
- [ ] `ChatViewModelTest`: `sendMessage updates UI immediately then syncs`

### **MEDIA PRIORIDAD** (Features Secundarias)

#### Presence
- [ ] `PresenceRepositoryTest`: `updatePresence sends to Supabase Realtime`
- [ ] `PresenceRepositoryTest`: `observePresence receives updates from other users`
- [ ] `PresenceViewModelTest`: `typing indicator timeout after 3 seconds`

#### Profile
- [ ] `ProfileRepositoryTest`: `updateProfile compresses avatar image`
- [ ] `ProfileRepositoryTest`: `updateProfile validates unique display name`
- [ ] `ProfileViewModelTest`: `loadProfile handles cached avatar`

#### Avatar
- [ ] `AvatarRepositoryTest`: `getAvatar downloads and caches image`
- [ ] `AvatarRepositoryTest`: `uploadAvatar resizes to max 512x512`

### **BAJA PRIORIDAD** (Utils, Edge Cases)

#### Utils
- [ ] `ContactsUtilsTest`: `filterContacts returns only Message App users`
- [ ] `SignatureLoggerTest`: `logSignature includes timestamp and device info`

#### Navigation
- [ ] `NavigationTest`: (archivo nuevo) tests para NavGraph

---

## ✅ Recomendaciones de Acción

### **Inmediatas (Esta Semana)**

1. **Mockear dependencias en AuthRepositoryTest**
   - Crear mocks para SupabaseAuthClient
   - Verificar interacciones, no solo "no crash"
   - Agregar 5 tests para errores de red específicos

2. **Completar tests de ChatViewModel**
   - Agregar tests para sendMessage, deleteMessage
   - Testear error propagation
   - Verificar typing indicator behavior

3. **Documentar lecciones en specs/lessons.md**
   ```markdown
   ## 2026-03-26 - Test Review Learnings
   - Problema: Tests de repositorio sin mocks dependen de red
   - Solución: Usar MockK para aislar dependencias externas
   - Prevención: Checklist pre-test: "¿Tiene mocks para todas las deps externas?"
   ```

### **Corto Plazo (Próximas 2 Semanas)**

4. **Crear tests faltantes de ALTA prioridad**
   - 10 tests de error handling para Auth
   - 5 tests de crypto key management
   - 8 tests de chat core functionality

5. **Refactorizar tests lentos**
   - MessageDaoTest: considerar FakeDao para unit tests
   - Mover tests de Room a androidTest si es necesario

### **Mediano Plazo (Próximo Mes)**

6. **Establecer métricas de calidad**
   - Cobertura mínima: 80% para código nuevo
   - Tiempo máximo de test: 500ms por test
   - 0 tests flaky en CI

7. **Crear test templates**
   - Template para Repository tests
   - Template para ViewModel tests
   - Template para Use Case tests

---

## 📝 Checklist para Escribir Tests (Memory Update)

### **Regla de Oro: AAA + 4 Tipos de Tests**

Cada feature debe tener **mínimo 4 tests**:

```kotlin
// 1. Happy Path
@Test
fun `functionName with valid input returns expected result`() {
    // Arrange
    val input = validInput
    
    // Act
    val result = subject.function(input)
    
    // Assert
    assertThat(result).isEqualTo(expectedOutput)
}

// 2. Edge Case (Null/Empty)
@Test
fun `functionName with null input returns empty result`() {
    // Arrange
    val input: InputType? = null
    
    // Act
    val result = subject.function(input)
    
    // Assert
    assertThat(result).isEmpty()
}

// 3. Edge Case (Invalid Format)
@Test
fun `functionName with invalid input throws IllegalArgumentException`() {
    // Arrange
    val invalidInput = createInvalidInput()
    
    // Act & Assert
    assertThrows<IllegalArgumentException> {
        subject.function(invalidInput)
    }
}

// 4. Error Handling (External Failure)
@Test
fun `functionName when external service fails returns Error state`() {
    // Arrange
    coEvery { mockDependency.call() } returns Result.failure(NetworkException())
    
    // Act
    val result = subject.function()
    
    // Assert
    assertThat(result).isInstanceOf<Result.Error>()
}
```

### **Checklist Pre-Submission**

Antes de commit, verificar:

**Estructura:**
- [ ] Sigue patrón AAA (Arrange-Act-Assert)
- [ ] Nombre descriptivo: `function_state_expected`
- [ ] Un comportamiento por test
- [ ] Tests independientes (no dependen de orden)

**Cobertura:**
- [ ] Happy path cubierto
- [ ] Al menos 2 edge cases (null, empty, boundary)
- [ ] Error handling cubierto
- [ ] Null/empty cases cubiertos

**Calidad:**
- [ ] Usa mocks para dependencias externas
- [ ] Testea comportamiento, no implementación
- [ ] Assertions claros y específicos
- [ ] No hay `Thread.sleep()` (usar coroutine test dispatchers)

**Coroutines/Flows:**
- [ ] Usa `runTest` para funciones suspend
- [ ] Usa `StandardTestDispatcher` para control
- [ ] Usa Turbine para testear Flows
- [ ] No hay `advanceUntilIdle()` sin propósito claro

**Documentación:**
- [ ] KDoc con descripción del test
- [ ] Referencia a user story si aplica
- [ ] Comentarios para edge cases complejos

**Performance:**
- [ ] Test ejecuta en < 500ms
- [ ] No hay I/O real (disk, network)
- [ ] Mocks configurados correctamente

---

## 🎯 Próximos Pasos

1. **Inmediato:** Corregir `AuthRepositoryTest` para usar mocks
2. **Esta semana:** Agregar 15 tests de ALTA prioridad
3. **Próxima semana:** Refactorizar tests lentos
4. **Próximo mes:** Alcanzar 80% cobertura

---

**Estado:** ✅ Review completado  
**Próxima Review:** 2026-04-02 (semanal)
