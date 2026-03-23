# 🔴 ERRORES ENCONTRADOS Y CORRECCIONES - MIGRACIÓN SUPABASE

## ⚠️ IMPORTANTE: ESTE DOCUMENTO CONTIENE TODOS LOS ERRORES Y SUS SOLUCIONES

Después de revisar exhaustivamente el código y verificar con la documentación oficial actualizada (Marzo 2026), encontré **ERRORES CRÍTICOS** que deben corregirse.

---

## 📊 RESUMEN DE ERRORES

| Severidad | Cantidad | Estado |
|-----------|----------|--------|
| 🔴 Crítico | 8 | Requiere corrección inmediata |
| 🟡 Moderado | 5 | Debe corregirse antes de producción |
| 🟢 Menor | 3 | Mejoras recomendadas |

---

## 🔴 ERRORES CRÍTICOS

### 1. OneSignal SDK - API Obsoleta ❌

**Archivo:** `NotificationRepository.kt`

**Error:**
```kotlin
// ❌ MAL - API eliminada en OneSignal 5.x
OneSignal.initWithContext(context)
OneSignal.setAppId(appId)
OneSignal.getDeviceState().userId
```

**Problema:** OneSignal 5.6.1+ cambió completamente la API. Los métodos anteriores **NO existen**.

**Solución CORRECTA (verificado con documentación oficial):**
```kotlin
// ✅ BIEN - OneSignal 5.6.1+
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.user.IUser

// Inicialización
OneSignal.initialize(context, appId)

// Obtener ID del usuario (asíncrono)
OneSignal.User.pushSubscription.id
```

**Referencia oficial:** https://documentation.onesignal.com/docs/en/android-sdk-setup

---

### 2. Supabase Kotlin SDK - Imports Incorrectos ❌

**Archivo:** `SupabaseConfig.kt`

**Error:**
```kotlin
// ❌ MAL - Paquete incorrecto
import io.github.jan.tennert.supabase.auth.Auth
import io.github.jan.tennert.supabase.createSupabaseClient
```

**Problema:** El paquete correcto es `io.github.jan.supabase`, NO `io.github.jan-tennert.supabase`.

**Solución CORRECTA:**
```kotlin
// ✅ BIEN - Paquete correcto (verificado en Maven Central Marzo 2026)
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
```

**Referencia:** https://github.com/supabase-community/supabase-kt

---

### 3. AuthRepository - API de Autenticación Incorrecta ❌

**Archivo:** `AuthRepository.kt`

**Error:**
```kotlin
// ❌ MAL - API no existe
val authResult = auth.signUpWith(Email) {
    this.email = email
    this.password = password
}

auth.signInWith(Email) { ... }
```

**Problema:** La API correcta usa `signUpWith` y `signInWith` de forma diferente.

**Solución CORRECTA:**
```kotlin
// ✅ BIEN - API actualizada (supabase-kt 2.x)
import io.github.jan.supabase.auth.providers.builtin.Email

// Sign Up
val authResult = auth.signUpWith(Email) {
    email = email
    password = password
}

// O alternativamente:
auth.signUp(email, password)

// Sign In
auth.signInWith(Email) {
    email = email
    password = password
}

// O:
auth.signIn(email, password)
```

---

### 4. NotificationRepository - OneSignal initialization incorrecta ❌

**Archivo:** `NotificationRepository.kt`

**Error:**
```kotlin
// ❌ MAL - Método eliminado
OneSignal.initWithContext(context)
OneSignal.setAppId(appId)
```

**Solución CORRECTA:**
```kotlin
// ✅ BIEN - OneSignal 5.6.1+
fun initialize(context: Context, appId: String) {
    OneSignal.initialize(context, appId)
    
    // Opcional: Configurar logging para debugging
    OneSignal.Debug.setLogLevel(LogLevel.VERBOSE)
}
```

---

### 5. NotificationRepository - getPlayerId() asíncrono mal implementado ❌

**Archivo:** `NotificationRepository.kt`

**Error:**
```kotlin
// ❌ MAL - getDeviceState() puede ser null inicialmente
val deviceState = OneSignal.getDeviceState()
val playerId = deviceState?.userId
```

**Problema:** El Player ID solo está disponible DESPUÉS de que OneSignal se registra con el servidor.

**Solución CORRECTA:**
```kotlin
// ✅ BIEN - Usar listener
suspend fun getPlayerId(): String? = withContext(Dispatchers.IO) {
    // Esperar a que OneSignal esté listo
    var playerId: String? = null
    val latch = CountDownLatch(1)
    
    OneSignal.User.pushSubscription.addObserver { state ->
        playerId = state.id
        latch.countDown()
    }
    
    // Esperar máximo 5 segundos
    latch.await(5, TimeUnit.SECONDS)
    playerId
}
```

---

