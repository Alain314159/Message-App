# Kotlin Best Practices & Style Guide - Message App

**Basado en:** Documentación Oficial de Kotlin (Android Developers)  
**Fecha:** 2026-03-26  
**Aplicación:** Inmediata y Obligatoria

---

## 📋 Principios Fundamentales de Kotlin

### 1. Concisión
- Escribir menos código para la misma funcionalidad
- Eliminar boilerplate innecesario
- Usar características del lenguaje (type inference, data classes, etc.)

### 2. Seguridad
- Null safety por defecto
- Smart casts
- Inmutabilidad preferida sobre mutabilidad

### 3. Expresividad
- Código que se lee como lenguaje natural
- DSLs y extension functions
- Funciones de orden superior

### 4. Interoperabilidad
- 100% compatible con Java
- Usar bibliotecas de Java directamente
- Platform types para código Java

---

## 1. Declaración de Variables

### val vs var

**✅ CORRECTO:**
```kotlin
// Usar val por defecto (inmutabilidad)
val userName: String = "John"
val itemCount = 42
val messages = mutableListOf<String>()  // Referencia inmutable, contenido mutable

// Usar var SOLO cuando reasignación es necesaria
var currentIndex = 0
var isLoading = false
```

**❌ INCORRECTO:**
```kotlin
// var innecesario - lleva a bugs de estado mutable
var userName = "John"  // Debería ser val

// Exceso de var hace código difícil de razonar
var temp = calculateValue()
temp = transform(temp)
temp = validate(temp)
```

**Regla:** Empezar con `val`. Solo usar `var` si el compilador exige reasignación.

### Inferencia de Tipo

**✅ CORRECTO:**
```kotlin
// Dejar que el compilador infiera tipos obvios
val userName = "John"           // String inferido
val itemCount = 42              // Int inferido
val isActive = true             // Boolean inferido
val numbers = listOf(1, 2, 3)   // List<Int> inferido

// Tipo explícito cuando añade claridad
val user: User = getGuestUser()  // Claro qué tipo esperamos
val result: Result<Data> = fetchData()
```

**❌ INCORRECTO:**
```kotlin
// Declaraciones de tipo redundantes
val userName: String = "John"   // Tipo es obvio
val count: Int = 0              // Innecesario

// Falta tipo cuando no es obvio
val data = fetchData()          // ¿Qué tipo es data?
```

### Convenciones de Nombres

**✅ CORRECTO:**
```kotlin
// camelCase para variables y funciones
val userName = "John"
fun calculateTotalPrice() { }

// PascalCase para clases y tipos
class UserProfile { }
interface MessageRepository { }

// SCREAMING_SNAKE_CASE para constantes
const val MAX_RETRY_COUNT = 3
const val API_BASE_URL = "https://api.example.com"

// Guion bajo para variables no usadas
list.forEach { _ -> 
    // No necesitamos el elemento
}
```

**❌ INCORRECTO:**
```kotlin
// Naming inconsistente
val UserName = "John"      // Debería ser userName
val user_name = "John"     // Debería ser userName (no snake_case)
class userProfile { }      // Debería ser UserProfile

// Notación húngara (redundante)
val strName: String = "John"   // No prefijar con tipo
val listUsers: List<User>      // No prefijar con tipo de colección
```

---

## 2. Null Safety

### Tipos Anulables

**✅ CORRECTO:**
```kotlin
// Tipos no-nulos por defecto
val userName: String = "John"  // No puede ser null

// Usar tipos anulables (?) solo cuando null es válido
val middleName: String? = null
val email: String? = getUserEmail()

// Inicializar propiedades anulables correctamente
class User {
    val id: String? = null  // OK si es genuinamente opcional
}
```

**❌ INCORRECTO:**
```kotlin
// Exceso de tipos anulables
val userName: String? = "John"  // ¿Por qué es anulable?
val age: Int? = 25              // Age nunca debería ser null

// Force unwrap con !!
val length = userName!!.length  // Riesgo de crash

// Propiedades anulables innecesarias
class User {
    lateinit var name: String  // Usar val con constructor
}
```

