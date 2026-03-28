package com.example.messageapp.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// COLORES PRINCIPALES - TEMA ROMÁNTICO
// ============================================

// Rosa Chanchita (usado para ticks de leído, mensajes enviados, accents)
val RosaChanchita = Color(0xFFFFB6C1)  // Rosa suave pero visible

// Rosa Chanchita Oscuro (para variantes)
val RosaChanchitaDark = Color(0xFFFF69B4)  // HotPink

// Gris Koala (usado para ticks de entregado, texto secundario)
val GrisKoala = Color(0xFF8E8E93)  // Gris medio

// Gris Koala Claro (para fondos suaves)
val GrisKoalaLight = Color(0xFFF2F2F7)

// Blanco Humo (fondos de pantalla)
val BlancoHumo = Color(0xFFF5F5F5)

// ============================================
// COLORES DEL TEMA (Material 3)
// ============================================

val RomanticPrimary = RosaChanchitaDark
val RomanticPrimaryVariant = Color(0xFFC71585)  // MediumVioletRed
val RomanticSecondary = Color(0xFFFFC0CB)  // Pink
val RomanticBackground = BlancoHumo
val RomanticSurface = Color.White
val RomanticError = Color(0xFFB00020)
val RomanticOnPrimary = Color.White
val RomanticOnSecondary = Color.Black
val RomanticOnBackground = Color(0xFF1C1B1F)
val RomanticOnSurface = Color(0xFF1C1B1F)
val RomanticOnError = Color.White

// ============================================
// COLORES DE TICKS DE MENSAJES
// ============================================

// Tick de ENVÍO (1 tick) - Gris Koala
val TickSentColor = GrisKoala

// Tick de ENTREGA (2 ticks grises) - Gris Koala
val TickDeliveredColor = GrisKoala

// Tick de LECTURA (2 ticks rosas) - Rosa Chanchita
val TickReadColor = RosaChanchitaDark

// ============================================
// COLORES DE ESTADOS DE PRESENCIA
// ============================================

// Online - Verde éxito
val OnlineGreen = Color(0xFF34C759)

// Offline - Gris Koala
val OfflineGray = GrisKoala

// Typing - Rosa Chanchita animado
val TypingPink = RosaChanchita

// ============================================
// COLORES PARA THEME.KT (Material 3 Defaults)
// ============================================
// Estos colores son requeridos por Theme.kt para light/dark themes

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