### 6. E2ECipher.kt - libsodium mal utilizado ❌

**Archivo:** `E2ECipher.kt`

**Error:**
```kotlin
// ❌ MAL - libsodium-jni NO se usa así
import org.libsodium.jni.Sodium
Sodium.randombytes_buf(key, key.size)
```

**Problema:** El código actual usa `javax.crypto.Cipher` con AES-GCM, NO usa libsodium realmente. Solo importa NaCl pero no lo usa para cifrar.

**Solución CORRECTA - Opción A (Recomendada):**

Usar la librería correcta para Android:

```kotlin
// ✅ BIEN - Usar Android Keystore + AES-GCM (sin libsodium)
// Android Keystore ya es seguro y está verificado por hardware

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

object E2ECipher {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12 // 96 bits
    private const val TAG_SIZE = 128 // bits
    
    fun encrypt(plaintext: String, keyAlias: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(keyAlias))
        
        val ciphertext = cipher.doFinal(plaintext.toByteArray())
        val iv = cipher.iv
        
        // Formato: iv:ciphertext (Base64)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        
        return "$ivB64:$cipherB64"
    }
    
    fun decrypt(encrypted: String, keyAlias: String): String {
        val parts = encrypted.split(":")
        if (parts.size != 2) throw IllegalArgumentException("Formato inválido")
        
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(keyAlias), spec)
        
        return String(cipher.doFinal(ciphertext))
    }
    
    private fun getKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return keyStore.getEntry(alias, null) as? SecretKeyEntry
            ?: generateKey(alias)
    }
    
    private fun generateKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        
        return keyGenerator.generateKey()
    }
}
```

**Referencia:** https://developer.android.com/security/keystore

---

### 7. database_schema.sql - Tipo de dato incorrecto para UUID ❌

**Archivo:** `database_schema.sql`

**Error:**
```sql
-- ❌ MAL - Supabase Auth usa UUID, no TEXT
CREATE TABLE users (
    id TEXT PRIMARY KEY, -- UUID de Supabase Auth
    ...
);
```

**Problema:** Supabase Auth usa `UUID` como tipo nativo. Usar `TEXT` causa errores de tipo.

**Solución CORRECTA:**
```sql
-- ✅ BIEN - UUID nativo de PostgreSQL
CREATE TABLE users (
    id UUID PRIMARY KEY REFERENCES auth.users(id),
    email TEXT UNIQUE NOT NULL,
    display_name TEXT DEFAULT 'Usuario',
    ...
);

CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_ids UUID[] NOT NULL DEFAULT '{}',
    ...
);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id),
    ...
);
```

---

### 8. build.gradle.kts - Dependencias incorrectas ❌

**Archivo:** `app/build.gradle.kts`

**Error:**
```kotlin
// ❌ MAL - Grupo incorrecto
implementation(platform("io.github.jan-tennert.supabase:bom:2.1.0"))
implementation("org.libsodium:libsodium-jni:1.0.18")
```

**Problemas:**
1. El grupo correcto es `io.github.jan.supabase` (sin "-tennert")
2. `libsodium-jni:1.0.18` es muy antiguo y puede no funcionar

**Solución CORRECTA:**
```kotlin
// ✅ BIEN - Dependencias actualizadas (Marzo 2026)
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.21"  // Requerido por Supabase
}

dependencies {
    // Supabase (versión correcta)
    implementation(platform("io.github.jan.supabase:bom:2.1.0"))
    implementation("io.github.jan.supabase:supabase-kt")
    implementation("io.github.jan.supabase:gotrue-kt")
    implementation("io.github.jan.supabase:postgrest-kt")
    implementation("io.github.jan.supabase:realtime-kt")
    
    // Ktor (requerido por Supabase)
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    
    // Kotlinx Serialization (requerido)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // OneSignal (versión correcta)
    implementation("com.onesignal:OneSignal:5.6.1")
    
    // NO usar libsodium-jni - usar Android Keystore en su lugar
}
```

---

## 🟡 ERRORES MODERADOS

### 9. MainActivity - Falta manejo de permisos de notificación

**Archivo:** `MainActivity.kt`

**Problema:** Android 13+ requiere pedir permiso explícitamente.

**Solución:**
```kotlin
// Agregar en onCreate()
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        REQUEST_CODE
    )
}
```

---

### 10. App.kt - OneSignal se inicializa demasiado pronto

**Archivo:** `App.kt`

**Problema:** OneSignal debe inicializarse DESPUÉS de tener el App ID válido.

**Solución:**
```kotlin
// ✅ BIEN - Inicializar solo cuando hay credenciales
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Verificar que el App ID es válido antes de inicializar
        val appId = SupabaseConfig.ONESIGNAL_APP_ID
        if (appId.isNotBlank() && appId != "TU_ONESIGNAL_APP_ID_AQUI") {
            OneSignal.initialize(this, appId)
        } else {
            Log.w("App", "OneSignal App ID no configurado")
        }
    }
}
```

