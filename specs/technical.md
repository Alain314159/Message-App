# Especificaciones Técnicas - Message App

## 🏗️ Arquitectura

### Patrón Arquitectónico: MVVM + Clean Architecture

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  - Screens                              │
│  - Components                           │
│  - ViewModels                           │
├─────────────────────────────────────────┤
│          Domain Layer (Use Cases)       │
│  - Business Logic                       │
│  - Entities                             │
│  - Repository Interfaces                │
├─────────────────────────────────────────┤
│           Data Layer                    │
│  - Repositories Implementation          │
│  - Room Database (Local)                │
│  - Supabase (Remote)                    │
│  - Crypto (E2EE)                        │
└─────────────────────────────────────────┘
```

---

## 🛠️ Stack Técnico

### Lenguajes
- **Kotlin**: 1.9.0+ (principal)
- **Kotlin Coroutines**: 1.7.x
- **Kotlin Flow**: Para streams reactivos

### UI
- **Jetpack Compose**: 1.5.x
- **Material 3**: Para diseño
- **Navigation Compose**: Navegación
- **Coil**: Carga de imágenes (avatars)

### Arquitectura
- **Hilt**: Inyección de dependencias
- **ViewModel + StateFlow**: Gestión de estado UI
- **Mavericks** (opcional): Para ViewModels más complejos

### Base de Datos
- **Room**: 2.6.x (almacenamiento local)
- **SQLCipher**: Para encriptación de DB (opcional)

### Red
- **Supabase**: Backend como servicio
  - PostgreSQL: Base de datos
  - Realtime: WebSockets para mensajes
  - Storage: Avatares y archivos
  - Auth: Autenticación

### Criptografía
- **Tink**: Criptografía de alto nivel
- **libsignal**: Protocolo Signal (E2EE)

### Testing
- **JUnit 4/5**: Tests unitarios
- **MockK**: Mocking
- **Kluent**: Aserciones
- **Turbine**: Testing de Flows
- **Compose UI Test**: Tests de UI

---

## 📦 Estructura de Paquetes

```
com.example.messageapp/
├── core/
│   ├── App.kt                    # Application class
│   ├── di/                       # Hilt modules
│   └── utils/                    # Utilidades compartidas
│
├── data/
│   ├── room/
│   │   ├── AppDatabase.kt
│   │   ├── MessageDao.kt
│   │   ├── ChatDao.kt
│   │   └── UserDao.kt
│   │
│   ├── supabase/
│   │   ├── SupabaseConfig.kt
│   │   └── ApiExtensions.kt
│   │
│   ├── repository/
│   │   ├── MessageRepository.kt
│   │   ├── ChatRepository.kt
│   │   ├── AuthRepository.kt
│   │   └── UserRepository.kt
│   │
│   └── model/
│       ├── MessageEntity.kt
│       ├── ChatEntity.kt
│       └── UserEntity.kt
│
├── domain/
│   ├── model/                    # Entidades de dominio
│   │   ├── Message.kt
│   │   ├── Chat.kt
│   │   └── User.kt
│   │
│   ├── repository/               # Interfaces
│   │   ├── MessageRepository.kt
│   │   └── ...
│   │
│   └── usecase/                  # Casos de uso
│       ├── SendMessageUseCase.kt
│       ├── GetMessagesUseCase.kt
│       └── ...
│
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   │
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   │
│   ├── chat/
│   │   ├── ChatScreen.kt
│   │   ├── ChatComponents.kt
│   │   ├── ChatViewModel.kt
│   │   └── ChatViewState.kt
│   │
│   ├── chatlist/
│   │   ├── ChatListScreen.kt
│   │   ├── ChatListComponents.kt
│   │   └── ChatListViewModel.kt
│   │
│   ├── auth/
│   │   ├── AuthScreen.kt
│   │   └── AuthViewModel.kt
│   │
│   └── components/               # Componentes reutilizables
│       ├── TopAppBar.kt
│       ├── MessageBubble.kt
│       └── Avatar.kt
│
└── crypto/
    ├── E2ECipher.kt
    ├── KeyManager.kt
    └── SecureRandom.kt
```

---

## 🔐 Seguridad

### Autenticación
```kotlin
// Flow de autenticación
1. Ingresar número → POST /auth/sms
2. Recibir código → POST /auth/verify
3. Obtener token → Guardar en EncryptedSharedPreferences
4. Refresh token → Automático cada 24h
```

### Cifrado de Mensajes
```kotlin
// E2EE con Double Ratchet
1. X3DH Key Agreement → Establecer shared secret
2. Double Ratchet → Derivar keys por mensaje
3. AES-256-GCM → Cifrar contenido
4. HMAC → Verificar integridad
```

### Almacenamiento Seguro
```kotlin
// Room con SQLCipher
- Clave derivada de master key
- Master key en Android Keystore
- Todos los datos cifrados en disco
```

---

## 📊 Modelos de Datos

### Message
```kotlin
data class Message(
    val id: String,              // UUID
    val chatId: String,
    val senderId: String,
    val content: String,         // Cifrado
    val timestamp: Long,
    val status: MessageStatus,   // PENDING, SENT, DELIVERED, READ, FAILED
    val encrypted: Boolean,
    val signature: String        // Firma digital
)

