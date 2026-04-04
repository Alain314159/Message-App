# 📖 GUÍA DE CONFIGURACIÓN PASO A PASO

Sigue esta guía **EXACTAMENTE** para configurar tu app.

**⚠️ Última actualización:** 2026-04-04

---

## 📌 PARTE 1: Configurar Credenciales

### Paso 1.1: Copiar Plantilla de Credenciales

**⚠️ IMPORTANTE:** Las credenciales se cargan desde `gradle.properties`, NO desde código Kotlin.

```bash
# En la raíz del proyecto:
cp gradle.properties.example gradle.properties
```

### Paso 1.2: Obtener Credenciales de Supabase

1. Ve a https://supabase.com
2. Click en **"Start your project"** o **"Sign Up"**
3. Puedes registrarte con:
   - Email y contraseña
   - GitHub
   - Google

### Paso 1.3: Crear Proyecto

1. Click en **"New Project"** (botón verde)
2. Llena los datos:
   - **Name:** `message-app` (o el que quieras)
   - **Database Password:** Elige una contraseña FUERTE (guárdala)
   - **Region:** Elige la más cercana a ti
     - Para Cuba/Latinoamérica: **South America (Brazil)**
3. Click en **"Create new project"**
4. ⏳ Espera 2-5 minutos mientras se crea el proyecto

### Paso 1.4: Obtener Credenciales del Proyecto

1. En el dashboard del proyecto, ve a **Settings** (engranaje abajo a la izquierda)
2. Click en **API**
3. Copia estos dos valores:
   - **Project URL:** `https://xxxxx.supabase.co`
   - **anon/public key:** `eyJhbG...` (cadena larga)

### Paso 1.5: Agregar Credenciales a gradle.properties

Edita el archivo `gradle.properties` que creaste:

```properties
# Reemplaza estos valores con tus credenciales reales
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**⚠️ NO subas este archivo a git** - Ya está en `.gitignore`

### Paso 1.6: Crear Tablas de Base de Datos

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

## 📌 PARTE 2: Configurar Firebase para Notificaciones (Opcional)

### Paso 2.1: Crear Proyecto Firebase

**⚠️ Solo si necesitas notificaciones push**

1. Ve a https://console.firebase.google.com
2. Click en **"Agregar proyecto"** o usa uno existente
3. Sigue los pasos de configuración

### Paso 2.2: Registrar App Android

1. Click en **"Agregar app"** → ícono de Android
2. Ingresa el package name: `com.example.messageapp`
3. Registra la app

### Paso 2.3: Descargar google-services.json

1. Descarga el archivo `google-services.json`
2. Colócalo en: `app/google-services.json`

**⚠️ IMPORTANTE:** Este archivo NO está en git por seguridad. Cada desarrollador debe generar el suyo.

---

## 📌 PARTE 3: Configurar la App Android

### Paso 3.1: Abrir Proyecto en Android Studio

1. Abre Android Studio
2. **File → Open**
3. Selecciona la carpeta del proyecto
4. Espera a que Gradle sincronice (puede tardar 5-10 minutos la primera vez)
5. ⚠️ **Si el build falla**, verifica que `gradle.properties` tenga credenciales válidas

### Paso 3.2: Verificar Build Configuration

1. Abre `app/build.gradle.kts`
2. Verifica que tenga las dependencias de Supabase y Firebase
3. Si hiciste cambios a `gradle.properties`, Gradle sincronizará automáticamente

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

### Error: "SUPABASE_URL no está configurada" o "SUPABASE_ANON_KEY no está configurada"

**Causa:** Credenciales faltantes en `gradle.properties`

**Solución:**
1. Copia `gradle.properties.example` a `gradle.properties`
2. Agrega tus credenciales de Supabase
3. Vuelve a hacer build

### Error: "Table does not exist"

**Causa:** No ejecutaste `database_schema.sql`

**Solución:**
1. Ve a Supabase → SQL Editor
2. Ejecuta el script completo
3. Reinicia la app

### Error: "google-services.json missing"

**Causa:** Firebase no configurado (opcional)

**Solución:**
1. Si necesitas notificaciones push, configura Firebase
2. Descarga `google-services.json` desde Firebase Console
3. Colócalo en `app/`
4. Si NO necesitas notificaciones, ignora este error

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

3. ✅ **Prueba notificaciones (si configuraste FCM):**
   - Cierra la app en un dispositivo
   - Envía mensaje desde el otro
   - Deberías recibir notificación push

---

## 📞 ¿Problemas?

Revisa:
- `README.md` - Documentación general
- `SECURITY_GUIDE.md` - Guía de seguridad
- Logs de Android Studio (`Logcat`)
- Dashboard de Supabase (errores de base de datos)

---

**¡Buena suerte! 🚀💕**

**Última actualización:** 2026-04-04
