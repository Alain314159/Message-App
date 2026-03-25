# 📱 Guía: Tests en Emulador (Ejecución Manual)

## 📋 ¿Qué es Este Workflow?

Workflow **manual** para ejecutar tests que requieren emulador Android.

**NO se ejecuta automáticamente** - solo cuando tú lo activas desde GitHub.

---

## 🚀 Cómo Ejecutar Manualmente

### Paso 1: Ir a GitHub Actions

```
https://github.com/TU_USUARIO/Message-App/actions
```

### Paso 2: Seleccionar Workflow

En la barra lateral izquierda, click en:
```
📱 Tests en Emulador (Manual)
```

### Paso 3: Click en "Run workflow"

Verás un botón verde:
```
┌────────────────────────────┐
│  Run workflow  ▼           │
└────────────────────────────┘
```

### Paso 4: Configurar Opciones

Se despliega un menú:

```
┌──────────────────────────────────────────┐
│  Run workflow                            │
├──────────────────────────────────────────┤
│  Branch: [develop            ▼]          │
│                                          │
│  Android API Level:                      │
│  ○ 26  ○ 27  ○ 28  ○ 29                  │
│  ● 30  ○ 31  ○ 32  ○ 33  ○ 34            │
│                                          │
│  Test Type:                              │
│  ● connectedAndroidTest                  │
│  ○ connectedDebugAndroidTest             │
│                                          │
│  Timeout (minutes): [30]                 │
│                                          │
│  ┌──────────┐  ┌──────────┐              │
│  │  Cancel  │  │ Run workflow│           │
│  └──────────┘  └──────────┘              │
└──────────────────────────────────────────┘
```

### Paso 5: Click en "Run workflow"

¡Listo! GitHub va a:
1. Crear emulador Android
2. Instalar tu app
3. Ejecutar tests
4. Subir resultados

---

## ⚙️ Opciones Disponibles

### API Level (Android Version)

| API Level | Android Version | Recomendado para |
|-----------|-----------------|------------------|
| 26 | Android 8.0 (Oreo) | Devices antiguos |
| 27 | Android 8.1 (Oreo) | - |
| 28 | Android 9 (Pie) | - |
| 29 | Android 10 | - |
| **30** | **Android 11** | **✅ Default (recomendado)** |
| 31 | Android 12 | Devices nuevos |
| 32 | Android 12L | - |
| 33 | Android 13 | Latest estable |
| 34 | Android 14 | Beta/Preview |

### Test Type

| Opción | Descripción |
|--------|-------------|
| `connectedAndroidTest` | Todos los tests de instrumentación |
| `connectedDebugAndroidTest` | Solo tests de build debug |

### Timeout

- **Default:** 30 minutos
- **Mínimo:** 5 minutos
- **Máximo:** 360 minutos (6 horas)

---

## 📊 ¿Qué Pasa Después de Ejecutar?

### Timeline Típica:

```
00:00 - Click en "Run workflow"
00:30 - GitHub crea máquina virtual
01:00 - Descarga tu código
01:30 - Instala JDK 17
02:00 - Crea emulador Android (API 30)
03:00 - Boot del emulador
05:00 - Instala tu app en emulador
05:30 - Ejecuta tests
08:00 - Tests completados
08:30 - Sube resultados y artifacts
09:00 - Emulador se destruye
```

**Total:** ~8-10 minutos

---

## 📥 Ver Resultados

### En GitHub Actions:

1. Ve a `https://github.com/TU_USUARIO/Message-App/actions`
2. Click en el workflow más reciente
3. Verás output como:

```
✅ Tests en Emulador (Manual) #3
   Success - 8m 45s
   
   Tests: 5 passed, 0 failed
   
   Emulator: Android API 30 (Nexus 6)
```

### Descargar Artifacts:

Al finalizar, en la página del workflow:

```
┌─────────────────────────────────┐
│  Artifacts                      │
├─────────────────────────────────┤
│  📄 emulator-test-results       │
│  📄 emulator-test-logs          │
│  📄 emulator-screenshots        │
└─────────────────────────────────┘
```

Click para descargar ZIP con:
- Reporte HTML de tests
- Logs detallados
- Screenshots (si los tests toman)

