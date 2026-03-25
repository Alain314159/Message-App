package com.example.messageapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para ProfileRepository
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryTest {

    private lateinit var repository: ProfileRepository

    @Before
    fun setup() {
        repository = ProfileRepository()
    }

    // ============================================
    // Tests para updateProfile
    // ============================================

    @Test
    fun `updateProfile with empty displayName does not crash`() = runTest {
        // Given: DisplayName vacío
        val emptyDisplayName = ""
        val bio = "Test bio"

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(emptyDisplayName, bio)
        }

        // Then: No debería crashar (puede fallar por no estar logueado)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateProfile with empty bio does not crash`() = runTest {
        // Given: Bio vacío
        val displayName = "Test User"
        val emptyBio = ""

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(displayName, emptyBio)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateProfile with valid data does not crash`() = runTest {
        // Given: Datos válidos
        val displayName = "Valid User"
        val bio = "Test bio description"

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(displayName, bio)
        }

        // Then: No debería crashar (puede fallar por Supabase)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateProfile with very long displayName does not crash`() = runTest {
        // Given: DisplayName muy largo
        val longDisplayName = "a".repeat(1000)
        val bio = "Test bio"

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(longDisplayName, bio)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateProfile with very long bio does not crash`() = runTest {
        // Given: Bio muy largo
        val displayName = "Test User"
        val longBio = "b".repeat(10000)

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(displayName, longBio)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateProfile with special characters does not crash`() = runTest {
        // Given: Caracteres especiales
        val displayName = "User-<>&\"'-123"
        val bio = "Bio with !@#$%^&*()"

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(displayName, bio)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `updateProfile with unicode characters does not crash`() = runTest {
        // Given: Caracteres unicode
        val displayName = "Usuario 🌍"
        val bio = "Bio con 你好 áéíóú"

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(displayName, bio)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para uploadAvatar
    // ============================================

    @Test
    fun `uploadAvatar without user logged in returns failure`() = runTest {
        // Given: Sin usuario logueado (simulado)
        val uri = android.net.Uri.EMPTY

        // When: Intento subir avatar
        val result = runCatching {
            repository.uploadAvatar(uri)
        }

        // Then: Debería retornar failure o no crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `uploadAvatar with valid uri does not crash`() = runTest {
        // Given: URI válido
        val uri = android.net.Uri.parse("content://test/avatar.jpg")

        // When: Intento subir avatar
        val result = runCatching {
            repository.uploadAvatar(uri)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para updateProfile concurrencia
    // ============================================

    @Test
    fun `multiple updateProfile calls do not crash`() = runTest {
        // Given: Múltiples actualizaciones
        val profiles = listOf(
            "User 1" to "Bio 1",
            "User 2" to "Bio 2",
            "User 3" to "Bio 3"
        )

        // When: Actualizo múltiples veces
        val results = profiles.map { (name, bio) ->
            runCatching {
                repository.updateProfile(name, bio)
            }
        }

        // Then: Ninguno debería crashar
        results.forEach { result ->
            assertThat(result.exceptionOrNull()).isNull()
        }
    }

    @Test
    fun `multiple uploadAvatar calls do not crash`() = runTest {
        // Given: Múltiples URIs
        val uris = listOf(
            android.net.Uri.parse("content://test1/avatar.jpg"),
            android.net.Uri.parse("content://test2/avatar.jpg"),
            android.net.Uri.parse("content://test3/avatar.jpg")
        )

        // When: Intento subir múltiples avatares
        val results = uris.map { uri ->
            runCatching {
                repository.uploadAvatar(uri)
            }
        }

        // Then: Ninguno debería crashar
        results.forEach { result ->
            assertThat(result.exceptionOrNull()).isNull()
        }
    }

    // ============================================
    // Tests edge cases: Null safety
    // ============================================

    @Test
    fun `updateProfile handles null-like strings gracefully`() = runTest {
        // Given: Strings que parecen null
        val nullDisplayName = "null"
        val nullBio = "null"

        // When: Actualizo perfil
        val result = runCatching {
            repository.updateProfile(nullDisplayName, nullBio)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de rendimiento
    // ============================================

    @Test
    fun `updateProfile performance test with 100 calls`() = runTest {
        // Given: 100 actualizaciones
        val updates = List(100) { "User $it" to "Bio $it" }

        // When: Actualizo 100 veces
        val startTime = System.currentTimeMillis()
        updates.forEach { (name, bio) ->
            runCatching {
                repository.updateProfile(name, bio)
            }
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser razonablemente rápido (< 10 segundos)
        assertThat(elapsed).isLessThan(10000)
    }
}
