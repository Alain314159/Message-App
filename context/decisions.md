# Decisiones Técnicas - Message App

## 📋 Registro de Decisiones de Arquitectura

---

### ADR-001: Jetpack Compose para UI

**Fecha:** 2026-03-24  
**Estado:** ✅ Aceptado

#### Contexto
Necesitamos un framework UI moderno, mantenible y con buen performance para la app de mensajería.

#### Opciones Consideradas

**Opción A: Jetpack Compose**
- ✅ Declarativo, menos código
- ✅ Integración perfecta con Kotlin
- ✅ Mejor performance con recomposición
- ✅ Soporte oficial de Google
- ❌ Curva de aprendizaje para equipo

**Opción B: XML Views tradicionales**
- ✅ Equipo familiarizado
- ✅ Más recursos disponibles
- ❌ Boilerplate code
- ❌ Performance inferior
- ❌ En mantenimiento (no nuevas features)

**Opción C: Flutter**
- ✅ Cross-platform
- ✅ Hot reload excelente
- ❌ Requiere aprender Dart
- ❌ APK más grande
- ❌ Integración con nativo más compleja

#### Decisión
**Jetpack Compose** - La mejor opción a largo plazo para app Android nativa.

#### Consecuencias
- Equipo necesita capacitación en Compose
- Menos código que mantener
- Mejor performance en listas de mensajes
- Futuro-proof (es la dirección de Android)

---

### ADR-002: Supabase como Backend

**Fecha:** 2026-03-24  
**Estado:** ✅ Aceptado

#### Contexto
Necesitamos un backend para autenticación, base de datos y realtime sin gestionar infraestructura.

#### Opciones Consideradas

**Opción A: Supabase**
- ✅ PostgreSQL (robusto, SQL estándar)
- ✅ Realtime incluido (WebSockets)
- ✅ Auth incluido
- ✅ Storage incluido
- ✅ Open source
- ❌ Menos maduro que Firebase

**Opción B: Firebase**
- ✅ Muy maduro, muchas features
- ✅ Documentación extensa
- ❌ Firestore (NoSQL, queries limitados)
- ❌ Vendor lock-in más fuerte
- ❌ Costos menos predecibles

**Opción C: Backend propio (Node.js + PostgreSQL)**
- ✅ Control total
- ✅ Sin vendor lock-in
- ❌ Requiere infraestructura
- ❌ Más mantenimiento
- ❌ Más tiempo de desarrollo

#### Decisión
**Supabase** - Mejor balance entre features, costo y control.

#### Consecuencias
- Menos tiempo en infraestructura
- PostgreSQL permite queries complejos
- Realtime incluido sin configuración adicional
- Posibilidad de self-host en el futuro

---

### ADR-003: Room para Base de Datos Local

**Fecha:** 2026-03-24  
**Estado:** ✅ Aceptado

#### Contexto
Necesitamos almacenamiento local para cache offline-first.

#### Opciones Consideradas

**Opción A: Room**
- ✅ Compile-time SQL verification
- ✅ Integración con Flow/LiveData
- ✅ Soporte oficial
- ✅ Migraciones automatizadas
- ❌ Boilerplate (DAOs, Entities)

**Opción B: SQLDelight**
- ✅ Type-safe queries
- ✅ Multiplatform
- ❌ Menos documentación
- ❌ Curva de aprendizaje

**Opción C: Realm**
- ✅ Más rápido que Room
- ✅ API más simple
- ❌ Binario más grande
- ❌ Menos control sobre SQL
- ❌ Historial de breaking changes

#### Decisión
**Room** - La opción más madura y con mejor soporte.

#### Consecuencias
- SQL verificado en compile-time
- Migraciones manejadas automáticamente
- Fácil integración con ViewModel + Flow

---

### ADR-004: Hilt para Inyección de Dependencias

**Fecha:** 2026-03-24  
**Estado:** ✅ Aceptado

#### Contexto
Necesitamos DI para testing y separación de responsabilidades.

#### Opciones Consideradas

**Opción A: Hilt**
- ✅ Menos boilerplate que Dagger
- ✅ Lifecycle-aware
- ✅ Soporte oficial para Android
- ✅ Integración con ViewModel
- ❌ Magic (menos control)

**Opción B: Koin**
- ✅ Más simple, menos magia
- ✅ Kotlin-first
- ❌ Runtime errors posibles
- ❌ Menos integración con Android

**Opción C: Dagger puro**
- ✅ Control total
- ✅ Muy poderoso
- ❌ Mucho boilerplate
- ❌ Curva de aprendizaje pronunciada

#### Decisión
**Hilt** - Mejor balance entre simplicidad y poder.

