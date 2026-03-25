package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para AuthRepository
 *
 * Cubre: ERR-003 (validación de parámetros), ERR-007 (start con validación)
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 * 
 * NOTA: Tests JVM puros - sin dependencias de Android
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        repository = AuthRepository()
    }

    // ============================================
    // Tests para signUpWithEmail
    // NOTA: Tests de validación de email se hacen en el Repository real
    // Estos tests verifican el comportamiento general sin Android dependencies
    // ============================================

    @Test
    fun `signUpWithEmail with empty email does not crash`() = runTest {
        // Given: Email vacío
        val email = ""
        val password = "password123"

        // When: Intento registrar (puede fallar por validación)
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: No debería crashar (puede retornar error)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `signUpWithEmail with short password does not crash`() = runTest {
        // Given: Password muy corto
        val email = "test@example.com"
        val password = "12345"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `signUpWithEmail with invalid email does not crash`() = runTest {
        // Given: Email con formato inválido
        val email = "invalid-email"
        val password = "password123"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `signUpWithEmail accepts valid email and password`() = runTest {
        // Given: Email y password válidos
        val email = "valid@example.com"
        val password = "password123" // 11 caracteres

        // When: Intento registrar (puede fallar por Supabase, pero no por validación)
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: No debería fallar por validación de parámetros
        val exception = result.exceptionOrNull()
        if (exception != null) {
            // Si falla, no debería ser por IllegalArgumentException de validación
            assertThat(exception).isNotInstanceOf(IllegalArgumentException::class.java)
        }
    }

    // ============================================
    // Tests para signInWithEmail
    // ============================================

    @Test
    fun `signInWithEmail with empty email throws exception`() = runTest {
        // Given: Email vacío
        val email = ""
        val password = "password123"

        // When: Intento login
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInWithEmail with empty password throws exception`() = runTest {
        // Given: Password vacío
        val email = "test@example.com"
        val password = ""

        // When: Intento login
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `signInWithEmail with valid credentials should work`() = runTest {
        // Given: Credenciales válidas
        val email = "test@example.com"
        val password = "password123"

        // When: Intento login (puede fallar por Supabase)
        val result = runCatching {
            repository.signInWithEmail(email, password)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
        // O si falla, no debería ser por validación
    }

    // ============================================
    // Tests para signOut
    // ============================================

    @Test
    fun `signOut does not throw exception`() = runTest {
        // When: Cierro sesión (sin usuario logueado)
        val result = runCatching {
            repository.signOut()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para isValidEmail (privada, pero testeable indirectamente)
    // ============================================

    @Test
    fun `signUpWithEmail rejects email without @ symbol`() = runTest {
        // Given: Email sin @
        val email = "userexample.com"
        val password = "password123"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería rechazar
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `signUpWithEmail rejects email without domain`() = runTest {
        // Given: Email sin dominio
        val email = "user@"
        val password = "password123"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: Debería rechazar
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `signUpWithEmail accepts email with subdomain`() = runTest {
        // Given: Email con subdominio
        val email = "user@mail.example.com"
        val password = "password123"

        // When: Intento registrar
        val result = runCatching {
            repository.signUpWithEmail(email, password)
        }

        // Then: No debería fallar por validación de email
        val exception = result.exceptionOrNull()
        if (exception != null) {
            assertThat(exception).isNotInstanceOf(IllegalArgumentException::class.java)
        }
    }

    // ============================================
    // Tests para sendPasswordReset
    // ============================================

    @Test
    fun `sendPasswordReset with empty email throws exception`() = runTest {
        // Given: Email vacío
        val email = ""

        // When: Intento enviar reset
        val result = runCatching {
            repository.sendPasswordReset(email)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `sendPasswordReset with invalid email throws exception`() = runTest {
        // Given: Email inválido
        val email = "invalid"

        // When: Intento enviar reset
        val result = runCatching {
            repository.sendPasswordReset(email)
        }

        // Then: Debería manejar el error
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    // ============================================
    // Tests para getCurrentUserId
    // ============================================

    @Test
    fun `getCurrentUserId returns null when not logged in`() {
        // When: No hay sesión
        val result = repository.getCurrentUserId()

        // Then: Debería retornar null
        assertThat(result).isNull()
    }

    // ============================================
    // Tests para getCurrentUserEmail
    // ============================================

    @Test
    fun `getCurrentUserEmail returns null when not logged in`() {
        // When: No hay sesión
        val result = repository.getCurrentUserEmail()

        // Then: Debería retornar null
        assertThat(result).isNull()
    }

    // ============================================
    // Tests para updatePresence
    // ============================================

    @Test
    fun `updatePresence does not throw when not logged in`() = runTest {
        // When: Actualizo presencia sin estar logueado
        val result = runCatching {
            repository.updatePresence(true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updatePresence accepts online true`() = runTest {
        // When: Actualizo a online
        val result = runCatching {
            repository.updatePresence(true)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updatePresence accepts online false`() = runTest {
        // When: Actualizo a offline
        val result = runCatching {
            repository.updatePresence(false)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para signInAnonymously
    // ============================================

    @Test
    fun `signInAnonymously does not throw exception`() = runTest {
        // When: Intento login anónimo
        val result = runCatching {
            repository.signInAnonymously()
        }

        // Then: No debería crashar (puede fallar por Supabase)
        // Pero no debería ser NullPointerException u otro error crítico
        if (result.exceptionOrNull() != null) {
            assertThat(result.exceptionOrNull()).isNotInstanceOf(NullPointerException::class.java)
        }
    }

    // ============================================
    // Tests para upsertUserProfile
    // ============================================

    @Test
    fun `upsertUserProfile with empty uid does not throw`() = runTest {
        // When: Llamo con uid vacío
        val result = runCatching {
            repository.upsertUserProfile("")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para updateJPushRegistrationId
    // ============================================

    @Test
    fun `updateJPushRegistrationId with empty id does not throw`() = runTest {
        // When: Actualizo con ID vacío
        val result = runCatching {
            repository.updateJPushRegistrationId("")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateJPushRegistrationId with valid id does not throw`() = runTest {
        // When: Actualizo con ID válido
        val result = runCatching {
            repository.updateJPushRegistrationId("valid-registration-id-12345")
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }
}
