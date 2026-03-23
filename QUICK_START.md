# 🚀 GUÍA RÁPIDA DE CONFIGURACIÓN

## ⚡ Configuración en 10 Minutos

### Paso 1: Supabase (3 minutos)

1. **Crear proyecto:**
   - Ve a https://supabase.com
   - Click "New Project"
   - Nombre: `chat-romantico`
   - Password: (guárdala)
   - Región: `South America (Brazil)`
   - Click "Create new project"
   - ⏳ Espera 2-3 minutos

2. **Obtener credenciales:**
   - Ve a **Settings** (engranaje abajo izquierda)
   - Click **API**
   - Copia:
     - `Project URL`: `https://xxxxx.supabase.co`
     - `anon/public key`: `eyJhbGc...`

---

### Paso 2: Ejecutar SQL en Supabase (2 minutos)

1. Ve a **SQL Editor** (icono de código)
2. Click **"New Query"**
3. Abre `database_schema.sql` en tu proyecto
4. Copia TODO el contenido
5. Pega en el SQL Editor
6. Click **"Run"** (o Ctrl+Enter)
7. ✅ Verifica: Ve a **Table Editor** → Deberías ver 4 tablas

---

### Paso 3: OneSignal (2 minutos)

1. **Crear cuenta:**
   - Ve a https://onesignal.com
   - Click "Sign Up"
   - Usa email (no Google)

2. **Crear app:**
   - Click "New App"
   - Nombre: `Message App`
   - Plataforma: `Android`
   - Copia el **App ID**

---

### Paso 4: Configurar la App (2 minutos)

1. Abre Android Studio
2. Abre el proyecto `mensaje_app_supabase`
3. Navega a: `app/src/main/java/com/example/messageapp/supabase/SupabaseConfig.kt`
4. Reemplaza los valores:

```kotlin
// LÍNEA 27 - Reemplaza con tu Project URL
const val SUPABASE_URL = "https://tu-proyecto.supabase.co"

// LÍNEA 28 - Reemplaza con tu anon key
const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

// LÍNEA 40 - Reemplaza con tu App ID de OneSignal
const val ONESIGNAL_APP_ID = "12345678-1234-1234-1234-123456789012"
```

5. **Guarda** el archivo (Ctrl+S)

---

### Paso 5: Build y Test (1 minuto)

1. **Gradle Sync:**
   - Android Studio debería sincronizar automáticamente
   - Si no: File → Sync Project with Gradle Files
   - Espera a que termine (puede tardar 2-5 minutos la primera vez)

2. **Build:**
   - Build → Make Project
   - Espera 2-3 minutos

3. **Run:**
   - Conecta tu dispositivo Android o inicia emulador
   - Click en Run (triángulo verde) o Shift+F10

4. **Prueba:**
   - Click en "Crear cuenta" o "Registro"
   - Ingresa email y contraseña
   - Click en "Registrar"
   - ✅ Deberías iniciar sesión y ver la pantalla principal

---

## ✅ Verificación

### Verifica en Supabase:

1. Ve a **Table Editor**
2. Click en tabla **users**
3. ✅ Deberías ver tu usuario registrado

### Verifica en Android:

1. Abre **Logcat** en Android Studio
2. Filtra por: `SupabaseConfig`
3. ✅ Deberías ver mensajes de conexión exitosa

---

## 🔧 Solución de Problemas

### Error: "Invalid API key"

**Causa:** Credenciales incorrectas

**Solución:**
1. Verifica que copiaste correctamente las credenciales
2. Asegúrate de que no hay espacios extra
3. Reinicia la app

---

### Error: "Table does not exist"

**Causa:** No ejecutaste el SQL

**Solución:**
1. Ve a Supabase → SQL Editor
2. Ejecuta `database_schema.sql` completo
3. Reinicia la app

---

### Error: "Unresolved reference: io.github.jan.supabase"

**Causa:** Gradle no descargó las dependencias

**Solución:**
1. File → Invalidate Caches / Restart
2. Build → Clean Project
3. Build → Rebuild Project

---

### OneSignal no funciona

**Causa:** App ID incorrecto o permisos faltantes

**Solución:**
1. Verifica `ONESIGNAL_APP_ID` en SupabaseConfig.kt
2. En Android 13+, concede permiso de notificaciones cuando la app lo pida
3. Revisa logs: `adb logcat | grep OneSignal`

---

## 📞 ¿Necesitas Ayuda?

1. **Revisa:** `ERRORS_AND_FIXES.md` - Lista completa de errores corregidos
2. **Revisa:** `README.md` - Documentación completa
3. **Logs:** `adb logcat | grep -i supabase`
4. **Dashboard:** Verifica en Supabase Dashboard → Table Editor

---

## 🎯 Checklist Final

Antes de empezar, verifica:

- [ ] Tienes cuenta en Supabase
- [ ] Creaste proyecto en Supabase
- [ ] Copiaste Project URL y anon key
- [ ] Ejecutaste database_schema.sql
- [ ] Tienes cuenta en OneSignal
- [ ] Copiaste App ID de OneSignal
- [ ] Editaste SupabaseConfig.kt con las credenciales
- [ ] Gradle sincronizó sin errores
- [ ] Build completó sin errores
- [ ] La app se ejecuta en el dispositivo

---

## 🚀 ¡Listo!

Si todo funciona:

✅ Tu app está conectada a Supabase
✅ Los usuarios pueden registrarse
✅ Los chats funcionan en tiempo real
✅ Los mensajes se cifran con Android Keystore
✅ OneSignal está configurado para notificaciones

**Siguiente paso:** Añadir features románticos 💕

---

**Tiempo estimado:** 10 minutos
**Dificultad:** Fácil

¡Buena suerte! 🎉