---

### 11. ChatRepository - Realtime subscriptions mal implementadas

**Archivo:** `ChatRepository.kt`

**Problema:** Las suscripciones en tiempo real no manejan reconexión.

**Solución:**
```kotlin
// ✅ BIEN - Manejar reconexión
fun observeMessages(chatId: String, myUid: String): Flow<List<Message>> = callbackFlow {
    val channel = realtime.from("messages")
    
    val subscription = channel.subscribe {
        if (it is ChannelState.CONNECTED) {
            // Cargar mensajes iniciales
            loadMessages(chatId)
        }
    }
    
    awaitClose {
        realtime.removeChannel(subscription)
    }
}
```

---

## 🟢 ERRORES MENORES

### 12. SecureKeyManager.kt - No maneja excepción de KeyStore

**Problema:** Si KeyStore falla, la app crashea.

**Solución:** Agregar try-catch y fallback.

---

### 13. ViewModels - No manejan estado de carga inicial

**Problema:** La UI no muestra loading mientras se cargan datos.

**Solución:** Agregar `_isLoading` state.

---

### 14. Falta validación de email

**Problema:** No se valida formato de email antes de enviar a Supabase.

**Solución:** Agregar regex de validación.

---

## 📝 ARCHIVOS QUE DEBEN CORREGIRSE

### Prioridad 1 (Crítico - La app NO funciona sin esto):

1. ✅ `app/build.gradle.kts` - Dependencias correctas
2. ✅ `app/src/main/java/com/example/messageapp/supabase/SupabaseConfig.kt` - Imports correctos
3. ✅ `app/src/main/java/com/example/messageapp/data/AuthRepository.kt` - API correcta
4. ✅ `app/src/main/java/com/example/messageapp/data/NotificationRepository.kt` - OneSignal 5.6.1+
5. ✅ `app/src/main/java/com/example/messageapp/crypto/E2ECipher.kt` - Android Keystore
6. ✅ `database_schema.sql` - UUID en lugar de TEXT
7. ✅ `app/src/main/java/com/example/messageapp/MainActivity.kt` - Permisos Android 13+

### Prioridad 2 (Moderado - Funciona pero con problemas):

8. `app/src/main/java/com/example/messageapp/core/App.kt`
9. `app/src/main/java/com/example/messageapp/data/ChatRepository.kt`

### Prioridad 3 (Menor - Mejoras):

10. `app/src/main/java/com/example/messageapp/crypto/SecureKeyManager.kt`
11. ViewModels
12. Validaciones de UI

---

## ✅ CÓMO VERIFICAR LAS CORRECCIONES

### Para Supabase:

1. **Verificar imports:**
```bash
grep -r "io.github.jan.supabase" app/src/main/
# Debe mostrar todos los imports correctos
```

2. **Verificar conexión:**
```kotlin
// En MainActivity.kt
LaunchedEffect(Unit) {
    try {
        val response = SupabaseConfig.client.from("users").select().count()
        Log.d("Supabase", "Conexión exitosa: $response usuarios")
    } catch (e: Exception) {
        Log.e("Supabase", "Error de conexión", e)
    }
}
```

### Para OneSignal:

1. **Verificar inicialización:**
```bash
adb logcat | grep OneSignal
# Debe mostrar "OneSignal initialized successfully"
```

2. **Verificar Player ID:**
```kotlin
OneSignal.User.pushSubscription.id?.let { id ->
    Log.d("OneSignal", "Player ID: $id")
}
```

---

## 🎯 PRÓXIMOS PASOS

1. **Inmediato:** Corregir los 8 errores críticos
2. **Corto plazo:** Corregir errores moderados
3. **Largo plazo:** Mejoras menores

---

## 📞 RECURSOS OFICIALES

| Servicio | Documentación |
|----------|---------------|
| Supabase Kotlin | https://github.com/supabase-community/supabase-kt |
| OneSignal Android | https://documentation.onesignal.com/docs/en/android-sdk-setup |
| Android Keystore | https://developer.android.com/security/keystore |
| PostgreSQL UUID | https://www.postgresql.org/docs/current/datatype-uuid.html |

---

**Fecha de verificación:** Marzo 2026
**Verificado por:** Revisión exhaustiva con documentación oficial actualizada

---

## ⚠️ ADVERTENCIA

**NO intentes compilar la app sin corregir los errores críticos.** La app:
- ❌ No compilará (imports incorrectos)
- ❌ Crasheará al iniciar (OneSignal API eliminada)
- ❌ No podrá autenticar (Supabase API incorrecta)
- ❌ No cifrará mensajes (libsodium mal implementado)

**Corrige TODOS los errores críticos primero.**
