package com.example.messageapp.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.example.messageapp.BuildConfig

/**
 * Configuración optimizada de Coil para caching de imágenes
 *
 * Responsabilidad única: Proporcionar un ImageLoader con caching configurado
 *
 * Features:
 * - Memory cache para imágenes frecuentes
 * - Disk cache para persistencia entre sesiones
 * - Cache policy optimizado para rendimiento
 * - Logging solo en modo debug
 *
 * Uso:
 * ```kotlin
 * val imageLoader = ImageLoaderConfig.create(context)
 * AsyncImage(model = url, imageLoader = imageLoader, contentDescription = null)
 * ```
 */
object ImageLoaderConfig {

    /**
     * Crea un ImageLoader con configuración optimizada
     *
     * @param context Contexto de la aplicación
     * @return ImageLoader configurado con caching
     */
    fun create(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            // Memory cache - 25% del heap disponible
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 25% del heap
                    .build()
            }

            // Disk cache - 100MB máximo
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100MB
                    .build()
            }

            // Respetar cache headers de red
            .respectCacheHeaders(false) // Ignorar cache headers para mejor performance

            // Logging solo en debug
            .logger(if (BuildConfig.DEBUG) DebugLogger() else null)

            .build()
    }

    /**
     * Crea un ImageLoader para avatares/imágenes pequeñas
     *
     * Cache más agresivo para imágenes que se usan frecuentemente
     */
    fun createForAvatars(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.10) // 10% para avatares
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_avatar_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true) // Animación suave para avatares
            .build()
    }

    /**
     * Crea un ImageLoader para imágenes de chat (optimizado para velocidad)
     *
     * Prioriza velocidad sobre calidad para mejor UX en chats
     */
    fun createForChatImages(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.30) // 30% para chat (más agresivo)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_chat_cache"))
                    .maxSizeBytes(150 * 1024 * 1024) // 150MB
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(false) // Sin animación para mejor performance
            .build()
    }
}
