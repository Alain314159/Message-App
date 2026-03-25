# Especificaciones Funcionales - Message App

## 📱 Descripción del Proyecto

Message App es una aplicación de mensajería para Android con soporte para:
- Chat en tiempo real
- Cifrado de extremo a extremo (E2EE)
- Gestión de contactos
- Notificaciones push
- Avatares de usuario
- Emparejamiento de dispositivos

---

## 🎯 User Stories Activas

### US-001: Envío de Mensajes
**Como** usuario registrado  
**Quiero** enviar mensajes de texto a mis contactos  
**Para** comunicarme en tiempo real

**Criterios de Aceptación:**
- [ ] Dado que estoy en un chat activo, cuando escribo un mensaje y presiono enviar, entonces el mensaje aparece en la conversación
- [ ] Dado que envío un mensaje, cuando hay error de red, entonces se muestra indicador de error y opción de reintentar
- [ ] Dado que el mensaje se envió exitosamente, cuando el servidor confirma, entonces se muestra check de leído

**Constraints:**
- No usar librerías de terceros para almacenamiento local (usar Room)
- Performance: mensaje debe aparecer en UI en < 100ms
- Los mensajes deben cifrarse antes de enviar

---

### US-002: Recepción de Mensajes
**Como** usuario registrado  
**Quiero** recibir mensajes de mis contactos  
**Para** mantener conversaciones activas

**Criterios de Aceptación:**
- [ ] Dado que recibo un mensaje, cuando la app está en primer plano, entonces se actualiza la lista de chats
- [ ] Dado que recibo un mensaje, cuando la app está en segundo plano, entonces se muestra notificación
- [ ] Dado que el mensaje está cifrado, cuando se recibe, entonces se descifra automáticamente antes de mostrar

**Constraints:**
- Las notificaciones no deben mostrar contenido sensible si hay configuración de privacidad
- Performance: notificación debe aparecer en < 2 segundos

---

### US-003: Lista de Chats
**Como** usuario  
**Quiero** ver una lista de todas mis conversaciones  
**Para** acceder rápidamente a cualquier chat

**Criterios de Aceptación:**
- [ ] Dado que tengo múltiples chats, cuando abro la app, entonces veo la lista ordenada por último mensaje
- [ ] Dado que un chat tiene mensajes no leídos, cuando veo la lista, entonces se muestra badge con contador
- [ ] Dado que hago scroll en la lista, cuando hay muchos chats, entonces se carga gradualmente (paginación)

**Constraints:**
- La lista debe soportar al menos 1000 chats sin degradación de performance
- Scroll debe ser suave a 60fps

---

### US-004: Autenticación
**Como** usuario nuevo  
**Quiero** registrarme con mi número de teléfono  
**Para** usar la aplicación

**Criterios de Aceptación:**
- [ ] Dado que ingreso mi número válido, cuando solicito código de verificación, entonces lo recibo por SMS
- [ ] Dado que ingreso el código correcto, cuando verifico, entonces se crea mi cuenta y accedo al home
- [ ] Dado que ingreso un número inválido, cuando intento registrar, entonces se muestra error específico

**Constraints:**
- No almacenar números de teléfono en texto plano
- Usar HTTPS para todas las comunicaciones de auth
- Rate limiting: máximo 3 intentos de verificación por hora

---

### US-005: Gestión de Contactos
**Como** usuario  
**Quiero** ver qué contactos usan la app  
**Para** iniciar conversaciones fácilmente

**Criterios de Aceptación:**
- [ ] Dado que tengo contactos en mi agenda, cuando abro la sección de contactos, entonces veo cuáles usan la app
- [ ] Dado que un contacto se registra, cuando actualizo la lista, entonces aparece como disponible
- [ ] Dado que no tengo permisos de contactos, cuando intento ver la lista, entonces se solicita permiso

**Constraints:**
- Respetar permisos de Android 13+ (READ_CONTACTS runtime)
- No subir agenda de contactos al servidor sin consentimiento explícito

