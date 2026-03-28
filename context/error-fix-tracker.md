# Error Fix Tracker - Message App

## Sesión: 2026-03-27 - Corrección Obsesiva-Compulsiva de Errores

### Progreso General
- **Total Errores:** 116
- **Críticos:** 15 (0% completado)
- **Altos:** 25 (0% completado)
- **Medios:** 30 (0% completado)
- **Bajos:** 46 (0% completado)

---

## 🔴 ERRORES CRÍTICOS (Semana 1)

| # | Error | Estado | Archivo | Línea |
|---|-------|--------|---------|-------|
| 1 | PresenceRepository.observePartnerOnline lógica incorrecta | ✅ CORREGIDO | PresenceRepository.kt | 67-73 |
| 2 | MessageEntity no coincide con Message | ✅ CORREGIDO | MessageEntity.kt | 12-22 |
| 3 | Room Database sin usar pero configurada | ⏳ Pendiente | Múltiples | - |
| 4 | ChatRepository.observeChat error handling | ✅ CORREGIDO | ChatRepository.kt | 88-112 |
| 5 | ChatViewModel.decryptMessage currentChatId | ✅ CORREGIDO | ChatViewModel.kt | 156-172 |
| 6 | BuildConfig validation faltante | ⏳ Pendiente | BuildConfig | - |
| 7 | MainActivity arquitectura con repositories | ⏳ Pendiente | MainActivity.kt | 24 |
| 8 | Message.status no maneja deletedForAll | ✅ CORREGIDO | Message.kt | 52-58 |
| 9 | User.uid debería ser User.id | ✅ CORREGIDO | User.kt | 8 |
| 10 | AuthViewModel sin estado de error | ✅ CORREGIDO | AuthViewModel.kt | 20-30 |
| 11 | ChatListViewModel retry logic incorrecta | ✅ CORREGIDO | ChatListViewModel.kt | 45-60 |
| 12 | E2ECipher empty string validation | ✅ CORREGIDO | E2ECipher.kt | 89-95 |
| 13 | ChatScreen myUid handling | ⏳ Pendiente | ChatScreen.kt | 55 |
| 14 | pgcrypto faltante en database_schema.sql | ⏳ Pendiente | database_schema.sql | - |
| 15 | specs/technical.md versiones desactualizadas | ⏳ Pendiente | specs/technical.md | 10-30 |

---

## 🟠 ERRORES ALTOS (Semana 2)

| # | Error | Estado | Archivo | Línea |
|---|-------|--------|---------|-------|
| 16 | PresenceRepository no verifica conexión | ⏳ Pendiente | PresenceRepository.kt | 45 |
| 17 | ChatRepository.sendText sin validar mensaje | ⏳ Pendiente | ChatRepository.kt | 218 |
| 18 | ChatViewModel.sendText sin validar | ⏳ Pendiente | ChatViewModel.kt | 95 |
| 19 | AuthRepository sin validar email antes | ✅ CORREGIDO | AuthRepository.kt | 50 |
| 20 | E2ECipher.generateKey sin thread safety | ⏳ Pendiente | E2ECipher.kt | 60 |
| 21 | NotificationRepository no existe | ✅ CORREGIDO | MainActivity.kt | 24 |
| 22 | StorageRepository no existe | ⏳ Pendiente | ChatScreen.kt | 54 |
| 23 | rememberMediaPickers no definido | ⏳ Pendiente | ChatScreen.kt | 56 |
| 24 | rememberUsers no definido | ⏳ Pendiente | ChatScreen.kt | 57 |
| 25 | rememberGroupedMessagesWithAuthors incompleto | ⏳ Pendiente | ChatScreen.kt | 58 |
| 26 | rememberSearchMatches sin verificar | ⏳ Pendiente | ChatScreen.kt | 59 |
| 27 | ChatActionsDialogState no definido | ⏳ Pendiente | ChatScreen.kt | 115 |
| 28 | ChatTopBarState sin verificar | ⏳ Pendiente | ChatScreen.kt | 75 |
| 29 | PinnedMessageBar no definido | ⏳ Pendiente | ChatScreen.kt | 92 |
| 30 | SearchNavigation no definido | ⏳ Pendiente | ChatScreen.kt | 99 |
| 31 | ChatAttachmentBar no definido | ⏳ Pendiente | ChatScreen.kt | 106 |
| 32 | ChatMessageInput no definido | ⏳ Pendiente | ChatScreen.kt | 107 |
| 33 | ChatMessageList no definido | ⏳ Pendiente | ChatScreen.kt | 105 |
| 34 | rememberMediaPickers tipo no especificado | ⏳ Pendiente | ChatScreen.kt | 56 |
| 35 | AuthRepository.createUserProfile race condition | ⏳ Pendiente | AuthRepository.kt | 162 |
| 36 | AuthRepository.upsertUserProfile race condition | ⏳ Pendiente | AuthRepository.kt | 184 |
| 37 | E2ECipher.deleteAllKeys sin manejar excepción | ✅ CORREGIDO | E2ECipher.kt | 192 |
| 38 | E2ECipher sin verificar Keystore disponible | ✅ CORREGIDO | E2ECipher.kt | 40 |
| 39 | ChatRepository.ensureDirectChat lógica incorrecta | ⏳ Pendiente | ChatRepository.kt | 48 |
| 40 | ChatRepository.observeChats filtro incorrecto | ⏳ Pendiente | ChatRepository.kt | 88 |

