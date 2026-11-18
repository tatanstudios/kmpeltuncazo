package com.tatanstudios.eltuncazometapan.extras

/**
 * Notificaciones push con OneSignal.
 *
 * El SDK de OneSignal se inicia en cada plataforma (Android: TuncazoApp, iOS: iOSApp.swift)
 * y se registra aquí con [puente]. El código compartido solo avisa cuándo hay sesión:
 * el cliente queda vinculado en OneSignal con su id (external_id), y el servidor
 * le manda notificaciones usando ese mismo id. Así no hay que guardar tokens en la base de datos.
 */
interface PuenteNotificaciones {
    /** Vincula este teléfono con el cliente (OneSignal.login). */
    fun iniciarSesion(idCliente: String)

    /** Desvincula el teléfono: deja de recibir notificaciones de ese cliente (OneSignal.logout). */
    fun cerrarSesion()

    /** Pide permiso para mostrar notificaciones (solo pregunta si aún no se ha respondido). */
    fun pedirPermiso()
}

object Notificaciones {

    /**
     * External ID que se registra en OneSignal. Lleva prefijo porque OneSignal rechaza en silencio
     * valores genéricos como "0", "1" o "-1" (el cliente 1 nunca quedaba vinculado).
     * El servidor (App\Services\Notificaciones) usa el mismo formato.
     */
    fun externalId(idCliente: String) = "cliente_$idCliente"

    /** Lo asigna cada plataforma al iniciar la app. Si es null (ej. pruebas), no se hace nada. */
    var puente: PuenteNotificaciones? = null

    fun iniciarSesion(idCliente: String) {
        if (idCliente.isBlank()) {
            println("TuncazoPush: sin sesión, no se vincula el teléfono")
            return
        }
        if (puente == null) {
            println("TuncazoPush: puente es null, OneSignal no está registrado en esta plataforma")
            return
        }
        runCatching {
            puente?.iniciarSesion(externalId(idCliente))
            puente?.pedirPermiso()
        }.onFailure { println("TuncazoPush: error al vincular cliente $idCliente: $it") }
    }

    fun cerrarSesion() {
        runCatching { puente?.cerrarSesion() }
            .onFailure { println("TuncazoPush: error al cerrar sesión: $it") }
    }
}
