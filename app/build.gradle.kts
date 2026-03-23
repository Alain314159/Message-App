plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Requerido para Supabase (Kotlin Serialization)
    kotlin("plugin.serialization") version "1.9.21"
    
    // Plugins de calidad de código (Versiones Marzo 2026)
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

android {
    namespace = "com.example.messageapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.messageapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "2.1-supabase-fixed"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21  // Actualizado a Java 21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { 
        jvmTarget = "21"  // Actualizado a Java 21
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
    
    // Habilitar buildConfig para variables de entorno
    buildFeatures {
        buildConfig = true
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    // Lint configuration
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        baseline = file("lint-baseline.xml")
        xmlReport = true
        htmlReport = true
    }
    
    // Test options
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    
    // Packaging options
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    // ============================================
    // ANDROIDX CORE
    // ============================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ============================================
    // NAVIGATION & UI
    // ============================================
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.foundation:foundation")

    // ============================================
    // IMAGE LOADING
    // ============================================
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")

    // ============================================
    // LIFECYCLE
    // ============================================
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ============================================
    // EMOJI
    // ============================================
    implementation("androidx.emoji2:emoji2:1.5.0")
    implementation("androidx.emoji2:emoji2-bundled:1.5.0")

    // ============================================
    // SUPABASE (Reemplaza a Firebase)
    // ============================================
    // Documentación: https://github.com/supabase-community/supabase-kt
    // Versión: 3.4.1 (Marzo 2026 - ÚLTIMA ESTABLE)
    // Nota: Antes de 3.0.0 se llamaba "gotrue-kt", ahora es "auth-kt"
    // Incluye: Auth, Database, Realtime, Storage (para multimedia)
    
    implementation(platform("io.github.jan.supabase:bom:3.4.1"))
    implementation("io.github.jan.supabase:supabase-kt")
    implementation("io.github.jan.supabase:auth-kt")        // Autenticación
    implementation("io.github.jan.supabase:postgrest-kt")   // Base de datos
    implementation("io.github.jan.supabase:realtime-kt")    // WebSockets
    implementation("io.github.jan.supabase:storage-kt")     // Multimedia (fotos/videos/audios)
    
    // Ktor client (requerido por Supabase 3.x - requiere Ktor 3.x)
    implementation("io.ktor:ktor-client-android:3.3.0")
    implementation("io.ktor:ktor-client-core:3.3.0")
    implementation("io.ktor:ktor-utils:3.3.0")
    implementation("io.ktor:ktor-client-plugins-http-timeout:3.3.0")

    // Kotlinx Serialization (requerido por Supabase)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // ============================================
    // ONE SIGNAL (Notificaciones Push)
    // ============================================
    // Documentación: https://documentation.onesignal.com/docs/en/android-sdk-setup
    // Versión: 5.7.3 (Marzo 2026 - ÚLTIMA, estable)
    // Nota: 5.7.0 tiene bugs, usar 5.7.2+ o 5.6.2
    
    implementation("com.onesignal:OneSignal:5.7.3")

    // ============================================
    // GOOGLE SIGN IN (OAuth)
    // ============================================
    // Para login con Google + Credential Manager
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.5.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // ============================================
    // COROUTINES
    // ============================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")
}

// ============================================
// CONFIGURACIÓN DE DETEKT PARA EL MÓDULO APP
// ============================================
detekt {
    config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    ignoreFailures = true
    basePath = rootProject.projectDir.absolutePath
    
    reports {
        html.enabled = true
        xml.enabled = true
        txt.enabled = true
        sarif.enabled = true
    }
}

// ============================================
// CONFIGURACIÓN DE KTLINT
// ============================================
ktlint {
    android = true
    outputToConsole = true
    ignoreFailures = true
    enableExperimentalRules = false
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
