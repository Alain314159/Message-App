# 🪝 Hooks de Pre-Entrega de Código

## ⚠️ REGLAS OBLIGATORIAS ANTES DE ENTREGAR CÓDIGO

### Checklist Pre-Commit (DEBE completar antes de entregar)

```markdown
## Verificación de Calidad

### 1. Especificaciones
- [ ] ¿Existe `specs/functional.md` para esta feature?
- [ ] ¿El código implementa exactamente lo especificado?
- [ ] ¿Se siguieron los criterios de aceptación?

### 2. Tests (TDD OBLIGATORIO)
- [ ] ¿Los tests se escribieron ANTES del código?
- [ ] ¿Los tests cubren el happy path?
- [ ] ¿Los tests cubren edge cases?
- [ ] ¿Los tests cubren manejo de errores?
- [ ] ¿Todos los tests pasan localmente?

### 3. Validación de Código
- [ ] ¿El código compila sin errores?
- [ ] ¿No hay warnings nuevos?
- [ ] ¿Se siguió el style guide del proyecto?
- [ ] ¿Los nombres son descriptivos?
- [ ] ¿Las funciones tienen < 30 líneas?

### 4. Contexto
- [ ] ¿Se actualizó `context/state.md`?
- [ ] ¿Se registraron lecciones en `specs/lessons.md`?
- [ ] ¿Se documentó en `context/decisions.md` si aplica?

### 5. Seguridad
- [ ] ¿No hay credentials hardcodeados?
- [ ] ¿Se validan inputs de usuario?
- [ ] ¿Se manejan errores sin exponer detalles?
```

---

## 🚫 CRITERIOS DE RECHAZO AUTOMÁTICO

El código se rechaza AUTOMÁTICAMENTE si:

1. ❌ **No tiene tests** - Sin excepciones
2. ❌ **Tests fallando** - 0 tolerancia
3. ❌ **No compila** - Errores de compilación
4. ❌ **Violaciones de seguridad** - Credentials, SQL injection, etc.
5. ❌ **Specs missing** - No hay especificación funcional

---

## 🔍 VERIFICACIÓN AUTOMÁTICA (Android - Sin Build Completo)

### Comandos de Validación Rápida

```bash
# 1. Verificar sintaxis Kotlin (rápido)
./gradlew :app:compileDebugKotlin --dry-run

# 2. Verificar tests unitarios (sin emulator)
./gradlew :app:testDebugUnitTest --tests "*Test"

# 3. Verificar lint (sin build completo)
./gradlew :app:lint

# 4. Verificar detekt (análisis estático)
./gradlew :app:detekt

# 5. Verificar formato
./gradlew :app:ktlintCheck
```

### En Termux (sin build completo)

```bash
# Validación estática disponible
kotlinc -script validate.kts src/**/*.kt

# O usar análisis de texto
grep -r "TODO\|FIXME" src/
grep -r "print\|println" src/
```

---

## 📊 MÉTRICAS MÍNIMAS ACEPTABLES

| Métrica | Mínimo | Óptimo | Cómo Verificar |
|---------|--------|--------|----------------|
| **Test Coverage** | 70% | 80% | `./gradlew jacocoTestReport` |
| **Funciones < 30 líneas** | 90% | 95% | Detekt |
| **Complejidad Ciclomática** | < 10 | < 8 | Detekt |
| **Tests por Clase** | 1 | 3+ | Contar archivos *Test.kt |
| **Build Time** | < 3 min | < 2 min | Timing del build |

---

## 🛡️ VALIDACIÓN POR TIPO DE CAMBIO

### Para Nueva Feature

```markdown
## Requisitos Mínimos

1. **Specs** (OBLIGATORIO)
   - [ ] `specs/functional.md` actualizado
   - [ ] Criterios de aceptación definidos
   - [ ] Casos de error documentados

2. **Tests** (OBLIGATORIO)
   - [ ] Test de happy path
   - [ ] Test de 2 edge cases mínimo
   - [ ] Test de error handling
   - [ ] Cobertura > 80% para código nuevo

3. **Código**
   - [ ] Sigue arquitectura MVVM + Clean
   - [ ] ViewModels con @HiltViewModel
   - [ ] UseCases para lógica de negocio
   - [ ] Repositories para datos

4. **Documentación**
   - [ ] README actualizado si aplica
   - [ ] KDoc para funciones públicas
   - [ ] CHANGELOG.md actualizado
```

### Para Bug Fix

```markdown
## Requisitos Mínimos

1. **Bug Report**
   - [ ] ID del bug (ej: BUG-001)
   - [ ] Descripción del problema
   - [ ] Pasos para reproducir

2. **Test** (OBLIGATORIO)
   - [ ] Test que reproduce el bug (falla antes del fix)
   - [ ] Test pasa después del fix
   - [ ] Test se queda en el codebase

3. **Fix**
   - [ ] Mínimo código posible
   - [ ] No rompe tests existentes
   - [ ] Maneja edge case similar

4. **Prevención**
   - [ ] Agregado a `specs/lessons.md`
   - [ ] Checklist actualizada si aplica
```

### Para Refactor

```markdown
## Requisitos Mínimos

1. **Justificación**
   - [ ] ¿Por qué se refactoriza?
   - [ ] ¿Qué mejora? (performance, legibilidad, etc.)
   - [ ] ¿Hay métricas de mejora?

2. **Tests Existentes**
   - [ ] Todos los tests pasan antes
   - [ ] Todos los tests pasan después
   - [ ] No se cambia comportamiento

3. **Review**
   - [ ] Code review obligatorio
   - [ ] Comparación before/after
   - [ ] Métricas de mejora verificadas
```

---

## 🎯 PROCESO DE REVISIÓN

### 1. Auto-Revisión (OBLIGATORIA)

Antes de pedir review:

```bash
# Ejecutar checklist completa
# Verificar que todos los items están marcados
# Ejecutar tests localmente
# Verificar que no hay cambios accidentales
```

### 2. Code Review (OBLIGATORIO)

Reviewer verifica:

- [ ] Checklist completa
- [ ] Tests tienen sentido
- [ ] Código sigue convenciones
- [ ] No hay code smells
- [ ] Seguridad verificada

### 3. Merge (SOLO SI TODO VERDE)

```bash
# Solo merge si:
# ✅ Todos los tests pasan
# ✅ Code review aprobado
# ✅ Checklist completa
# ✅ Specs actualizadas
```

---

## 📝 PLANTILLA DE COMMIT

```markdown
[TIPO] Descripción concisa

## Contexto
- Issue: #XXX
- Specs: specs/functional.md#US-XXX

## Cambios
- Feature: [descripción]
- Tests: [qué se testeó]
- Docs: [qué se documentó]

## Verificación
- [ ] Tests pasan
- [ ] Specs actualizadas
- [ ] Contexto actualizado
- [ ] Lecciones documentadas

## Testing
```bash
# Comandos ejecutados
./gradlew :app:testDebugUnitTest
./gradlew :app:detekt
```
```

---

## 🚨 ESCAPE HATCH (Solo Emergencias)

Si necesitas saltarte los hooks:

1. **Justificación escrita** en `context/state.md`
2. **Aprobación** de tech lead
3. **Follow-up** obligatorio en 24h

```markdown
## Emergency Bypass

**Fecha:** 2026-03-24  
**Justificación:** [Por qué se saltó el proceso]  
**Aprobado por:** [Nombre]  
**Follow-up:** [Fecha límite para fix]
```

---

**Última Actualización:** 2026-03-24  
**Aplicación:** Inmediata  
**Excepciones:** Solo con aprobación de tech lead
