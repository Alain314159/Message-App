// Configuración temporal para BuildConfig
// Esta sección debe agregarse al defaultConfig en app/build.gradle.kts

// Agregar dentro de defaultConfig { }:
/*
        // JPush AppKey - Se carga desde gradle.properties
        val jpushAppKey = project.findProperty("JPUSH_APP_KEY")?.toString() ?: "TU_JPUSH_APP_KEY_AQUI"
        manifestPlaceholders["JPUSH_APPKEY"] = jpushAppKey

        // Supabase credentials - Se cargan desde gradle.properties
        buildConfigField("String", "SUPABASE_URL", "\"\${project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"\${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
        buildConfigField("String", "JPUSH_APP_KEY", "\"\$jpushAppKey\"")
*/
