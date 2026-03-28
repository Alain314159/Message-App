package com.example.messageapp.model

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

/**
 * Tests para modelos de Temas (ThemeConfig, ThemeType, ColorScheme)
 *
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 *
 * Correcciones aplicadas (2026-03-26):
 * - Añadido @Before para setup
 * - Aserciones descriptivas con assertWithMessage
 * - Tests de valores específicos en ColorScheme
 * - Tests de unicidad para ThemeType
 * - Tests de edge cases para id/name vacío
 * - Tests de equals, hashCode y toString
 * - Tests de batch con verificación de consistencia
 * - Eliminados tests redundantes de ThemeType mapping
 *
 * Resumen de cambios:
 * - 25 tests totales (happy path, edge cases, integration, performance)
 * - Todas las aserciones usan assertWithMessage para mensajes descriptivos
 * - Tests de equals/hashCode/toString para data classes
 * - Tests de valores hex específicos para ColorScheme
 * - Threshold de performance ajustado a 2000ms para CI
 * - Documentación actualizada en specs/lessons.md
 */
class ThemeModelsTest {

    // ============================================
    // Setup y Teardown
    // ============================================

    private lateinit var defaultColorScheme: ColorScheme
    private lateinit var defaultTheme: ThemeConfig

    @Before
    fun setUp() {
        // Inicializar recursos comunes para cada test
        defaultColorScheme = ColorScheme()
        defaultTheme = ThemeConfig(
            id = "test-default",
            name = "Default Theme",
            type = ThemeType.LIGHT,
            colors = defaultColorScheme
        )
    }

    // ============================================
    // Tests para ThemeType
    // ============================================

    @Test
    fun `ThemeType has all required values`() {
        // When: Obtengo todos los valores de ThemeType
        val values = ThemeType.values()

        // Then: Debería tener todos los temas definidos con mensajes descriptivos
        assertWithMessage("ThemeType debería contener LIGHT")
            .that(values)
            .contains(ThemeType.LIGHT)
        
        assertWithMessage("ThemeType debería contener DARK")
            .that(values)
            .contains(ThemeType.DARK)
        
        assertWithMessage("ThemeType debería contener PIG")
            .that(values)
            .contains(ThemeType.PIG)
        
        assertWithMessage("ThemeType debería contener KOALA")
            .that(values)
            .contains(ThemeType.KOALA)
        
        assertWithMessage("ThemeType debería contener MIXED")
            .that(values)
            .contains(ThemeType.MIXED)
        
        assertWithMessage("ThemeType debería contener CUSTOM_IMAGE")
            .that(values)
            .contains(ThemeType.CUSTOM_IMAGE)
    }

    @Test
    fun `ThemeType values count is correct`() {
        // When: Cuento los valores
        val count = ThemeType.values().size

        // Then: Debería tener 6 valores
        assertWithMessage("ThemeType debería tener exactamente 6 valores")
            .that(count)
            .isEqualTo(6)
    }

    @Test
    fun `ThemeType values are unique`() {
        // When: Obtengo todos los valores
        val values = ThemeType.values()

        // Then: Todos los valores deberían ser únicos
        assertWithMessage("ThemeType values deberían ser únicos")
            .that(values.distinct())
            .hasSize(values.size)
    }

    // ============================================
    // Tests para ColorScheme
    // ============================================

