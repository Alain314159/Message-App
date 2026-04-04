# 📋 ESPECIFICACIÓN TÉCNICA FINAL - Message App Romántica

## 🎯 CONFIGURACIÓN FINAL

| Componente | Elección | Implementación |
|------------|----------|----------------|
| **Backend** | Supabase | PostgreSQL + Realtime + Auth + Storage |
| **Login** | Email + Google | Dual auth con fallback |
| **Cifrado** | Android Keystore | AES-256-GCM por chat |
| **Pareja** | Código 6 dígitos + Email | Búsqueda e invitación |
| **Frontend** | Corregir existente | Migración Firebase→Supabase |
| **Push** | Firebase Cloud Messaging (FCM) | Implementación actual |
| **Multimedia** | Supabase Storage | Solo Storage (sin Cloudinary) |
| **Presencia** | Completa | Online + Typing + Last seen |
| **Ticks** | WhatsApp-style | Gris Koala / Rosa Chanchita |
| **UI** | Material 3 | Tema romántico personalizado |
| **Build** | Gradle 8.x + Kotlin 1.9.22 | Validación de credenciales |

---

## 🎨 PALETA DE COLORES PERSONALIZADA

### Colores de Ticks de Mensajes

| Estado | Color | Hex | Uso |
|--------|-------|-----|-----|
| **Enviado** | Gris Koala | `#8E8E93` | 1 tick |
| **Entregado** | Gris Koala | `#8E8E93` | 2 ticks |
| **Leído** | Rosa Chanchita | `#FF69B4` | 2 ticks |

### Colores del Tema

| Color | Hex | Uso |
|-------|-----|-----|
| **Rosa Chanchita** | `#FFB6C1` | Primary, accents |
| **Rosa Chanchita Dark** | `#FF69B4` | Primary variant, ticks de leído |
| **Gris Koala** | `#8E8E93` | Texto secundario, ticks de entregado |
| **Gris Koala Light** | `#F2F2F7` | Fondos suaves |
| **Blanco Humo** | `#F5F5F5` | Fondo de pantalla |
| **Verde Online** | `#34C759` | Indicador de presencia |

---

## 🗄️ DATABASE SCHEMA (PostgreSQL)

### Tabla: users

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY REFERENCES auth.users(id),
    email TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL DEFAULT 'Usuario',
    photo_url TEXT,              -- URL de Supabase Storage
    
    -- Sistema de emparejamiento
    pairing_code VARCHAR(6) UNIQUE,
    partner_id UUID REFERENCES users(id),
    is_paired BOOLEAN DEFAULT FALSE,
    
    -- Presencia
    is_online BOOLEAN DEFAULT FALSE,
    last_seen BIGINT,
    is_typing BOOLEAN DEFAULT FALSE,
    
    -- Notificaciones
    onesignal_player_id TEXT,
    
    created_at BIGINT,
    updated_at BIGINT
);
```

### Tabla: messages

```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    chat_id UUID REFERENCES chats(id),
    sender_id UUID REFERENCES users(id),
    type TEXT DEFAULT 'text',
    text_enc TEXT,               -- Cifrado con Android Keystore
    media_url TEXT,              -- URL de Supabase Storage
    nonce TEXT,                  -- IV para AES-256-GCM
    
    -- Estados de mensaje
    created_at BIGINT,
    delivered_at BIGINT,         -- Cuando llegó al dispositivo
    read_at BIGINT,              -- Cuando abrió el chat
    
    deleted_for_all BOOLEAN DEFAULT FALSE,
    deleted_for UUID[] DEFAULT '{}'
);
```

---

## 📦 DEPENDENCIAS FINALES

### Build.gradle.kts (App)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.18"
    id("com.google.gms.google-services") // Para FCM
}

android {
    namespace = "com.example.messageapp"
    compileSdk = 35
    minSdk = 26
    targetSdk = 35
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Supabase 3.4.1 (Marzo 2026)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.4.1"))
    implementation("io.github.jan-tennert.supabase:supabase-kt:3.4.1")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.4.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.4.1")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.4.1")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.4.1")

    // Ktor 3.3.0 (requerido por Supabase 3.x)
    implementation("io.ktor:ktor-client-android:3.3.0")
    implementation("io.ktor:ktor-client-core:3.3.0")

    // Firebase Cloud Messaging - Solo notificaciones push
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")

    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
}
```

