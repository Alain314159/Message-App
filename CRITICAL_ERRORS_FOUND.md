# 🔴 ERRORES CRÍTICOS ENCONTRADOS - REVISIÓN EXHAUSTIVA

**Fecha:** 24 de Marzo, 2026  
**Estado:** ⚠️ **CRÍTICO - LA APP NO FUNCIONARÁ**

---

## 📊 RESUMEN EJECUTIVO

Después de una revisión exhaustiva del código, se encontraron **ERRORES CRÍTICOS DE MIGRACIÓN**:

- ✅ **Dependencias:** Corregidas (ver `DEPENDENCY_FIXES_MARCH_2026.md`)
- ❌ **Código Firebase:** SIN MIGRAR - 57 ocurrencias encontradas
- ❌ **Repositorios:** 3 repositorios completos usan Firebase
- ❌ **UI:** 6 pantallas usan Firebase directamente

---

## 🔴 ERROR CRÍTICO #1: FIREBASE SIN MIGRAR

### Archivos que TODAVÍA usan Firebase:

| Archivo | Imports de Firebase | Uso en Código | Severidad |
|---------|---------------------|---------------|-----------|
| `ContactsRepository.kt` | 4 imports | 100% Firebase | 🔴 CRÍTICO |
| `StorageRepository.kt` | 3 imports | 100% Firebase | 🔴 CRÍTICO |
| `ProfileRepository.kt` | 3 imports | 100% Firebase | 🔴 CRÍTICO |
| `HomeScreen.kt` | 1 import | Usa `FirebaseAuth.getInstance()` | 🔴 CRÍTICO |
| `ContactsScreen.kt` | 2 imports | Usa `FirebaseFirestore` | 🔴 CRÍTICO |
| `AuthScreen.kt` | 1 import | Usa `FirebaseAuthException` | 🟡 MEDIO |
| `StorageAclWarmup.kt` | 2 imports | 100% Firebase | 🟡 MEDIO |
| `Time.kt` | 1 import | Usa `Timestamp` | 🟢 MENOR |

---

## 📝 DETALLE POR ARCHIVO

### 1. `ContactsRepository.kt` 🔴 CRÍTICO

**Problema:** 100% código de Firebase

```kotlin
// ❌ MAL - Usa Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class ContactsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun addContact(myUid: String, otherUid: String, alias: String?) {
        col(myUid).document(otherUid).set(...) // Firebase
    }
}
```

**Solución requerida:**
- Migrar a Supabase Postgrest
- Reemplazar `FirebaseFirestore` con `SupabaseConfig.client.plugin(Postgrest)`
- Reemplazar `.collection().document().set()` con `db.from("contacts").insert()`

---

### 2. `StorageRepository.kt` 🔴 CRÍTICO

**Problema:** 100% código de Firebase Storage

```kotlin
// ❌ MAL - Usa Firebase Storage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore

class StorageRepository(
    private val st: FirebaseStorage = FirebaseStorage.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun sendMedia(...) {
        val ref = st.reference.child("chats/$chatId/$type/$name")
        ref.putFile(uri).await() // Firebase Storage
    }
}
```

**Solución requerida:**
- Migrar a Supabase Storage
- Reemplazar `FirebaseStorage` con `SupabaseConfig.client.plugin(Storage)`
- Reemplazar `ref.putFile()` con `storage.upload()`

---

### 3. `ProfileRepository.kt` 🔴 CRÍTICO

**Problema:** 100% código de Firebase

```kotlin
// ❌ MAL - Usa Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun updateProfile(...) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid // Firebase
        db.collection("users").document(uid).update(...) // Firebase
    }
}
```

**Solución requerida:**
- Migrar a Supabase Auth + Postgrest + Storage
- Usar `auth.currentUserOrNull()` en lugar de `FirebaseAuth.getInstance().currentUser`
- Usar `db.from("users").update()` en lugar de `db.collection().document().update()`

---

### 4. `HomeScreen.kt` 🔴 CRÍTICO

**Problema:** Usa FirebaseAuth directamente en la UI

```kotlin
// ❌ MAL - Firebase en la UI
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(...) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    // ...
}
```

**Solución requerida:**
- Usar `AuthViewModel` en lugar de acceder directamente a FirebaseAuth
- `val myUid by authVm.currentUserId.collectAsStateWithLifecycle()`

---

### 5. `ContactsScreen.kt` 🔴 CRÍTICO

**Problema:** Usa FirebaseFirestore directamente

```kotlin
// ❌ MAL - Firebase en la UI
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

val db = remember { FirebaseFirestore.getInstance() }
reg = db.collection("users")... // Firebase
```

**Solución requerida:**
- Usar `ContactsRepository` con Supabase
- Eliminar `ListenerRegistration` (usar `Flow` de Supabase Realtime)

---

### 6. `AuthScreen.kt` 🟡 MEDIO

**Problema:** Usa excepción de Firebase

```kotlin
// ⚠️ MEDIO - Solo usa la excepción
import com.google.firebase.auth.FirebaseAuthException

val codeErr = (e as? FirebaseAuthException)?.errorCode
```

**Solución requerida:**
- Reemplazar con manejo de errores de Supabase Auth
- Usar `AuthException` de supabase-kt

---

