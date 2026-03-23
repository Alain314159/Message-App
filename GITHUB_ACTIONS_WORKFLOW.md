# 🤖 GitHub Actions Workflow - Android CI/CD

## 📋 Descripción

Workflow robusto y detallado para análisis estático, compilación y generación de APK con logs exhaustivos.

---

## 🚀 Cómo Usar

### Ejecución Automática

El workflow se ejecuta automáticamente en:

1. **Push a `main` o `develop`:**
   ```bash
   git push origin main
   ```

2. **Pull Request a `main`:**
   ```bash
   git pull-request
   ```

3. **Ejecución Manual:**
   - Ve a la pestaña **Actions** en GitHub
   - Selecciona **Android CI - Build & Verify**
   - Click en **Run workflow**

---

## 📊 Jobs del Workflow

### Job 1: 🔍 Code Quality Analysis

**Propósito:** Análisis estático del código sin compilación.

**Herramientas:**
- **Detekt:** Análisis estático de Kotlin
- **Android Lint:** Análisis de código Android
- **KtLint:** Verificación de formato
- **OWASP Dependency Check:** Vulnerabilidades en dependencias

**Artefactos generados:**
- `code-quality-reports` (reportes HTML/XML)
- `detekt-report.log`
- `lint-report.log`
- `ktlint-report.log`

**Duración estimada:** 2-4 minutos

---

### Job 2: 🔨 Build & Test

**Propósito:** Compilación y tests unitarios.

**Pasos:**
1. Setup de Java 17 + Android SDK
2. Verificación de estructura del proyecto
3. Verificación de configuración (Supabase, AndroidManifest)
4. Compilación Debug APK
5. Tests unitarios
6. Compilación Release APK (si hay signing config)
7. Análisis de APK

**Artefactos generados:**
- `app-debug-apk` (APK listo para instalar)
- `app-release-apk` (si existe signing config)
- `build-logs` (logs de compilación)

**Duración estimada:** 5-10 minutos

---

### Job 3: 📋 Final Report

**Propósito:** Generar resumen ejecutivo del build.

**Salida:**
- Reporte en GitHub Actions Summary
- Estado de cada job
- Enlaces de descarga

**Duración estimada:** 30 segundos

---

## 📥 Descargar APK

### Desde GitHub Actions:

1. Ve a **Actions** en tu repositorio
2. Selecciona la ejecución más reciente
3. Espera a que termine (✅ verde)
4. Baja hasta la sección **Artifacts**
5. Click en `app-debug-apk`
6. El APK se descargará (válido por 30 días)

### Instalar en Dispositivo:

```bash
# Conectar dispositivo y ejecutar:
adb install app-debug.apk
```

---

## 🔧 Configuración Requerida

### 1. Agregar al `build.gradle.kts` (Proyecto):

```kotlin
plugins {
    // ... tus plugins ...
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.owasp.dependencycheck") version "9.0.9"
}
```

### 2. Crear `config/detekt/detekt.yml`:

```yaml
build:
  maxIssues: 0
  ignoreFailures: true

complexity:
  LongMethod:
    threshold: 60
  LongParameterList:
    functionThreshold: 6
```

### 3. Configurar Signing (Opcional para Release):

En `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 4. Secrets de GitHub (Para Release):

Ve a **Settings → Secrets and variables → Actions** y agrega:

- `KEYSTORE_PATH`: Ruta al keystore (o súbelo al repo)
- `KEYSTORE_PASSWORD`: Contraseña del keystore
- `KEY_ALIAS`: Alias de la llave
- `KEY_PASSWORD`: Contraseña de la llave

---

## 🐛 Troubleshooting

### Error: "SupabaseConfig no encontrado"

**Causa:** El archivo tiene los placeholders.

**Solución:**
```bash
# Editar SupabaseConfig.kt y reemplazar:
const val SUPABASE_URL = "https://tu-proyecto.supabase.co"
const val SUPABASE_ANON_KEY = "tu-anon-key"
```

### Error: "Out of memory"

**Causa:** Gradle necesita más RAM.

**Solución:** En `.github/workflows/android-ci.yml`:
```yaml
env:
  GRADLE_OPTS: -Dorg.gradle.jvmargs=-Xmx6g  # Aumentar a 6GB
```

### Error: "No se generó el APK"

**Causa:** Falta algún archivo Gradle.

**Solución:**
```bash
# Verificar estructura:
ls -la
find . -name "*.gradle*" -type f
```

### Error: "Detekt failed"

**Causa:** Errores de calidad de código.

**Solución:**
```bash
# Ver reportes:
./gradlew detekt --info

# O ignora errores (solo reporta):
# En build.gradle.kts: detekt { ignoreFailures = true }
```

---

## 📊 Logs y Reportes

### Ver Logs en GitHub:

1. Ve a **Actions**
2. Selecciona la ejecución
3. Click en el job (ej: "Build & Test")
4. Expande cada paso para ver logs

### Descargar Logs Completos:

1. En la ejecución, baja a **Artifacts**
2. Descarga `build-logs`
3. Contiene:
   - `build-debug.log`
   - `build-release.log`
   - `test-results.log`

### Reportes de Calidad:

1. Descarga `code-quality-reports`
2. Abre en navegador:
   - `detekt.html` - Errores Kotlin
   - `lint-results-debug.html` - Errores Android

---

## ⚙️ Personalización

### Cambiar Java Version:

En `.github/workflows/android-ci.yml`:
```yaml
env:
  JAVA_VERSION: '21'  # Cambiar a 21
```

### Desactivar Tests:

Comenta el paso en el workflow:
```yaml
# - name: 🧪 Run Unit Tests
#   run: |
#     ./gradlew testDebugUnitTest
```

### Solo Ejecutar en Main:

```yaml
on:
  push:
    branches: [ main ]  # Solo main
```

### Agregar Notificaciones:

```yaml
- name: 📧 Notify on Failure
  if: failure()
  uses: actions/github-script@v7
  with:
    script: |
      github.rest.issues.create({
        owner: context.repo.owner,
        repo: context.repo.repo,
        title: 'Build Failed!',
        body: 'El build falló. Revisa: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}'
      })
```

---

## 📈 Métricas

| Métrica | Valor |
|---------|-------|
| **Tiempo promedio** | 8-12 minutos |
| **Artefactos** | 3-5 |
| **Jobs** | 3 |
| **Pasos totales** | ~20 |
| **Retención** | 30 días |

---

## 🔗 Recursos

- [Documentación oficial de GitHub Actions](https://docs.github.com/es/actions)
- [Android CI/CD Best Practices](https://developer.android.com/studio/build/building-cmdline)
- [Detekt Documentation](https://detekt.dev/docs/)
- [OWASP Dependency Check](https://jeremylong.github.io/DependencyCheck/)

---

**Última actualización:** 23 de Marzo, 2026  
**Versión:** 1.0
