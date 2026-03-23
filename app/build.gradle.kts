plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.messageapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.messageapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "2.0-supabase"
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
}

dependencies {
    // --- AndroidX Core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // --- Coil para imágenes ---
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.6.0")

    // --- Lifecycle Compose ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // --- Emoji2 ---
    implementation("androidx.emoji2:emoji2:1.5.0")
    implementation("androidx.emoji2:emoji2-bundled:1.5.0")

    // --- Foundation ---
    implementation("androidx.compose.foundation:foundation")

    // ============================================
    // SUPABASE (Reemplaza a Firebase)
    // ============================================
    implementation(platform("io.github.jan-tennert.supabase:bom:2.1.0"))
    implementation("io.github.jan-tennert.supabase:core")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    
    // Kotlinx Serialization (requerido por Supabase)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Ktor client (requerido por Supabase)
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-utils:2.3.7")

    // ============================================
    // ONESIGNAL (Notificaciones Push)
    // ============================================
    implementation("com.onesignal:OneSignal:5.1.3")

    // ============================================
    // LIBSODIUM (Cifrado E2E AES-256-GCM)
    // ============================================
    implementation("org.libsodium:libsodium-jni:1.0.18")

    // ============================================
    // COROUTINES
    // ============================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