---

## 🏗️ ARQUITECTURA DEL PROYECTO

```
app/src/main/java/com/example/messageapp/
├── core/
│   ├── App.kt                    # Inicialización FCM
│   └── SessionManager.kt
├── data/
│   ├── AuthRepository.kt         # Facade (deprecated)
│   ├── AuthReadRepository.kt     # Lectura de auth
│   ├── AuthWriteRepository.kt    # Escritura de auth
│   ├── AuthProfileRepository.kt  # Perfil de usuario
│   ├── ChatReadRepository.kt     # Lectura de chats
│   ├── ChatRepository.kt         # Facade (deprecated)
│   ├── MessageRepository.kt      # Mensajes + Realtime
│   ├── MessageActionsRepository.kt # Acciones de mensajes
│   ├── PairingRepository.kt      # Código 6 dígitos
│   ├── MediaRepository.kt        # Supabase Storage
│   ├── NotificationRepository.kt # Facade FCM
│   ├── FCMConfigRepository.kt    # Configuración FCM
│   ├── FCMTokenRepository.kt     # Tokens FCM
│   ├── FCMLifecycleRepository.kt # Ciclo de vida FCM
│   ├── PresenceRepository.kt     # Typing + Online
│   ├── ContactsRepository.kt     # Contactos
│   ├── StorageRepository.kt      # Almacenamiento
│   └── ProfileRepository.kt      # Perfil completo
├── crypto/
│   ├── E2ECipher.kt              # Android Keystore
│   └── MessageDecryptor.kt       # Desencriptación
├── model/
│   ├── User.kt
│   ├── Chat.kt
│   ├── Message.kt                # Con estados
│   ├── Avatar.kt
│   └── ThemeModels.kt            # Colores y temas
├── supabase/
│   └── SupabaseConfig.kt         # ⭐ Cliente Supabase (NUEVO)
├── push/
│   ├── FCMMessageService.kt      # Servicio FCM
│   └── JPushBroadcastReceiver.kt # Legacy (no usado)
├── ui/
│   ├── theme/
│   │   ├── Color.kt              # Rosa Chanchita + Gris Koala
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── auth/
│   │   └── AuthScreen.kt
│   ├── pairing/
│   │   ├── PairingScreen.kt
│   │   └── FindPartnerScreen.kt
│   ├── chat/
│   │   ├── ChatScreen.kt
│   │   ├── ChatInputBar.kt
│   │   ├── ChatMessageList.kt
│   │   ├── ChatTopBar.kt
│   │   ├── ChatComponents.kt
│   │   ├── ChatHelpers.kt
│   │   ├── ChatInfoScreen.kt
│   │   ├── ChatActionsDialog.kt
│   │   └── MessageBubble.kt
│   ├── chatlist/
│   │   ├── ChatListScreen.kt
│   │   ├── ChatsTab.kt
│   │   └── ChatListComponents.kt
│   ├── profile/
│   │   └── ProfileScreen.kt
│   ├── contacts/
│   │   ├── ContactsScreen.kt
│   │   ├── DeviceContacts.kt
│   │   └── ContactsPermissions.kt
│   ├── groups/
│   │   └── GroupCreateScreen.kt
│   └── avatar/
│       └── AvatarPickerScreen.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── ChatViewModel.kt
│   ├── ChatListViewModel.kt
│   ├── MessageActionsViewModel.kt
│   ├── PresenceViewModel.kt
│   ├── PairingViewModel.kt
│   └── AvatarViewModel.kt
├── utils/
│   ├── Time.kt
│   ├── Crypto.kt
│   ├── Contacts.kt
│   └── SignatureLogger.kt
└── storage/
    └── StorageAcl.kt
```

---

## 📱 ESTADOS DE MENSAJE (TICKS)

### Enum MessageStatus

```kotlin
enum class MessageStatus {
    SENT,       // 1 tick Gris Koala (#8E8E93)
    DELIVERED,  // 2 ticks Gris Koala (#8E8E93)
    READ        // 2 ticks Rosa Chanchita (#FF69B4)
}
```

### Cálculo del Estado

```kotlin
val status: MessageStatus
    get() = when {
        readAt != null -> MessageStatus.READ      // Rosa
        deliveredAt != null -> MessageStatus.DELIVERED  // Gris
        else -> MessageStatus.SENT                // Gris
    }
```