### 7. `StorageAclWarmup.kt` 🟡 MEDIO

**Problema:** Usa Firebase Auth + Firestore

```kotlin
// ⚠️ MEDIO - Inicialización de ACL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val uid = FirebaseAuth.getInstance().currentUser?.uid
val db = FirebaseFirestore.getInstance()
```

**Solución requerida:**
- Migrar a Supabase Auth + Postgrest
- O eliminar si no es necesario

---

### 8. `Time.kt` 🟢 MENOR

**Problema:** Importa Timestamp de Firebase

```kotlin
// 🟢 MENOR - Solo el import
import com.google.firebase.Timestamp
```

**Solución requerida:**
- Reemplazar con `Long` (timestamp Unix) o `java.time.Instant`

---

## 📊 ESTADÍSTICAS DE ERRORES

| Tipo de Error | Cantidad | Severidad |
|---------------|----------|-----------|
| Repositorios 100% Firebase | 3 | 🔴 CRÍTICO |
| UI con Firebase directo | 3 | 🔴 CRÍTICO |
| Imports de Firebase | 57 | 🔴 CRÍTICO |
| Usos de `.collection()` | 15+ | 🔴 CRÍTICO |
| Usos de `.document()` | 10+ | 🔴 CRÍTICO |
| Usos de `FirebaseAuth.getInstance()` | 5+ | 🔴 CRÍTICO |
| Usos de `FirebaseFirestore.getInstance()` | 5+ | 🔴 CRÍTICO |
| Usos de `FirebaseStorage.getInstance()` | 2+ | 🔴 CRÍTICO |

---

## 🔥 IMPACTO EN LA APP

### Lo que NO va a funcionar:

1. ❌ **Contactos:** No funcionará (100% Firebase)
2. ❌ **Subir multimedia:** No funcionará (100% Firebase Storage)
3. ❌ **Actualizar perfil:** No funcionará (100% Firebase)
4. ❌ **Home Screen:** Crash al iniciar (FirebaseAuth no configurado)
5. ❌ **Lista de chats:** Puede fallar (usa Firestore en ContactsScreen)

### Lo que SÍ va a funcionar:

1. ✅ **Login/Registro:** Funciona (AuthRepository migrado)
2. ✅ **Chat básico:** Funciona (ChatRepository migrado)
3. ✅ **Avatares:** Funciona (AvatarRepository nuevo)
4. ✅ **Emparejamiento:** Funciona (PairingRepository migrado)
5. ✅ **Presencia:** Funciona (PresenceRepository migrado)

---

## 🛠️ PLAN DE MIGRACIÓN

### Prioridad 1 (CRÍTICO - La app no inicia sin esto):

1. **`HomeScreen.kt`** - Eliminar Firebase, usar AuthViewModel
2. **`ProfileRepository.kt`** - Migrar a Supabase

### Prioridad 2 (ALTO - Funcionalidades rotas):

3. **`StorageRepository.kt`** - Migrar a Supabase Storage
4. **`ContactsRepository.kt`** - Migrar a Supabase Postgrest

### Prioridad 3 (MEDIO - Mejoras necesarias):

5. **`ContactsScreen.kt`** - Eliminar Firebase directo
6. **`AuthScreen.kt`** - Reemplazar FirebaseAuthException
7. **`StorageAclWarmup.kt`** - Migrar o eliminar

### Prioridad 4 (BAJO - Limpieza):

8. **`Time.kt`** - Reemplazar Timestamp

---

## 📋 CHECKLIST DE MIGRACIÓN

```
[ ] 1. HomeScreen.kt - Usar AuthViewModel
[ ] 2. ProfileRepository.kt - Migrar a Supabase
[ ] 3. StorageRepository.kt - Migrar a Supabase Storage
[ ] 4. ContactsRepository.kt - Migrar a Supabase Postgrest
[ ] 5. ContactsScreen.kt - Eliminar FirebaseFirestore
[ ] 6. AuthScreen.kt - Reemplazar FirebaseAuthException
[ ] 7. StorageAclWarmup.kt - Migrar o eliminar
[ ] 8. Time.kt - Reemplazar Timestamp
[ ] 9. Eliminar imports de Firebase del proyecto
[ ] 10. Eliminar dependencias de Firebase del build.gradle
```

---

## ⚠️ ADVERTENCIA

**LA APP NO ESTÁ LISTA PARA PRODUCCIÓN**

Aunque las dependencias están corregidas, el código tiene:
- 🔴 **3 repositorios completos sin migrar**
- 🔴 **57 imports de Firebase**
- 🔴 **Múltiples crashes garantizados al iniciar**

**Se estima que se necesitan 4-8 horas de desarrollo para migrar todo correctamente.**

---

## 📅 PRÓXIMOS PASOS INMEDIATOS

1. **NO hacer build/release** hasta migrar todo
2. **Comenzar con Prioridad 1** (HomeScreen + ProfileRepository)
3. **Testear cada migración** antes de continuar
4. **Eliminar Firebase del build.gradle** solo cuando TODO esté migrado

---

**Fecha del análisis:** 24 de Marzo, 2026  
**Analista:** Revisión exhaustiva automatizada  
**Estado:** 🔴 **CRÍTICO - REQUIERE ACCIÓN INMEDIATA**
