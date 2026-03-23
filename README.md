# 💕 Message App - Versión Supabase Corregida

App de mensajería romántica para 2 personas con cifrado E2E usando Android Keystore (AES-256-GCM).

## 🚨 IMPORTANTE - ERRORES CORREGIDOS

Esta versión corrige **8 errores críticos** encontrados en la migración anterior:

| Error | Corrección | Estado |
|-------|------------|--------|
| ❌ Imports de Supabase incorrectos | ✅ `io.github.jan.supabase` (no `io.github.jan-tennert`) | ✅ Corregido |
| ❌ OneSignal API obsoleta | ✅ `OneSignal.initialize()` (v5.6.1+) | ✅ Corregido |
| ❌ AuthRepository API incorrecta | ✅ `signUpWith()` y `signInWith()` correctos | ✅ Corregido |
| ❌ libsodium mal implementado | ✅ Android Keystore nativo | ✅ Corregido |
| ❌ UUID como TEXT en SQL | ✅ UUID nativo de PostgreSQL | ✅ Corregido |
| ❌ Dependencias incorrectas | ✅ Versions 2024-2025 verificadas | ✅ Corregido |
| ❌ getPlayerId() síncrono | ✅ Asíncrono con observer | ✅ Corregido |
| ❌ Falta validación de email | ✅ Pattern de email agregado | ✅ Corregido |

**Documentación completa de errores:** Ver `ERRORS_AND_FIXES.md`

---

## 📋 Configuración Rápida

### 1. Supabase (5 minutos)

```
1. Ve a https://supabase.com
2. Crea cuenta → New Project
3. Nombre: "chat-romantico"
4. Región: South America (Brazil)
5. Espera 2-3 minutos
6. Ve a Settings → API
7. Copia:
   - Project URL
   - anon/public key
```

### 2. Ejecutar SQL en Supabase

```
1. Ve a SQL Editor en Supabase
2. Copia TODO el contenido de database_schema.sql
3. Pega en el editor
4. Click en "Run"
5. ✅ Verifica en Table Editor que hay 4 tablas
```

### 3. OneSignal (3 minutos)

```
1. Ve a https://onesignal.com
2. Sign Up → New App
3. Nombre: "Message App"
4. Plataforma: Android
5. Copia el App ID
```

### 4. Configurar la App

Edita `app/src/main/java/com/example/messageapp/supabase/SupabaseConfig.kt`:

```kotlin
const val SUPABASE_URL = "https://tu-proyecto.supabase.co"
const val SUPABASE_ANON_KEY = "eyJhbGc..."  // Tu anon key
const val ONESIGNAL_APP_ID = "12345678-..."  // Tu App ID
```

### 5. Build y Test

```
1. Abre en Android Studio
2. Gradle Sync (espera a que termine)
3. Run en dispositivo/emulador
4. Prueba registro y chat
```

---

## 🔐 Seguridad

### Cifrado E2E

| Característica | Implementación |
|----------------|----------------|
| **Algoritmo** | AES-256-GCM |
| **Claves** | Android Keystore (hardware) |
| **Derivación** | Por chat (alias único) |
| **IV** | 96 bits aleatorio por mensaje |
| **Auth Tag** | 128 bits |
| **Formato** | `{iv}:{ciphertext}` (Base64) |

**Ventajas sobre libsodium-jni:**
- ✅ Integrado en el sistema operativo
- ✅ Usa hardware seguro (TEE/Secure Element)
- ✅ No requiere librerías nativas
- ✅ Las claves NUNCA salen del Keystore

### Row Level Security (RLS)

Todas las tablas tienen RLS activado:
- Users: Solo ves tu propio perfil
- Chats: Solo ves chats donde eres miembro
- Messages: Solo ves mensajes de tus chats
- Contacts: Solo ves tus contactos

---

## 🏗️ Arquitectura