### Operadores Seguros

**✅ CORRECTO:**
```kotlin
// Safe call operator (?.)
val emailLength = user.email?.length  // Retorna null si email es null

// Elvis operator (?:) para defaults
val displayName = user.name ?: "Guest"
val emailLength = user.email?.length ?: 0

// Safe call con let para operaciones null-safe
user.email?.let { email ->
    sendEmail(email)  // Solo ejecuta si email no es null
}

// Operadores combinados
val firstChar = user.name?.firstOrNull() ?: 'U'
```

**❌ INCORRECTO:**
```kotlin
// Force unwrap sin null check
val length = user.name!!.length  // Riesgo de NullPointerException

// Null checks anidados (pirámide del doom)
if (user != null) {
    if (user.email != null) {
        if (user.email.address != null) {
            sendEmail(user.email.address)
        }
    }
}

// Null checks redundantes
val name = if (user.name != null) user.name else "Guest"
// Mejor: val name = user.name ?: "Guest"
```

### Patrones Comunes de Null Safety

**✅ CORRECTO:**
```kotlin
// Patrón 1: Early return para valores null
fun processUser(user: User?) {
    val validUser = user ?: return
    // Continúa con user no-nulo
}

// Patrón 2: Null check con when
when {
    user == null -> showGuestScreen()
    user.isPremium -> showPremiumFeatures()
    else -> showFreeFeatures()
}

// Patrón 3: Operaciones seguras con colecciones
val emails = users.mapNotNull { it.email }  // Filtra nulls
val firstEmail = users.firstOrNull()?.email

// Patrón 4: Platform types (Java interop)
fun processJavaString(str: String!) {
    if (str == null) return  // Check explícito para platform types
    // Ahora str es smart-cast a String
}
```

---

## 3. Condicionales

### if-else vs when

**✅ CORRECTO:**
```kotlin
// Usar if como expresión para condiciones simples
val max = if (a > b) a else b
val status = if (user.isActive) "Active" else "Inactive"

// Usar when para múltiples condiciones (reemplaza switch)
when (userType) {
    UserType.ADMIN -> showAdminPanel()
    UserType.PREMIUM -> showPremiumFeatures()
    UserType.FREE -> showFreeFeatures()
    else -> showLimitedFeatures()
}

// when con condiciones arbitrarias
when {
    score >= 90 -> "A"
    score >= 80 -> "B"
    score >= 70 -> "C"
    else -> "F"
}

// when como expresión (retorna valor)
val grade = when (score) {
    in 90..100 -> "A"
    in 80..89 -> "B"
    in 70..79 -> "C"
    else -> "F"
}
```

**❌ INCORRECTO:**
```kotlin
// if-else anidados (difícil de leer)
if (userType == UserType.ADMIN) {
    showAdminPanel()
} else {
    if (userType == UserType.PREMIUM) {
        showPremiumFeatures()
    } else {
        showFreeFeatures()
    }
}

// Comparaciones booleanas redundantes
if (isActive == true) { }  // Debería ser: if (isActive) { }
if (isActive == false) { } // Debería ser: if (!isActive) { }

// when sin else cuando no es exhaustivo
when (day) {  // Error del compilador si Day es sealed class
    Day.MONDAY -> "Work"
    Day.TUESDAY -> "Work"
    // Casos faltantes
}
```

### Smart Casts

**✅ CORRECTO:**
```kotlin
// Smart cast después de type check
fun process(obj: Any) {
    if (obj is String) {
        println(obj.length)  // obj es automáticamente casteado a String
    }
    
    when (obj) {
        is String -> println("String: ${obj.length}")
        is Int -> println("Int: $obj")
        is User -> println("User: ${obj.name}")
    }
}

// Smart cast con tipos anulables
fun printName(user: User?) {
    if (user != null) {
        println(user.name)  // user es smart-cast a User no-nulo
    }
}
```

