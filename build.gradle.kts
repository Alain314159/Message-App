// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    
    // ============================================
    // PLUGINS DE CALIDAD DE CÓDIGO (Versiones Marzo 2026)
    // ============================================
    
    // Detekt: Análisis estático de Kotlin - Versión estable más reciente
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    
    // KtLint: Formateo y verificación de estilo Kotlin - Versión más reciente
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
    
    // OWASP Dependency Check: Vulnerabilidades en dependencias - Versión más reciente
    id("org.owasp.dependencycheck") version "12.2.0" apply false
}

// ============================================
// CONFIGURACIÓN GLOBAL DE DETEKT
// ============================================
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    detekt {
        toolVersion = "1.23.8"  // Versión estable más reciente (Feb 2025)
        config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        ignoreFailures = true  // No falla el build, solo reporta
        basePath = rootProject.projectDir.absolutePath
    }
}

// ============================================
// CONFIGURACIÓN DE OWASP DEPENDENCY CHECK
// ============================================
allprojects {
    apply(plugin = "org.owasp.dependencycheck")
    
    dependencyCheck {
        // Formatos de reporte
        formats = listOf("HTML", "JUNIT", "JSON")
        
        // NPM (para dependencias JavaScript)
        nodeEnabled = false
        
        // Retention period (días)
        retentionDays = 30
        
        // Fallar solo en vulnerabilidades críticas (CVSS >= 7.0)
        failBuildOnCVSS = 7.0
        
        // Supresiones para falsos positivos
        suppressionFile = "${rootProject.projectDir}/config/dependency-check/suppressions.xml"
        
        // Analizar solo tipos relevantes para Android
        analyzedTypes = listOf("jar", "aar")
        
        // Auto-actualización de base de datos NVD
        autoUpdate = true
        
        // Directorio de datos NVD
        dataDirectory = file("${rootProject.projectDir}/build/dependency-check-data")
    }
}
