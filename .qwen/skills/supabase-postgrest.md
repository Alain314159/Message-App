---
title: Supabase PostgREST - Official Source Code Reference (v3.2.0)
description: Extracted from actual SDK source JAR: postgrest-kt-3.2.0-sources.jar
source: postgrest-kt-3.2.0-sources.jar (Maven Central)
---

# Supabase PostgREST - OFFICIAL Source Code Reference (v3.2.0)

## Source: Extracted from actual SDK JAR

### Extension Property (from `Postgrest.kt`)

```kotlin
// Interface definition
interface Postgrest : MainPlugin<Postgrest.Config> {
    fun from(table: String): PostgrestQueryBuilder
    fun from(schema: String, table: String): PostgrestQueryBuilder
    operator fun get(table: String): PostgrestQueryBuilder = from(table)
    operator fun get(schema: String, table: String): PostgrestQueryBuilder = from(schema, table)
    suspend fun rpc(function: String, request: RpcRequestBuilder.() -> Unit = {}): PostgrestResult
}
```

**Access:**
```kotlin
import io.github.jan.supabase.postgrest.postgrest

val db = client.postgrest  // ✅ Extension property

// ❌ WRONG:
val db = client.plugin(Postgrest)
```

### Query Building (from `PostgrestQueryBuilder.kt`)

```kotlin
// SELECT
val result = db.from("table_name")
    .select(columns = Columns.list("id, name, email")) {
        // filter DSL block
    }
    .decodeSingle<User>()       // throws if no result
    .decodeSingleOrNull<User>() // returns null if no result
    .decodeList<User>()         // returns list (may be empty)

// INSERT
db.from("table_name").insert(
    mapOf("column1" to value1, "column2" to value2)
)

// UPDATE
db.from("table_name").update(
    mapOf("column1" to newValue)
) {
    filter { eq("id", targetId) }
}

// DELETE
db.from("table_name").delete {
    filter { eq("id", targetId) }
}

// UPSERT
db.from("table_name").upsert(listOf(dataObject)) {
    onConflict("id")
    ignoreDuplicates = false
}
```

### Filter Operators (from `PostgrestFilterBuilder.kt` - 387 lines of source)

**Available functions:**
```kotlin
// Equality
eq(column, value)          // equals
neq(column, value)         // not equals

// Comparison
gt(column, value)          // greater than
gte(column, value)         // greater than or equal
lt(column, value)          // less than
lte(column, value)         // less than or equal

// Pattern matching
like(column, pattern)      // LIKE pattern
ilike(column, pattern)     // case-insensitive LIKE
likeAll(column, patterns)  // matches ALL patterns
likeAny(column, patterns)  // matches ANY pattern
ilikeAll(column, patterns)
ilikeAny(column, patterns)

// NULL check
isNull(column)             // IS NULL
notNull(column)            // IS NOT NULL

// Array/JSON containment
contains(column, value)    // contains all elements
containedIn(column, list)  // value IN list

// Range
overlaps(column, list)     // overlaps with list
adj(column, range)         // is adjacent to range

// Logical operators
not { eq("col", value) }           // negates filter
and { eq("a", 1); eq("b", 2) }     // all must match
or { eq("a", 1); eq("b", 2) }      // at least one matches
```

### Ordering (from `Order.kt`)

```kotlin
order(column, ascending = true)           // ASC
order(column, ascending = false)          // DESC
order(column, ascending, nullsFirst = true)  // NULLS FIRST
```

### Pagination

```kotlin
// Limit
select { limit(10) }

// Range (from-to, inclusive)
select { range(from = 0, to = 9) }
```

### Required Imports

```kotlin
// Extension property
import io.github.jan.supabase.postgrest.postgrest

// Filter operators (CRITICAL)
import io.github.jan.supabase.postgrest.query.filter.*

// Columns
import io.github.jan.supabase.postgrest.query.Columns

// Count
import io.github.jan.supabase.postgrest.query.Count
```

### Complete Query Examples

```kotlin
// Basic select with filter
val messages = db.from("messages")
    .select {
        eq("chat_id", chatId)
        order("created_at", ascending = true)
    }
    .decodeList<Message>()

// Complex filter with logical operators
val users = db.from("users")
    .select {
        and {
            eq("is_active", true)
            gt("age", 18)
        }
        or {
            eq("role", "admin")
            eq("role", "moderator")
        }
        not { isNull("email") }
        order("created_at", ascending = false)
        limit(50)
    }
    .decodeList<User>()

// Insert with map
db.from("users").insert(
    mapOf(
        "id" to uid,
        "email" to email,
        "display_name" to "User",
        "is_online" to true,
        "created_at" to System.currentTimeMillis() / 1000
    )
)

// Update with filter
db.from("users").update(
    mapOf(
        "is_online" to true,
        "last_seen" to System.currentTimeMillis() / 1000
    )
) {
    filter { eq("id", uid) }
}
```

### Alternative Syntax (from source code)

```kotlin
// Using operator function directly
db.from("users").select {
    User::id eq 2        // Uses KProperty operator overloading
    User::name like "J%"
}
```

### Common Mistakes

| ❌ WRONG | ✅ CORRECT |
|----------|-----------|
| `order("col" to true)` | `order("col", ascending = true)` |
| `filter { and(eq("a",1), eq("b",2)) }` | `filter { and { eq("a",1); eq("b",2) } }` |
| `client.plugin(Postgrest)` | `client.postgrest` |
| Missing `import io.github.jan.supabase.postgrest.query.filter.*` | Always include this import |
