# 🎯 PLAN DE MEJORA COMPLETO - Message App

**Fecha:** 2026-03-26  
**Objetivo:** Elevar calidad de código y tests a estándares de producción  
**Estado Actual:** 72% cobertura → **Meta:** 85%+ cobertura

---

## 📊 RESUMEN EJECUTIVO

### Métricas Actuales vs. Objetivo

| Métrica | Actual | Objetivo | Brecha |
|---------|--------|----------|--------|
| **Cobertura de Tests** | 72% | 85% | -13% |
| **Calidad de Código** | 72/100 | 90/100 | -18pts |
| **Errores Críticos** | 52 | 0 | +52 |
| **Fallos Silenciosos** | 23 | 0 | +23 |
| **Funciones >30 líneas** | 17 | 0 | +17 |

### Hallazgos Principales

#### 🔴 Crítico (Acción Inmediata)
1. **52 errores de compilación** potenciales
2. **23 fallos silenciosos** (catch blocks vacíos o sin logging)
3. **Credenciales hardcodeadas** en código fuente
4. **Referencias a Firebase** en proyecto migrado a Supabase
5. **7 archivos con código que no compila**

#### 🟡 Alto (Esta Semana)
1. **8 tests críticos faltantes** para funcionalidad core
2. **17 funciones exceden 30 líneas** (violación de specs)
3. **Diseño de tipos débil** en 23 data classes
4. **Falta validación de inputs** en funciones públicas

#### 🟢 Medio (Próximas 2 Semanas)
1. **Refactorizar tipos** para usar value classes
2. **Implementar Use Case layer** (Clean Architecture)
3. **Agregar retry logic** para operaciones de red
4. **Documentar patrones de error handling**

---

## 🚨 FASE 1: CORRECCIONES CRÍTICAS (Días 1-3)

### 1.1 Eliminar Credenciales Hardcodeadas
**Archivo:** `app/src/main/java/com/example/messageapp/supabase/SupabaseConfig.kt`

**Problema:**
```kotlin
const val SUPABASE_URL = "https://h-2_uEBOtaXgANRmUTMsdQ.supabase.co"
const val SUPABASE_ANON_KEY = "sb_publishable_H-2_uEBOtaXgANRmUTMsdQ_qcTjP-WH"
```

**Solución:**
```kotlin
// Usar BuildConfig con variables de entorno
const val SUPABASE_URL = BuildConfig.SUPABASE_URL
const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
```

**Acción:**
- [ ] Mover credentials a `gradle.properties` (no commitear)
- [ ] Actualizar `build.gradle.kts` para leer variables
- [ ] Actualizar `SupabaseConfig.kt` para usar BuildConfig
- [ ] Agregar `.gitignore` para `gradle.properties` local

---

### 1.2 Corregir Errores de Compilación - Theme.kt
**Archivo:** `app/src/main/java/com/example/messageapp/ui/theme/Theme.kt`

**Problema:** Colores no definidos en `Color.kt`

**Acción:**
- [ ] Agregar a `Color.kt`:
```kotlin
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
```

---

### 1.3 Corregir Errores de Compilación - ChatInfoScreen.kt
**Archivo:** `app/src/main/java/com/example/messageapp/ui/chat/ChatInfoScreen.kt`

**Problema:** Función helper definida después del Scaffold (fuera del scope)

**Acción:**
- [ ] Mover bloque `Scaffold` antes de la función helper
- [ ] Verificar que todas las llaves de cierre estén correctas

---

### 1.4 Corregir Nombres de Métodos Incorrectos
**Archivos afectados:**
- `AuthScreen.kt` (línea 69-72)
- `ChatScreen.kt` (línea 41, 48-49)
- `ProfileScreen.kt` (línea 107)

**Correcciones:**
```kotlin
// AuthScreen.kt
repo.signInWithEmail(email, pass)  // era: signInEmail
repo.signUpWithEmail(email, pass)  // era: signUpEmail

// ChatScreen.kt
vm.markAsRead(chatId, myUid)  // era: markRead
vm.unpinMessage(chatId)  // era: unpin
vm.pinMessage(chatId, selected!!)  // era: pin
repo.deleteMessageForUser(...)  // era: hideMessageForUser

// ProfileScreen.kt
repo.signOut()  // era: signOutAndRemoveToken
```

**Acción:**
- [ ] Corregir todos los nombres de métodos
- [ ] Verificar con IDE que existen

---

### 1.5 Migrar Firebase → Supabase (Archivos Críticos)
**Archivos:**
1. `ChatScreen.kt` - FirebaseAuth
2. `ChatListScreen.kt` - FirebaseFirestore
3. `ProfileScreen.kt` - FirebaseAuth, FirebaseFirestore
4. `StorageAcl.kt` - FirebaseStorage (archivo completo)

**Acción:**
- [ ] Reemplazar `FirebaseAuth.getInstance()` con `SupabaseConfig.client.auth`
- [ ] Reemplazar `FirebaseFirestore` con `SupabaseConfig.client.plugin(Postgrest)`
- [ ] Reemplazar `FirebaseStorage` con `SupabaseConfig.client.storage`
- [ ] Eliminar `StorageAcl.kt` o migrar completamente

