# 🚀 PLAN MAESTRO - Message App 100%

**Objetivo:** Llevar el proyecto a estado de producción (100% calidad)  
**Fecha Inicio:** 2026-03-26  
**Meta:** 0 errores críticos, 85%+ tests, arquitectura limpia

---

## 📋 ULTRA LISTA DE PASOS (200 tareas)

### FASE 0: PREPARACIÓN (Tareas 1-10)

- [ ] **0.1** Leer todos los archivos de configuración del proyecto
- [ ] **0.2** Verificar estructura de directorios
- [ ] **0.3** Identificar TODOS los archivos Kotlin del proyecto
- [ ] **0.4** Crear backup del estado actual
- [ ] **0.5** Establecer línea base de métricas
- [ ] **0.6** Configurar entorno de desarrollo
- [ ] **0.7** Verificar versiones de dependencias
- [ ] **0.8** Crear rama git para cambios mayores
- [ ] **0.9** Documentar estado inicial
- [ ] **0.10** Preparar sistema de tracking de progreso

---

### FASE 1: SEGURIDAD CRÍTICA (Tareas 11-25)

#### 1.1 Credenciales Hardcodeadas
- [ ] **1.1.1** Leer `SupabaseConfig.kt` completo
- [ ] **1.1.2** Identificar todas las credenciales hardcodeadas
- [ ] **1.1.3** Leer `build.gradle.kts` para configuración
- [ ] **1.1.4** Agregar configuración de BuildConfig en gradle
- [ ] **1.1.5** Crear plantilla de `gradle.properties.example`
- [ ] **1.1.6** Actualizar `.gitignore` para excluir `gradle.properties` local
- [ ] **1.1.7** Modificar `SupabaseConfig.kt` para usar BuildConfig
- [ ] **1.1.8** Verificar que no haya otras credenciales en otros archivos
- [ ] **1.1.9** Buscar patrones de API keys en todo el código
- [ ] **1.1.10** Documentar en specs/lessons.md

#### 1.2 Permisos y Seguridad
- [ ] **1.2.1** Leer `AndroidManifest.xml`
- [ ] **1.2.2** Verificar todos los permisos declarados
- [ ] **1.2.3** Identificar permisos innecesarios
- [ ] **1.2.4** Verificar permisos de runtime necesarios
- [ ] **1.2.5** Revisar configuración de red (HTTPS obligatorio)
- [ ] **1.2.6** Verificar certificate pinning (si existe)
- [ ] **1.2.7** Revisar almacenamiento de datos sensibles
- [ ] **1.2.8** Verificar uso de Android Keystore
- [ ] **1.2.9** Auditar E2ECipher por fugas de claves
- [ ] **1.2.10** Documentar hallazgos de seguridad
- [ ] **1.2.11** Crear checklist de seguridad
- [ ] **1.2.12** Agregar políticas de seguridad a specs/technical.md
- [ ] **1.2.13** Configurar ProGuard para ofuscación
- [ ] **1.2.14** Verificar que no haya logs con datos sensibles
- [ ] **1.2.15** Eliminar cualquier hardcoded secret restante

---

### FASE 2: ERRORES DE COMPILACIÓN (Tareas 26-80)

#### 2.1 Theme.kt (YA COMPLETADO - Tarea 26)
- [x] **2.1.1** Agregar Purple80, PurpleGrey80, Pink80 a Color.kt ✅
- [x] **2.1.2** Agregar Purple40, PurpleGrey40, Pink40 a Color.kt ✅
- [ ] **2.1.3** Verificar compilación de Theme.kt

#### 2.2 ChatInfoScreen.kt
- [ ] **2.2.1** Leer archivo completo
- [ ] **2.2.2** Identificar función helper fuera de scope
- [ ] **2.2.3** Mover Scaffold dentro del scope de la función
- [ ] **2.2.4** Verificar llaves de cierre
- [ ] **2.2.5** Corregir sintaxis
- [ ] **2.2.6** Verificar compilación

