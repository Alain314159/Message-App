# Android Testing Best Practices

## General Principles

1. **Test behavior, not implementation** — Assert what the code does, not how it does it
2. **Given-When-Then structure** — Arrange inputs, execute, verify outputs
3. **One assertion per test** — Each test verifies one specific behavior
4. **Descriptive test names** — Use backtick names that describe the scenario and expected result
5. **Test failure cases** — Invalid inputs, null values, exceptions, edge cases
6. **No test interdependencies** — Each test must be independently runnable
7. **Fast tests** — Each test under 1 second (no real network, no real DB for unit tests)

## Unit Tests (src/test/)

### Testing Models (data classes)
- Test `init` block validations (valid data, invalid data, edge cases)
- Test computed properties
- Test `equals`/`hashCode` behavior
- Test `copy` behavior
- Test boundary values (empty strings, max length, null)

### Testing Mappers
- Test roundtrip: `Domain → Entity → Domain` preserves all fields
- Test null handling: null in → null out
- Test type conversions: `List<String> ↔ JSON`, empty string → null
- Test default values are preserved

### Testing Repositories
- Test `require()` validation: blank strings, null, invalid ranges
- Test error handling: exception wrapping, Result wrapping
- Test pure logic functions (no Supabase/Android deps)
- Mock SupabaseClient when testing flow-emitting methods
- Use `io.mockk.mockk(relaxed = true)` for complex dependencies

### Testing ViewModels
- Mock all repository dependencies
- Use `StandardTestDispatcher` + `Dispatchers.setMain()` for coroutine control
- Use `testDispatcher.scheduler.advanceUntilIdle()` to run pending coroutines
- Test initial state values
- Test state transitions on method calls
- Test error state handling (error messages, success flags)
- Test validation (`require()` throws)
- Test idempotent calls (calling start() twice doesn't crash)
- Use `coEvery { mockRepo.method() } returns value` for suspend functions
- Use `every { mockRepo.flowMethod() } returns flowOf(value)` for Flow methods
- Reset Dispatchers in `@After` with `Dispatchers.resetMain()`

### Testing Coroutines
- Use `kotlinx-coroutines-test` with `runTest {}`
- Use `StandardTestDispatcher` for manual time control
- Use `testDispatcher.scheduler.advanceUntilIdle()` for async flows
- Use `app.cash.turbine` for testing Flow emissions
- Use `MockK` `coEvery` for mocking suspend functions

### Testing Retry Logic
- Test success on first attempt
- Test success after N failures
- Test exception thrown after max retries
- Test that retry count matches maxRetries parameter
- Test exponential delay progression

### Testing Crypto
- Test encrypt/decrypt roundtrip
- Test empty/blank input handling
- Test malformed input handling
- Test validation of parameters (require/throws)
- Test extension functions (toHex, etc.)

### Testing Utils
- Test normal cases with known inputs/outputs
- Test edge cases (empty, null, zero, negative, max value)
- Test unicode handling
- Test boundary conditions (exactly at threshold, just above, just below)

### Naming Convention
```kotlin
@Test
fun `methodName returns expectedValue when condition`() { ... }

@Test
fun `methodName throws ExceptionType when invalidInput`() { ... }

@Test
fun `methodName handles edgeCase gracefully`() { ... }
```

## Instrumented Tests (src/androidTest/)

### Testing UI (Compose)
- Use `createComposeRule()` from `androidx.compose.ui:ui-test-junit4`
- Test user interactions (click, type text, scroll)
- Test content visibility based on state
- Test navigation flows
- Test loading states and error messages

### Testing Database (Room)
- Use `Room.inMemoryDatabaseBuilder()` for isolation
- Use `allowMainThreadQueries()` for test simplicity
- Test CRUD operations
- Test foreign key constraints
- Test type converters
- Each test should create its own test data and clean up

## Anti-Patterns (NEVER DO)

❌ `assertThat(true).isTrue()` — Tests nothing
❌ `assertThat(obj).isNotNull()` — Only tests instantiation
❌ Mocking everything without verifying real logic
❌ `@Ignore` without a tracking issue
❌ Testing private methods directly (test through public API)
❌ Shared state between tests
❌ `Thread.sleep()` — Use `runTest` with test dispatchers instead
❌ Hardcoded file paths or environment dependencies

## Required Dependencies

```kotlin
// Unit testing
testImplementation("junit:junit:4.13.2")
testImplementation("com.google.truth:truth:1.4.2")
testImplementation("io.mockk:mockk:1.13.12")
testImplementation("app.cash.turbine:turbine:1.1.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
testImplementation("org.robolectric:robolectric:4.14.1")

// Instrumented testing
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

## Robolectric Configuration

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class MyTest {
    // Tests that need Android APIs (Patterns, Context, etc.)
}
```

## MockK Patterns

```kotlin
// Relaxed mock (returns default values for everything)
val mock = mockk<Repository>(relaxed = true)

// Stubbing suspend functions
coEvery { mock.someMethod() } returns Result.success(value)

// Stubbing Flow returns
every { mock.observeSomething() } returns flowOf(value)

// Verifying calls
coVerify { mock.someMethod() }

// Verifying never called
coVerify(exactly = 0) { mock.someMethod() }
```
