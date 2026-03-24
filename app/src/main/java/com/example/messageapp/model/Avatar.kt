package com.example.messageapp.model

/**
 * Tipos de avatares disponibles para el usuario
 */
enum class AvatarType(val id: String, val displayName: String, val drawableResId: Int) {
    CERDITA(
        id = "cerdita",
        displayName = "Cerdita",
        drawableResId = com.example.messageapp.R.drawable.avatar_cerdita
    ),
    KOALA(
        id = "koala",
        displayName = "Koala",
        drawableResId = com.example.messageapp.R.drawable.avatar_koala  // Se agregará cuando tengas la imagen
    );

    companion object {
        /**
         * Obtiene el AvatarType desde un ID de string
         */
        fun fromId(id: String): AvatarType {
            return entries.find { it.id == id } ?: CERDITA
        }

        /**
         * Lista todos los avatares disponibles
         */
        fun getAll(): List<AvatarType> = entries.toList()
    }
}