#### 2.3 AuthScreen.kt
- [ ] **2.3.1** Leer archivo completo
- [ ] **2.3.2** Identificar métodos con nombres incorrectos
- [ ] **2.3.3** Corregir `signInEmail` → `signInWithEmail`
- [ ] **2.3.4** Corregir `signUpEmail` → `signUpWithEmail`
- [ ] **2.3.5** Verificar método `sendPasswordReset`
- [ ] **2.3.6** Identificar métodos de phone auth inexistentes
- [ ] **2.3.7** Eliminar o implementar phone auth
- [ ] **2.3.8** Verificar compilación

#### 2.4 ChatScreen.kt
- [ ] **2.4.1** Leer archivo completo
- [ ] **2.4.2** Reemplazar `FirebaseAuth` con Supabase
- [ ] **2.4.3** Corregir `markRead` → `markAsRead`
- [ ] **2.4.4** Corregir `unpin` → `unpinMessage`
- [ ] **2.4.5** Corregir `pin` → `pinMessage`
- [ ] **2.4.6** Corregir `hideMessageForUser` → `deleteMessageForUser`
- [ ] **2.4.7** Eliminar referencia a `StorageAcl`
- [ ] **2.4.8** Verificar imports
- [ ] **2.4.9** Verificar compilación

#### 2.5 ChatComponents.kt
- [ ] **2.5.1** Leer archivo completo
- [ ] **2.5.2** Corregir `deliveredTo` → `deliveredAt`
- [ ] **2.5.3** Corregir `readBy` → `readAt`
- [ ] **2.5.4** Agregar import para `offset`
- [ ] **2.5.5** Agregar import para `graphicsLayer`
- [ ] **2.5.6** Verificar compilación

#### 2.6 MessageBubble.kt
- [ ] **2.6.1** Leer archivo completo
- [ ] **2.6.2** Agregar import para `detectTapGestures`
- [ ] **2.6.3** Verificar compilación

#### 2.7 ChatListScreen.kt
- [ ] **2.7.1** Leer archivo completo
- [ ] **2.7.2** Eliminar `@OptIn(ExperimentalMaterial3Api)`
- [ ] **2.7.3** Agregar import para `collectAsState`
- [ ] **2.7.4** Agregar import para `clickable`
- [ ] **2.7.5** Agregar import para `clip`
- [ ] **2.7.6** Corregir `visibleFor` (agregar o eliminar)
- [ ] **2.7.7** Corregir `name` → propiedad correcta
- [ ] **2.7.8** Corregir `photoUrl` → propiedad correcta
- [ ] **2.7.9** Corregir `members` → `memberIds`
- [ ] **2.7.10** Eliminar referencia a `lastMessage`
- [ ] **2.7.11** Corregir `ownerId` (agregar o eliminar)
- [ ] **2.7.12** Implementar o eliminar métodos de hide/unhide
- [ ] **2.7.13** Verificar compilación

#### 2.8 ChatListComponents.kt
- [ ] **2.8.1** Leer archivo completo
- [ ] **2.8.2** Verificar parámetros unused
- [ ] **2.8.3** Corregir warnings
- [ ] **2.8.4** Verificar compilación

#### 2.9 GroupCreateScreen.kt
- [ ] **2.9.1** Leer archivo completo
- [ ] **2.9.2** Migrar `FirebaseFirestore` → Supabase
- [ ] **2.9.3** Migrar `FirebaseStorage` → Supabase
- [ ] **2.9.4** Implementar o eliminar `createGroup`
- [ ] **2.9.5** Implementar o eliminar `updateGroupMeta`
- [ ] **2.9.6** Verificar compilación

#### 2.10 ProfileScreen.kt
- [ ] **2.10.1** Leer archivo completo
- [ ] **2.10.2** Migrar `FirebaseAuth` → Supabase
- [ ] **2.10.3** Migrar `FirebaseFirestore` → Supabase
- [ ] **2.10.4** Corregir `signOutAndRemoveToken` → `signOut`
- [ ] **2.10.5** Verificar compilación