### UI del Indicador

```kotlin
@Composable
fun MessageStatusIndicator(status: MessageStatus, isMine: Boolean) {
    if (!isMine) return
    
    val tickColor = when (status) {
        MessageStatus.READ -> RosaChanchitaDark  // #FF69B4
        else -> GrisKoala                         // #8E8E93
    }
    
    // Dibujar 1 o 2 ticks según el estado
}
```

---

## 🎯 CHECKLIST DE IMPLEMENTACIÓN

### Fase 1: Corrección de Código (✅ HECHO 2026-04-04)
- [x] Crear SupabaseConfig.kt con inicialización correcta
- [x] Actualizar Ktor 2.x → 3.x (compatible con Supabase 3.4.1)
- [x] Agregar import de E2ECipher en ChatViewModel
- [x] Validación de credenciales en build time
- [x] Remover google-services.json de git
- [x] Crear placeholder google-services.json.example
- [x] Actualizar documentación

### Fase 2: Autenticación (✅ IMPLEMENTADO)
- [x] AuthRepository con Supabase Auth
- [x] AuthReadRepository - lectura de sesión
- [x] AuthWriteRepository - operaciones de auth
- [x] AuthProfileRepository - gestión de perfil
- [x] Google Sign In configurado
- [x] Login anónimo implementado

### Fase 3: Chat y Mensajes (✅ IMPLEMENTADO)
- [x] ChatReadRepository con Supabase Realtime
- [x] MessageRepository - CRUD de mensajes
- [x] MessageActionsRepository - pin, delete, etc.
- [x] Cifrado E2E con Android Keystore
- [x] Descifrado en ChatViewModel
- [x] Observación en tiempo real

### Fase 4: Presencia (✅ IMPLEMENTADO)
- [x] PresenceRepository creado
- [x] Indicadores online/offline
- [x] Typing indicators
- [x] Last seen tracking

### Fase 5: Notificaciones (✅ IMPLEMENTADO)
- [x] Firebase Cloud Messaging configurado
- [x] FCMMessageService implementado
- [x] Token management
- [x] Lifecycle handling

### Fase 6: Multimedia (✅ IMPLEMENTADO)
- [x] Supabase Storage configurado
- [x] StorageRepository implementado
- [x] Avatar system
- [x] Image loading con Coil

### Fase 7: Features Adicionales (⚠️ PENDIENTE)
- [ ] Sistema de emparejamiento con código 6 dígitos
- [ ] UI de pairing completa
- [ ] MessageStatusIndicator (ticks visuales)
- [ ] Animaciones románticas
- [ ] Tema de colores completo
- [ ] Contador de días juntos

---

## 🔧 COMANDOS ÚTILES

### Build y Test

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build
./gradlew clean

# Verificar credenciales (fallará si faltan)
./gradlew assembleDebug --info
```

### Configuración Inicial

```bash
# 1. Copiar credenciales
cp gradle.properties.example gradle.properties

# 2. Editar con credenciales reales
# Editar gradle.properties

# 3. Configurar Firebase (opcional)
# Descargar google-services.json desde Firebase Console
# Colocar en app/google-services.json
```

### Database Schema

```sql
-- Ejecutar en Supabase SQL Editor
-- Copiar y pegar contenido de database_schema.sql
```

---

## 📝 NOTAS IMPORTANTES

1. **Credenciales:** NUNCA subas `gradle.properties` o `google-services.json` a git
2. **Build:** Fallará temprano si faltan credenciales de Supabase
3. **Multimedia:** Solo usar Supabase Storage (NO Cloudinary)
4. **Colores:** Rosa Chanchita (#FF69B4) y Gris Koala (#8E8E93) en TODA la app
5. **Ticks:** 1 tick gris (enviado), 2 ticks grises (entregado), 2 ticks rosas (leído)
6. **Cifrado:** Android Keystore AES-256-GCM (ya implementado)
7. **Backend:** Supabase (PostgreSQL + Realtime + Storage)
8. **Notificaciones:** Firebase Cloud Messaging (FCM) únicamente

---

**Fecha:** 4 de Abril, 2026
**Versión:** 2.5-supabase-fcm
**Estado:** 🚧 EN DESARROLLO - Correcciones críticas aplicadas