    @Test
    fun `ColorScheme with default values creates successfully`() {
        // When: Creo ColorScheme con valores por defecto
        val colorScheme = ColorScheme()

        // Then: Debería crear exitosamente con valores específicos esperados
        assertWithMessage("ColorScheme primary debería ser azul (0xFF1976D2)")
            .that(colorScheme.primary)
            .isEqualTo(androidx.compose.ui.graphics.Color(0xFF1976D2))
        
        assertWithMessage("ColorScheme onPrimary debería ser blanco")
            .that(colorScheme.onPrimary)
            .isEqualTo(androidx.compose.ui.graphics.Color.White)
        
        assertWithMessage("ColorScheme secondary debería ser cyan (0xFF03DAC6)")
            .that(colorScheme.secondary)
            .isEqualTo(androidx.compose.ui.graphics.Color(0xFF03DAC6))
        
        assertWithMessage("ColorScheme onSecondary debería ser negro")
            .that(colorScheme.onSecondary)
            .isEqualTo(androidx.compose.ui.graphics.Color.Black)
        
        assertWithMessage("ColorScheme background debería ser gris claro (0xFFFAFAFA)")
            .that(colorScheme.background)
            .isEqualTo(androidx.compose.ui.graphics.Color(0xFFFAFAFA))
        
        assertWithMessage("ColorScheme onBackground debería ser gris oscuro (0xFF212121)")
            .that(colorScheme.onBackground)
            .isEqualTo(androidx.compose.ui.graphics.Color(0xFF212121))
        
        assertWithMessage("ColorScheme surface debería ser blanco")
            .that(colorScheme.surface)
            .isEqualTo(androidx.compose.ui.graphics.Color.White)
        
        assertWithMessage("ColorScheme onSurface debería ser gris oscuro (0xFF212121)")
            .that(colorScheme.onSurface)
            .isEqualTo(androidx.compose.ui.graphics.Color(0xFF212121))
        
        assertWithMessage("ColorScheme error debería ser rojo (0xFFB00020)")
            .that(colorScheme.error)
            .isEqualTo(androidx.compose.ui.graphics.Color(0xFFB00020))
        
        assertWithMessage("ColorScheme onError debería ser blanco")
            .that(colorScheme.onError)
            .isEqualTo(androidx.compose.ui.graphics.Color.White)
    }

    @Test
    fun `ColorScheme with custom colors creates successfully`() {
        // When: Creo ColorScheme con colores personalizados
        val customColorScheme = ColorScheme(
            primary = androidx.compose.ui.graphics.Color.Red,
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = androidx.compose.ui.graphics.Color.Blue,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            background = androidx.compose.ui.graphics.Color.LightGray,
            onBackground = androidx.compose.ui.graphics.Color.Black,
            surface = androidx.compose.ui.graphics.Color.Gray,
            onSurface = androidx.compose.ui.graphics.Color.White,
            error = androidx.compose.ui.graphics.Color.Magenta,
            onError = androidx.compose.ui.graphics.Color.White
        )

        // Then: Debería crear exitosamente con colores custom
        assertThat(customColorScheme.primary).isEqualTo(androidx.compose.ui.graphics.Color.Red)
        assertThat(customColorScheme.onPrimary).isEqualTo(androidx.compose.ui.graphics.Color.White)
        assertThat(customColorScheme.secondary).isEqualTo(androidx.compose.ui.graphics.Color.Blue)
        assertThat(customColorScheme.onSecondary).isEqualTo(androidx.compose.ui.graphics.Color.White)
        assertThat(customColorScheme.background).isEqualTo(androidx.compose.ui.graphics.Color.LightGray)
    }

    @Test
    fun `ColorScheme copy creates new instance with changes`() {
        // Given: ColorScheme original
        val original = ColorScheme(primary = androidx.compose.ui.graphics.Color.Red)

        // When: Creo copia con cambio
        val copy = original.copy(primary = androidx.compose.ui.graphics.Color.Blue)

        // Then: Debería tener nuevo color pero mantener los demás
        assertWithMessage("Copy debería tener el nuevo color primary")
            .that(copy.primary)
            .isEqualTo(androidx.compose.ui.graphics.Color.Blue)
        
        assertWithMessage("Original debería mantener su color primary")
            .that(original.primary)
            .isEqualTo(androidx.compose.ui.graphics.Color.Red)
        
        assertWithMessage("Copy debería mantener otros campos sin cambios")
            .that(copy.onPrimary)
            .isEqualTo(original.onPrimary)
    }