---

## 🟡 ERRORES MEDIOS (Semana 3)

| # | Error | Estado | Archivo | Línea |
|---|-------|--------|---------|-------|
| 41 | Logging inconsistente - múltiples tags | ⏳ Pendiente | Múltiples | - |
| 42 | Funciones >30 líneas en ChatRepository | ⏳ Pendiente | ChatRepository.kt | 88,163 |
| 43 | Funciones >30 líneas en ChatViewModel | ⏳ Pendiente | ChatViewModel.kt | 46 |
| 44 | ChatRepository.sendText Dispatchers.IO | ⏳ Pendiente | ChatRepository.kt | 218 |
| 45 | AuthRepository con Context innecesario | ⏳ Pendiente | AuthRepository.kt | - |
| 46 | MessageEntity sin constructor secundario | ✅ CORREGIDO | MessageEntity.kt | 12 |
| 47 | Message sin toEntity() | ✅ CORREGIDO | MessageMappers.kt | - |
| 48 | Chat sin toEntity() | ✅ CORREGIDO | ChatMappers.kt | - |
| 49 | User sin toEntity() | ✅ CORREGIDO | UserMappers.kt | - |
| 50 | specs/functional.md incompleto | ⏳ Pendiente | specs/functional.md | 30 |
| 51 | specs/technical.md no menciona JPush | ⏳ Pendiente | specs/technical.md | - |
| 52 | context/state.md desactualizado | ⏳ Pendiente | context/state.md | - |
| 53 | context/decisions.md sin ADR JPush | ⏳ Pendiente | context/decisions.md | - |
| 54 | README.md desactualizado | ⏳ Pendiente | README.md | 30 |
| 55 | database_schema.sql idiomas mezclados | ⏳ Pendiente | database_schema.sql | - |
| 56 | Message.status sin deletedForAll | ⏳ Pendiente | Message.kt | 52 |
| 57 | Chat.isUserTyping sin manejar caso | ⏳ Pendiente | Chat.kt | 47 |
| 58 | E2ECipher.toHex() no usada | ⏳ Pendiente | E2ECipher.kt | 228 |
| 59 | AuthViewModelTest signOut incompleto | ⏳ Pendiente | AuthViewModelTest.kt | - |
| 60 | ChatRepositoryTest solo una función | ⏳ Pendiente | ChatRepositoryTest.kt | - |
| 61 | PresenceRepositoryTest sin timeout | ⏳ Pendiente | PresenceRepositoryTest.kt | - |
| 62 | E2ECipherTest sin key rotation | ⏳ Pendiente | E2ECipherTest.kt | - |
| 63 | Models sin tests serialización | ⏳ Pendiente | ModelsTest.kt | - |
| 64 | TimeUtilsTest sin timezones | ⏳ Pendiente | TimeUtilsTest.kt | - |
| 65 | MessageDaoTest requiere Robolectric | ⏳ Pendiente | MessageDaoTest.kt | - |
| 66 | build.gradle.kts sin Jacoco | ⏳ Pendiente | build.gradle.kts | - |
| 67 | build.gradle.kts sin lint config | ⏳ Pendiente | build.gradle.kts | - |
| 68 | build.gradle.kts sin detekt | ⏳ Pendiente | build.gradle.kts | - |
| 69 | .gitignore incompleto | ⏳ Pendiente | .gitignore | - |
| 70 | gradle.properties.example inseguro | ⏳ Pendiente | gradle.properties.example | - |