#### 2.11 ChatHelpers.kt
- [ ] **2.11.1** Leer archivo completo
- [ ] **2.11.2** Migrar `FirebaseFirestore` → Supabase
- [ ] **2.11.3** Corregir null check en `Crypto.decrypt`
- [ ] **2.11.4** Verificar compilación

#### 2.12 ChatViewModel.kt
- [ ] **2.12.1** Leer archivo completo
- [ ] **2.12.2** Verificar método `cleanup()` en PresenceRepository
- [ ] **2.12.3** Implementar o eliminar llamada
- [ ] **2.12.4** Verificar compilación

#### 2.13 Avatar.kt
- [ ] **2.13.1** Leer archivo completo
- [ ] **2.13.2** Verificar recursos drawable
- [ ] **2.13.3** Crear placeholders si no existen
- [ ] **2.13.4** Verificar compilación

#### 2.14 StorageAcl.kt
- [ ] **2.14.1** Leer archivo completo
- [ ] **2.14.2** Migrar `FirebaseStorage` → Supabase Storage
- [ ] **2.14.3** O eliminar archivo si no es necesario
- [ ] **2.14.4** Verificar compilación

#### 2.15 JPushBroadcastReceiver.kt
- [ ] **2.15.1** Leer archivo completo
- [ ] **2.15.2** Verificar constante `EXTRA_RICHPUSH_FILE_PATH`
- [ ] **2.15.3** Eliminar variable unused `messageId`
- [ ] **2.15.4** Verificar compilación

#### 2.16 PresenceRepository.kt
- [ ] **2.16.1** Leer archivo completo
- [ ] **2.16.2** Corregir type mismatch en decode
- [ ] **2.16.3** Verificar compilación

#### 2.17 ChatRepository.kt
- [ ] **2.17.1** Leer archivo completo
- [ ] **2.17.2** Verificar import de Realtime
- [ ] **2.17.3** Agregar null check para `change.record`
- [ ] **2.17.4** Verificar compilación

#### 2.18 MediaRepository.kt
- [ ] **2.18.1** Leer archivo completo
- [ ] **2.18.2** Verificar API de Supabase Storage
- [ ] **2.18.3** Corregir `contentType` en upload
- [ ] **2.18.4** Corregir `createSignedUrl`
- [ ] **2.18.5** Verificar compilación

#### 2.19 StorageRepository.kt
- [ ] **2.19.1** Leer archivo completo
- [ ] **2.19.2** Corregir `upsert` en upload
- [ ] **2.19.3** Corregir `publicUrl`
- [ ] **2.19.4** Corregir lectura de URI con ContentResolver
- [ ] **2.19.5** Verificar compilación

#### 2.20 ProfileRepository.kt
- [ ] **2.20.1** Leer archivo completo
- [ ] **2.20.2** Corregir httpClient request
- [ ] **2.20.3** Usar ContentResolver para download
- [ ] **2.20.4** Verificar compilación

#### 2.21 MainActivity.kt
- [ ] **2.21.1** Leer archivo completo
- [ ] **2.21.2** Eliminar import redundante de ViewModel
- [ ] **2.21.3** Simplificar cast de ChatViewModel
- [ ] **2.21.4** Verificar compilación

---

### FASE 3: FALLOS SILENCIOSOS (Tareas 81-110)

#### 3.1 ChatViewModel.kt
- [x] **3.1.1** Corregir `markAsRead` catch vacío ✅ COMPLETADO
- [ ] **3.1.2** Verificar otros catch blocks en el archivo
- [ ] **3.1.3** Corregir cualquier otro fallo silencioso

#### 3.2 ChatHelpers.kt
- [ ] **3.2.1** Leer líneas 40, 46, 52, 58
- [ ] **3.2.2** Corregir `runCatching` en image picker
- [ ] **3.2.3** Corregir `runCatching` en video picker
- [ ] **3.2.4** Corregir `runCatching` en audio picker
- [ ] **3.2.5** Corregir `runCatching` en file picker
- [ ] **3.2.6** Agregar try-catch con logging apropiado
- [ ] **3.2.7** Verificar cambios

