# 🐨🐷 SISTEMA DE AVATARES - GUÍA RÁPIDA

## ✅ LO QUE ESTÁ IMPLEMENTADO

### Archivos Creados:

1. **`model/Avatar.kt`** - Modelo de datos con los dos avatares:
   - 🐷 `CERDITA` (ya tienes la imagen)
   - 🐨 `KOALA` (falta la imagen)

2. **`data/AvatarRepository.kt`** - Repositorio para guardar/seleccionar avatares

3. **`viewmodel/AvatarViewModel.kt`** - ViewModel para la UI

4. **`ui/avatar/AvatarPickerScreen.kt`** - Pantalla de selección de avatares

5. **`drawable/avatar_cerdita.jpg`** - Imagen de la cerdita ✅

6. **`database_schema.sql`** - Actualizado con campo `avatar_type`

7. **`database_updates/add_avatar_type.sql`** - Script para actualizar DB existente

---

## 📋 CÓMO AGREGAR EL AVATAR DE KOALA

Cuando tengas la imagen del koala:

### Paso 1: Copiar la imagen
```bash
# En Termux, copiá la imagen del koala a:
cp /sdcard/ruta/de/tu/imagen.jpg /data/data/com.termux/files/home/Message-App/app/src/main/res/drawable/avatar_koala.jpg
```

### Paso 2: Actualizar el código (YA ESTÁ LISTO!)

El código **YA ESTÁ PREPARADO** para cuando agregues la imagen. Solo necesitás:

1. Agregar la imagen `avatar_koala.jpg` en la carpeta `drawable`
2. Hacer commit y push
3. ¡Listo! El sistema automáticamente mostrará ambos avatares

---

## 🎮 CÓMO FUNCIONA LA SELECCIÓN DE AVATARES

### En la App:

1. **Usuario abre Profile** → Click en "Cambiar Avatar"
2. **Se abre AvatarPickerScreen** → Muestra Koala y Cerdita
3. **Usuario selecciona** → Toca el avatar que quiere
4. **Guarda** → Se actualiza en Supabase

### En la Base de Datos:

```sql
-- Tabla users ahora tiene:
avatar_type VARCHAR(20) DEFAULT 'cerdita'
-- Valores posibles: 'cerdita', 'koala'
```

---

## 🔄 ACTUALIZAR BASE DE DATOS EN SUPABASE

### Opción 1: Si es una DB nueva
Ejecutá `database_schema.sql` completo en el SQL Editor de Supabase.

### Opción 2: Si ya tenés la DB creada
Ejecutá solo este script en Supabase SQL Editor:

```sql
-- Agregar columna avatar_type
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_type VARCHAR(20) DEFAULT 'cerdita';

-- Comentario descriptivo
COMMENT ON COLUMN users.avatar_type IS 'Tipo de avatar seleccionado: cerdita, koala';
```

---

## 📱 CÓMO SE VE EN LA UI

```
┌─────────────────────────────────┐
│     Elige tu Avatar             │
│  Selecciona entre Koala o Cerdita│
│                                 │
│    🐨          🐷              │
│   Koala      Cerdita            │
│   (gris)    (seleccionado)      │
│                                 │
│      Vista previa:              │
│         🐷                      │
│                                 │
│  [Cancelar]    [Guardar]        │
└─────────────────────────────────┘
```

---

## 🎨 PERSONALIZACIÓN

### Cambiar los emojis temporales:

En `AvatarPickerScreen.kt`, buscá:

```kotlin
Text(
    text = when (avatarType) {
        AvatarType.CERDITA -> "🐷"  // Cambiar acá
        AvatarType.KOALA -> "🐨"    // Cambiar acá
    },
    fontSize = 48.sp
)
```

### Cuando tengas las imágenes:

Descomentá el bloque `Image()` en `AvatarPickerScreen.kt`:

```kotlin
/* DESCOMENTAR CUANDO TENGAS LAS IMÁGENES */
Image(
    painter = painterResource(id = avatarType.drawableResId),
    contentDescription = avatarType.displayName,
    modifier = Modifier.fillMaxSize()
)
```

---

## 📊 ESTADO ACTUAL

| Componente | Estado | Observación |
|------------|--------|-------------|
| Avatar Cerdita | ✅ Completo | Imagen agregada |
| Avatar Koala | ⏳ Pendiente | Falta imagen |
| Database Schema | ✅ Actualizado | Campo `avatar_type` agregado |
| UI de Selección | ✅ Completa | Funcional con emojis |
| Repository | ✅ Completo | Guarda en Supabase |
| ViewModel | ✅ Completo | Gestiona estado UI |

---

## 🚀 PRÓXIMOS PASOS

1. **Conseguir imagen del koala** (cuando la tengas)
2. **Copiar a `drawable/avatar_koala.jpg`**
3. **Ejecutar script SQL en Supabase**
4. **¡Listo! Los usuarios podrán elegir**

---

**Fecha:** 2026-03-24  
**Versión:** 1.0-avatar-system  
**Estado:** ✅ Funcional (falta imagen de koala)