    @Test
    fun `ColorScheme copy changes all fields`() {
        // Given: ColorScheme original
        val original = ColorScheme(primary = androidx.compose.ui.graphics.Color.Red)

        // When: Creo copia con múltiples cambios
        val copy = original.copy(
            primary = androidx.compose.ui.graphics.Color.Blue,
            secondary = androidx.compose.ui.graphics.Color.Green,
            background = androidx.compose.ui.graphics.Color.Yellow
        )

        // Then: Todos los campos cambiados deberían tener nuevos valores
        assertThat(copy.primary).isEqualTo(androidx.compose.ui.graphics.Color.Blue)
        assertThat(copy.secondary).isEqualTo(androidx.compose.ui.graphics.Color.Green)
        assertThat(copy.background).isEqualTo(androidx.compose.ui.graphics.Color.Yellow)
        // Campos no cambiados deberían mantener valores originales
        assertThat(copy.onPrimary).isEqualTo(original.onPrimary)
    }

    @Test
    fun `ColorScheme equals and hashCode work correctly`() {
        // Given: Dos ColorScheme con mismos valores
        val scheme1 = ColorScheme(primary = androidx.compose.ui.graphics.Color.Red)
        val scheme2 = ColorScheme(primary = androidx.compose.ui.graphics.Color.Red)

        // Then: Deberían ser iguales y tener mismo hashCode
        assertWithMessage("ColorScheme con mismos valores deberían ser iguales")
            .that(scheme1)
            .isEqualTo(scheme2)
        
        assertWithMessage("ColorScheme iguales deberían tener mismo hashCode")
            .that(scheme1.hashCode())
            .isEqualTo(scheme2.hashCode())
    }

    @Test
    fun `ColorScheme toString returns meaningful representation`() {
        // Given: ColorScheme con valores específicos
        val colorScheme = ColorScheme(primary = androidx.compose.ui.graphics.Color.Red)

        // When: Obtengo string representation
        val toString = colorScheme.toString()

        // Then: Debería contener información útil
        assertWithMessage("toString debería contener información del color primary")
            .that(toString)
            .contains("primary")
    }

    // ============================================
    // Tests para ThemeConfig
    // ============================================

    @Test
    fun `ThemeConfig with default values creates successfully`() {
        // When: Creo ThemeConfig con valores por defecto
        val theme = ThemeConfig(
            id = "test-theme",
            name = "Test Theme",
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )

        // Then: Debería crear exitosamente
        assertThat(theme.id).isEqualTo("test-theme")
        assertThat(theme.name).isEqualTo("Test Theme")
        assertThat(theme.type).isEqualTo(ThemeType.LIGHT)
        assertThat(theme.isCustom).isFalse()
        assertThat(theme.imageUri).isNull()
    }

    @Test
    fun `ThemeConfig default isCustom is false`() {
        // When: Creo ThemeConfig sin especificar isCustom
        val theme = ThemeConfig(
            id = "default-is-custom",
            name = "Default IsCustom Test",
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )

        // Then: isCustom debería ser false por defecto
        assertWithMessage("isCustom debería ser false por defecto")
            .that(theme.isCustom)
            .isFalse()
    }

    @Test
    fun `ThemeConfig with custom image creates successfully`() {
        // When: Creo ThemeConfig con imagen personalizada
        val customTheme = ThemeConfig(
            id = "custom-1",
            name = "Custom Theme",
            type = ThemeType.CUSTOM_IMAGE,
            colors = ColorScheme(),
            isCustom = true,
            imageUri = "content://images/photo.jpg"
        )

        // Then: Debería crear exitosamente con imagen
        assertThat(customTheme.isCustom).isTrue()
        assertThat(customTheme.imageUri).isEqualTo("content://images/photo.jpg")
        assertThat(customTheme.type).isEqualTo(ThemeType.CUSTOM_IMAGE)
    }

    @Test
    fun `ThemeConfig with non-CUSTOM_IMAGE type has null imageUri`() {
        // When: Creo ThemeConfig con tipo no CUSTOM_IMAGE y imageUri null
        val theme = ThemeConfig(
            id = "no-image",
            name = "No Image Theme",
            type = ThemeType.PIG,
            colors = ColorScheme(),
            imageUri = null
        )

        // Then: imageUri debería ser nulo
        assertWithMessage("imageUri debería ser null para tipos no CUSTOM_IMAGE")
            .that(theme.imageUri)
            .isNull()
        
        assertWithMessage("El tipo debería ser diferente de CUSTOM_IMAGE")
            .that(theme.type)
            .isNotEqualTo(ThemeType.CUSTOM_IMAGE)
    }