#### 3.3 ChatComponents.kt
- [ ] **3.3.1** Leer línea 86
- [ ] **3.3.2** Corregir `runCatching` en Intent de media
- [ ] **3.3.3** Agregar catch para `ActivityNotFoundException`
- [ ] **3.3.4** Agregar feedback al usuario
- [ ] **3.3.5** Verificar cambios

#### 3.4 ChatRepository.kt
- [ ] **3.4.1** Leer líneas 445-447
- [ ] **3.4.2** Corregir `countUnreadMessages` para propagar error
- [ ] **3.4.3** Agregar logging antes de propagar
- [ ] **3.4.4** Verificar cambios

#### 3.5 PresenceRepository.kt
- [ ] **3.5.1** Leer línea 216
- [ ] **3.5.2** Corregir `getPartnerLastSeen` para retornar Result
- [ ] **3.5.3** Agregar logging
- [ ] **3.5.4** Verificar línea 74 (`setTypingStatus`)
- [ ] **3.5.5** Corregir para retornar Result
- [ ] **3.5.6** Verificar cambios

#### 3.6 ProfileRepository.kt
- [ ] **3.6.1** Leer línea 40
- [ ] **3.6.2** Agregar logging en catch de `updateProfile`
- [ ] **3.6.3** Verificar cambios

#### 3.7 AvatarRepository.kt
- [ ] **3.7.1** Leer línea 29
- [ ] **3.7.2** Corregir `getUserAvatar` para retornar Result
- [ ] **3.7.3** Agregar logging
- [ ] **3.7.4** Verificar cambios

#### 3.8 NotificationRepository.kt
- [ ] **3.8.1** Leer línea 65
- [ ] **3.8.2** Corregir `initialize` para retornar boolean
- [ ] **3.8.3** Agregar verificación de estado
- [ ] **3.8.4** Verificar cambios

#### 3.9 E2ECipher.kt
- [ ] **3.9.1** Leer líneas 199-203
- [ ] **3.9.2** Corregir `deleteAllKeys` para propagar error
- [ ] **3.9.3** Verificar cambios

#### 3.10 AuthRepository.kt
- [ ] **3.10.1** Leer líneas 82, 212, 243
- [ ] **3.10.2** Corregir catch en getUserData
- [ ] **3.10.3** Corregir catch en upsertUserProfile
- [ ] **3.10.4** Verificar elvis operator en email
- [ ] **3.10.5** Verificar cambios

#### 3.11 PairingRepository.kt
- [ ] **3.11.1** Leer línea 145
- [ ] **3.11.2** Corregir catch para agregar logging
- [ ] **3.11.3** Verificar cambios

#### 3.12 StorageRepository.kt
- [ ] **3.12.1** Leer línea 102
- [ ] **3.12.2** Corregir catch para agregar logging
- [ ] **3.12.3** Verificar cambios

#### 3.13 ContactsRepository.kt
- [ ] **3.13.1** Leer línea 83
- [ ] **3.13.2** Corregir catch para agregar logging
- [ ] **3.13.3** Verificar cambios

---

### FASE 4: MIGRACIÓN FIREBASE → SUPABASE (Tareas 111-130)

- [ ] **4.1** Crear guía de migración Firebase → Supabase
- [ ] **4.2** Listar TODAS las referencias Firebase en el código
- [ ] **4.3** Mapear equivalentes Supabase
- [ ] **4.4** Migrar `FirebaseAuth` → `SupabaseClient.auth`
- [ ] **4.5** Migrar `FirebaseFirestore` → `SupabaseClient.plugin(Postgrest)`
- [ ] **4.6** Migrar `FirebaseStorage` → `SupabaseClient.storage`
- [ ] **4.7** Migrar `FirebaseMessaging` → JPush (ya implementado)
- [ ] **4.8** Actualizar imports en todos los archivos
- [ ] **4.9** Verificar que no queden referencias Firebase
- [ ] **4.10** Eliminar dependencias Firebase de build.gradle
- [ ] **4.11** Agregar dependencias Supabase faltantes
- [ ] **4.12** Actualizar AndroidManifest si es necesario
- [ ] **4.13** Probar autenticación con Supabase
- [ ] **4.14** Probar database operations con Supabase
- [ ] **4.15** Probar storage operations con Supabase
- [ ] **4.16** Verificar realtime subscriptions
- [ ] **4.17** Documentar migración en specs/technical.md
- [ ] **4.18** Actualizar README con nueva configuración
- [ ] **4.19** Crear script de verificación post-migración
- [ ] **4.20** Ejecutar tests de integración