```
app/
├── data/
│   ├── AuthRepository.kt          # Supabase Auth ✅
│   ├── ChatRepository.kt          # Supabase Postgrest + Realtime ✅
│   └── NotificationRepository.kt  # OneSignal 5.6.1+ ✅
├── crypto/
│   └── E2ECipher.kt               # Android Keystore AES-256-GCM ✅
├── model/
│   ├── User.kt
│   ├── Chat.kt
│   └── Message.kt
├── supabase/
│   └── SupabaseConfig.kt          # Configuración ✅
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── ChatListViewModel.kt
│   └── ChatViewModel.kt           # Cifrado/descifrado ✅
└── ui/
    ├── auth/
    ├── chat/
    ├── chatlist/
    └── ...
```

---

## 📱 Funcionalidades

### ✅ Autenticación
- [x] Registro con email/password
- [x] Login con email/password
- [x] Login anónimo (email temporal)
- [x] Logout con limpieza de claves
- [x] Gestión de sesión
- [x] Perfil de usuario

### ✅ Base de Datos
- [x] Tabla users (UUID, REFERENCES auth.users)
- [x] Tabla chats (UUID[], GIN index)
- [x] Tabla messages (UUID, CASCADE DELETE)
- [x] Tabla contacts (composite PK)
- [x] Row Level Security (RLS)
- [x] Triggers automáticos (updated_at)
- [x] Función `get_or_create_direct_chat()`

### ✅ Tiempo Real
- [x] WebSockets para chats
- [x] Actualización en tiempo real
- [x] Presencia online/offline
- [x] Reconexión automática

### ✅ Notificaciones
- [x] OneSignal 5.6.1+ integrado
- [x] Obtención de Player ID (asíncrono)
- [x] Actualización en Supabase
- [x] Permisos Android 13+
- [x] Canales de notificación

### ✅ Cifrado
- [x] Android Keystore AES-256-GCM
- [x] Claves por chat (alias único)
- [x] IV aleatorio por mensaje
- [x] Cifrado en ChatViewModel
- [x] Descifrado en ChatViewModel
- [x] Limpieza de claves en logout

---

## 🔧 Dependencias Principales

```kotlin
// Supabase (2.1.0)
implementation(platform("io.github.jan.supabase:bom:2.1.0"))
implementation("io.github.jan.supabase:supabase-kt")
implementation("io.github.jan.supabase:gotrue-kt")
implementation("io.github.jan.supabase:postgrest-kt")
implementation("io.github.jan.supabase:realtime-kt")

// OneSignal (5.6.1)
implementation("com.onesignal:OneSignal:5.6.1")

// Ktor
implementation("io.ktor:ktor-client-android:2.3.7")

// Kotlinx Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
```

---

## 🇨🇺 Consideraciones para Cuba

### OneSignal desde Cuba

| Estado | Notas |
|--------|-------|
| **Funciona** | ✅ No es Google |
| **Intermitencia** | ⚠️ Puede variar |
| **Backup** | 🔄 Notificaciones locales |

### Si OneSignal falla

La app incluye backup con notificaciones locales:
- Se muestran cuando abres la app
- Verifican mensajes nuevos en Supabase
- No requieren servicios externos

---

## 📝 Archivos de Configuración

| Archivo | Propósito | ¿Editar? |
|---------|-----------|----------|
| `SupabaseConfig.kt` | Credenciales | ✅ SÍ - Pon tus claves aquí |
| `database_schema.sql` | Tablas SQL | ✅ Ejecuta en Supabase |
| `ERRORS_AND_FIXES.md` | Errores corregidos | ℹ️ Solo lectura |
| `CONFIGURATION_GUIDE.md` | Guía paso a paso | ℹ️ Solo lectura |

---

## ⚠️ IMPORTANTE

### NUNCA subas credenciales reales a GitHub

