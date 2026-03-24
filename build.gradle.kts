// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Plugins de calidad de código (opcional - deshabilitado para build rápido)
    // id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    // id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
    // id("org.owasp.dependencycheck") version "12.2.0" apply false
}

// La configuración de calidad de código está comentada para builds más rápidos
// Descomentar cuando se necesite análisis de código
