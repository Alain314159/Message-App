---
title: Supabase Realtime - Official Source Code Reference (v3.2.0)
description: Extracted from actual SDK source JAR: realtime-kt-3.2.0-sources.jar
source: realtime-kt-3.2.0-sources.jar (Maven Central)
---

# Supabase Realtime - OFFICIAL Source Code Reference (v3.2.0)

## Source: Extracted from actual SDK JAR

### Extension Property (from `Realtime.kt`)

```kotlin
// From source code:
interface Realtime : MainPlugin<Realtime.Config> {
    val status: StateFlow<Status>
    val subscriptions: Map<String, RealtimeChannel>
    suspend fun connect()
    fun disconnect()
    suspend fun removeChannel(channel: RealtimeChannel)
    suspend fun removeAllChannels()
    suspend fun block()
}
```

**Access:**
```kotlin
import io.github.jan.supabase.realtime.realtime

val realtime = client.realtime  // ✅ Extension property

// ❌ WRONG:
val realtime = client.plugin(Realtime)
```

### Channel Creation (from `Realtime.kt`)

```kotlin
// From source code docstring:
// val channel = supabase.realtime.channel("channelId")

val channel = client.realtime.channel("uniqueChannelId")
```

**ChannelId format:** Any unique string. Convention: `"table:schema:table"` for postgres changes.

```kotlin
val channel = client.realtime.channel("chats:public:chats")
val channel = client.realtime.channel("users:public:users")
val channel = client.realtime.channel("messages:public:messages")
```

### Listening to Changes (from `PostgrestExtensions.kt`)

```kotlin
// From source code:
val changeFlow = channel.postgresChangeFlow<Product>(schema = "public") {
    table = "products"
}
```

**Full usage:**
```kotlin
// Typed flow - returns decoded objects directly
val changeFlow = channel.postgresChangeFlow<Chat>(schema = "public") {
    table = "chats"
}

changeFlow.collect { chat ->
    // chat is already decoded as Chat type
    println("Chat changed: ${chat.id}")
}
```

### Subscribing (from `RealtimeChannel.kt`)

```kotlin
// From source code:
interface RealtimeChannel {
    suspend fun subscribe(blockUntilSubscribed: Boolean = false)
    suspend fun unsubscribe()
}

channel.subscribe()                              // Non-blocking
channel.subscribe(blockUntilSubscribed = true)   // Blocks until subscribed
```

### Complete Pattern: callbackFlow for StateFlow

```kotlin
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch

fun observeChats(uid: String): Flow<List<Chat>> = callbackFlow {
    // 1. Create channel
    val channel = client.realtime.channel("chats:public:chats")

    // 2. Set up change flow
    val changeFlow = channel.postgresChangeFlow<Chat>(schema = "public") {
        table = "chats"
    }

    // 3. Subscribe
    channel.subscribe()

    // 4. Load initial data
    launch {
        try {
            val initialChats = loadChatsForUser(uid)
            trySend(initialChats)
        } catch (e: Exception) {
            trySend(emptyList())
        }
    }

    // 5. Listen for changes
    val job = launch {
        changeFlow.collect { chat ->
            // chat is already decoded - no Json.decodeFromJsonElement needed
            val updatedChats = loadChatsForUser(uid)
            trySend(updatedChats)
        }
    }

    // 6. Cleanup on close
    awaitClose {
        job.cancel()
        // Note: realtime.removeChannel(channel) may not be available
        // Use channel.unsubscribe() instead
        runBlocking { channel.unsubscribe() }
    }
}
```

### PostgresAction Types (from `PostgresAction.kt`)

```kotlin
// If using non-typed flow:
val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
    table = "users"
}

changeFlow.collect { action ->
    when (action) {
        is PostgresAction.Insert -> println("Inserted: ${action.record}")
        is PostgresAction.Update -> println("Updated: ${action.record}")
        is PostgresAction.Delete -> println("Deleted: ${action.oldRecord}")
        is PostgresAction.Select -> println("Selected: ${action.record}")
    }
}
```

### Required Imports

```kotlin
// Extension property
import io.github.jan.supabase.realtime.realtime

// All Realtime APIs (recommended)
import io.github.jan.supabase.realtime.*

// For callbackFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
```

### Common Mistakes

| ❌ WRONG | ✅ CORRECT |
|----------|-----------|
| `channelV2("public", "table")` | `channel("table:public:table")` |
| `postgrestChangeFlow` | `postgresChangeFlow` (no 't' before 'gres') |
| `channel.postgresChangeFlow<T>()` | `channel.postgresChangeFlow<T>(schema = "public") { table = "..." }` |
| `Json.decodeFromJsonElement<T>(change.record)` | Use typed flow: `postgresChangeFlow<T>()` returns decoded objects |
| `realtime.channel("public", "table")` | `realtime.channel("table:public:table")` |
| `import io.github.jan.supabase.realtime.v2.*` | `import io.github.jan.supabase.realtime.*` |
| `client.plugin(Realtime)` | `client.realtime` |