```kotlin
// ❌ MAL - NO hacer esto
const val SUPABASE_URL = "https://mi-proyecto-real.supabase.co"
const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

// ✅ BIEN - Dejar placeholders
const val SUPABASE_URL = "https://TU_PROYECTO.supabase.co"
const val SUPABASE_ANON_KEY = "TU_ANON_KEY_AQUI"
```

### El archivo `.gitignore` ya incluye:

```
**/SupabaseConfig.kt
supabase_config.env
onesignal_config.env
```

---

## 🎯 Estado de la Migración

| Componente | Versión Anterior | Versión Actual | Estado |
|------------|------------------|----------------|--------|
| Supabase SDK | `io.github.jan-tennert` | `io.github.jan.supabase` | ✅ Corregido |
| OneSignal | `5.1.3` (API vieja) | `5.6.1` (API nueva) | ✅ Corregido |
| Cifrado | libsodium-jni | Android Keystore | ✅ Corregido |
| Database | TEXT para UUID | UUID nativo | ✅ Corregido |
| Auth API | Incorrecta | Correcta | ✅ Corregido |
| NotificationRepo | `getDeviceState()` | `pushSubscription.id` | ✅ Corregido |

---

## 🚀 Próximos Pasos (Features Románticos)

Estos features NO están incluidos. Se añadirán después:

- [ ] Tema de colores romántico (rosa/rojo)
- [ ] Enviar corazones animados
- [ ] Contador de días juntos
- [ ] Mensajes automáticos (buenos días/noches)
- [ ] Foto de pareja en perfil
- [ ] Estados de ánimo
- [ ] Galería de recuerdos
- [ ] Notas/recordatorios para la pareja

---

## 📞 Soporte

### Si tienes problemas:

1. **Revisa ERROS_AND_FIXES.md** - Lista completa de errores
2. **Verifica SupabaseConfig.kt** - ¿Pusiste las credenciales correctas?
3. **Revisa database_schema.sql** - ¿Ejecutaste el SQL en Supabase?
4. **Mira los logs** - `adb logcat | grep -i supabase`

### Errores comunes:

| Error | Solución |
|-------|----------|
| "Unresolved reference: io.github.jan-tennert" | ✅ Ya corregido en esta versión |
| "OneSignal.initialize() no existe" | ✅ Ya corregido (ahora usa 5.6.1) |
| "Table does not exist" | Ejecuta database_schema.sql |
| "Invalid API key" | Verifica credenciales en SupabaseConfig.kt |

---

## 📚 Recursos Oficiales

| Servicio | Documentación |
|----------|---------------|
| Supabase Kotlin | https://github.com/supabase-community/supabase-kt |
| Supabase Android | https://supabase.com/docs/guides/getting-started/quickstarts/android |
| OneSignal Android | https://documentation.onesignal.com/docs/en/android-sdk-setup |
| Android Keystore | https://developer.android.com/security/keystore |
| PostgreSQL UUID | https://www.postgresql.org/docs/current/datatype-uuid.html |

---

## 👥 Créditos

**Desarrollo original:**
- Gabriel Misao
- Caroline Cortes
- Angelo Toshio
- Joao Vitor

**Migración a Supabase:**
- Alain314159

**Corrección de errores:**
- Revisión exhaustiva con documentación oficial 2024-2025

---

## 📄 Licencia

Proyecto educativo - Universidad Federal de Uberlândia (UFU)

---

**Última actualización:** 2026-03-23
**Versión:** 2.1-supabase-fixed

---

## 🎉 ¡Listo!

Ahora tienes:
- ✅ **8 errores críticos corregidos**
- ✅ App migrada de Firebase a Supabase
- ✅ Cifrado E2E real con Android Keystore
- ✅ OneSignal 5.6.1+ para notificaciones
- ✅ Todo verificado con documentación oficial

**Solo falta:**
1. Configurar tus credenciales en `SupabaseConfig.kt`
2. Ejecutar `database_schema.sql` en Supabase
3. Hacer build y test
4. ¡Añadir features románticos!

💕🚀🇨🇺
