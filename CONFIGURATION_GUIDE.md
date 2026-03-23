# 📖 GUÍA DE CONFIGURACIÓN PASO A PASO

Sigue esta guía **EXACTAMENTE** para configurar tu app.

---

## 📌 PARTE 1: Configurar Supabase

### Paso 1.1: Crear Cuenta

1. Ve a https://supabase.com
2. Click en **"Start your project"** o **"Sign Up"**
3. Puedes registrarte con:
   - Email y contraseña
   - GitHub
   - Google

### Paso 1.2: Crear Proyecto

1. Click en **"New Project"** (botón verde)
2. Llena los datos:
   - **Name:** `message-app` (o el que quieras)
   - **Database Password:** Elige una contraseña FUERTE (guárdala)
   - **Region:** Elige la más cercana a ti
     - Para Cuba/Latinoamérica: **South America (Brazil)**
3. Click en **"Create new project"**
4. ⏳ Espera 2-5 minutos mientras se crea el proyecto

### Paso 1.3: Obtener Credenciales

1. En el dashboard del proyecto, ve a **Settings** (engranaje abajo a la izquierda)
2. Click en **API**
3. Copia estos dos valores:
   - **Project URL:** `https://xxxxx.supabase.co`
   - **anon/public key:** `eyJhbG...` (cadena larga)

### Paso 1.4: Crear Tablas de Base de Datos

1. En el dashboard, ve a **SQL Editor** (icono de código a la izquierda)
2. Click en **"New Query"**
3. Abre el archivo `database_schema.sql` en tu computadora
4. Copia **TODO** el contenido
5. Pega en el SQL Editor de Supabase
6. Click en **"Run"** (o Ctrl+Enter)
7. ✅ Deberías ver "Success. No rows returned"

**Verificación:**
- Ve a **Table Editor** (icono de tabla)
- Deberías ver 4 tablas: `users`, `chats`, `messages`, `contacts`

---

## 📌 PARTE 2: Configurar OneSignal

### Paso 2.1: Crear Cuenta

1. Ve a https://onesignal.com
2. Click en **"Sign Up"**
3. Regístrate con email

### Paso 2.2: Crear App

1. Click en **"Add App"** o **"Create App"**
2. Elige **"Android"** como plataforma
3. Pon un nombre: `Message App`
4. Click en **"Next"**

### Paso 2.3: Obtener Credenciales

1. En la página de configuración de la app:
   - Copia el **App ID** (cadena UUID)
   - Ve a **Settings** → **Keys & IDs**
   - Copia el **REST API Key**

**Nota:** La REST API Key se usa en el servidor (Edge Function), no en la app móvil.

---

## 📌 PARTE 3: Configurar la App Android

### Paso 3.1: Abrir Proyecto en Android Studio

1. Abre Android Studio
2. **File → Open**
3. Selecciona la carpeta `mensaje_app_supabase`
4. Espera a que Gradle sincronice (puede tardar 5-10 minutos la primera vez)

### Paso 3.2: Editar SupabaseConfig.kt

1. Navega a: `app/src/main/java/com/example/messageapp/supabase/SupabaseConfig.kt`
2. Reemplaza los valores:

```kotlin
// ANTES (valores por defecto):
const val SUPABASE_URL = "https://TU_PROYECTO.supabase.co"
const val SUPABASE_ANON_KEY = "TU_ANON_KEY_AQUI"
const val ONESIGNAL_APP_ID = "TU_ONESIGNAL_APP_ID_AQUI"

// DESPUÉS (tus valores reales):
const val SUPABASE_URL = "https://abc123.supabase.co"
const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
const val ONESIGNAL_APP_ID = "12345678-1234-1234-1234-123456789012"
```

3. **Guarda** el archivo (Ctrl+S)

### Paso 3.3: Verificar build.gradle.kts

1. Abre `app/build.gradle.kts`
2. Verifica que tenga las dependencias de Supabase y OneSignal
3. Si hiciste cambios, Gradle sincronizará automáticamente

---

## 📌 PARTE 4: Build y Pruebas

### Paso 4.1: Build

1. En Android Studio: **Build → Make Project**
2. Espera a que compile (2-5 minutos)
3. Si hay errores, revisa:
   - ¿Editaste correctamente `SupabaseConfig.kt`?
   - ¿Gradle sincronizó correctamente?

### Paso 4.2: Ejecutar en Dispositivo/Emulador

1. Conecta tu dispositivo Android o inicia un emulador
2. Click en **Run** (triángulo verde) o Shift+F10
3. La app debería abrirse

### Paso 4.3: Probar Registro/Login

1. En la app, elige **"Crear cuenta"** o **"Registro"**
2. Ingresa un email y contraseña
3. Click en **"Registrar"**
4. ✅ Deberías iniciar sesión y ver la pantalla principal

### Paso 4.4: Probar en Supabase

1. Ve a Supabase Dashboard → **Table Editor**
2. Click en tabla **users**
3. ✅ Deberías ver tu usuario registrado

---

## 📌 PARTE 5: Solución de Problemas

### Error: "Invalid API key" o "Unauthorized"

**Causa:** Credenciales incorrectas en `SupabaseConfig.kt`

**Solución:**
1. Verifica que copiaste correctamente las credenciales
2. Asegúrate de que no hay espacios extra
3. Reinicia la app

### Error: "Table does not exist"

**Causa:** No ejecutaste `database_schema.sql`

**Solución:**
1. Ve a Supabase → SQL Editor
2. Ejecuta el script completo
3. Reinicia la app

### OneSignal no envía notificaciones

**Causa:** App ID incorrecto o permisos faltantes

**Solución:**
1. Verifica `ONESIGNAL_APP_ID` en `SupabaseConfig.kt`
2. En Android 13+, concede permiso de notificaciones
3. Revisa logs con `adb logcat | grep OneSignal`

### Error de compilación: "Unresolved reference"

**Causa:** Dependencias no se descargaron correctamente

**Solución:**
1. **File → Invalidate Caches / Restart**
2. **Build → Clean Project**
3. **Build → Rebuild Project**

---

## 📌 PARTE 6: Próximos Pasos

Una vez que la app funcione:

1. ✅ **Verifica en Supabase:**
   - Tabla `users` tiene tu usuario
   - Tabla `chats` se crea al iniciar chat

2. ✅ **Prueba el chat:**
   - Crea 2 cuentas (2 dispositivos o logout/login)
   - Envía mensajes entre ellas
   - Verifica que se cifran/descifran

3. ✅ **Prueba notificaciones:**
   - Cierra la app en un dispositivo
   - Envía mensaje desde el otro
   - Deberías recibir notificación push

---

## 📞 ¿Problemas?

Revisa:
- `README.md` - Documentación general
- `supabase_config.env` - Template de configuración
- Logs de Android Studio (`Logcat`)
- Dashboard de Supabase (errores de base de datos)

---

**¡Buena suerte! 🚀💕**