    @Test
    fun `ThemeConfig copy creates new instance`() {
        // Given: ThemeConfig original
        val original = ThemeConfig(
            id = "original",
            name = "Original",
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )

        // When: Creo copia con cambios
        val copy = original.copy(name = "Modified", isCustom = true)

        // Then: Debería tener cambios pero mantener ID
        assertThat(copy.id).isEqualTo("original")
        assertThat(copy.name).isEqualTo("Modified")
        assertThat(copy.isCustom).isTrue()
        assertThat(original.name).isEqualTo("Original")
        assertThat(original.isCustom).isFalse()
    }

    @Test
    fun `ThemeConfig with null imageUri creates successfully`() {
        // When: Creo ThemeConfig con imageUri null
        val theme = ThemeConfig(
            id = "no-image",
            name = "No Image Theme",
            type = ThemeType.PIG,
            colors = ColorScheme(),
            imageUri = null
        )

        // Then: Debería crear exitosamente con null
        assertThat(theme.imageUri).isNull()
    }

    @Test
    fun `ThemeConfig equals and hashCode work correctly`() {
        // Given: Dos ThemeConfig con mismos valores
        val theme1 = ThemeConfig(
            id = "same-id",
            name = "Same Name",
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )
        val theme2 = ThemeConfig(
            id = "same-id",
            name = "Same Name",
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )

        // Then: Deberían ser iguales y tener mismo hashCode
        assertWithMessage("ThemeConfig con mismos valores deberían ser iguales")
            .that(theme1)
            .isEqualTo(theme2)
        
        assertWithMessage("ThemeConfig iguales deberían tener mismo hashCode")
            .that(theme1.hashCode())
            .isEqualTo(theme2.hashCode())
    }

    // ============================================
    // Edge Cases - Validación de parámetros
    // ============================================

    @Test
    fun `ThemeConfig with empty id creates successfully`() {
        // Nota: Actualmente no hay validación en el data class
        // When: Creo ThemeConfig con id vacío
        val theme = ThemeConfig(
            id = "",
            name = "Empty ID Theme",
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )

        // Then: Debería crear exitosamente (sin validación actual)
        assertThat(theme.id).isEmpty()
    }

    @Test
    fun `ThemeConfig with empty name creates successfully`() {
        // Nota: Actualmente no hay validación en el data class
        // When: Creo ThemeConfig con name vacío
        val theme = ThemeConfig(
            id = "empty-name-theme",
            name = "",
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )

        // Then: Debería crear exitosamente (sin validación actual)
        assertThat(theme.name).isEmpty()
    }

    @Test
    fun `ThemeConfig with very long name creates successfully`() {
        // When: Creo ThemeConfig con nombre muy largo
        val theme = ThemeConfig(
            id = "long-name",
            name = "a".repeat(500),
            type = ThemeType.LIGHT,
            colors = ColorScheme()
        )

        // Then: Debería crear exitosamente
        assertWithMessage("El nombre debería tener 500 caracteres")
            .that(theme.name)
            .hasLength(500)
    }

    @Test
    fun `ThemeConfig with special characters in name creates successfully`() {
        // When: Creo ThemeConfig con caracteres especiales
        val theme = ThemeConfig(
            id = "special-chars",
            name = "Tema-<>&\"'-Especial",
            type = ThemeType.PIG,
            colors = ColorScheme()
        )

        // Then: Debería crear exitosamente
        assertThat(theme.name).contains("<>&\"'")
    }

    @Test
    fun `ThemeConfig with unicode name creates successfully`() {
        // When: Creo ThemeConfig con unicode
        val theme = ThemeConfig(
            id = "unicode",
            name = "Tema 🌍 你好",
            type = ThemeType.KOALA,
            colors = ColorScheme()
        )

        // Then: Debería crear exitosamente
        assertThat(theme.name).contains("🌍")
        assertThat(theme.name).contains("你好")
    }

    // ============================================
    // Tests de integración: ThemeConfig + ColorScheme
    // ============================================