**❌ INCORRECTO:**
```kotlin
// Casts explícitos innecesarios
if (obj is String) {
    val str = obj as String  // Redundante - smart cast ya ocurrió
    println(str.length)
}

// Cast sin check (crasheará)
val str = obj as String  // Riesgo de ClassCastException
```

---

## 4. Funciones

### Estilo de Declaración

**✅ CORRECTO:**
```kotlin
// Nombres claros y descriptivos
fun calculateTotalPrice(items: List<Item>): Double { }
fun sendEmail(to: String, subject: String, body: String) { }

// Parámetros por defecto para valores opcionales
fun createUser(
    name: String,
    email: String,
    role: UserRole = UserRole.FREE,
    isActive: Boolean = true
) { }

// Argumentos nombrados para claridad
createUser(
    name = "John",
    email = "john@example.com",
    isActive = false  // Claro qué significa este boolean
)
```

**❌ INCORRECTO:**
```kotlin
// Nombres vagos
fun calc(items: List<Item>): Double { }  // ¿Qué hace calc?
fun send(t: String, s: String, b: String) { }  // Parámetros unclear

// Demasiadas sobrecargas (usar parámetros por defecto)
fun createUser(name: String) { }
fun createUser(name: String, email: String) { }
fun createUser(name: String, email: String, role: UserRole) { }
```

### Funciones de Expresión Única

**✅ CORRECTO:**
```kotlin
// Usar sintaxis = para funciones simples
fun double(x: Int): Int = x * 2
fun isEmpty() = size == 0
fun getDisplayName() = userName ?: "Guest"

// Tipo de retorno inferido cuando es claro
fun double(x: Int) = x * 2  // Int inferido
```

**❌ INCORRECTO:**
```kotlin
// Bloque innecesario para retorno simple
fun double(x: Int): Int {
    return x * 2
}

// Expresión única con return explícito
fun isEmpty(): Boolean {
    return size == 0
}
```

### Lambdas y Funciones Anónimas

**✅ CORRECTO:**
```kotlin
// Lambda con nombre de parámetro implícito (it)
numbers.map { it * 2 }
users.filter { it.isActive }

// Parámetro nombrado cuando se necesita claridad
numbers.map { number -> number * 2 }

// Lambda con múltiples parámetros
pairs.forEach { (key, value) ->
    println("$key: $value")
}

// Trailing lambda syntax (fuera de paréntesis)
numbers.filter { it > 0 }
    .map { it * 2 }
    .forEach { println(it) }

// Lambda como último parámetro
fun operateOnList(
    list: List<Int>,
    operation: (Int) -> Int
) { }

operateOnList(numbers) { it * 2 }  // Trailing lambda
```

**❌ INCORRECTO:**
```kotlin
// Función anónima innecesaria
numbers.map(fun(x: Int) = x * 2)  // Usar lambda: { it * 2 }

// Ignorar parámetros sin guion bajo
list.forEach { item ->
    // item no usado
}
// Mejor: list.forEach { _ -> }

// Lambdas anidadas sin claridad
data.map { a ->
    data2.map { b ->
        data3.map { c ->
            // Difícil de seguir
        }
    }
}
```

### Funciones de Orden Superior

**✅ CORRECTO:**
```kotlin
// Funciones de biblioteca estándar
list.filter { it > 0 }
    .map { it * 2 }
    .firstOrNull()

// Funciones de orden superior custom
fun <T> List<T>.forEachIndexed(action: (Int, T) -> Unit) {
    for (i in this.indices) {
        action(i, this[i])
    }
}

// Scope functions para configuración de objetos
val user = User().apply {
    name = "John"
    email = "john@example.com"
}

// let para operaciones null-safe
user?.let {
    processUser(it)
}

// run para configuración y resultado
val result = config.run {
    enabled = true
    timeout = 5000
    build()
}
```

