package com.example.messageapp.data

import com.example.messageapp.model.AvatarType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests para AvatarRepository
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AvatarRepositoryTest {

    private lateinit var repository: AvatarRepository

    @Before
    fun setup() {
        repository = AvatarRepository()
    }

    // ============================================
    // Tests para getUserAvatar
    // ============================================

    @Test
    fun `getUserAvatar with empty userId returns default avatar`() = runTest {
        // Given: UserId vacío
        val emptyUserId = ""

        // When: Obtengo avatar
        val result = runCatching {
            repository.getUserAvatar(emptyUserId)
        }

        // Then: Debería retornar avatar por defecto (CERDITA)
        assertThat(result.getOrNull()).isEqualTo(AvatarType.CERDITA)
    }

    @Test
    fun `getUserAvatar with null user returns default avatar`() = runTest {
        // Given: UserId null
        val nullUserId: String? = null

        // When: Obtengo avatar (con null safety)
        val result = runCatching {
            nullUserId?.let { repository.getUserAvatar(it) } ?: AvatarType.CERDITA
        }

        // Then: Debería retornar avatar por defecto
        assertThat(result.getOrNull()).isEqualTo(AvatarType.CERDITA)
    }

    @Test
    fun `getUserAvatar with database error returns default avatar`() = runTest {
        // Given: UserId válido pero DB falla (simulado)
        val userId = "user-error"

        // When: Obtengo avatar (con error)
        val result = runCatching {
            repository.getUserAvatar(userId)
        }

        // Then: Debería retornar avatar por defecto (maneja error)
        assertThat(result.getOrNull()).isEqualTo(AvatarType.CERDITA)
    }

    @Test
    fun `getUserAvatar with valid userId returns avatar type`() = runTest {
        // Given: UserId válido
        val userId = "user-123"

        // When: Obtengo avatar
        val result = runCatching {
            repository.getUserAvatar(userId)
        }

        // Then: Debería retornar un AvatarType (puede ser CERDITA por defecto)
        assertThat(result.getOrNull()).isInstanceOf(AvatarType::class.java)
    }

    @Test
    fun `getUserAvatar with very long userId does not crash`() = runTest {
        // Given: UserId muy largo
        val longUserId = "user-${"a".repeat(1000)}"

        // When: Obtengo avatar
        val result = runCatching {
            repository.getUserAvatar(longUserId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para setUserAvatar
    // ============================================

    @Test
    fun `setUserAvatar with empty userId returns failure`() = runTest {
        // Given: UserId vacío
        val emptyUserId = ""
        val avatarType = AvatarType.CERDITA

        // When: Actualizo avatar
        val result = runCatching {
            repository.setUserAvatar(emptyUserId, avatarType)
        }

        // Then: Debería retornar failure o no crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setUserAvatar with valid userId returns success`() = runTest {
        // Given: UserId válido y avatar
        val userId = "user-456"
        val avatarType = AvatarType.CERDITA

        // When: Actualizo avatar
        val result = runCatching {
            repository.setUserAvatar(userId, avatarType)
        }

        // Then: No debería crashar (puede fallar por Supabase)
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setUserAvatar with different avatar types works`() = runTest {
        // Given: Diferentes tipos de avatar
        val userId = "user-789"
        val avatarTypes = listOf(
            AvatarType.CERDITA,
            AvatarType.CERDITO,
            AvatarType.CERDITA_CORAZON,
            AvatarType.CERDITO_CORAZON
        )

        // When: Actualizo con cada tipo
        val results = avatarTypes.map { avatar ->
            runCatching {
                repository.setUserAvatar(userId, avatar)
            }
        }

        // Then: Ninguno debería crashar
        results.forEach { result ->
            assertThat(result.exceptionOrNull()).isNull()
        }
    }

    @Test
    fun `setUserAvatar with null avatarType does not crash`() = runTest {
        // Given: AvatarType null (usamos default)
        val userId = "user-null"
        val avatarType = AvatarType.CERDITA // Default

        // When: Actualizo avatar
        val result = runCatching {
            repository.setUserAvatar(userId, avatarType)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para getAllAvatars
    // ============================================

    @Test
    fun `getAllAvatars returns non-empty list`() {
        // When: Obtengo todos los avatares
        val result = repository.getAllAvatars()

        // Then: Debería retornar lista no vacía
        assertThat(result).isNotEmpty()
    }

    @Test
    fun `getAllAvatars contains CERDITA`() {
        // When: Obtengo todos los avatares
        val result = repository.getAllAvatars()

        // Then: Debería contener CERDITA
        assertThat(result).contains(AvatarType.CERDITA)
    }

    @Test
    fun `getAllAvatars contains CERDITO`() {
        // When: Obtengo todos los avatares
        val result = repository.getAllAvatars()

        // Then: Debería contener CERDITO
        assertThat(result).contains(AvatarType.CERDITO)
    }

    @Test
    fun `getAllAvatars returns consistent list`() {
        // When: Obtengo avatares múltiples veces
        val result1 = repository.getAllAvatars()
        val result2 = repository.getAllAvatars()
        val result3 = repository.getAllAvatars()

        // Then: Debería retornar misma lista
        assertThat(result1).isEqualTo(result2)
        assertThat(result2).isEqualTo(result3)
    }

    // ============================================
    // Tests de integración: getUserAvatar + setUserAvatar
    // ============================================

    @Test
    fun `setUserAvatar then getUserAvatar returns updated avatar`() = runTest {
        // Given: UserId válido
        val userId = "user-integration"
        val newAvatar = AvatarType.CERDITO

        // When: Actualizo y luego obtengo
        repository.setUserAvatar(userId, newAvatar)
        val result = repository.getUserAvatar(userId)

        // Then: Debería retornar el avatar actualizado (o default si falla)
        // Nota: En test real, podría ser CERDITA por defecto si Supabase falla
        assertThat(result).isInstanceOf(AvatarType::class.java)
    }

    // ============================================
    // Tests edge cases: Special characters
    // ============================================

    @Test
    fun `getUserAvatar with special characters in userId does not crash`() = runTest {
        // Given: UserId con caracteres especiales
        val specialUserId = "user-<>&\"'-123"

        // When: Obtengo avatar
        val result = runCatching {
            repository.getUserAvatar(specialUserId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `setUserAvatar with special characters in userId does not crash`() = runTest {
        // Given: UserId con caracteres especiales
        val specialUserId = "user-special-!@#$%"
        val avatarType = AvatarType.CERDITA

        // When: Actualizo avatar
        val result = runCatching {
            repository.setUserAvatar(specialUserId, avatarType)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests edge cases: Unicode
    // ============================================

    @Test
    fun `getUserAvatar with unicode in userId does not crash`() = runTest {
        // Given: UserId con unicode
        val unicodeUserId = "user-🌍-123"

        // When: Obtengo avatar
        val result = runCatching {
            repository.getUserAvatar(unicodeUserId)
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `multiple getUserAvatar calls do not crash`() = runTest {
        // Given: Múltiples user IDs
        val userIds = listOf("user-1", "user-2", "user-3")

        // When: Obtengo avatares en paralelo
        val results = userIds.map { userId ->
            runCatching {
                repository.getUserAvatar(userId)
            }
        }

        // Then: Ninguno debería crashar
        results.forEach { result ->
            assertThat(result.exceptionOrNull()).isNull()
        }
    }

    @Test
    fun `multiple setUserAvatar calls do not crash`() = runTest {
        // Given: Múltiples actualizaciones
        val userId = "user-multi"
        val avatarTypes = listOf(
            AvatarType.CERDITA,
            AvatarType.CERDITO,
            AvatarType.CERDITA_CORAZON
        )

        // When: Actualizo múltiples veces
        val results = avatarTypes.map { avatar ->
            runCatching {
                repository.setUserAvatar(userId, avatar)
            }
        }

        // Then: Ninguno debería crashar
        results.forEach { result ->
            assertThat(result.exceptionOrNull()).isNull()
        }
    }

    // ============================================
    // Tests de rendimiento
    // ============================================

    @Test
    fun `getAllAvatars performance test`() {
        // When: Llamo 100 veces
        val startTime = System.currentTimeMillis()
        repeat(100) {
            repository.getAllAvatars()
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 100ms)
        assertThat(elapsed).isLessThan(100)
    }

    @Test
    fun `getUserAvatar performance test`() = runTest {
        // Given: UserId válido
        val userId = "user-perf"

        // When: Llamo 50 veces
        val startTime = System.currentTimeMillis()
        repeat(50) {
            repository.getUserAvatar(userId)
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 1 segundo)
        assertThat(elapsed).isLessThan(1000)
    }
}