---

## 💰 Costo - Minutos de GitHub Actions

### Consumo Estimado:

| Recurso | Minutos por Ejecución |
|---------|----------------------|
| **Setup (2 min)** | 2 min |
| **Emulator Boot (3 min)** | 3 min |
| **Tests (3-5 min)** | 3-5 min |
| **TOTAL** | **~8-10 minutos** |

### Tu Límite Gratis:

```
2000 minutos/mes
Si ejecutas 1 vez al día: 300 min/mes
Te sobran: 1700 min/mes
```

**Recomendación:** Ejecutar 2-3 veces por semana máximo.

---

## 🚨 Solución de Problemas

### Problema: Workflow No Aparece

**Causa:** GitHub Actions deshabilitado

**Solución:**
1. Ve a `Settings > Actions`
2. Selecciona "Allow all actions"
3. Click "Save"

### Problema: Emulador Tarda Mucho

**Causa:** API level muy alto o perfil muy grande

**Solución:**
- Usar API 30 en lugar de 33/34
- Cambiar perfil a `Nexus 4` (más pequeño)
- Reducir `ram-size` a 1024M

### Problema: Tests Fallan

**Causa:** Tests mal escritos o app crasha

**Solución:**
1. Ver logs del paso "Ejecutar Tests en Emulador"
2. Identificar error específico
3. Fixear código o test
4. Ejecutar de nuevo

### Problema: Timeout

**Causa:** Tests muy lentos o emulador se cuelga

**Solución:**
- Aumentar `timeout-minutes` a 45 o 60
- Revisar logs para ver dónde se trabó
- Reducir número de tests

---

## 🎯 Cuándo Usar Este Workflow

### ✅ Usar Cuando:

- [ ] Tienes tests de UI (Compose, Views)
- [ ] Necesitas probar en Android real
- [ ] Tests usan Camera, Location, Sensors
- [ ] Quieres verificar integración completa
- [ ] Antes de release importante

### ❌ NO Usar Cuando:

- [ ] Solo tienes tests unitarios JVM
- [ ] Tests no requieren Android framework
- [ ] Quieres ejecución automática con push
- [ ] Estás en desarrollo rápido (usa tests JVM)

---

## 📊 Comparación: Unit Tests vs Emulator Tests

| Característica | Unit Tests (Auto) | Emulator Tests (Manual) |
|----------------|-------------------|------------------------|
| **Trigger** | Automático (push) | Manual (click) |
| **Tiempo** | 3-5 min | 8-10 min |
| **Costo** | ~5 min/meses | ~10 min/ejecución |
| **Requiere** | JVM | Emulador Android |
| **Tipo Tests** | Repository, ViewModel | UI, Integration |
| **Frecuencia** | Cada push | 2-3 veces/semana |

---

## ✅ Checklist de Ejecución

### Antes de Ejecutar:

- [ ] Tests de UI escritos en `src/androidTest/`
- [ ] Tests subidos a GitHub
- [ ] Tienes minutos disponibles en GitHub Actions
- [ ] No hay otros workflows ejecutándose

### Después de Ejecutar:

- [ ] Verificar tests pasan
- [ ] Descargar artifacts si es necesario
- [ ] Revisar logs si hay fallos
- [ ] Cerrar issue/PR si todo está bien

---

## 🎯 Flujo Recomendado

### Desarrollo Diario:

```
1. Escribir código + tests unitarios (JVM)
   ↓
2. git push (ejecuta unit tests automático)
   ↓
3. Ver resultados en Actions (3-5 min)
   ↓
4. ✅ Si pasa: continuar
   ❌ Si falla: fixear
```

### Antes de Release:

```
1. Escribir tests de UI (androidTest)
   ↓
2. git push
   ↓
3. Ir a Actions > Tests en Emulador
   ↓
4. Click "Run workflow" (API 30, 30 min)
   ↓
5. Esperar 8-10 min
   ↓
6. Ver resultados
   ↓
7. ✅ Si pasa: hacer release
   ❌ Si falla: fixear y repetir
```

---

**Estado:** ✅ Workflow creado, listo para usar manualmente  
**Próximo Paso:** Subir a GitHub y ejecutar cuando necesites tests de UI 🚀
