package com.tatanstudios.eltuncazometapan

import android.app.Application
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.user.state.IUserStateObserver
import com.onesignal.user.state.UserChangedState
import com.tatanstudios.eltuncazometapan.extras.Notificaciones
import com.tatanstudios.eltuncazometapan.extras.PuenteNotificaciones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** App de OneSignal (no es secreta: va dentro de la app). */
private const val ONESIGNAL_APP_ID = "757d1cff-cdc8-4b18-a332-e1cd7bae3eb5"

/** Etiqueta para filtrar en Logcat: tag:TuncazoPush */
private const val TAG = "TuncazoPush"

class TuncazoApp : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
        Log.i(TAG, "OneSignal iniciado")

        // avisa cuando OneSignal confirma (o cambia) el usuario vinculado a este teléfono
        OneSignal.User.addObserver(object : IUserStateObserver {
            override fun onUserStateChange(state: UserChangedState) {
                Log.i(TAG, "Usuario OneSignal cambió: externalId='${state.current.externalId}' onesignalId='${state.current.onesignalId}'")
            }
        })

        // el código compartido vincula al cliente al iniciar o cerrar sesión
        Notificaciones.puente = object : PuenteNotificaciones {
            override fun iniciarSesion(idCliente: String) {
                Log.i(TAG, "login('$idCliente') → antes: externalId='${OneSignal.User.externalId}'")
                OneSignal.login(idCliente)
                Log.i(TAG, "login llamado. externalId='${OneSignal.User.externalId}' " +
                        "suscripción='${OneSignal.User.pushSubscription.id}' optedIn=${OneSignal.User.pushSubscription.optedIn} " +
                        "permiso=${OneSignal.Notifications.permission}")
            }

            override fun cerrarSesion() {
                Log.i(TAG, "logout()")
                OneSignal.logout()
            }

            override fun pedirPermiso() {
                // en Android 13+ muestra el permiso del sistema; si ya se respondió, no vuelve a preguntar
                scope.launch {
                    val concedido = OneSignal.Notifications.requestPermission(false)
                    Log.i(TAG, "permiso de notificaciones: $concedido")
                }
            }
        }
    }
}
