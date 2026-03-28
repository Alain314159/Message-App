package com.example.messageapp.utils

import android.util.Base64
import android.util.Log

private const val TAG = "MessageApp"

object Crypto {

    fun encrypt(plain: String): String =
        if (plain.isEmpty()) "" else Base64.encodeToString(plain.toByteArray(), Base64.NO_WRAP)

    fun decrypt(enc: String?): String {
        if (enc.isNullOrBlank()) return ""

        val s = enc.trim()

        if (s.startsWith("gcm:", ignoreCase = true)) {
            return "[Criptografia Antiga]"
        }

        return try {
            String(Base64.decode(s, Base64.NO_WRAP))
        } catch (e: Throwable) {
            Log.w(TAG, "Crypto: Falló decodificación Base64, retornando original", e)
            s
        }
    }
}
