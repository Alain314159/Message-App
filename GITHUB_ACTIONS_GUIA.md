# 🚀 Guía: Ejecutar Tests en GitHub Actions (No en tu Teléfono)

## 📋 ¿Qué Hicimos?

Creamos el archivo `.github/workflows/unit-tests.yml` que le dice a GitHub:

> "Cada vez que haga push, ejecuta los tests en TUS servidores, no en mi teléfono"

---

## 🎯 Cómo Usar

### Paso 1: Subir Cambios a GitHub

```bash
cd /data/data/com.termux/files/home/Message-App

# Agregar cambios
git add .

# Commit
git commit -m "Agregar GitHub Actions para tests"

# Push a GitHub
git push origin develop
```

### Paso 2: GitHub Ejecuta Tests Automáticamente

Después de hacer push:

1. GitHub recibe tu código
2. Detecta el archivo `unit-tests.yml`
3. Crea una máquina virtual Ubuntu
4. Ejecuta: `./gradlew test`
5. Sube resultados

### Paso 3: Ver Resultados

Ve a:
```
https://github.com/TU_USUARIO/Message-App/actions
```

Verás algo como:

```
✅ Tests Unitarios #1
   Success - 3m 45s
   
   70 tests passed, 0 failed
```

O si hay error:

```
❌ Tests Unitarios #2
   Failure - 2m 15s
   
   68 tests passed, 2 failed
```

---

## 📊 ¿Qué Pasa en GitHub Actions?

```
┌─────────────────────────────────────────┐
│  Haces push a GitHub                    │
│  git push origin develop                │
└─────────────┬───────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  GitHub detecta .github/workflows/      │
│  Crea máquina virtual Ubuntu            │
│  - 2 núcleos CPU                        │
│  - 7 GB RAM                             │
│  - Gradle preinstalado                  │
│  - JDK 17 instalado                     │
└─────────────┬───────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Ejecuta pasos del workflow:            │
│  1. Checkout (descarga tu código)       │
│  2. Setup Java 17                       │
│  3. Cache Gradle (si ya existe)         │
│  4. chmod +x gradlew                    │
│  5. ./gradlew test                      │
└─────────────┬───────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Sube resultados como artifacts:        │
│  - Reporte HTML                         │
│  - Logs de tests                        │
└─────────────┬───────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Tú ves resultados en:                  │
│  github.com/.../actions                 │
└─────────────────────────────────────────┘
```

---

## 🔍 Ver Detalles de Tests

### Desde GitHub Actions:

1. Ve a `https://github.com/TU_USUARIO/Message-App/actions`
2. Click en el workflow más reciente
3. Click en `test` (el job)
4. Expandir paso `Ejecutar Tests Unitarios`

Verás output como:

```
> Task :app:testDebugUnitTest

ChatRepositoryTest > directChatIdFor trims whitespace PASSED
ChatRepositoryTest > sendText throws when chatId is empty PASSED
ChatViewModelTest > initial state has null chat PASSED
...

BUILD SUCCESSFUL in 3m 45s
70 tests completed, 0 failed
```

---

## 📥 Descargar Reporte Completo

### Después que el workflow termina:

1. En la página del workflow
2. Scroll abajo a "Artifacts"
3. Click en `test-results-html` o `test-logs`
4. Descarga ZIP
5. Abre `index.html` en tu navegador

Verás reporte HTML como:

```
┌──────────────────────────────────────┐
│  Test Results - 70 tests             │
├──────────────────────────────────────┤
│  ✅ 70 passed                        │
│  ❌ 0 failed                         │
│  ⏭️  0 skipped                       │
├──────────────────────────────────────┤
│  Packages:                           │
│  - ChatRepositoryTest (12 tests)     │
│  - ChatViewModelTest (22 tests)      │
│  - MessageDaoTest (1 test)           │
└──────────────────────────────────────┘
```

---

## ⚙️ Personalización del Workflow

### Ejecutar Tests Solo en Push

```yaml
on:
  push:
    branches: [ main, develop ]
```

### Ejecutar en Push Y Pull Requests

```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
```

### Ejecutar en Horarios Específicos

```yaml
on:
  schedule:
    # Ejecutar todos los días a las 3 AM
    - cron: '0 3 * * *'
```

---

## 💰 Costo - ¿Es Gratis?

### GitHub Free (Tu Caso)

| Recurso | Límite | Tu Uso |
|---------|--------|--------|
| **Minutos/mes** | 2000 min | ~100-300 min/mes |
| **Almacenamiento** | 500 MB | ~50 MB |
| **Artifacts** | 30 días | 7 días |

**¡Sí, es GRATIS para tu uso!**

### Estimado de Uso

```
Cada ejecución de tests: ~3-5 minutos
Si ejecutas 2 veces al día: 60-300 min/mes
Te sobran: 1700-1940 min/mes
```

---

## 🚨 Solución de Problemas

### Problema: Workflow No Se Ejecuta

**Causa:** GitHub Actions deshabilitado

**Solución:**
1. Ve a `https://github.com/TU_USUARIO/Message-App/settings/actions`
2. Asegúrate que "Allow all actions" está seleccionado
3. Click "Save"

### Problema: Tests Fallan

**Causa:** Tests mal escritos o código roto

**Solución:**
1. Click en workflow fallido
2. Ver logs del paso "Ejecutar Tests Unitarios"
3. Identificar qué test falló
4. Fixear código o test
5. Hacer push de nuevo

### Problema: Se Agota el Tiempo

**Causa:** Tests muy lentos (> 6 horas)

**Solución:**
- No aplica para tu caso (tests son ~3-5 min)

---

## 📊 Flujo de Trabajo Recomendado

### Desarrollo Diario en Termux:

```bash
# 1. Escribir código y tests
#    (sin ejecutar tests por falta de Gradle)

# 2. Commit y push
git add .
git commit -m "Feature: X con tests"
git push origin develop

# 3. Esperar 5 minutos

# 4. Ver resultados en GitHub Actions
#    https://github.com/TU_USUARIO/Message-App/actions

# 5. Si tests pasan: ✅ Continuar
#    Si tests fallan: ❌ Fixear y hacer push de nuevo
```

---

## ✅ Checklist de Verificación

- [ ] Archivo `.github/workflows/unit-tests.yml` creado
- [ ] Subir archivo a GitHub
- [ ] Hacer push de código
- [ ] Ver workflow ejecutándose en Actions tab
- [ ] Esperar 3-5 minutos
- [ ] Verificar tests pasan (70 tests, 0 failed)
- [ ] Descargar reporte HTML si es necesario

---

## 🎯 Siguientes Pasos

1. **Subir workflow a GitHub:**
   ```bash
   git add .github/workflows/unit-tests.yml
   git commit -m "Agregar GitHub Actions para tests"
   git push origin develop
   ```

2. **Ver primera ejecución:**
   - Ve a `https://github.com/TU_USUARIO/Message-App/actions`
   - Deberías ver "Tests Unitarios #1" ejecutándose

3. **Esperar resultados:**
   - ~3-5 minutos
   - 70 tests deberían pasar

---

**Estado:** ✅ Workflow creado, listo para subir  
**Próximo Paso:** `git push` y ver magia en GitHub Actions 🚀
