package com.tatanstudios.eltuncazometapan

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.tatanstudios.eltuncazometapan.extras.Notificaciones
import com.tatanstudios.eltuncazometapan.extras.PuenteNotificaciones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** App de OneSignal (no es secreta: va dentro de la app). */
private const val ONESIGNAL_APP_ID = "757d1cff-cdc8-4b18-a332-e1cd7bae3eb5"

class TuncazoApp : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.WARN
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        // el código compartido vincula al cliente al iniciar o cerrar sesión
        Notificaciones.puente = object : PuenteNotificaciones {
            override fun iniciarSesion(idCliente: String) = OneSignal.login(idCliente)
            override fun cerrarSesion() = OneSignal.logout()
            override fun pedirPermiso() {
                // en Android 13+ muestra el permiso del sistema; si ya se respondió, no vuelve a preguntar
                scope.launch { OneSignal.Notifications.requestPermission(false) }
            }
        }
    }
}