#### Consecuencias
- Menos código de setup
- ViewModels inyectables automáticamente
- Testing más fácil con mocks

---

### ADR-005: Tink para Criptografía

**Fecha:** 2026-03-24  
**Estado:** ✅ Aceptado

#### Contexto
Necesitamos cifrado E2EE para mensajes.

#### Opciones Consideradas

**Opción A: Tink**
- ✅ API de alto nivel
- ✅ Auditado por expertos
- ✅ Soporte de Google
- ✅ Actualizaciones automáticas
- ❌ Menos control sobre algoritmos

**Opción B: libsignal**
- ✅ Protocolo Signal (gold standard)
- ✅ Double Ratchet
- ❌ Más complejo de implementar
- ❌ Más código que mantener

**Opción C: Crypto API nativa**
- ✅ Control total
- ✅ Sin dependencias
- ❌ Fácil de implementar mal
- ❌ Requiere expertise criptográfico

#### Decisión
**Tink para MVP**, evaluar migración a libsignal después.

#### Consecuencias
- Implementación más rápida
- Menos riesgos de seguridad por mala implementación
- Posible migración futura a Signal para más features

---

### ADR-006: StateFlow para Gestión de Estado

**Fecha:** 2026-03-24  
**Estado:** ✅ Aceptado

#### Contexto
Necesitamos manejar estado UI de forma reactiva y predecible.

#### Opciones Consideradas

**Opción A: StateFlow + ViewModel**
- ✅ Lifecycle-aware
- ✅ Cold (no emite sin observers)
- ✅ Integración con Compose
- ✅ Oficial de Kotlin
- ❌ Requiere conversiones (stateIn)

**Opción B: LiveData**
- ✅ Lifecycle-aware
- ✅ Oficial de Android
- ❌ No es Kotlin-native
- ❌ API más verbosa
- ❌ No funciona fuera de Android

**Opción C: Flow puro**
- ✅ Cold, backpressure
- ✅ Kotlin-native
- ❌ No es lifecycle-aware
- ❌ Requiere manejo manual

#### Decisión
**StateFlow** - La mejor opción para Kotlin + Compose.

#### Consecuencias
- Estado inmutable
- Actualizaciones atómicas
- Fácil de testear

---

### ADR-007: MVVM + Clean Architecture

**Fecha:** 2026-03-24  
**Estado:** ✅ Aceptado

#### Contexto
Necesitamos una arquitectura escalable y mantenible.

#### Opciones Consideradas

**Opción A: MVVM + Clean**
- ✅ Separación clara
- ✅ Testable
- ✅ Escalable
- ❌ Más capas, más código

**Opción B: MVI**
- ✅ Estado predecible
- ✅ Unidireccional
- ❌ Más boilerplate
- ❌ Curva de aprendizaje

**Opción C: MVC tradicional**
- ✅ Simple
- ✅ Familiar
- ❌ Menos testable
- ❌ Tiende a God Activities

#### Decisión
**MVVM + Clean Architecture** - Balance entre testabilidad y complejidad.

#### Consecuencias
- ViewModels delgados
- UseCases para lógica de negocio
- Repositories para abstracción de datos

---

## 📊 Métricas de Decisiones

| Categoría | Decisiones | Implementadas | Pendientes |
|-----------|------------|---------------|------------|
| UI/UX | 1 | 1 | 0 |
| Backend | 1 | 1 | 0 |
| Database | 1 | 1 | 0 |
| Architecture | 3 | 3 | 0 |
| Security | 1 | 0 | 1 |

---

## 🔄 Decisiones a Revisar

### Próximo Sprint
- [ ] ADR-005: Evaluar migración a libsignal
- [ ] ADR-002: Revisar costos de Supabase

### Próximo Mes
- [ ] ADR-007: Revisar complejidad de Clean Architecture
- [ ] ADR-006: Evaluar Mavericks para ViewModels complejos

---

## 📝 Plantilla para Nuevas Decisiones

```markdown
### ADR-XXX: [Título]

**Fecha:** YYYY-MM-DD  
**Estado:** ⏳ Propuesto | ✅ Aceptado | ❌ Rechazado

#### Contexto
[Describir problema o decisión]

#### Opciones Consideradas

**Opción A: [Nombre]**
- ✅ Pros
- ❌ Contras

**Opción B: [Nombre]**
- ✅ Pros
- ❌ Contras

#### Decisión
[Opción seleccionada y justificación]

#### Consecuencias
[Impacto de la decisión]
```

---

**Última Actualización:** 2026-03-24  
**Próxima Revisión:** 2026-04-01  
**Responsable:** Tech Lead
