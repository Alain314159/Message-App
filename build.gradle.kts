// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    
    // ============================================
    // PLUGINS DE CALIDAD DE CÓDIGO
    // ============================================
    
    // Detekt: Análisis estático de Kotlin
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
    
    // KtLint: Formateo y verificación de estilo Kotlin
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    
    // OWASP Dependency Check: Vulnerabilidades en dependencias
    id("org.owasp.dependencycheck") version "9.0.9" apply false
}

// ============================================
// CONFIGURACIÓN GLOBAL DE DETEKT
// ============================================
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    detekt {
        toolVersion = "1.23.5"
        config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        ignoreFailures = true  // No falla el build, solo reporta
    }
}

// ============================================
// CONFIGURACIÓN DE OWASP DEPENDENCY CHECK
// ============================================
allprojects {
    apply(plugin = "org.owasp.dependencycheck")
    
    dependencyCheck {
        // Formatos de reporte
        formats = listOf("HTML", "JUNIT")
        
        // NPM (para dependencias JavaScript)
        nodeEnabled = false
        
        // Retention period (días)
        retentionDays = 30
    }
}
