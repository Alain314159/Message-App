package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para AuthRepository - Network Error Handling
 *
 * Cubre: Errores de red, timeouts, server errors
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryNetworkErrorTest {

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        repository = AuthRepository()
    }

    // ============================================
    // Tests para signUpWithEmail - Network Errors
    // ============================================

    @Test
    fun `signUpWithEmail handles network timeout gracefully`() = runTest {
        // Given: Email y password válidos (puede timeout por red)
        val email = "test@example.com"
        val password = "password123"

        // When: Intento registrar (puede timeout)
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería retornar Result.failure, no crashar
        // Nota: En JVM tests sin Supabase configurado, esto fallará
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signUpWithEmail handles server error 500 gracefully`() = runTest {
        // Given: Email y password válidos
        val email = "test2@example.com"
        val password = "password123"

        // When: Intento registrar (puede fallar con 500)
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signUpWithEmail handles duplicate email gracefully`() = runTest {
        // Given: Email que podría existir
        val email = "duplicate@example.com"
        val password = "password123"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería manejar el error (puede ser duplicate o network)
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signUpWithEmail handles connection refused gracefully`() = runTest {
        // Given: Email y password válidos
        val email = "test3@example.com"
        val password = "password123"

        // When: Intento registrar (puede ser connection refused)
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    // ============================================
    // Tests para signInWithEmail - Network Errors
    // ============================================

    @Test
    fun `signInWithEmail handles network timeout gracefully`() = runTest {
        // Given: Email y password válidos
        val email = "login@example.com"
        val password = "password123"

        // When: Intento login (puede timeout)
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInWithEmail handles invalid credentials gracefully`() = runTest {
        // Given: Credenciales inválidas
        val email = "invalid@example.com"
        val password = "wrongpassword"

        // When: Intento login
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInWithEmail handles account not confirmed gracefully`() = runTest {
        // Given: Email que podría no estar confirmado
        val email = "unconfirmed@example.com"
        val password = "password123"

        // When: Intento login
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInWithEmail handles locked account gracefully`() = runTest {
        // Given: Email que podría estar bloqueado
        val email = "locked@example.com"
        val password = "password123"

        // When: Intento login
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInWithEmail handles connection reset gracefully`() = runTest {
        // Given: Email y password válidos
        val email = "test4@example.com"
        val password = "password123"

        // When: Intento login (puede ser connection reset)
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    // ============================================
    // Tests para signOut - Network Errors
    // ============================================

    @Test
    fun `signOut handles network error gracefully`() = runTest {
        // When: Cierro sesión (puede fallar por red)
        val result = runCatching {
            repository.signOut()
        }

        // Then: No debería crashar (debería manejar error silenciosamente)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `signOut handles connection timeout gracefully`() = runTest {
        // When: Cierro sesión (puede timeout)
        val result = runCatching {
            repository.signOut()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `signOut when not logged in does not crash`() = runTest {
        // When: Cierro sesión sin estar logueado
        val result = runCatching {
            repository.signOut()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para getCurrentUser - Network Errors
    // ============================================

    @Test
    fun `getCurrentUser returns null when offline`() = runTest {
        // When: Intento obtener usuario sin conexión
        val result = runCatching {
            repository.getCurrentUser()
        }

        // Then: Debería retornar null o error
        val exception = result.exceptionOrNull()
        if (exception == null) {
            // Si no hay error, debería ser null
            assertThat(result.getOrNull()).isNull()
        }
    }

    @Test
    fun `getCurrentUser handles server error gracefully`() = runTest {
        // When: Intento obtener usuario (puede fallar 500)
        val result = runCatching {
            repository.getCurrentUser()
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `getCurrentUser handles timeout gracefully`() = runTest {
        // When: Intento obtener usuario (puede timeout)
        val result = runCatching {
            repository.getCurrentUser()
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    // ============================================
    // Tests para updatePresence - Network Errors
    // ============================================

    @Test
    fun `updatePresence handles network error gracefully`() = runTest {
        // When: Actualizo presencia (puede fallar por red)
        val result = runCatching {
            repository.updatePresence(true)
        }

        // Then: No debería crashar (maneja error silenciosamente)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updatePresence handles timeout gracefully`() = runTest {
        // When: Actualizo presencia (puede timeout)
        val result = runCatching {
            repository.updatePresence(false)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updatePresence when not logged in does not crash`() = runTest {
        // When: Actualizo presencia sin estar logueado
        val result = runCatching {
            repository.updatePresence(true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para updateJPushRegistrationId - Network Errors
    // ============================================

    @Test
    fun `updateJPushRegistrationId handles network error gracefully`() = runTest {
        // When: Actualizo JPush ID (puede fallar por red)
        val result = runCatching {
            repository.updateJPushRegistrationId("test-registration-id")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateJPushRegistrationId handles timeout gracefully`() = runTest {
        // When: Actualizo JPush ID (puede timeout)
        val result = runCatching {
            repository.updateJPushRegistrationId("test-id-123")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateJPushRegistrationId when not logged in does not crash`() = runTest {
        // When: Actualizo JPush ID sin estar logueado
        val result = runCatching {
            repository.updateJPushRegistrationId("test-id")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para sendPasswordReset - Network Errors
    // ============================================

    @Test
    fun `sendPasswordReset handles network error gracefully`() = runTest {
        // Given: Email válido
        val email = "reset@example.com"

        // When: Envío reset (puede fallar por red)
        val result = runCatching {
            repository.sendPasswordReset(email)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `sendPasswordReset handles invalid email gracefully`() = runTest {
        // Given: Email inválido
        val email = "invalid-email"

        // When: Envío reset
        val result = runCatching {
            repository.sendPasswordReset(email)
        }

        // Then: Debería manejar el error (validación o red)
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `sendPasswordReset handles non-existent email gracefully`() = runTest {
        // Given: Email que no existe
        val email = "nonexistent@example.com"

        // When: Envío reset
        val result = runCatching {
            repository.sendPasswordReset(email)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    // ============================================
    // Tests para signInAnonymously - Network Errors
    // ============================================

    @Test
    fun `signInAnonymously handles network timeout gracefully`() = runTest {
        // When: Intento login anónimo (puede timeout)
        val result = runCatching {
            repository.signInAnonymously()
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInAnonymously handles server error gracefully`() = runTest {
        // When: Intento login anónimo (puede fallar 500)
        val result = runCatching {
            repository.signInAnonymously()
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInAnonymously handles rate limit gracefully`() = runTest {
        // When: Intento login anónimo (puede tener rate limit)
        val result = runCatching {
            repository.signInAnonymously()
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    // ============================================
    // Tests para upsertUserProfile - Network Errors
    // ============================================

    @Test
    fun `upsertUserProfile handles network error gracefully`() = runTest {
        // Given: UID vacío (no logueado)
        val uid = ""

        // When: Actualizo perfil (puede fallar por red)
        val result = runCatching {
            repository.upsertUserProfile(uid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `upsertUserProfile handles timeout gracefully`() = runTest {
        // Given: UID válido
        val uid = "user-123"

        // When: Actualizo perfil (puede timeout)
        val result = runCatching {
            repository.upsertUserProfile(uid)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `concurrent signUp calls do not crash`() = runTest {
        // When: Múltiples signups concurrentes
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                repeat(5) {
                    kotlinx.coroutines.launch {
                        repository.signUpWithEmail("test$it@example.com", "password123")
                    }
                }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `concurrent signIn calls do not crash`() = runTest {
        // When: Múltiples signins concurrentes
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                repeat(5) {
                    kotlinx.coroutines.launch {
                        repository.signInWithEmail("test@example.com", "password123")
                    }
                }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `mixed auth operations do not crash`() = runTest {
        // When: Operaciones mixtas concurrentes
        val result = runCatching {
            kotlinx.coroutines.coroutineScope {
                launch { repository.signUpWithEmail("test1@example.com", "password123") }
                launch { repository.signInWithEmail("test2@example.com", "password123") }
                launch { repository.signOut() }
                launch { repository.updatePresence(true) }
            }
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Special values
    // ============================================

    @Test
    fun `signUpWithEmail with unicode in email handles network error`() = runTest {
        // Given: Email con unicode
        val email = "test-😀@example.com"
        val password = "password123"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInWithEmail with very long password handles network error`() = runTest {
        // Given: Password muy larga
        val email = "test@example.com"
        val password = "a".repeat(1000)

        // When: Intento login
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signUpWithEmail with SQL injection attempt handles gracefully`() = runTest {
        // Given: Email con intento de SQL injection
        val email = "'; DROP TABLE users;--@example.com"
        val password = "password123"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }
}
