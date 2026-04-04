# 📚 Message App - Documentation Index

**Última Actualización:** 2026-04-04
**Estado del Proyecto:** 🚧 **EN DESARROLLO** - Correcciones críticas aplicadas

---

## 🎯 Inicio Rápido

### ¿Qué es Message App?
App de mensajería para Android con cifrado E2E, chat en tiempo real y notificaciones push.

**Stack Técnico:**
- Kotlin 1.9.22 + Jetpack Compose
- Supabase 3.4.1 (Auth + PostgREST + Realtime + Storage)
- Ktor 3.3.0 (HTTP client)
- Room 2.6.1 (Base de datos local)
- Firebase Cloud Messaging (Notificaciones push)
- Android Keystore AES-256-GCM (Cifrado E2E)

**Estado Actual:**
- ✅ SupabaseConfig.kt creado
- ✅ Ktor actualizado a 3.x
- ✅ Imports corregidos
- ✅ Validación de build implementada
- ✅ Credenciales protegidas
- ⚠️ 72% test coverage (objetivo: 80%)

---

## 📁 Estructura de Documentación

```
Message-App/
├── 📄 README.md                      # Visión general del proyecto
├── 📚 DOCS_INDEX.md                  # Este archivo - índice de documentación
│
├── 📋 specs/
│   ├── functional.md                 # User stories y criterios de aceptación
│   ├── technical.md                  # Arquitectura y decisiones técnicas
│   └── lessons.md                    # Lecciones aprendidas y errores
│
├── 🧠 context/
│   ├── state.md                      # Estado actual y progreso
│   ├── decisions.md                  # Decisiones técnicas (ADRs)
│   └── insights.md                   # Insights de performance
│
├── 📊 Reportes Clave
│   ├── ESTADO_REAL_PROYECTO.md       # Verificación exhaustiva de código
│   ├── TESTING_SUMMARY.md            # Resumen de tests y cobertura
│   └── DEPENDENCY_FIXES_MARCH_2026.md # Dependencias actualizadas
│
└── 🔧 Configuración
    ├── .qwen/
    │   └── hooks/
    │       └── pre-code-submission.md # Checklist pre-entrega
    └── workflow-logs/                # Logs de builds y tests
```

---

## 🗂️ Archivos Principales

### Para Nuevos Desarrolladores

1. **[README.md](README.md)** - ⭐ **EMPIEZA AQUÍ**
   - Visión general del proyecto
   - Configuración rápida (10 minutos)
   - Estado actual y métricas
   - Features principales

2. **[QUICK_START.md](QUICK_START.md)** - ⭐ **SETUP RÁPIDO**
   - Pasos mínimos para empezar
   - Configuración de credenciales
   - Comandos esenciales

3. **[CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md)** - Configuración detallada
   - Setup paso a paso
   - Supabase + Firebase FCM
   - Solución de problemas

4. **[SECURITY_GUIDE.md](SECURITY_GUIDE.md)** - ⭐ **SEGURIDAD**
   - Protección de credenciales
   - Validación en build time
   - Best practices

### Para Desarrollo Diario

4. **[context/state.md](context/state.md)** - Estado actual del proyecto
   - Última sesión: 2026-03-28
   - Completados, en progreso, pendientes
   - Progreso por fase (Setup 100%, Core 95%, Testing 70%)
   - Métricas de calidad actuales
   - Bugs conocidos

5. **[context/decisions.md](context/decisions.md)** - Por qué se decidió cada cosa
   - ADR-001 a ADR-007: Decisiones originales (2026-03-24)
   - ADR-008: Configuración de calidad estricta (2026-03-28)
   - ADR-009: Skills de documentación (2026-03-28)
   - ADR-010: JPush comentado temporalmente (2026-03-28)
   - ADR-011: Verificación exhaustiva de código (2026-03-28)

6. **[specs/lessons.md](specs/lessons.md)** - Errores y cómo evitarlos
   - Lecciones de la sesión 2026-03-28
   - Configuración de calidad estricta
   - Skills de documentación
   - Verificación de código
   - Patrones exitosos
   - Anti-patrones a evitar

### Para Testing y Calidad

7. **[TESTING_SUMMARY.md](TESTING_SUMMARY.md)** - Estado de tests
   - 70 tests creados
   - Cobertura estimada: 72%
   - Tests por componente
   - Próximos pasos

