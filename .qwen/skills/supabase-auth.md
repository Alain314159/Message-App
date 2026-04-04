---
title: Supabase Auth - Official Source Code Reference (v3.2.0)
description: Extracted from actual SDK source JAR: auth-kt-3.2.0-sources.jar
source: auth-kt-3.2.0-sources.jar (Maven Central)
---

# Supabase Auth - OFFICIAL Source Code Reference (v3.2.0)

## Source: Extracted from actual SDK JAR

### Email Provider (from `builtin/Email.kt`)

```kotlin
package io.github.jan.supabase.auth.providers.builtin

data object Email : DefaultAuthProvider<Email.Config, UserInfo> {
    override val grantType: String = "password"

    @Serializable
    data class Config(
        var email: String = "",
        var password: String = ""
    ) : DefaultAuthProvider.Config()
}
```

**Usage:**
```kotlin
import io.github.jan.supabase.auth.providers.builtin.Email

// signUpWith - returns UserInfo
val userInfo = supabase.auth.signUpWith(Email) {
    this.email = "user@example.com"      // this. needed to avoid shadowing
    this.password = "securePassword"
}
val uid = userInfo.id  // UserInfo has .id directly

// signInWith - returns nothing (Unit), logs in user
supabase.auth.signInWith(Email) {
    this.email = email       // this. needed when local var has same name
    this.password = password
}
```

### IDToken Provider (from `builtin/IDToken.kt`)

```kotlin
package io.github.jan.supabase.auth.providers.builtin

data object IDToken : DefaultAuthProvider<IDToken.Config, UserInfo> {
    override val grantType: String = "id_token"

    @Serializable
    data class Config(
        @SerialName("id_token") var idToken: String = "",
        var provider: IDTokenProvider? = null,       // Apple, Google, Facebook, Azure
        @SerialName("access_token") var accessToken: String? = null,
        var nonce: String? = null
    ) : DefaultAuthProvider.Config()
}
```

**Usage:**
```kotlin
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.Google

supabase.auth.signInWith(IDToken) {
    idToken = googleToken    // No this. needed (no shadowing if local var renamed)
    provider = Google        // or Apple, Facebook, Azure
    nonce = null             // optional
}
```

### Phone Provider (from `builtin/Phone.kt`)

```kotlin
supabase.auth.signUpWith(Phone) {
    phone = "+1234567890"
    password = "password"
    channel = Phone.Channel.SMS  // or WHATSAPP
}
```

### Required Imports

```kotlin
// Extension property
import io.github.jan.supabase.auth.auth

// Providers
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Facebook
import io.github.jan.supabase.auth.providers.Azure
```

### Session/User Access

```kotlin
val session = supabase.auth.currentSessionOrNull()  // UserSession?
val user = supabase.auth.currentUserOrNull()        // UserInfo?
val uid = user?.id

// From signUpWith result:
val userInfo = supabase.auth.signUpWith(Email) { ... }
// userInfo: UserInfo (has .id, .email, etc.)
```

### signOut

```kotlin
supabase.auth.signOut()
```

### Password Reset

```kotlin
supabase.auth.resetPasswordForEmail("user@example.com")
```

### CRITICAL: Shadowing Pattern

When local variable names match DSL property names, use `this.`:

```kotlin
suspend fun login(email: String, password: String) {
    supabase.auth.signInWith(Email) {
        this.email = email        // ✅ this.email = DSL property, email = parameter
        this.password = password  // ✅ this.password = DSL property, password = parameter
    }
}
```

Without `this.`, the compiler sees `email = email` as assigning the DSL property to itself.
