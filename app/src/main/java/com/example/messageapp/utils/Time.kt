package com.example.messageapp.utils

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utilidades para manejo de tiempo
 * ✅ Sin Firebase - Usa Long (timestamp Unix)
 */

/**
 * Convierte timestamp Unix (Long) a String formateado
 */
fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Obtiene timestamp de hace 1 día
 */
fun getTimestampOneDayAgo(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    return cal.timeInMillis
}

/**
 * Formatea timestamp relativo (hace X minutos/horas/días)
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "Ahora"
        diff < 3_600_000 -> "${diff / 60_000} min"
        diff < 86_400_000 -> "${diff / 3_600_000} h"
        diff < 604_800_000 -> "${diff / 86_400_000} d"
        else -> toFormattedDate("dd/MM/yy")
    }
}

/**
 * Verifica si el timestamp es de hoy
 */
fun Long.isToday(): Boolean {
    val now = Calendar.getInstance()
    val timestamp = Calendar.getInstance().apply { timeInMillis = this@isToday }
    
    return now.get(Calendar.YEAR) == timestamp.get(Calendar.YEAR) &&
           now.get(Calendar.DAY_OF_YEAR) == timestamp.get(Calendar.DAY_OF_YEAR)
}

/**
 * Verifica si el timestamp es de esta semana
 */
fun Long.isThisWeek(): Boolean {
    val now = Calendar.getInstance()
    val timestamp = Calendar.getInstance().apply { timeInMillis = this@isThisWeek }
    
    return now.get(Calendar.YEAR) == timestamp.get(Calendar.YEAR) &&
           now.get(Calendar.WEEK_OF_YEAR) == timestamp.get(Calendar.WEEK_OF_YEAR)
}