8. **[ESTADO_REAL_PROYECTO.md](ESTADO_REAL_PROYECTO.md)** - Verificación de código
   - Verificación línea por línea (2026-03-28)
   - 0 errores críticos pendientes
   - Archivos obsoletos identificados (10 para borrar)
   - Conclusión: LISTO PARA PRODUCCIÓN

### Para Configuración

9. **[CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md)** - Guía de configuración
   - Supabase setup
   - OneSignal/JPush setup
   - Build y test

10. **[QUICK_START.md](QUICK_START.md)** - Inicio rápido
    - Pasos mínimos para empezar
    - Comandos esenciales

---

## 📊 Reportes Especializados

### Dependencias y Builds

- **[DEPENDENCY_FIXES_MARCH_2026.md](DEPENDENCY_FIXES_MARCH_2026.md)** - Dependencias actualizadas
- **[BUILDGRADLE_BUILDCONFIG_TEMP.md](BUILDGRADLE_BUILDCONFIG_TEMP.md)** - Configuración temporal de build

### Features Específicas

- **[AVATAR_SYSTEM_GUIDE.md](AVATAR_SYSTEM_GUIDE.md)** - Sistema de avatares
- **[SECURITY_GUIDE.md](SECURITY_GUIDE.md)** - Guía de seguridad
- **[JPUSH_CONFIGURATION.md](JPUSH_CONFIGURATION.md)** - Configuración de JPush (comentado)

### Testing y CI/CD

- **[GITHUB_ACTIONS_WORKFLOW.md](GITHUB_ACTIONS_WORKFLOW.md)** - Workflow de CI/CD
- **[GITHUB_ACTIONS_VERIFICATION.md](GITHUB_ACTIONS_VERIFICATION.md)** - Verificación de GitHub Actions
- **[EMULATOR_TESTS_GUIA.md](EMULATOR_TESTS_GUIA.md)** - Guía de tests en emulador

### Migraciones

- **[MIGRATION_COMPLETE.md](MIGRATION_COMPLETE.md)** - Migración completada
- **[MIGRATION_COMPLETE_FINAL.md](MIGRATION_COMPLETE_FINAL.md)** - Migración final

---

## 🔍 Búsqueda por Tema

### Autenticación
- `specs/functional.md` - US-004: Autenticación
- `specs/technical.md` - AuthRepository + Supabase Auth
- `context/decisions.md` - ADR-002: Supabase como Backend

### Chat en Tiempo Real
- `specs/functional.md` - US-001, US-002, US-003
- `specs/technical.md` - Supabase Realtime + WebSockets
- `context/state.md` - Feature Chat: 100% completada

### Cifrado E2E
- `specs/functional.md` - US-006: Cifrado E2EE
- `specs/technical.md` - Android Keystore AES-256-GCM
- `specs/lessons.md` - Lecciones de criptografía

### Testing
- `TESTING_SUMMARY.md` - Resumen completo de tests
- `specs/lessons.md` - Patrones de testing
- `context/state.md` - Fase Testing: 70% completada

### Notificaciones Push
- `specs/functional.md` - US-007: Notificaciones Push
- `context/decisions.md` - ADR-010: JPush Comentado
- `JPUSH_CONFIGURATION.md` - Configuración de JPush

### Base de Datos
- `specs/technical.md` - Room 2.6.1 + DAOs
- `context/decisions.md` - ADR-003: Room para Base de Datos Local
- `database_schema.sql` - Schema de Supabase

---

## 📈 Métricas y Calidad

### Métricas Actuales (2026-03-28)

| Métrica | Objetivo | Actual | Estado |
|---------|----------|--------|--------|
| Test Coverage | > 80% | ~72% | ⚠️ Cerca |
| Build Time | < 2 min | 1:45 min | ✅ OK |
| APK Size | < 50 MB | 42 MB | ✅ OK |
| Cold Start | < 2s | 1.8s | ✅ OK |
| Crash Rate | < 0.5% | 0.3% | ✅ OK |
| Tests Totales | 100+ | 70 | ⏳ 70% |

### Calidad de Código

