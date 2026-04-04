plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Requerido para Supabase (Kotlin Serialization) - versión compatible con Kotlin 1.9.22
    kotlin("plugin.serialization") version "1.9.22"
    // Requerido para Room Database (KSP - Kotlin Symbol Processing)
    id("com.google.devtools.ksp") version "1.9.22-1.0.18"
    // Firebase Cloud Messaging - Google Services Plugin
    id("com.google.gms.google-services")

    // Plugins de calidad de código
    // Habilitados para análisis estático automático
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

android {
    namespace = "com.example.messageapp"
    compileSdk = 35  // Cambiado a 35 (estable, sin warnings)

    defaultConfig {
        applicationId = "com.example.messageapp"
        minSdk = 26
        targetSdk = 35  // Cambiado a 35 (estable)
        versionCode = 1
        versionName = "2.5-supabase-fcm" // Versión con Supabase + FCM

        // Validación de credenciales de Supabase en build time
        val supabaseUrl = project.findProperty("SUPABASE_URL") as String?
        val supabaseKey = project.findProperty("SUPABASE_ANON_KEY") as String?

        if (supabaseUrl.isNullOrBlank()) {
            throw GradleException(
                "SUPABASE_URL no está configurada.\n" +
                "1. Copia gradle.properties.example a gradle.properties\n" +
                "2. Agrega tu URL de Supabase desde: https://supabase.com/dashboard → Settings → API"
            )
        }

        if (supabaseKey.isNullOrBlank()) {
            throw GradleException(
                "SUPABASE_ANON_KEY no está configurada.\n" +
                "1. Copia gradle.properties.example a gradle.properties\n" +
                "2. Agrega tu Anon Key desde: https://supabase.com/dashboard → Settings → API"
            )
        }

        // Supabase credentials - Se cargan desde gradle.properties (BUILD CONFIG)
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"  // Compatible con Kotlin 2.1.0
    }

    lint {
        abortOnError = true  // ✅ Abortar si hay errores de lint
        checkReleaseBuilds = true  // ✅ Checkear builds release
        // baseline = file("lint-baseline.xml")  // Comentado hasta crear el archivo
        xmlReport = true
        htmlReport = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = false  // ✅ Fallar si hay mocks sin configurar (NO devolver valores por defecto)
            
            // Configuración de reportes para GitHub Actions
            all {
                it.useJUnitPlatform()
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                    showExceptions = true
                    showCauses = true
                    showStackTraces = true
                    showStandardStreams = true
                }
                it.reports {
                    junitXml.required.set(true)  // ✅ Genera XML para GitHub Actions
                    junitXml.outputLocation.set(file("$buildDir/test-results/test"))
                    html.required.set(true)  // ✅ Genera HTML para visualización
                    html.outputLocation.set(file("$buildDir/reports/tests/test"))
                }
            }
        }
    }

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
    // AndroidX Core (desde Version Catalog)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.bundled)

    // Testing
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("io.mockk:mockk-android:1.13.12")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.robolectric:robolectric:4.12.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    // ============================================
    // SUPABASE - Versión estable Marzo 2026 (3.4.1)
    // Documentación: https://github.com/supabase-community/supabase-kt
    // Group ID actualizado: io.github.jan-tennert.supabase
    // ============================================
    implementation(platform("io.github.jan-tennert.supabase:bom:3.4.1"))
    implementation("io.github.jan-tennert.supabase:supabase-kt:3.4.1")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.4.1")  // Autenticación (antes gotrue-kt)
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.4.1")  // Base de datos
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.4.1")  // Tiempo real
    implementation("io.github.jan-tennert.supabase:storage-kt:3.4.1")  // Storage

    // ============================================
    // KTOR - Versión 3.x compatible con Supabase 3.4.1
    // Documentación: https://ktor.io/docs/welcome.html
    // ============================================
    implementation("io.ktor:ktor-client-android:3.3.0")
    implementation("io.ktor:ktor-client-core:3.3.0")
    implementation("io.ktor:ktor-utils:3.3.0")
    implementation("io.ktor:ktor-client-plugins:3.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")

    // Kotlinx Serialization - Compatible con Kotlin 2.1.0
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ============================================
    // FIREBASE CLOUD MESSAGING - Migración desde JPush (Marzo 2026)
    // Documentación: https://firebase.google.com/docs/cloud-messaging/android/client
    // NOTA: Se usan versiones explícitas para evitar errores de resolución en CI
    // ============================================
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation("com.google.firebase:firebase-analytics-ktx:22.3.0")

    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.5.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")

    // ============================================
    // ROOM DATABASE - Base de datos local
    // ============================================
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