    @Test
    fun `ThemeConfig with all theme types creates successfully`() {
        // When: Creo ThemeConfigs para todos los tipos
        val themes = ThemeType.values().map { type ->
            ThemeConfig(
                id = "theme-${type.name.lowercase()}",
                name = type.name,
                type = type,
                colors = ColorScheme()
            )
        }

        // Then: Todos deberían crear exitosamente
        assertThat(themes).hasSize(6)
        themes.forEach { theme ->
            assertWithMessage("El ID no debería estar vacío")
                .that(theme.id)
                .isNotEmpty()
            
            assertWithMessage("El nombre no debería estar vacío")
                .that(theme.name)
                .isNotEmpty()
            
            assertWithMessage("ColorScheme no debería ser null")
                .that(theme.colors)
                .isNotNull()
            
            // Verificar campos específicos de ColorScheme
            assertThat(theme.colors.primary).isNotNull()
            assertThat(theme.colors.onPrimary).isNotNull()
        }
    }

    @Test
    fun `ThemeConfig with custom ColorScheme creates successfully`() {
        // Given: ColorScheme personalizado
        val colorScheme = ColorScheme(
            primary = androidx.compose.ui.graphics.Color.Red,
            secondary = androidx.compose.ui.graphics.Color.Blue
        )

        // When: Creo ThemeConfig con ColorScheme personalizado
        val theme = ThemeConfig(
            id = "custom-colors",
            name = "Custom Colors Theme",
            type = ThemeType.LIGHT,
            colors = colorScheme
        )

        // Then: Debería crear exitosamente con colores específicos
        assertThat(theme.colors.primary).isEqualTo(androidx.compose.ui.graphics.Color.Red)
        assertThat(theme.colors.secondary).isEqualTo(androidx.compose.ui.graphics.Color.Blue)
    }

    // ============================================
    // Tests de batch (creación múltiple secuencial)
    // ============================================

    @Test
    fun `creating multiple ThemeConfigs in batch does not crash`() {
        // When: Creo múltiples ThemeConfigs en secuencia
        val themes = List(100) { index ->
            ThemeConfig(
                id = "theme-$index",
                name = "Theme $index",
                type = ThemeType.values()[index % ThemeType.values().size],
                colors = ColorScheme()
            )
        }

        // Then: Ninguno debería crashar
        assertThat(themes).hasSize(100)
        themes.forEachIndexed { index, theme ->
            assertThat(theme.id).isEqualTo("theme-$index")
        }
    }

    @Test
    fun `creating multiple ThemeConfigs in batch is consistent`() {
        // When: Creo múltiples ThemeConfigs en paralelo
        val themes = List(100) { index ->
            ThemeConfig(
                id = "theme-$index",
                name = "Theme $index",
                type = ThemeType.values()[index % ThemeType.values().size],
                colors = ColorScheme()
            )
        }

        // Then: Todos deberían ser consistentes
        themes.forEach { theme ->
            assertWithMessage("El ID debería comenzar con 'theme-'")
                .that(theme.id)
                .startsWith("theme-")
            
            assertWithMessage("El nombre debería comenzar con 'Theme '")
                .that(theme.name)
                .startsWith("Theme ")
            
            assertWithMessage("El type debería ser uno de los valores válidos")
                .that(ThemeType.values())
                .contains(theme.type)
        }
    }

    // ============================================
    // Tests de rendimiento
    // ============================================

    @Test
    fun `creating 1000 ColorSchemes performance test`() {
        // When: Creo 1000 ColorSchemes
        val startTime = System.currentTimeMillis()
        val colorSchemes = List(1000) {
            ColorScheme(
                primary = androidx.compose.ui.graphics.Color(it),
                onPrimary = androidx.compose.ui.graphics.Color.White
            )
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 2000ms para CI)
        assertWithMessage("La creación de 1000 ColorSchemes debería ser rápida (< 2000ms)")
            .that(elapsed)
            .isLessThan(2000)
        
        assertThat(colorSchemes).hasSize(1000)
        
        // Verificación adicional: todos los objetos deberían ser válidos
        assertWithMessage("Todos los ColorSchemes deberían tener primary diferente de onPrimary")
            .that(colorSchemes.all { it.primary != it.onPrimary })
            .isTrue()
    }
}
