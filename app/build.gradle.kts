plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Requerido para Supabase (Kotlin Serialization)
    kotlin("plugin.serialization") version "1.9.21"
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    
    // Habilitar buildConfig para variables de entorno (opcional)
    // buildFeatures {
    //     buildConfig = true
    // }
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
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ============================================
    // NAVIGATION & UI
    // ============================================
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("androidx.compose.foundation:foundation")

    // ============================================
    // IMAGE LOADING
    // ============================================
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.6.0")

    // ============================================
    // LIFECYCLE
    // ============================================
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // ============================================
    // EMOJI
    // ============================================
    implementation("androidx.emoji2:emoji2:1.5.0")
    implementation("androidx.emoji2:emoji2-bundled:1.5.0")

    // ============================================
    // SUPABASE (Reemplaza a Firebase)
    // ============================================
    // Documentación: https://github.com/supabase-community/supabase-kt
    // Versión: 2.x (2024-2025)
    
    implementation(platform("io.github.jan.supabase:bom:2.1.0"))
    implementation("io.github.jan.supabase:supabase-kt")
    implementation("io.github.jan.supabase:gotrue-kt")      // Autenticación
    implementation("io.github.jan.supabase:postgrest-kt")   // Base de datos
    implementation("io.github.jan.supabase:realtime-kt")    // WebSockets
    implementation("io.github.jan.supabase:storage-kt")     // Storage (opcional)
    
    // Ktor client (requerido por Supabase)
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-utils:2.3.7")
    implementation("io.ktor:ktor-client-plugins-http-timeout:2.3.7")

    // Kotlinx Serialization (requerido por Supabase)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // ============================================
    // ONE SIGNAL (Notificaciones Push)
    // ============================================
    // Documentación: https://documentation.onesignal.com/docs/en/android-sdk-setup
    // Versión: 5.6.1+ (2024-2025)
    
    implementation("com.onesignal:OneSignal:5.6.1")

    // ============================================
    // COROUTINES
    // ============================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
