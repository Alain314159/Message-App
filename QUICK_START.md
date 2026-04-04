# 🚀 GUÍA RÁPIDA DE CONFIGURACIÓN

## ⚡ Configuración en 10 Minutos

### Paso 1: Configurar Credenciales (2 minutos)

**⚠️ IMPORTANTE:** Las credenciales se cargan desde `gradle.properties`, NO desde código Kotlin.

```bash
# En la raíz del proyecto:
cp gradle.properties.example gradle.properties
```

Ahora edita `gradle.properties` con tus credenciales reales.

---

### Paso 2: Supabase - Crear Proyecto (3 minutos)

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

3. **Agregar a gradle.properties:**
```properties
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=eyJhbGc...
```

---

### Paso 3: Ejecutar SQL en Supabase (2 minutos)

1. Ve a **SQL Editor** (icono de código)
2. Click **"New Query"**
3. Abre `database_schema.sql` en tu proyecto
4. Copia TODO el contenido
5. Pega en el SQL Editor
6. Click **"Run"** (o Ctrl+Enter)
7. ✅ Verifica: Ve a **Table Editor** → Deberías ver 4 tablas

---

### Paso 4: Configurar FCM para Notificaciones (Opcional, 2 minutos)

**Solo si necesitas notificaciones push:**

1. Ve a https://console.firebase.google.com
2. Crea proyecto o usa uno existente
3. Agrega app Android con package name: `com.example.messageapp`
4. Descarga `google-services.json`
5. Colócalo en: `app/google-services.json`

**Nota:** Este archivo NO está en git por seguridad.

---

### Paso 5: Build y Test (1 minuto)

1. **Abre Android Studio**
2. **Abre el proyecto**
3. **Gradle Sync:**
   - Android Studio debería sincronizar automáticamente
   - Si no: File → Sync Project with Gradle Files
   - Espera a que termine (2-5 minutos la primera vez)

4. **Build:**
   - Build → Make Project
   - ⚠️ **Si el build falla**, verifica que `gradle.properties` tenga credenciales válidas

5. **Run:**
   - Conecta tu dispositivo Android o inicia emulador
   - Click en Run (triángulo verde) o Shift+F10

6. **Prueba:**
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
3. ✅ Deberías ver mensajes de inicialización exitosa

---

## 🔧 Solución de Problemas

### Error: "SUPABASE_URL no está configurada"

**Causa:** No copiaste o editaste `gradle.properties`

**Solución:**
```bash
cp gradle.properties.example gradle.properties
# Editar gradle.properties con credenciales reales
```

---

### Error: "Supabase credentials cannot be empty"

**Causa:** Credenciales vacías en `gradle.properties`

**Solución:**
1. Abre `gradle.properties`
2. Verifica que `SUPABASE_URL` y `SUPABASE_ANON_KEY` tengan valores válidos
3. Asegúrate de que no hay espacios extra

---

### Error: "Table does not exist"

**Causa:** No ejecutaste el SQL

**Solución:**
1. Ve a Supabase → SQL Editor
2. Ejecuta `database_schema.sql` completo
3. Reinicia la app

---

### Error: "Unresolved reference: io.github.jan-tennert.supabase"

**Causa:** Gradle no descargó las dependencias

**Solución:**
1. File → Invalidate Caches / Restart
2. Build → Clean Project
3. Build → Rebuild Project

---

### Error: "google-services.json missing"

**Causa:** Firebase no configurado (opcional)

**Solución:**
1. Si necesitas notificaciones push, configura Firebase
2. Descarga `google-services.json` desde Firebase Console
3. Colócalo en `app/google-services.json`
4. Si NO necesitas notificaciones, ignora este error

---

## 📞 ¿Necesitas Ayuda?

1. **Revisa:** `SECURITY_GUIDE.md` - Guía de seguridad actualizada
2. **Revisa:** `README.md` - Documentación completa
3. **Logs:** `adb logcat | grep -i supabase`
4. **Dashboard:** Verifica en Supabase Dashboard → Table Editor

---

## 🎯 Checklist Final

Antes de empezar, verifica:

- [ ] Copiaste `gradle.properties.example` a `gradle.properties`
- [ ] Tienes cuenta en Supabase
- [ ] Creaste proyecto en Supabase
- [ ] Copiaste Project URL y anon key a `gradle.properties`
- [ ] Ejecutaste database_schema.sql
- [ ] (Opcional) Configuraste Firebase para FCM
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
✅ FCM está configurado para notificaciones (si configuraste Firebase)

**Siguiente paso:** Añadir features románticos 💕

---

**Tiempo estimado:** 10 minutos
**Dificultad:** Fácil

¡Buena suerte! 🎉