---

### FASE 5: MEJORAR TESTS (Tareas 131-160)

#### 5.1 Mocks para Tests Existentes
- [ ] **5.1.1** Leer `AuthRepositoryTest.kt`
- [ ] **5.1.2** Agregar MockK como dependencia
- [ ] **5.1.3** Mockear SupabaseClient en tests
- [ ] **5.1.4** Reescribir tests para usar mocks
- [ ] **5.1.5** Verificar que tests no requieren red
- [ ] **5.1.6** Leer `ChatRepositoryTest.kt`
- [ ] **5.1.7** Mockear dependencias
- [ ] **5.1.8** Reescribir tests

#### 5.2 Tests de Edge Cases
- [ ] **5.2.1** Crear `AuthRepositoryEdgeCasesTest.kt`
- [ ] **5.2.2** Tests para emails inválidos
- [ ] **5.2.3** Tests para null inputs
- [ ] **5.2.4** Tests para unicode
- [ ] **5.2.5** Tests para SQL injection
- [ ] **5.2.6** Crear `ChatRepositoryEdgeCasesTest.kt`
- [ ] **5.2.7** Tests para IDs vacíos
- [ ] **5.2.8** Tests para mensajes largos
- [ ] **5.2.9** Tests para caracteres especiales

#### 5.3 Tests de Integración
- [ ] **5.3.1** Crear tests DB + Repository
- [ ] **5.3.2** Crear tests ViewModel + Repository
- [ ] **5.3.3** Crear tests Use Case + Repository
- [ ] **5.3.4** Tests de transacciones de Room
- [ ] **5.3.5** Tests de migraciones de DB

#### 5.4 Tests de UI (Compose Testing)
- [ ] **5.4.1** Configurar Compose Testing
- [ ] **5.4.2** Crear test para ChatScreen
- [ ] **5.4.3** Crear test para AuthScreen
- [ ] **5.4.4** Crear test para ChatListScreen
- [ ] **5.4.5** Tests de navegación
- [ ] **5.4.6** Tests de estados de UI

#### 5.5 Medición de Cobertura
- [ ] **5.5.1** Configurar Jacoco en build.gradle
- [ ] **5.5.2** Ejecutar tests con cobertura
- [ ] **5.5.3** Generar reporte Jacoco
- [ ] **5.5.4** Identificar archivos sin cobertura
- [ ] **5.5.5** Crear tests para archivos faltantes
- [ ] **5.5.6** Verificar >85% cobertura

---

### FASE 6: ARQUITECTURA CLEAN (Tareas 161-180)

#### 6.1 Use Case Layer
- [ ] **6.1.1** Crear directorio `domain/usecase/`
- [ ] **6.1.2** Crear `SendMessageUseCase.kt`
- [ ] **6.1.3** Crear `DeleteMessageUseCase.kt`
- [ ] **6.1.4** Crear `CreateChatUseCase.kt`
- [ ] **6.1.5** Crear `AuthenticateUserUseCase.kt`
- [ ] **6.1.6** Crear `GetUserAvatarUseCase.kt`
- [ ] **6.1.7** Crear `ObserveMessagesUseCase.kt`
- [ ] **6.1.8** Crear `MarkAsReadUseCase.kt`
- [ ] **6.1.9** Crear `SetTypingStatusUseCase.kt`
- [ ] **6.1.10** Actualizar ViewModels para usar Use Cases

