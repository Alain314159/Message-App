# 💕 Message App - Versión Supabase

App de mensajería romántica para 2 personas con cifrado E2E usando libsodium (AES-256-GCM).

## 🚀 Migración Completada

Esta versión ha sido migrada de Firebase a **Supabase + OneSignal** para:
- ✅ Funcionar en Cuba (sin dependencia de Google)
- ✅ Cifrado E2E real con libsodium
- ✅ Notificaciones push con OneSignal
- ✅ Tiempo real con WebSockets

---

## 📋 Configuración Requerida

### 1. Supabase

1. Ve a https://supabase.com y crea una cuenta
2. Crea un nuevo proyecto
3. Ve a **Settings → API** y copia:
   - `Project URL` → `SUPABASE_URL`
   - `anon/public key` → `SUPABASE_ANON_KEY`

4. Ve a **SQL Editor** y ejecuta el script `database_schema.sql`

### 2. OneSignal

1. Ve a https://onesignal.com y crea una cuenta
2. Crea una nueva app (Android)
3. Copia:
   - `App ID` → `ONESIGNAL_APP_ID`
   - `REST API Key` → `ONESIGNAL_REST_API_KEY` (para el servidor)

### 3. Configurar la App

Edita `app/src/main/java/com/example/messageapp/supabase/SupabaseConfig.kt`:

```kotlin
const val SUPABASE_URL = "https://tu-proyecto.supabase.co"
const val SUPABASE_ANON_KEY = "tu-anon-key"
const val ONESIGNAL_APP_ID = "tu-onesignal-app-id"
```

---

## 🔐 Seguridad

### Cifrado E2E

- **Algoritmo:** AES-256-GCM con libsodium
- **Claves:** Guardadas en Android Keystore (hardware)
- **Derivación:** HKDF-SHA256 por chat
- **Formato:** `{nonce}:{ciphertext}:{authTag}` (Base64)

### Seguridad de Datos

- Row Level Security (RLS) en todas las tablas
- Solo los miembros del chat pueden ver mensajes
- Las claves NUNCA salen del dispositivo

---

## 🏗️ Arquitectura

```
app/
├── data/
│   ├── AuthRepository.kt          # Supabase Auth
│   ├── ChatRepository.kt          # Supabase Postgrest + Realtime
│   └── NotificationRepository.kt  # OneSignal
├── crypto/
│   ├── E2ECipher.kt               # AES-256-GCM con libsodium
│   └── SecureKeyManager.kt        # Android Keystore
├── model/
│   ├── User.kt
│   ├── Chat.kt
│   └── Message.kt
├── supabase/
│   └── SupabaseConfig.kt          # Configuración
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── ChatListViewModel.kt
│   └── ChatViewModel.kt
└── ui/
    ├── auth/
    ├── chat/
    ├── chatlist/
    └── ...
```

---

## 📱 Funcionalidades Actuales

- ✅ Login con email/password
- ✅ Login anónimo
- ✅ Chat 1:1 en tiempo real
- ✅ Mensajes cifrados E2E
- ✅ Estado de entrega/lectura
- ✅ Mensajes fijados
- ✅ Eliminar mensajes (para mí/para todos)
- ✅ Notificaciones push (OneSignal)
- ✅ Presencia online/offline

---

## 🔧 Próximos Pasos (Features Románticos)

- [ ] Tema de colores romántico (rosa/rojo)
- [ ] Enviar corazones animados
- [ ] Contador de días juntos
- [ ] Mensajes automáticos (buenos días/noches)
- [ ] Foto de pareja en perfil
- [ ] Estados de ánimo
- [ ] Galería de recuerdos

---

## 🇨🇺 Consideraciones para Cuba

### OneSignal desde Cuba

- ✅ Funciona (no es Google)
- ⚠️ Puede tener intermitencia
- 🔄 Backup: notificaciones locales

### Si OneSignal falla

La app usará notificaciones locales que:
- Se muestran cuando abres la app
- Verifican mensajes nuevos en Supabase
- No requieren servicios externos

---

## 🛠️ Desarrollo

### Dependencias Principales

```kotlin
// Supabase
implementation(platform("io.github.jan-tennert.supabase:bom:2.1.0"))
implementation("io.github.jan-tennert.supabase:core")
implementation("io.github.jan-tennert.supabase:postgrest-kt")
implementation("io.github.jan-tennert.supabase:gotrue-kt")
implementation("io.github.jan-tennert.supabase:realtime-kt")

// OneSignal
implementation("com.onesignal:OneSignal:5.1.3")

// libsodium
implementation("org.libsodium:libsodium-jni:1.0.18")
```

### Build

```bash
./gradlew assembleDebug
```

---

## 📄 Archivos de Configuración

| Archivo | Propósito |
|---------|-----------|
| `SupabaseConfig.kt` | Credenciales de Supabase y OneSignal |
| `database_schema.sql` | Esquema de base de datos |
| `supabase_config.env` | Template de configuración (no usar en prod) |

---

## ⚠️ IMPORTANTE

1. **NUNCA** subas `SupabaseConfig.kt` con credenciales reales a GitHub
2. Usa variables de entorno o BuildConfig para producción
3. Las claves de Supabase son públicas (anon key) pero la REST API key es secreta

---

## 📝 Estado de la Migración

| Componente | Estado |
|------------|--------|
| Supabase Auth | ✅ Completado |
| Supabase Database | ✅ Completado |
| Supabase Realtime | ✅ Completado |
| OneSignal | ✅ Completado |
| Cifrado E2E (libsodium) | ✅ Completado |
| UI existente | ⚠️ Sin cambios (funciona) |
| Features románticos | ⏳ Pendiente |

---

## 🤝 Contribuciones

Esta app es parte del proyecto de PDM - Universidad Federal de Uberlândia (UFU).

Alumnos originales:
- Gabriel Misao
- Caroline Cortes
- Angelo Toshio
- Joao Vitor

Migración a Supabase por: Alain314159

---

## 📞 Soporte

Para issues relacionados con:
- **Supabase:** Revisa la configuración en `SupabaseConfig.kt`
- **OneSignal:** Verifica el App ID y permisos de notificación
- **Cifrado:** Las claves están en Android Keystore (se borran al desinstalar)

---

## 🎯 Siguiente Paso

1. Configura tus credenciales en `SupabaseConfig.kt`
2. Ejecuta `database_schema.sql` en Supabase SQL Editor
3. Build y run en Android Studio
4. ¡Prueba la app!

---

**Hecho con 💕 para Cuba**