**❌ INCORRECTO:**
```kotlin
// Reinventar la biblioteca estándar
fun filterPositive(numbers: List<Int>): List<Int> {
    val result = mutableListOf<Int>()
    for (n in numbers) {
        if (n > 0) result.add(n)
    }
    return result
}
// Mejor: numbers.filter { it > 0 }

// Exceso de scope functions
user?.let { u ->
    u.apply {
        run {
            // Anidación confusa
        }
    }
}
```

### Orden y Nombrado de Parámetros

**✅ CORRECTO:**
```kotlin
// Parámetros requeridos primero, opcionales último
fun createNotification(
    title: String,
    message: String,
    priority: Priority = Priority.NORMAL,
    sound: Boolean = true,
    vibration: Boolean = true
)

// Agrupar parámetros relacionados
fun drawRectangle(
    x: Int, y: Int,      // Posición
    width: Int, height: Int,  // Dimensiones
    color: Color,
    stroke: Stroke = Stroke.SOLID
)

// Parámetros booleanos con nombres claros
fun setUserActive(isActive: Boolean)  // Claro qué significa true/false
```

**❌ INCORRECTO:**
```kotlin
// Parámetros opcionales antes de requeridos
fun createNotification(
    priority: Priority = Priority.NORMAL,  // Opcional primero
    title: String,  // Requerido después de opcional - ¡error!
    message: String
)

// Parámetros booleanos unclear
fun update(true) { }  // ¿Qué significa true?
fun setFlag(flag: Boolean)  // ¿Qué flag? ¿Qué significa true?

// Demasiados parámetros (considerar data class)
fun createUser(name: String, email: String, age: Int, 
               phone: String, address: String, city: String,
               country: String, zipCode: String) { }
// Mejor: fun createUser(user: UserConfig)
```

---

## 5. Clases

### Declaración de Propiedades

**✅ CORRECTO:**
```kotlin
// Constructor primario con propiedades
class User(
    val id: String,      // Propiedad inmutable
    var email: String,   // Propiedad mutable
    val name: String     // Propiedad inmutable
)

// Valores por defecto en constructor
class User(
    val id: String = generateId(),
    val name: String,
    val isActive: Boolean = true
)

// Custom getter cuando es necesario
class User(val name: String) {
    val displayName: String
        get() = name.ifBlank { "Guest" }
}
```

**❌ INCORRECTO:**
```kotlin
// Propiedades innecesarias en el cuerpo
class User(val name: String) {
    val id: String = ""  // Debería estar en constructor
    var email: String = ""  // Debería estar en constructor
    
    init {
        id = generateId()  // init block para asignación simple
    }
}

// Propiedades mutables sin necesidad
class User {
    var name: String = ""  // Debería ser val en constructor
    var age: Int = 0
}
```

### Patrones de Encapsulación

**✅ CORRECTO:**
```kotlin
// Propiedades privadas con interfaz pública
class BankAccount(
    private val accountNumber: String,
    private var balance: Double  // Mutable pero privado
) {
    val displayedBalance: Double  // Vista pública de solo lectura
        get() = balance
    
    fun deposit(amount: Double) {
        require(amount > 0) { "Amount must be positive" }
        balance += amount
    }
}

// Usar modificadores de visibilidad apropiadamente
class User {
    public val id: String      // API pública explícita
    internal val cache: Map<String, Any>  // Interno del módulo
    private val secrets: String  // Privado de la clase
}
```

**❌ INCORRECTO:**
```kotlin
// Propiedades mutables públicas (rompe encapsulación)
class BankAccount {
    var balance: Double = 0.0  // Cualquiera puede modificar
    var accountNumber: String = ""  // Debería ser inmutable
}

// Exceso de internal/private (oculta API necesaria)
class User {
    private val id: String  // ¿Cómo identificamos usuarios?
    private val name: String  // ¿Cómo mostramos usuarios?
}
```

### Mejores Prácticas de Constructor

