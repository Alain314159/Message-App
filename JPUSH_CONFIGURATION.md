# 🔔 Configuración de JPush - Message App

## 📱 Información de la Aplicación

**Package Name:** `com.example.messageapp`  
**Application ID:** `com.example.messageapp`  
**JPush Version:** 4.3.9

---

## 🚀 Configuración Requerida

### 1. Obtener AppKey de JPush

1. Registrarse en [JPush Console](https://www.jpush.io/)
2. Crear nueva aplicación:
   - **Nombre:** Message App
   - **Plataforma:** Android
   - **Package Name:** `com.example.messageapp`
3. Copiar **AppKey** desde el dashboard

### 2. Configurar en build.gradle.kts

```kotlin
android {
    defaultConfig {
        applicationId = "com.example.messageapp"
        
        // ⚠️ REEMPLAZAR con tu AppKey real de JPush
        manifestPlaceholders["JPUSH_APPKEY"] = "TU_JPUSH_APP_KEY_AQUI"
    }
}
```

### 3. Verificar AndroidManifest.xml

El manifest ya está configurado con:

```xml
<!-- Permisos JPush -->
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

<!-- JPush BroadcastReceiver -->
<receiver
    android:name=".push.JPushBroadcastReceiver"
    android:enabled="true"
    android:exported="false">
    <intent-filter>
        <action android:name="cn.jiguang.jpush.intent.REGISTRATION" />
        <action android:name="cn.jiguang.jpush.intent.MESSAGE_RECEIVED" />
        <action android:name="cn.jiguang.jpush.intent.NOTIFICATION_RECEIVED" />
        <action android:name="cn.jiguang.jpush.intent.NOTIFICATION_OPENED" />
        <category android:name="${applicationId}" />
    </intent-filter>
</receiver>

<!-- JPush Metadata -->
<meta-data
    android:name="JPUSH_CHANNEL"
    android:value="DEFAULT" />
<meta-data
    android:name="JPUSH_APPKEY"
    android:value="${JPUSH_APPKEY}" />
```

---

## 📂 Archivos Existentes

### JPushBroadcastReceiver.kt
**Ubicación:** `app/src/main/java/com/example/messageapp/push/JPushBroadcastReceiver.kt`

```kotlin
package com.example.messageapp.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.jiguang.jpush.model.JPushMessage

class JPushBroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // Registro de dispositivo
            "cn.jiguang.jpush.intent.REGISTRATION" -> {
                val registrationId = intent.getStringExtra("registrationId")
                // Guardar registrationId para enviar al servidor
            }
            
            // Mensaje recibido
            "cn.jiguang.jpush.intent.MESSAGE_RECEIVED" -> {
                val message = intent.getStringExtra("cn.jiguang.jpush.message")
                // Procesar mensaje personalizado
            }
            
            // Notificación recibida
            "cn.jiguang.jpush.intent.NOTIFICATION_RECEIVED" -> {
                val notificationId = intent.getIntExtra("cn.jiguang.jpush.NOTIFICATION_ID", -1)
                val title = intent.getStringExtra("cn.jiguang.jpush.NOTIFICATION_TITLE")
                val content = intent.getStringExtra("cn.jiguang.jpush.NOTIFICATION_CONTENT")
                // Mostrar notificación
            }
            
            // Notificación abierta
            "cn.jiguang.jpush.intent.NOTIFICATION_OPENED" -> {
                val extras = intent.extras
                // Navegar a la pantalla correspondiente
            }
        }
    }
}
```

### JPushService.kt (Por crear)
**Ubicación:** `app/src/main/java/com/example/messageapp/push/JPushService.kt`

```kotlin
package com.example.messageapp.push

import android.app.Service
import android.content.Intent
import android.os.IBinder
import cn.jpush.android.api.JPushInterface

class JPushService : Service() {
    
    override fun onCreate() {
        super.onCreate()
        // Inicializar JPush cuando inicia el servicio
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
```

---

## 🔧 Inicialización de JPush

### En App.kt (Application Class)

```kotlin
package com.example.messageapp.core

import android.app.Application
import cn.jpush.android.api.JPushInterface

class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar JPush
        JPushInterface.setDebugMode(true) // true para debug, false para production
        JPushInterface.init(this)
    }
}
```

---

## 📝 Configuración por Ambiente

### Debug
```kotlin
JPushInterface.setDebugMode(true)
manifestPlaceholders["JPUSH_APPKEY"] = "TU_JPUSH_APP_KEY_DEBUG"
```

### Production
```kotlin
JPushInterface.setDebugMode(false)
manifestPlaceholders["JPUSH_APPKEY"] = "TU_JPUSH_APP_KEY_PRODUCTION"
```

---

## 🔗 Registro de Dispositivo

### Obtener Registration ID

```kotlin
// En tu ViewModel o Repository
fun getRegistrationId(): String? {
    return JPushInterface.getRegistrationID(applicationContext)
}

// Escuchar cambios de registration ID
val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val registrationId = intent.getStringExtra("registrationId")
        // Enviar al servidor para asociar con el usuario
        sendRegistrationIdToServer(registrationId)
    }
}

registerReceiver(receiver, IntentFilter("cn.jiguang.jpush.intent.REGISTRATION"))
```

### Enviar al Servidor

```kotlin
// En tu AuthRepository o UserRepository
suspend fun registerPushNotification(registrationId: String) {
    supabase
        .from("user_devices")
        .insert(mapOf(
            "user_id" to currentUserId,
            "registration_id" to registrationId,
            "platform" to "android",
            "app_version" to BuildConfig.VERSION_NAME
        ))
}
```

---

## 📨 Enviar Notificaciones

### Desde el Servidor (Ejemplo con cURL)

```bash
curl -X POST "https://api.jpush.cn/v3/push" \
  -H "Authorization: Basic TU_APP_SECRET_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "platform": "android",
    "audience": {
      "registration_id": ["REGISTRATION_ID_DEL_DISPOSITIVO"]
    },
    "notification": {
      "android": {
        "alert": "Nuevo mensaje de Juan",
        "title": "Message App",
        "extras": {
          "chat_id": "123",
          "message_type": "text"
        }
      }
    }
  }'
```

### Tipos de Notificaciones

#### 1. Notificación de Mensaje
```json
{
  "alert": "Nuevo mensaje",
  "title": "Message App",
  "extras": {
    "chat_id": "123",
    "sender_id": "456",
    "message_type": "text"
  }
}
```

#### 2. Notificación de Llamada
```json
{
  "alert": "Llamada entrante",
  "title": "Message App - Llamada",
  "extras": {
    "call_type": "voice",
    "caller_id": "456",
    "action": "answer"
  }
}
```

#### 3. Notificación de Grupo
```json
{
  "alert": "Nuevo mensaje en Grupo Familia",
  "title": "Message App - Grupo",
  "extras": {
    "group_id": "789",
    "message_type": "group"
  }
}
```

---

## 🎯 Manejo de Notificaciones

### Al Tocar Notificación

```kotlin
// En JPushBroadcastReceiver
"cn.jiguang.jpush.intent.NOTIFICATION_OPENED" -> {
    val extras = intent.extras
    val chatId = extras?.getString("chat_id")
    val messageType = extras?.getString("message_type")
    
    // Navegar al chat
    val navIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("navigate_to_chat", chatId)
        putExtra("message_type", messageType)
    }
    
    context.startActivity(navIntent)
}
```

---

## ✅ Checklist de Implementación

### Configuración
- [ ] Obtenido AppKey de JPush
- [ ] Reemplazado en build.gradle.kts
- [ ] Verificado AndroidManifest.xml
- [ ] JPushBroadcastReceiver implementado
- [ ] JPushService creado
- [ ] Inicialización en App.kt

### Testing
- [ ] Registro de dispositivo funciona
- [ ] Registration ID se guarda en servidor
- [ ] Notificación se recibe en dispositivo
- [ ] Al tocar notificación navega al chat
- [ ] Notificaciones en foreground funcionan
- [ ] Notificaciones en background funcionan

### Producción
- [ ] Debug mode desactivado
- [ ] AppKey de producción configurada
- [ ] Proguard rules configuradas
- [ ] Battery usage < 5% diario

---

## 📚 Recursos

### Documentación Oficial
- [JPush Android SDK](https://docs.jpush.io/client/android_guide/)
- [JPush API](https://docs.jpush.io/server/http_api/)
- [JPush GitHub](https://github.com/jpush/jpush-api-android-client)

### Troubleshooting
- [JPush FAQ](https://community.jiguang.cn/)
- [JPush Status](https://status.jpush.io/)

---

## ⚠️ Importante

### Para Cuba
✅ JPush funciona sin restricciones en Cuba  
✅ No requiere Google Play Services  
✅ Gratis para aplicaciones pequeñas  
✅ Soporte para notificaciones push nativas  

### Límites Gratuitos
- **Dispositivos:** Ilimitados
- **Notificaciones:** 1,000,000 por mes
- **API Calls:** 10,000 por hora

---

**Última Actualización:** 2026-03-24  
**Estado:** ⏳ Pendiente de configurar AppKey  
**Responsable:** Equipo de desarrollo