---

### 1.6 Agregar Imports Faltantes
**Archivos:**
- `ChatComponents.kt` - offset, graphicsLayer
- `MessageBubble.kt` - detectTapGestures
- `ChatListScreen.kt` - clickable, clip, collectAsState

**Acción:**
- [ ] Agregar imports faltantes identificados en reporte
- [ ] Verificar compilación con IDE

---

### 1.7 Corregir Fallos Silenciosos Críticos
**Archivos:**
1. `ChatViewModel.kt:188-193` - catch vacío en markAsRead
2. `ChatHelpers.kt:40,46,52,58` - runCatching ignorado
3. `ChatComponents.kt:86` - runCatching en Intent
4. `ChatRepository.kt:445-447` - retorna 0 en error

**Acción:**
```kotlin
// ChatViewModel.kt
catch (e: Exception) {
    android.util.Log.w(TAG, "Mark as read failed: $chatId", e)
    _error.value = "No se pudo marcar como leído"
}

// ChatHelpers.kt
try {
    context.contentResolver.takePersistableUriPermission(...)
    // ...
} catch (e: SecurityException) {
    Log.e("ChatMedia", "Permission denied", e)
} catch (e: Exception) {
    Log.e("ChatMedia", "Failed to take permission", e)
}

// ChatComponents.kt
try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(m.mediaUrl))
    ctx.startActivity(intent)
} catch (e: ActivityNotFoundException) {
    Log.e("ChatMedia", "No app to handle URL", e)
    Toast.makeText(ctx, "No se puede abrir", Toast.LENGTH_SHORT).show()
}

// ChatRepository.kt
catch (e: Exception) {
    android.util.Log.w("ChatRepository", "Count unread failed", e)
    throw e  // Propagar error
}
```

---

## 🧪 FASE 2: MEJORAR TESTS (Días 4-7)

### 2.1 Tests Críticos Faltantes

**Prioridad 1: AuthRepository**
- [ ] `AuthRepositoryNetworkErrorTest.kt` - 31 tests (YA CREADO ✅)
- [ ] `AuthRepositoryEdgeCasesTest.kt` - 8 tests (email inválido, null, unicode)

**Prioridad 2: ChatRepository**
- [ ] `ChatRepositoryRealtimeTest.kt` - 22 tests (YA CREADO ✅)
- [ ] `ChatRepositoryMessageOperationsTest.kt` - 29 tests (YA CREADO ✅)
- [ ] `ChatRepositoryEdgeCasesTest.kt` - 6 tests (IDs vacíos, unicode, SQL injection)

**Prioridad 3: ChatViewModel**
- [ ] `ChatViewModelSendTextTest.kt` - 23 tests (YA CREADO ✅)
- [ ] `ChatViewModelDeleteMessageTest.kt` - 21 tests (YA CREADO ✅)
- [ ] `ChatViewModelTypingTest.kt` - 19 tests (YA CREADO ✅)

**Prioridad 4: PresenceRepository**
- [ ] `PresenceRepositoryTypingTimeoutTest.kt` - 26 tests (YA CREADO ✅)

**Total Tests Nuevos:** 171 tests (8 archivos creados)

---

### 2.2 Mejorar Calidad de Tests Existentes

**Problemas Identificados:**
1. Tests sin mocks para dependencias externas
2. Tests que dependen de conexión a Supabase
3. Cobertura insuficiente de edge cases

**Acción:**
- [ ] Revisar `AuthRepositoryTest.kt` - agregar mocks con MockK
- [ ] Revisar `ChatRepositoryTest.kt` - agregar mocks
- [ ] Revisar `E2ECipherTest.kt` - mantener como referencia (95% coverage)

---

## 🏗️ FASE 3: ARQUITECTURA Y TIPOS (Días 8-14)

### 3.1 Crear Use Case Layer (Clean Architecture)

**Casos de Uso a Crear:**
1. `SendMessageUseCase` - Validar + Enviar mensaje
2. `DeleteMessageUseCase` - Eliminar mensaje (user/all)
3. `CreateChatUseCase` - Crear chat directo
4. `AuthenticateUserUseCase` - Login/Registro
5. `GetUserAvatarUseCase` - Obtener avatar

**Estructura:**
```
app/src/main/java/com/example/messageapp/domain/
├── usecase/
│   ├── SendMessageUseCase.kt
│   ├── DeleteMessageUseCase.kt
│   └── ...
└── model/ (tipos de dominio puros)
```

---

### 3.2 Mejorar Diseño de Tipos

**Value Classes a Crear:**
```kotlin
// Identidades
@JvmInline value class UserId(val value: String)
@JvmInline value class ChatId(val value: String)
@JvmInline value class MessageId(val value: String)

// Tipos validados
@JvmInline value class Email private constructor(val value: String)
@JvmInline value class UnixTimestamp(val seconds: Long)
@JvmInline value class EncryptedText(val value: String)
```