**✅ CORRECTO:**
```kotlin
// Constructor primario para inicialización
class User(
    val id: String,
    val name: String,
    val email: String
)

// Constructor secundario delegando al primario
class User {
    val id: String
    val name: String
    
    constructor(id: String, name: String) {
        this.id = id
        this.name = name
    }
    
    constructor(name: String) : this(generateId(), name)
}

// Init block para inicialización compleja
class User(val name: String) {
    val normalizedEmail: String
    
    init {
        normalizedEmail = name.lowercase().trim()
    }
}
```

**❌ INCORRECTO:**
```kotlin
// Constructores secundarios innecesarios
class User {
    val name: String
    
    constructor(name: String) {
        this.name = name
    }
}
// Mejor: class User(val name: String)

// Init block para asignaciones simples
class User {
    lateinit var name: String
    
    init {
        name = ""  // Debería estar en constructor
    }
}
```

### Getters y Setters

**✅ CORRECTO:**
```kotlin
// Custom getter para propiedad computada
class User(val firstName: String, val lastName: String) {
    val fullName: String
        get() = "$firstName $lastName"  // Computada, no almacenada
}

// Custom setter con validación
class User {
    var age: Int = 0
        set(value) {
            require(value >= 0) { "Age must be non-negative" }
            field = value  // Usar field para backing property
        }
}

// Backing property para lógica custom
class User {
    private var _email: String = ""
    val email: String
        get() = _email
    
    fun setEmail(value: String) {
        require(value.contains(" @")) { "Invalid email" }
        _email = value
    }
}
```

**❌ INCORRECTO:**
```kotlin
// Custom getter innecesario
class User(val name: String) {
    val name: String
        get() = name  // Redundante - default getter hace esto
}

// Setter sin usar field
class User {
    var count: Int = 0
        set(value) {
            count = value  // ¡Recursión infinita! Debería ser: field = value
        }
}

// Propiedad mutable cuando inmutable es suficiente
class User {
    var name: String = ""  // Debería ser val en constructor
        set(value) {
            field = value.trim()
        }
}
// Mejor: class User(val name: String) { init { require(...) } }
```

---

## 6. Estilo General de Kotlin

### Organización del Código

**✅ CORRECTO:**
```kotlin
// Estructura de archivo: constantes → propiedades → funciones → clases
private const val MAX_RETRY = 3
private const val TAG = "UserRepository"

class UserRepository {
    // Propiedades primero
    private val api: UserApi
    private val database: UserDatabase
    
    // Constructor
    constructor(api: UserApi, database: UserDatabase) {
        this.api = api
        this.database = database
    }
    
    // Métodos públicos
    fun getUser(id: String): User { }
    
    // Métodos privados
    private fun validateUser(user: User) { }
    
    // Companion object último
    companion object {
        fun createGuest(): User { }
    }
}
```

**❌ INCORRECTO:**
```kotlin
// Organización mezclada (difícil de navegar)
class UserRepository {
    fun getUser(id: String): User { }
    
    private val api: UserApi
    
    companion object {
        fun createGuest(): User { }
    }
    
    private fun validateUser(user: User) { }
}
```

### Legibilidad

**✅ CORRECTO:**
```kotlin
// Nombres significativos sobre comentarios
val activeUsers = users.filter { it.isActive }  // Auto-documentado

// Romper cadenas largas para legibilidad
val result = users
    .filter { it.isActive }
    .map { it.toDto() }
    .sortedBy { it.name }
    .firstOrNull()

// Usar extension functions para lógica de dominio
fun User.isEligibleForPremium(): Boolean {
    return age >= 18 && isActive && hasVerifiedEmail
}

// Líneas vacías entre secciones lógicas
class User {
    val id: String
    val name: String
    
    fun displayName(): String { }
    
    companion object { }
}
```

**❌ INCORRECTO:**
```kotlin
// Nombres de variables unclear
val x = users.filter { it.a }  // ¿Qué significa 'a'?

// Cadenas demasiado largas sin breaks
val result = users.filter { it.isActive }.map { it.toDto() }.sortedBy { it.name }.firstOrNull()

// Comentarios explicando código malo
// Check if user is active and over 18
if (user.active && user.age > 18) { }  // Debería ser: if (user.isEligible()) { }
```

