package com.example.messageapp.supabase

import android.util.Log
import com.example.messageapp.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Configuración de Supabase - Cliente singleton
 *
 * Inicializa el cliente de Supabase con las credenciales de BuildConfig
 * y los plugins necesarios para la aplicación.
 *
 * Plugins configurados:
 * - Auth: Autenticación de usuarios
 * - Postgrest: Base de datos PostgreSQL
 * - Realtime: WebSockets para actualizaciones en tiempo real
 * - Storage: Almacenamiento de archivos (imágenes, avatares)
 *
 * Uso:
 * ```kotlin
 * val client = SupabaseConfig.client
 * val auth = client.plugin(Auth)
 * val db = client.plugin(Postgrest)
 * ```
 */
object SupabaseConfig {

    private const val TAG = "MessageApp"

    /**
     * Cliente de Supabase configurado y listo para usar
     *
     * Se inicializa una única vez cuando se accede por primera vez.
     * Las credenciales se cargan desde BuildConfig (gradle.properties).
     */
    val client by lazy {
        Log.d(TAG, "SupabaseConfig: Inicializando cliente Supabase")
        
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY

        if (url.isBlank() || key.isBlank()) {
            Log.e(TAG, "SupabaseConfig: Credenciales vacías - verifica gradle.properties")
            error("Supabase credentials cannot be empty")
        }

        Log.d(TAG, "SupabaseConfig: URL configurada correctamente")

        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Postgrest)
            install(Auth)
            install(Realtime)
            install(Storage)
        }
    }
}