enum class MessageStatus {
    PENDING, SENT, DELIVERED, READ, FAILED
}
```

### Chat
```kotlin
data class Chat(
    val id: String,
    val name: String?,
    val avatarUrl: String?,
    val lastMessage: Message?,
    val lastMessageTimestamp: Long,
    val unreadCount: Int,
    val isGroup: Boolean,
    val participants: List<String>
)
```

### User
```kotlin
data class User(
    val id: String,
    val phoneNumber: String,
    val displayName: String?,
    val avatarUrl: String?,
    val publicKey: String,       // Para E2EE
    val createdAt: Long,
    val lastSeen: Long?
)
```

---

## 🔄 Flujo de Datos

### Envío de Mensaje
```
UI (ChatScreen)
  ↓
ViewModel (SendMessage)
  ↓
UseCase (Validate + Encrypt)
  ↓
Repository (Save Local + Send Remote)
  ↓
Room (Insert Message) + Supabase (Realtime)
  ↓
UI (Update State)
```

### Recepción de Mensaje
```
Supabase Realtime
  ↓
Repository (Receive)
  ↓
UseCase (Decrypt + Validate)
  ↓
Room (Insert Message)
  ↓
ViewModel (Update State)
  ↓
UI (Show Message + Notify)
```

---

## 🧪 Estrategia de Testing

### Pirámide de Testing

```
         /\
        /  \       E2E Tests (10%)
       /----\      - Flujos completos
      /      \     - Instrumented tests
     /--------\   
    /          \   Integration Tests (20%)
   /------------\  - Repository tests
  /              \ - Database tests
 /----------------\ 
/                  \ Unit Tests (70%)
--------------------  - ViewModels
                        - UseCases
                        - Utils
```

### Cobertura Mínima Requerida
- **Unit Tests**: > 80%
- **Integration Tests**: > 60%
- **Critical Paths**: 100%

---

## 📈 Performance

### Objetivos
| Métrica | Objetivo | Estrategia |
|---------|----------|------------|
| Cold Start | < 2s | Lazy loading, App Startup |
| Message List | 60fps | Paging 3.0, DiffUtil |
| Image Loading | < 500ms | Coil cache, resize |
| Database Query | < 100ms | Índices, queries optimizados |
| Network Call | < 1s | WebSocket, retry logic |

### Optimizaciones
1. **Paging 3.0**: Carga gradual de mensajes
2. **Coil**: Cache de imágenes en memoria y disco
3. **Room**: Queries con LiveData/Flow
4. **Coroutines**: IO dispatcher para DB/Red
5. **StateFlow**: Solo emite cuando hay cambios reales

---

## 🔧 Configuración de Build

### build.gradle.kts (app)
```kotlin
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.messageapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.4")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Crypto
    implementation("com.google.crypto.tink:tink-android:1.11.0")
    
    // Coil
    implementation("io.coil-kt:coil-compose:2.5.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.amshove.kluent:kluent-android:1.73")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## 🚨 Manejo de Errores

### Estrategia
```kotlin
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val code: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Uso en ViewModel
fun sendMessage(content: String) {
    viewModelScope.launch {
        _state.update { it.copy(status = Loading) }
        
        when (val result = sendMessageUseCase(content)) {
            is Result.Success -> {
                _state.update { it.copy(status = Success(result.data)) }
            }
            is Result.Error -> {
                _state.update { it.copy(status = Error(result.exception)) }
            }
            else -> {}
        }
    }
}
```

### Errores Conocidos y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| `SSLHandshakeException` | Certificado inválido | Verificar configuración Supabase |
| `SQLiteFullException` | DB llena | Implementar limpieza automática |
| `OutOfMemoryError` | Imágenes grandes | Resize con Coil, limitar cache |
| `NetworkOnMainThreadException` | Red en UI thread | Usar Dispatchers.IO |
| `IllegalStateException` | ViewModel sin Hilt | Verificar @HiltViewModel |

---

## 📝 Decisiones de Diseño

### Por qué Jetpack Compose?
- ✅ Declarativo, menos código
- ✅ Mejor performance con recomposición
- ✅ Integración con Material 3
- ✅ Soporte oficial de Google

### Por qué Supabase?
- ✅ Backend listo, menos infraestructura
- ✅ Realtime incluido (WebSockets)
- ✅ PostgreSQL (robusto, conocido)
- ✅ Auth incluido

### Por qué Room?
- ✅ Cache offline-first
- ✅ Integración con Flow
- ✅ Compile-time SQL verification
- ✅ Soporte oficial

### Por qué Hilt?
- ✅ Menos boilerplate que Dagger
- ✅ Lifecycle-aware
- ✅ Soporte oficial para Android

---

## 🔄 Historial de Decisiones

| Fecha | Decisión | Razón |
|-------|----------|-------|
| 2026-03-24 | Specs iniciales | Establecer base técnica |
| | | |

---

**Última Actualización:** 2026-03-24  
**Estado:** ✅ Activo  
**Próxima Revisión:** 2026-03-31
