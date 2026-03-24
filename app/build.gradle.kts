plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Requerido para Supabase (Kotlin Serialization)
    kotlin("plugin.serialization") version "2.1.0"

    // Plugins de calidad de código (Versiones Marzo 2026)
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
        versionName = "2.3-jpush" // Versión con JPush y avatares

        // JPush AppKey - Reemplaza con tu AppKey de JPush
        manifestPlaceholders["JPUSH_APPKEY"] = "TU_JPUSH_APP_KEY_AQUI"

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
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
        kotlinCompilerExtensionVersion = "1.5.8"  // Compatible con Kotlin 2.1.0
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        baseline = file("lint-baseline.xml")
        xmlReport = true
        htmlReport = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
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
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
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
    // SUPABASE - Versión estable en JitPack (Marzo 2026)
    // Documentación: https://github.com/supabase-community/supabase-kt
    // ============================================
    implementation(platform("io.github.jan.supabase:bom:2.1.0"))
    implementation("io.github.jan.supabase:supabase-kt:2.1.0")
    implementation("io.github.jan.supabase:gotrue-kt:2.1.0")  // Autenticación
    implementation("io.github.jan.supabase:postgrest-kt:2.1.0")  // Base de datos
    implementation("io.github.jan.supabase:realtime-kt:2.1.0")  // Tiempo real
    implementation("io.github.jan.supabase:storage-kt:2.1.0")  // Storage

    // ============================================
    // KTOR - Versión compatible con Supabase 2.x
    // ============================================
    implementation("io.ktor:ktor-client-android:2.3.13")
    implementation("io.ktor:ktor-client-core:2.3.13")
    implementation("io.ktor:ktor-utils:2.3.13")
    implementation("io.ktor:ktor-client-plugins:2.3.13")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ============================================
    // JPUSH (Aurora Mobile) - GRÁTIS y funciona desde Cuba
    // Repositorio: https://maven.aliyun.com/repository/jcenter
    // ============================================
    implementation("cn.jiguang.jpush:jpush:4.3.9")

    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.5.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")
}

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
