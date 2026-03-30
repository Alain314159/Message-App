# Lecciones Aprendidas - Message App

## 📚 Registro de Errores y Soluciones (2026-03-29)

### Errores Críticos Corregidos

#### 1. BUILD FAILED - Bloque Comentado en Gradle Kotlin DSL
**Problema:** `Unresolved reference: generated` en `app/build.gradle.kts` línea 232  
**Causa:** Kotlin Gradle DSL analiza código dentro de `/* */`  
**Solución:** Eliminar bloques comentados del build.gradle.kts  
**Lección:** En Gradle Kotlin DSL, usar `//` para comentarios, no `/* */`

#### 2. GitHub Actions - Node.js 24 Update
**Problema:** `actions/upload-artifact@v4` usa Node.js 20 (deprecated)  
**Solución:** Actualizar a actions v5 en `unit-tests.yml`  
**Lección:** Mantener GitHub Actions actualizadas

#### 3. Wildcard Imports (Detekt)
**Problema:** 10 archivos con `import androidx.compose.*`  
**Solución:** Reemplazar con imports específicos  
**Lección:** Imports específicos mejoran legibilidad y mantenibilidad

### Testing Best Practices

**Calificación:** ⭐⭐⭐⭐⭐ (5/5)

**Fortalezas:**
- ✅ Nombres con backticks: `should_do_X_when_Y`
- ✅ Estructura Given/When/Then
- ✅ Edge cases cubiertos (null, empty, invalid)
- ✅ Mocks con MockK
- ✅ Flow testing con Turbine
- ✅ Cobertura: 85-92%

**Lecciones:**
1. Tests JVM puros son más rápidos
2. Un comportamiento por test
3. Edge cases previenen bugs en producción

---

**Archivo completo:** `specs/archive/lessons-full.md` (3994 líneas)