---

## 🟢 ERRORES BAJOS (Semana 4)

| # | Error | Estado | Archivo | Línea |
|---|-------|--------|---------|-------|
| 71 | Typo "Ir al fim" | ⏳ Pendiente | ChatScreen.kt | 88 |
| 72 | Imports desordenados | ⏳ Pendiente | Múltiples | - |
| 73 | KDoc faltante | ⏳ Pendiente | Múltiples | - |
| 74 | Nombres no descriptivos | ⏳ Pendiente | Múltiples | - |
| 75 | Magic numbers | ⏳ Pendiente | Múltiples | - |
| 76 | Comentarios español/inglés | ⏳ Pendiente | Múltiples | - |
| 77 | TODOs sin fecha | ⏳ Pendiente | Múltiples | - |
| 78 | FIXMEs sin explicación | ⏳ Pendiente | Múltiples | - |
| 79 | OptIn sin justificación | ⏳ Pendiente | Múltiples | - |
| 80 | contentDescription = null | ⏳ Pendiente | Múltiples | 17 |

*(Errores 81-116 pendientes de detallar)*

---

## Lecciones Aprendidas

### Sesión 1: 2026-03-27 - Corrección Obsesiva-Compulsiva
- **Problema:** 116 errores encontrados en todo el proyecto
- **Causa raíz:** Falta de revisión sistemática continua
- **Solución:** Este tracker obsesivo-compulsivo
- **Prevención:** Revisión automática con agents antes de cada commit

### Errores Corregidos en Esta Sesión:
1. ✅ **ERROR #1** - PresenceRepository.observePartnerOnline: Tipo incorrecto (Chat en lugar de UserStatusResponse)
2. ✅ **ERROR #2** - MessageEntity: Agregados todos los campos faltantes para coincidir con Message
3. ✅ **ERROR #4** - ChatRepository.observeChat: Mejorado error handling con logging consistente
4. ✅ **ERROR #5** - ChatViewModel.decryptMessage: Validación de currentChatId mejorada con logging
5. ✅ **ERROR #8** - Message.status: Ahora retorna null cuando deletedForAll=true
6. ✅ **ERROR #9** - User.uid → User.id: Renombrado para consistencia con la base de datos
7. ✅ **ERROR #10** - AuthViewModel: Agregado estado de error (_error, isLoading)
8. ✅ **ERROR #11** - ChatListViewModel: Agregado mecanismo de retry con backoff exponencial
9. ✅ **ERROR #12** - E2ECipher.encrypt: Validación de empty string mejorada con require()
10. ✅ **ERROR #19** - AuthRepository.signInWithEmail: Validación de email agregada
11. ✅ **ERROR #21** - NotificationRepository: Eliminado (no existe, usar JPush directamente)
12. ✅ **ERROR #37** - E2ECipher.deleteAllKeys: Manejo de excepción de KeyStore.load()
13. ✅ **ERROR #38** - E2ECipher: Agregada función isKeystoreAvailable()
14. ✅ **ERROR #41** - Logging inconsistente: TAG constante "MessageApp" en múltiples archivos
15. ✅ **ERROR #46** - MessageEntity: Agregados campos faltantes (type, mediaUrl, deliveredAt, readAt, deletedForAll, deletedFor)
16. ✅ **ERROR #47** - MessageMappers.kt: Creadas funciones toEntity() y toDomain()
17. ✅ **ERROR #48** - ChatMappers.kt: Creadas funciones toEntity() y toDomain()
18. ✅ **ERROR #49** - UserMappers.kt: Creadas funciones toEntity() y toDomain()
19. ✅ **ERROR #57** - Chat.isUserTyping: Ahora lanza excepción cuando userId no está en el chat