---

### US-006: Cifrado E2EE
**Como** usuario preocupado por privacidad  
**Quiero** que mis mensajes estén cifrados de extremo a extremo  
**Para** que nadie más pueda leerlos

**Criterios de Aceptación:**
- [ ] Dado que inicio un chat, cuando se establecen las claves, entonces todos los mensajes se cifran
- [ ] Dado que cambio de dispositivo, cuando restauro mi cuenta, entonces puedo recuperar mis claves
- [ ] Dado que verifico la seguridad, cuando comparo huellas, entonces confirmo que no hay MITM

**Constraints:**
- Usar algoritmos estándar (AES-256-GCM, X3DH, Double Ratchet)
- Las claves privadas nunca salen del dispositivo
- Backup de claves debe estar protegido con contraseña

---

### US-007: Notificaciones Push
**Como** usuario  
**Quiero** recibir notificaciones cuando hay mensajes nuevos  
**Para** responder rápidamente incluso con la app cerrada

**Criterios de Aceptación:**
- [ ] Dado que recibo un mensaje, cuando la app está cerrada, entonces se muestra notificación
- [ ] Dado que toco la notificación, cuando abro la app, entonces voy directamente al chat
- [ ] Dado que estoy en modo no molestar, cuando recibo mensajes, entonces no se muestran notificaciones

**Constraints:**
- Usar **JPush** (Aurora Mobile) v4.3.9
- Las notificaciones no deben drenar batería (>5% diario)
- Soportar notificaciones agrupadas por chat
- Funcionar sin Google Play Services (importante para Cuba)

---

### US-008: Perfil de Usuario
**Como** usuario  
**Quiero** personalizar mi perfil con nombre y avatar  
**Para** que mis contactos me identifiquen

**Criterios de Aceptación:**
- [ ] Dado que cambio mi nombre, cuando guardo, entonces se actualiza para todos mis contactos
- [ ] Dado que selecciono un avatar, cuando guardo, entonces se comprime y sube al servidor
- [ ] Dado que veo el perfil de un contacto, cuando cargo, entonces veo su nombre y avatar actualizados

**Constraints:**
- Avatar máximo 512x512 pixels, < 100KB
- El nombre debe ser único (validar con servidor)
- Cache de avatares en disco para performance

---

## 🚨 Casos de Error Conocidos

### ERR-001: Sin Conexión a Internet
**Escenario:** Usuario intenta enviar mensaje sin conexión  
**Comportamiento esperado:** 
- Mensaje se guarda como "pendiente"
- Se reintenta automáticamente cuando hay conexión
- Usuario puede cancelar envío pendiente

### ERR-002: Mensaje Fallido
**Escenario:** Servidor rechaza mensaje  
**Comportamiento esperado:**
- Se muestra icono de error junto al mensaje
- Usuario puede tocar para reintentar o eliminar
- Se registra log del error para debugging

### ERR-003: Sesión Expirada
**Escenario:** Token de autenticación expiró  
**Comportamiento esperado:**
- Se redirige a pantalla de login
- Se preservan mensajes locales
- Se intenta refresh token automáticamente

---

## 📊 Métricas de Calidad

| Métrica | Objetivo | Cómo Medir |
|---------|----------|------------|
| Cold Start Time | < 2 segundos | Android Vitals |
| Message Delivery | < 1 segundo | Timestamp server |
| Battery Impact | < 5% diario | Battery Historian |
| Crash Rate | < 0.5% | Firebase Crashlytics |
| ANR Rate | < 0.1% | Android Vitals |

---

## 🔄 Historial de Cambios

| Versión | Fecha | Cambios Principales |
|---------|-------|---------------------|
| 0.1.0 | 2026-03-24 | Specs iniciales |
| | | |

---

**Última Actualización:** 2026-03-24  
**Estado:** ✅ Activo  
**Próxima Revisión:** 2026-03-31