### Kotlin Idiomático

**✅ CORRECTO:**
```kotlin
// Usar funciones de biblioteca estándar
list.filter { it > 0 }.map { it * 2 }  // En lugar de loops manuales
text.isNullOrEmpty()  // En lugar de: text == null || text.isEmpty()
list.firstOrNull()  // En lugar de: if (list.isNotEmpty()) list[0] else null

// Destructuring declarations
val (name, age) = user  // Si User tiene component1(), component2()
for ((index, value) in list.withIndex()) { }

// Object expressions para implementaciones one-off
val comparator = Comparator<Int> { a, b -> b - a }

// Inline functions para lambdas
inline fun <T> List<T>.lock(action: (T) -> Unit) {
    // Lambda no creará objeto
}

// Sealed classes para jerarquías type-safe
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val message: String) : Result()
}
```

**❌ INCORRECTO:**
```kotlin
// Código estilo Java en Kotlin
for (i in 0 until list.size) {
    val item = list[i]
    // Debería ser: for (item in list) { }
}

if (text == null || text.isEmpty()) {
    // Debería ser: if (text.isNullOrEmpty()) { }
}

// Null checks manuales
if (user != null) {
    process(user)
}
// Debería ser: user?.let { process(it) }

// Punto y coma innecesarios
val x = 5; val y = 10;  // Kotlin no necesita punto y coma
```

---

## 📋 Tarjeta de Referencia Rápida

| Categoría | Regla | Ejemplo |
|-----------|-------|---------|
| **Variables** | Default a `val` | `val name = "John"` |
| **Tipos** | Inferir cuando obvio | `val count = 42` |
| **Null** | Evitar `!!` | `user?.name ?: "Guest"` |
| **Condicionales** | Usar `when` para 3+ casos | `when (type) { ... }` |
| **Funciones** | Expresión única si simple | `fun isEmpty() = size == 0` |
| **Clases** | Propiedades en constructor | `class User(val name: String)` |
| **Colecciones** | Usar funciones stdlib | `list.filter { ... }.map { ... }` |
| **Strings** | Usar templates | `"Hello $name"` |
| **Loops** | Preferir foreach | `list.forEach { }` |
| **Visibilidad** | Más restrictivo posible | `private`, `internal` |

---

## 🎯 Principios de Kotlin

### Concisión
```kotlin
// Reducir boilerplate
data class User(val id: String, val name: String)  // equals, hashCode, toString generados

// Type inference
val user = User("1", "John")  // No necesita tipo explícito

// Funciones de expresión única
fun isEmpty() = size == 0
```

### Seguridad
```kotlin
// Null safety por defecto
val name: String = "John"  // No puede ser null
val nullable: String? = null  // Explícitamente anulable

// Smart casts
if (obj is String) {
    obj.length  // Automáticamente casteado
}

// When expressions (exhaustivo)
when (result) {
    is Success -> handle(result.data)
    is Error -> handle(result.message)
}
```

### Expresividad
```kotlin
// Domain-specific language (DSL)
val html = html {
    body {
        h1 { +"Hello" }
        p { +"World" }
    }
}

// Extension functions
fun String.isValidEmail(): Boolean = contains(" @") && contains(".")

// Infix functions para legibilidad
infix fun Int.pow(exponent: Int): Int = ...
val result = 2 pow 3  // Se lee como lenguaje natural
```

---

## 📚 Recursos

- **Documentación Oficial:** Android Developers - Kotlin Documentation
- **Guía de Estilo:** Kotlin Coding Conventions (kotlinlang.org)
- **Principios Kotlin:** Concisión, Seguridad, Expresividad, Interoperabilidad

---

**Última Actualización:** 2026-03-26  
**Aplicación:** Inmediata y Obligatoria para todo el código nuevo  
**Revisión:** Code reviewer debe verificar cumplimiento en cada PR