#### 6.2 Value Classes
- [ ] **6.2.1** Crear `UserId.kt` value class
- [ ] **6.2.2** Crear `ChatId.kt` value class
- [ ] **6.2.3** Crear `MessageId.kt` value class
- [ ] **6.2.4** Crear `Email.kt` value class (con validación)
- [ ] **6.2.5** Crear `UnixTimestamp.kt` value class
- [ ] **6.2.6** Crear `EncryptedText.kt` value class
- [ ] **6.2.7** Actualizar modelos para usar value classes
- [ ] **6.2.8** Actualizar repositories para usar value classes

#### 6.3 Sealed Classes
- [ ] **6.3.1** Refactorizar `PairingStatus` a sealed class
- [ ] **6.3.2** Refactorizar `MessageStatus` a sealed class
- [ ] **6.3.3** Crear sealed class para errores de dominio
- [ ] **6.3.4** Actualizar repositorios para usar sealed classes

#### 6.4 Validación en Data Classes
- [ ] **6.4.1** Agregar `init` validation a `User`
- [ ] **6.4.2** Agregar `init` validation a `Chat`
- [ ] **6.4.3** Agregar `init` validation a `Message`
- [ ] **6.4.4** Agregar `init` validation a todas las data classes
- [ ] **6.4.5** Verificar tests después de validación

---

### FASE 7: DOCUMENTACIÓN (Tareas 181-195)

- [ ] **7.1** Actualizar `specs/technical.md` con patrones de error handling
- [ ] **7.2** Actualizar `specs/lessons.md` con lecciones de esta auditoría
- [ ] **7.3** Crear `SECURITY.md` con políticas de seguridad
- [ ] **7.4** Crear `CONTRIBUTING.md` con guías para contribuidores
- [ ] **7.5** Actualizar `README.md` con configuración completa
- [ ] **7.6** Crear checklist de code review en `.qwen/hooks/`
- [ ] **7.7** Documentar arquitectura en `ARCHITECTURE.md`
- [ ] **7.8** Crear diagramas de arquitectura
- [ ] **7.9** Documentar APIs de Supabase usadas
- [ ] **7.10** Crear guía de debugging
- [ ] **7.11** Crear guía de deployment
- [ ] **7.12** Documentar decisiones de diseño (ADRs)
- [ ] **7.13** Actualizar `context/state.md` con progreso
- [ ] **7.14** Actualizar `context/decisions.md` con nuevas decisiones
- [ ] **7.15** Crear CHANGELOG.md

---

### FASE 8: CI/CD Y AUTOMATIZACIÓN (Tareas 196-200)

- [ ] **8.1** Configurar GitHub Actions para builds automáticos
- [ ] **8.2** Configurar tests automáticos en CI
- [ ] **8.3** Configurar linting con detekt en CI
- [ ] **8.4** Configurar medición de cobertura en CI
- [ ] **8.5** Configurar deployment automático de releases

---

## 📊 PROGRESO ACTUAL

**Completado:** 3/200 tareas (1.5%)  
**En Progreso:** Fase 1, 2, 3  
**Próxima:** Continuar con Fase 2 (errores de compilación)

---

## 🎯 ESTADO POR FASE

| Fase | Total | Completadas | Progreso |
|------|-------|-------------|----------|
| **Fase 0: Preparación** | 10 | 0 | 0% |
| **Fase 1: Seguridad** | 15 | 0 | 0% |
| **Fase 2: Compilación** | 55 | 2 | 4% |
| **Fase 3: Silent Failures** | 30 | 1 | 3% |
| **Fase 4: Migración** | 20 | 0 | 0% |
| **Fase 5: Tests** | 30 | 0 | 0% |
| **Fase 6: Arquitectura** | 20 | 0 | 0% |
| **Fase 7: Docs** | 15 | 0 | 0% |
| **Fase 8: CI/CD** | 5 | 0 | 0% |
| **TOTAL** | **200** | **3** | **1.5%** |

---

**Última Actualización:** 2026-03-26  
**Próximo Hito:** Fase 2 completa (55 tareas de compilación)
