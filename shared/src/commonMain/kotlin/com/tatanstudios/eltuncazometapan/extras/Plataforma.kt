package com.tatanstudios.eltuncazometapan.extras

import coil3.PlatformContext
import kotlin.math.abs
import kotlin.math.roundToLong

/** Versión de la app instalada (versionName en Android, CFBundleShortVersionString en iOS). */
expect fun getVersionName(context: PlatformContext): String

/** Abre los ajustes del sistema para esta app (permisos). */
expect fun redireccionarAjustes(context: PlatformContext)

/**
 * Limpieza única heredada de la app Android anterior (SharedPreferences → DataStore).
 * En iOS no hace nada.
 */
expect suspend fun migrarPreferenciasAntiguas()

/** Reemplazo multiplataforma de String.format(Locale.US, "%.2f", monto). */
fun formatoDosDecimales(monto: Double): String {
    val centavos = (abs(monto) * 100).roundToLong()
    val signo = if (monto < 0 && centavos != 0L) "-" else ""
    val decimales = (centavos % 100).toString().padStart(2, '0')
    return "$signo${centavos / 100}.$decimales"
}