**Sealed Classes a Crear:**
```kotlin
// Estado de pairing
sealed class PairingStatus {
    object Unpaired : PairingStatus()
    data class GeneratingCode(val code: String) : PairingStatus()
    data class Paired(val partnerId: UserId) : PairingStatus()
}

// Estado de mensaje
sealed class MessageStatus {
    object Sent : MessageStatus()
    object Delivered : MessageStatus()
    object Read : MessageStatus()
}
```

---

### 3.3 Agregar Validación a Data Classes

**Data Classes con `init` validation:**
```kotlin
data class User(
    val uid: String,
    val email: String,
    // ...
) {
    init {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(email.matches(emailRegex)) { "Invalid email: $email" }
    }
}

data class Chat(
    val id: String,
    val memberIds: List<String>,
    // ...
) {
    init {
        require(id.isNotBlank()) { "Chat ID cannot be blank" }
        require(memberIds.size == 2) { "Couple chat must have 2 members" }
    }
}
```

---

## 📝 FASE 4: DOCUMENTACIÓN Y PREVENCIÓN (Días 15-21)

### 4.1 Actualizar Especificaciones

**Archivos:**
- [ ] `specs/technical.md` - Agregar patrones de error handling
- [ ] `specs/lessons.md` - Agregar lecciones de este audit
- [ ] `context/state.md` - Actualizar progreso

**Contenido para `specs/lessons.md`:**
```markdown
## 2026-03-26 - Silent Failure Audit

### Problema
23 fallos silenciosos encontrados en código.

### Causa Raíz
- Catch blocks vacíos
- runCatching sin manejo de error
- Falta de política explícita de error handling

### Solución
1. Nunca usar catch vacío
2. Siempre propagar errores o dar feedback al usuario
3. runCatching debe tener .onFailure {} o .getOrElse {}

### Prevención
- Agregar regla de code review: "¿Catch blocks vacíos?"
- Usar regla detekt: EmptyCatchBlock
```

---

### 4.2 Agregar Reglas de Code Review

**Checklist Pre-Commit:**
```markdown
## Checklist de Código

### Error Handling
- [ ] ¿Todos los catch blocks tienen logging o propagación?
- [ ] ¿runCatching tiene .onFailure {} o .getOrElse {}?
- [ ] ¿Los errores críticos se propagan al usuario?

### Testing
- [ ] ¿Tests escritos antes del código (TDD)?
- [ ] ¿Cobertura >80% para código nuevo?
- [ ] ¿Edge cases cubiertos (null, vacío, unicode)?

### Tipos
- [ ] ¿Data classes tienen validación en `init`?
- [ ] ¿Se usan value classes para identidades?
- [ ] ¿Sealed classes para estados mutuamente exclusivos?

### Seguridad
- [ ] ¿No hay credentials en código?
- [ ] ¿Inputs de usuario validados?
- [ ] ¿Errores no exponen detalles sensibles?
```

---

## 📊 CRONOGRAMA ESTIMADO

| Fase | Días | Entregables |
|------|------|-------------|
| **Fase 1: Crítico** | 1-3 | 52 errores compilación fixeados, 23 fallos silenciosos corregidos |
| **Fase 2: Tests** | 4-7 | 171 tests nuevos, cobertura 78% → 85% |
| **Fase 3: Arquitectura** | 8-14 | Use Case layer, value classes, sealed classes |
| **Fase 4: Documentación** | 15-21 | Specs actualizadas, checklist code review |

---

## ✅ CRITERIOS DE ACEPTACIÓN

### Fase 1 Completa Cuando:
- [ ] `./gradlew build` pasa sin errores
- [ ] 0 errores críticos en code review
- [ ] 0 fallos silenciosos críticos
- [ ] Credentials movidos a BuildConfig

### Fase 2 Completa Cuando:
- [ ] 241 tests totales (171 nuevos + 70 existentes)
- [ ] Cobertura >85% en Jacoco report
- [ ] Todos los tests pasan en CI

### Fase 3 Completa Cuando:
- [ ] 5 Use Cases implementados
- [ ] 10+ value classes creadas
- [ ] 3+ sealed classes para estados
- [ ] Clean Architecture documentada

### Fase 4 Completa Cuando:
- [ ] `specs/technical.md` actualizado
- [ ] `specs/lessons.md` con 5+ lecciones nuevas
- [ ] Checklist pre-commit implementado
- [ ] Team capacitado en patrones

---

## 🎯 MÉTRICAS DE ÉXITO

| Métrica | Línea Base | Meta | Medición |
|---------|------------|------|----------|
| Build Success | ❌ Falla | ✅ 100% | `./gradlew build` |
| Test Coverage | 72% | 85% | Jacoco report |
| Critical Issues | 52 | 0 | Code review |
| Silent Failures | 23 | 0 | Silent failure hunter |
| Lines/Function | 28 avg | <30 max | Detekt |

---

**Documento creado:** 2026-03-26  
**Próxima revisión:** 2026-04-02 (Fase 1 completa)  
**Responsable:** Equipo de desarrollo