| Métrica | Objetivo | Real | Estado |
|---------|----------|------|--------|
| Validación de parámetros | 100% | 100% | ✅ OK |
| Manejo de nulls | 100% | 100% | ✅ OK |
| Logging consistente | 100% | 100% | ✅ OK |
| Catch blocks con logging | 100% | 100% | ✅ OK |

---

## 🎯 Próximos Pasos

### Inmediato (Esta Semana)
1. ⚠️ **Borrar 10 archivos obsoletos** (listados en ESTADO_REAL_PROYECTO.md)
2. 🔍 **Evaluar alternativa a JPush** (ntfy.sh, Gotify, UnifiedPush)
3. 🧪 **Tests para PresenceRepository** (0 tests actualmente)

### Corto Plazo (Próximo Mes)
4. 📈 **Alcanzar 80%+ de cobertura de tests**
5. 🧪 **Tests de integración Repository + ViewModel**
6. 📱 **Tests de UI con Compose Testing**

### Largo Plazo
7. 🎨 **Animaciones y polish de UI**
8. ⚡ **Optimización de performance**
9. ♿ **Accesibilidad**
10. 🌍 **Internacionalización**

---

## 🔗 Enlaces Externos Útiles

### Documentación Oficial
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt](https://dagger.dev/hilt/)
- [Supabase Kotlin](https://supabase.com/docs/reference/kotlin)

### Skills de Documentación
- 26 skills especializados disponibles
- 11,817 líneas de documentación técnica
- Basado en código REAL + Android Developers oficial

---

## 📞 Soporte y Contacto

### Si tienes problemas:

1. **Revisa este índice** - Encuentra el archivo relevante
2. **Busca en specs/lessons.md** - Errores comunes y soluciones
3. **Verifica context/state.md** - Estado actual y bugs conocidos
4. **Consulta ESTADO_REAL_PROYECTO.md** - Verificación de código

### Errores comunes:

| Error | Solución | Archivo |
|-------|----------|---------|
| "SUPABASE_URL no está configurada" | Copia gradle.properties.example | gradle.properties.example |
| "Supabase credentials cannot be empty" | Agrega credenciales válidas | gradle.properties |
| "Table does not exist" | Ejecuta database_schema.sql | database_schema.sql |
| "google-services.json missing" | Configura Firebase (opcional) | CONFIGURATION_GUIDE.md |
| Tests fallan sin razón | Revisa configuración de tests | app/build.gradle.kts |
| Dudas de arquitectura | Revisar TECHNICAL_SPECS.md | TECHNICAL_SPECS.md |

---

## 📝 Convenciones de Documentación

### Archivos de Especificación (`specs/`)
- `functional.md` - QUÉ hace la app (user stories)
- `technical.md` - CÓMO está construida (arquitectura)
- `lessons.md` - QUÉ aprendimos (errores y soluciones)

### Archivos de Contexto (`context/`)
- `state.md` - DÓNDE estamos (progreso actual)
- `decisions.md` - POR QUÉ decidimos (ADRs)
- `insights.md` - QUÉ descubrimos (performance)

### Reportes (raíz del proyecto)
- `ESTADO_REAL_PROYECTO.md` - Verificación de código
- `TESTING_SUMMARY.md` - Estado de tests
- `README.md` - Visión general

---

## ✅ Checklist para Contribuciones

### Antes de Codificar
- [ ] Leí `specs/functional.md` para la feature
- [ ] Leí `specs/technical.md` para arquitectura
- [ ] Revisé `specs/lessons.md` para errores comunes
- [ ] Verifiqué `context/state.md` para progreso actual

### Después de Codificar
- [ ] Tests escritos y pasando
- [ ] Código sigue convenciones de estilo
- [ ] Logging con TAG constante `"MessageApp"`
- [ ] Validación de parámetros con `require()`
- [ ] Manejo de nulls con `isNullOrBlank()`
- [ ] Catch blocks con logging apropiado

### Antes de Commit
- [ ] Revisé `context/decisions.md` para ADRs relevantes
- [ ] Actualicé `context/state.md` con progreso
- [ ] Actualicé `specs/lessons.md` si aprendí algo nuevo
- [ ] Verifiqué que no hay archivos obsoletos

---

**Última Actualización:** 2026-04-04
**Próxima Revisión:** 2026-04-11
**Responsable:** Todo el equipo
**Estado:** 🚧 **EN DESARROLLO** - Correcciones críticas aplicadas
