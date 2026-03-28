plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Requerido para Supabase (Kotlin Serialization) - versión compatible con Kotlin 2.1.0
    kotlin("plugin.serialization") version "2.1.0"
    // Requerido para Room Database (KSP - Kotlin Symbol Processing)
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"

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
        versionName = "2.3-jpush" // Versión con JPush y avatares

        // JPush AppKey - Se carga desde gradle.properties
        val jpushAppKey = project.findProperty("JPUSH_APP_KEY")?.toString() ?: "TU_JPUSH_APP_KEY_AQUI"
        manifestPlaceholders["JPUSH_APPKEY"] = jpushAppKey

        // Supabase credentials - Se cargan desde gradle.properties (BUILD CONFIG)
        buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
        buildConfigField("String", "JPUSH_APP_KEY", "\"$jpushAppKey\"")

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
        kotlinCompilerExtensionVersion = "1.5.10"  // Compatible con Kotlin 2.1.0
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        // baseline = file("lint-baseline.xml")  // Comentado hasta crear el archivo
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
    // KTOR - Versión compatible con Supabase 3.4.1
    // NOTA: Ktor 3.x no existe, usar versión 2.x estable
    // ============================================
    implementation("io.ktor:ktor-client-android:2.3.13")
    implementation("io.ktor:ktor-client-core:2.3.13")
    implementation("io.ktor:ktor-utils:2.3.13")
    implementation("io.ktor:ktor-client-plugins:2.3.13")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.13")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.13")

    // Kotlinx Serialization - Compatible con Kotlin 2.1.0
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ============================================
    // JPUSH - COMENTADO TEMPORALMENTE (dependencia no disponible)
    // La versión 4.3.8 no existe en repositorios Maven
    // ============================================
    // implementation("cn.jiguang.jpush:jpush:4.3.8")

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

// Plugins de calidad de código comentados - los plugins no están aplicados
// Descomentar solo cuando se necesiten y los plugins estén configurados
/*
detekt {
    // Usar configuración minimal que permite backticks en tests
    config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt-minimal.yml"))
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    ignoreFailures = false  // Fallar si hay issues
    basePath = rootProject.projectDir.absolutePath

    reports {
        html.enabled = true
        xml.enabled = true
        txt.enabled = true
        sarif.enabled = true
    }
}

/*
 * BLOQUE COMENTADO - API de ktlint cambió en v12.0.0+
 * El filtro 'generated' ya no existe
 * 
 * Si necesitas ktlint en el futuro, usar:
 * ktlint {
 *     android = true
 *     outputToConsole = true
 *     ignoreFailures = true
 *     filter {
 *         exclude { element -> element.file.path.contains("generated") }
 *     }
 * }
 */
/*
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
*/