### Progreso Actual:
- **Críticos:** 8/15 (53%) ✅
- **Altos:** 3/25 (12%) ✅
- **Medios:** 6/30 (20%) ✅
- **Bajos:** 0/46 (0%)
- **Total:** 19/116 (16%)

### Archivos Creados:
- `app/src/main/java/com/example/messageapp/data/room/MessageMappers.kt`
- `app/src/main/java/com/example/messageapp/data/room/ChatEntity.kt`
- `app/src/main/java/com/example/messageapp/data/room/ChatMappers.kt`
- `app/src/main/java/com/example/messageapp/data/room/UserEntity.kt`
- `app/src/main/java/com/example/messageapp/data/room/UserMappers.kt`

### Archivos Modificados:
- `app/src/main/java/com/example/messageapp/data/PresenceRepository.kt` - TAG constante, logging consistente, UserStatusResponse
- `app/src/main/java/com/example/messageapp/data/ChatRepository.kt` - Logging consistente, mejor error handling
- `app/src/main/java/com/example/messageapp/data/AuthRepository.kt` - Validación email, logging consistente, decodeSingleOrNull
- `app/src/main/java/com/example/messageapp/crypto/E2ECipher.kt` - Validación mejorada, isKeystoreAvailable(), logging TAG
- `app/src/main/java/com/example/messageapp/viewmodel/ChatViewModel.kt` - Logging, validación currentChatId
- `app/src/main/java/com/example/messageapp/viewmodel/AuthViewModel.kt` - Estado de error, isLoading, logging
- `app/src/main/java/com/example/messageapp/viewmodel/ChatListViewModel.kt` - Mecanismo de retry, cache myUid
- `app/src/main/java/com/example/messageapp/MainActivity.kt` - Eliminado NotificationRepository inexistente
- `app/src/main/java/com/example/messageapp/model/Message.kt` - status retorna null para deletedForAll
- `app/src/main/java/com/example/messageapp/model/User.kt` - uid → id
- `app/src/main/java/com/example/messageapp/model/Chat.kt` - isUserTyping con excepción
- `app/src/main/java/com/example/messageapp/data/room/MessageEntity.kt` - Campos agregados
- `app/src/main/java/com/example/messageapp/ui/contacts/ContactsScreen.kt` - contact.uid → contact.id
- `app/src/main/java/com/example/messageapp/ui/groups/GroupCreateScreen.kt` - u.uid → u.id
- `app/src/main/java/com/example/messageapp/ui/pairing/FindPartnerScreen.kt` - foundUser.uid → foundUser.id
- `app/src/main/java/com/example/messageapp/ui/chat/ChatInfoScreen.kt` - m.uid → m.id

---

## Próxima Sesión
- Continuar con errores críticos restantes:
  - #3 Room Database sin usar pero configurada
  - #6 BuildConfig validation faltante
  - #7 MainActivity arquitectura con repositories
  - #11 ChatListViewModel retry logic incorrecta
  - #13 ChatScreen myUid handling
  - #14 pgcrypto faltante en database_schema.sql
  - #15 specs/technical.md versiones desactualizadas
- Priorizar errores que causan crashes
- Actualizar specs/lessons.md después de cada fix
- Revisar tests para actualizar referencias a .uid

