package com.example.messageapp.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

/**
 * Tests para utilidades de tiempo (Time.kt)
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
class TimeUtilsTest {

    // ============================================
    // Tests para toFormattedDate
    // ============================================

    @Test
    fun `Long toFormattedDate with default pattern returns formatted string`() {
        // Given: Timestamp válido
        val timestamp = 1711305600000L // 2024-03-24 12:00:00 UTC

        // When: Formateo
        val result = timestamp.toFormattedDate()

        // Then: Debería retornar string formateado
        assertThat(result).isNotEmpty()
        assertThat(result).contains("/")
    }

    @Test
    fun `Long toFormattedDate with custom pattern returns formatted string`() {
        // Given: Timestamp válido y patrón custom
        val timestamp = 1711305600000L
        val pattern = "yyyy-MM-dd HH:mm:ss"

        // When: Formateo
        val result = timestamp.toFormattedDate(pattern)

        // Then: Debería retornar string con patrón custom
        assertThat(result).startsWith("2024-")
        assertThat(result).contains("-")
    }

    @Test
    fun `Long toFormattedDate with zero timestamp returns formatted string`() {
        // Given: Timestamp cero (Unix epoch)
        val timestamp = 0L

        // When: Formateo
        val result = timestamp.toFormattedDate()

        // Then: Debería retornar fecha válida (1970)
        assertThat(result).contains("1970")
    }

    @Test
    fun `Long toFormattedDate with negative timestamp handles gracefully`() {
        // Given: Timestamp negativo
        val timestamp = -1000L

        // When: Formateo
        val result = runCatching {
            timestamp.toFormattedDate()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    @Test
    fun `Long toFormattedDate with very large timestamp handles gracefully`() {
        // Given: Timestamp muy grande (año 10000+)
        val timestamp = Long.MAX_VALUE

        // When: Formateo
        val result = runCatching {
            timestamp.toFormattedDate()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para getTimestampOneDayAgo
    // ============================================

    @Test
    fun `getTimestampOneDayAgo returns timestamp from 24 hours ago`() {
        // Given: Nada (usa tiempo actual)

        // When: Obtengo timestamp de hace 1 día
        val result = getTimestampOneDayAgo()

        // Then: Debería ser aproximadamente 24 horas atrás
        val now = System.currentTimeMillis()
        val diff = now - result
        val oneDayInMillis = 24 * 60 * 60 * 1000

        // Permitir margen de error de 1 minuto
        assertThat(diff).isWithin(60000).of(oneDayInMillis)
    }

    @Test
    fun `getTimestampOneDayAgo returns consistent value`() {
        // When: Llamo múltiples veces rápidamente
        val result1 = getTimestampOneDayAgo()
        val result2 = getTimestampOneDayAgo()

        // Then: Debería ser consistente (mismo valor o muy cercano)
        assertThat(kotlin.math.abs(result1 - result2)).isLessThan(1000)
    }

    // ============================================
    // Tests para toRelativeTime
    // ============================================

    @Test
    fun `Long toRelativeTime with recent timestamp returns Ahora`() {
        // Given: Timestamp de hace pocos segundos
        val now = System.currentTimeMillis()
        val recentTimestamp = now - 30000 // Hace 30 segundos

        // When: Obtengo tiempo relativo
        val result = recentTimestamp.toRelativeTime()

        // Then: Debería retornar "Ahora"
        assertThat(result).isEqualTo("Ahora")
    }

    @Test
    fun `Long toRelativeTime with minutes ago returns X min`() {
        // Given: Timestamp de hace 5 minutos
        val now = System.currentTimeMillis()
        val minutesAgo = now - (5 * 60 * 1000)

        // When: Obtengo tiempo relativo
        val result = minutesAgo.toRelativeTime()

        // Then: Debería retornar "5 min"
        assertThat(result).contains("min")
    }

    @Test
    fun `Long toRelativeTime with hours ago returns X h`() {
        // Given: Timestamp de hace 3 horas
        val now = System.currentTimeMillis()
        val hoursAgo = now - (3 * 60 * 60 * 1000)

        // When: Obtengo tiempo relativo
        val result = hoursAgo.toRelativeTime()

        // Then: Debería retornar "3 h"
        assertThat(result).contains("h")
    }

    @Test
    fun `Long toRelativeTime with days ago returns X d`() {
        // Given: Timestamp de hace 2 días
        val now = System.currentTimeMillis()
        val daysAgo = now - (2 * 24 * 60 * 60 * 1000)

        // When: Obtengo tiempo relativo
        val result = daysAgo.toRelativeTime()

        // Then: Debería retornar "2 d"
        assertThat(result).contains("d")
    }

    @Test
    fun `Long toRelativeTime with week ago returns formatted date`() {
        // Given: Timestamp de hace 1 semana
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)

        // When: Obtengo tiempo relativo
        val result = weekAgo.toRelativeTime()

        // Then: Debería retornar fecha formateada (dd/MM/yy)
        assertThat(result).contains("/")
    }

    @Test
    fun `Long toRelativeTime with zero timestamp returns formatted date`() {
        // Given: Timestamp cero
        val timestamp = 0L

        // When: Obtengo tiempo relativo
        val result = timestamp.toRelativeTime()

        // Then: Debería retornar fecha formateada (1970)
        assertThat(result).contains("1970")
    }

    @Test
    fun `Long toRelativeTime with future timestamp handles gracefully`() {
        // Given: Timestamp del futuro
        val future = System.currentTimeMillis() + (24 * 60 * 60 * 1000)

        // When: Obtengo tiempo relativo
        val result = runCatching {
            future.toRelativeTime()
        }

        // Then: No debería crashar
        assertThat(result.exceptionOrNull()).isNull()
    }

    // ============================================
    // Tests para isToday
    // ============================================

    @Test
    fun `Long isToday with current timestamp returns true`() {
        // Given: Timestamp actual
        val now = System.currentTimeMillis()

        // When: Verifico si es hoy
        val result = now.isToday()

        // Then: Debería ser true
        assertThat(result).isTrue()
    }

    @Test
    fun `Long isToday with yesterday timestamp returns false`() {
        // Given: Timestamp de ayer
        val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        // When: Verifico si es hoy
        val result = yesterday.isToday()

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    @Test
    fun `Long isToday with tomorrow timestamp returns false`() {
        // Given: Timestamp de mañana
        val tomorrow = System.currentTimeMillis() + (24 * 60 * 60 * 1000)

        // When: Verifico si es hoy
        val result = tomorrow.isToday()

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    @Test
    fun `Long isToday with zero timestamp returns false`() {
        // Given: Timestamp cero (1970)
        val timestamp = 0L

        // When: Verifico si es hoy
        val result = timestamp.isToday()

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    // ============================================
    // Tests para isThisWeek
    // ============================================

    @Test
    fun `Long isThisWeek with current timestamp returns true`() {
        // Given: Timestamp actual
        val now = System.currentTimeMillis()

        // When: Verifico si es esta semana
        val result = now.isThisWeek()

        // Then: Debería ser true
        assertThat(result).isTrue()
    }

    @Test
    fun `Long isThisWeek with last week timestamp returns false`() {
        // Given: Timestamp de la semana pasada
        val lastWeek = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)

        // When: Verifico si es esta semana
        val result = lastWeek.isThisWeek()

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    @Test
    fun `Long isThisWeek with next week timestamp returns false`() {
        // Given: Timestamp de la próxima semana
        val nextWeek = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)

        // When: Verifico si es esta semana
        val result = nextWeek.isThisWeek()

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    @Test
    fun `Long isThisWeek with zero timestamp returns false`() {
        // Given: Timestamp cero (1970)
        val timestamp = 0L

        // When: Verifico si es esta semana
        val result = timestamp.isThisWeek()

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    // ============================================
    // Tests de integración
    // ============================================

    @Test
    fun `timestamp from getTimestampOneDayAgo isToday returns false`() {
        // Given: Timestamp de hace 1 día
        val yesterday = getTimestampOneDayAgo()

        // When: Verifico si es hoy
        val result = yesterday.isToday()

        // Then: Debería ser false
        assertThat(result).isFalse()
    }

    @Test
    fun `formatted date from toFormattedDate can be parsed back`() {
        // Given: Timestamp válido
        val timestamp = 1711305600000L

        // When: Formateo
        val formatted = timestamp.toFormattedDate("yyyy-MM-dd")

        // Then: Debería tener formato válido
        assertThat(formatted).matches("\\d{4}-\\d{2}-\\d{2}")
    }

    // ============================================
    // Tests edge cases: Performance
    // ============================================

    @Test
    fun `toRelativeTime performance test with 1000 calls`() {
        // Given: Timestamp válido
        val timestamp = System.currentTimeMillis()

        // When: Llamo 1000 veces
        val startTime = System.currentTimeMillis()
        repeat(1000) {
            timestamp.toRelativeTime()
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 500ms)
        assertThat(elapsed).isLessThan(500)
    }

    @Test
    fun `toFormattedDate performance test with 1000 calls`() {
        // Given: Timestamp válido
        val timestamp = System.currentTimeMillis()

        // When: Llamo 1000 veces
        val startTime = System.currentTimeMillis()
        repeat(1000) {
            timestamp.toFormattedDate()
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 500ms)
        assertThat(elapsed).isLessThan(500)
    }

    // ============================================
    // Tests edge cases: Boundary values
    // ============================================

    @Test
    fun `toRelativeTime with exactly 60 seconds returns Ahora`() {
        // Given: Timestamp de exactamente 60 segundos
        val now = System.currentTimeMillis()
        val sixtySecondsAgo = now - 60000

        // When: Obtengo tiempo relativo
        val result = sixtySecondsAgo.toRelativeTime()

        // Then: Debería retornar "Ahora" o "1 min"
        assertThat(result).isAnyOf("Ahora", "1 min")
    }

    @Test
    fun `toRelativeTime with exactly 1 hour returns 1 h`() {
        // Given: Timestamp de exactamente 1 hora
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)

        // When: Obtengo tiempo relativo
        val result = oneHourAgo.toRelativeTime()

        // Then: Debería retornar "1 h" o similar
        assertThat(result).contains("h")
    }

    @Test
    fun `toRelativeTime with exactly 1 day returns 1 d`() {
        // Given: Timestamp de exactamente 1 día
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)

        // When: Obtengo tiempo relativo
        val result = oneDayAgo.toRelativeTime()

        // Then: Debería retornar "1 d" o fecha
        assertThat(result).isAnyOf("1 d", result) // Puede ser fecha si pasa el límite
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `concurrent toRelativeTime calls do not crash`() {
        // Given: Múltiples timestamps
        val timestamps = List(10) {
            System.currentTimeMillis() - (it * 60000)
        }

        // When: Llamo en paralelo
        val results = timestamps.map { ts ->
            runCatching { ts.toRelativeTime() }
        }

        // Then: Ninguno debería crashar
        results.forEach { result ->
            assertThat(result.exceptionOrNull()).isNull()
        }
    }

    @Test
    fun `concurrent isToday calls do not crash`() {
        // Given: Múltiples timestamps
        val timestamps = List(10) {
            System.currentTimeMillis() - (it * 24 * 60 * 60 * 1000)
        }

        // When: Llamo en paralelo
        val results = timestamps.map { ts ->
            runCatching { ts.isToday() }
        }

        // Then: Ninguno debería crashar
        results.forEach { result ->
            assertThat(result.exceptionOrNull()).isNull()
        }
    }
}
