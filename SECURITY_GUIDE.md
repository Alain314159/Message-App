# 🔐 SECURITY GUIDE - Message App

**Fecha:** 2026-04-04
**Estado:** ✅ Credenciales aseguradas + Firebase seguro

---

## ✅ CREDENTIALS SECURITY - COMPLETADO

### Problemas Resueltos

1. ✅ **Supabase**: Credenciales validadas en build time
2. ✅ **Firebase**: google-services.json removido de git
3. ✅ **Build**: Falla temprano si faltan credenciales

### Solución Implementada

#### 1. BuildConfig para Credenciales con Validación
**Archivo:** `app/build.gradle.kts`

```kotlin
// Validación en build time - FALLA si no hay credenciales
val supabaseUrl = project.findProperty("SUPABASE_URL") as String?
val supabaseKey = project.findProperty("SUPABASE_ANON_KEY") as String?

if (supabaseUrl.isNullOrBlank()) {
    throw GradleException("SUPABASE_URL no está configurada...")
}

if (supabaseKey.isNullOrBlank()) {
    throw GradleException("SUPABASE_ANON_KEY no está configurada...")
}

buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
```

#### 2. SupabaseConfig Usa BuildConfig con Validación Runtime
**Archivo:** `app/src/main/java/com/example/messageapp/supabase/SupabaseConfig.kt`

```kotlin
val client by lazy {
    val url = BuildConfig.SUPABASE_URL
    val key = BuildConfig.SUPABASE_ANON_KEY

    if (url.isBlank() || key.isBlank()) {
        error("Supabase credentials cannot be empty")
    }

    createSupabaseClient(supabaseUrl = url, supabaseKey = key) { ... }
}
```

#### 3. Firebase/FCM Seguro
**Archivo:** `app/google-services.json` → **REMUEV** de git

```bash
# Remover del repositorio
git rm --cached app/google-services.json

# Agregar al .gitignore
echo "google-services.json" >> .gitignore
```

**Archivo:** `app/google-services.json.example` → **PLACEHOLDER** para devs

Cada desarrollador debe generar su propio `google-services.json` desde Firebase Console si necesita FCM.

---

## 📋 CONFIGURACIÓN PARA DESARROLLADORES

### Primer Setup

1. **Copiar y configurar credenciales:**
```bash
cp gradle.properties.example gradle.properties
# Editar gradle.properties con tus credenciales reales
```

2. **Configurar Firebase para FCM (opcional):**
```bash
# Si necesitas notificaciones push:
cp app/google-services.json.example app/google-services.json
# Editar con tus credenciales desde Firebase Console
```

3. **Nunca commitear archivos con credenciales:**
- `gradle.properties` → ✅ En .gitignore
- `google-services.json` → ✅ En .gitignore

---

## 🔒 BEST PRACTICES DE SEGURIDAD

### ✅ Implementado
- [x] Credenciales en BuildConfig (no en código)
- [x] Validación de credenciales en build time (FALLA temprano)
- [x] gradle.properties en .gitignore
- [x] google-services.json en .gitignore
- [x] Plantilla .example para desarrolladores
- [x] Tags constantes para logging (no exponen datos)
- [x] SupabaseConfig valida credenciales en runtime

### ⏳ Pendiente
- [ ] Certificate pinning para Supabase
- [ ] Validación de inputs en todos los endpoints
- [ ] Encriptación de datos sensibles en SharedPreferences
- [ ] ProGuard configurado para ofuscación
- [ ] Auditoría de logs (no exponer datos sensibles)
- [ ] Permisos de Android revisados (mínimos necesarios)

---

## 🚨 SECURITY CHECKLIST

### Antes de Cada Release

#### Código
- [ ] No hay credentials hardcodeadas
- [ ] No hay logs con datos sensibles
- [ ] Inputs de usuario validados
- [ ] HTTPS obligatorio (no HTTP)

#### Permisos
- [ ] Solo permisos necesarios declarados
- [ ] Permisos de runtime solicitados apropiadamente
- [ ] Justificación de permisos en README

#### Dependencies
- [ ] Todas las dependencias actualizadas
- [ ] Sin vulnerabilidades conocidas (OWASP Top 10)
- [ ] ProGuard/R8 habilitado para release

#### Datos
- [ ] SharedPreferences encriptados para datos sensibles
- [ ] Database encriptada (SQLCipher si es necesario)
- [ ] Keys de encriptación en Android Keystore

---

## 📊 AUDITORÍA DE SEGURIDAD

### Realizada: 2026-03-26

| Categoría | Estado | Notas |
|-----------|--------|-------|
| **Credentials** | ✅ Asegurado | BuildConfig + gradle.properties |
| **Network** | ⚠️ Pendiente | Falta certificate pinning |
| **Storage** | ⏳ Pendiente | Revisar SharedPreferences |
| **Permissions** | ⏳ Pendiente | Auditar permisos |
| **Logging** | ✅ Parcial | Tags constantes, revisar contenido |

### Próxima Auditoría: 2026-04-26

---

## 🔗 RECURSOS

- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/)
- [Supabase Security](https://supabase.com/docs/guides/database/security)

---

**Última Actualización:** 2026-03-26  
**Responsable:** Equipo de desarrollo
