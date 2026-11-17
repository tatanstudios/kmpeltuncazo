package com.tatanstudios.eltuncazometapan.extras

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

internal const val DATASTORE_FILE_NAME = "user_prefs.preferences_pb"

/** Ruta absoluta del archivo de preferencias; cada plataforma decide dónde guardarlo. */
internal expect fun dataStoreFilePath(): String

private val dataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(produceFile = { dataStoreFilePath().toPath() })
}

/**
 * Sesión del usuario guardada con DataStore multiplataforma.
 * Mismo API que la versión Android (idUsuario, saveID, deletePreferences).
 */
object TokenManager {

    private val ID_KEY = stringPreferencesKey("ID")
    private val DEDOS_KEY = stringPreferencesKey("DEDOS")
    private val TOKEN_KEY = stringPreferencesKey("TOKEN")

    /** Guarda la sesión que devuelve login/registro: el token primero y luego el id (que marca "hay sesión"). */
    suspend fun guardarSesion(id: String, token: String) {
        dataStore.edit {
            it[TOKEN_KEY] = token
            it[ID_KEY] = id
        }
        // este teléfono recibe las notificaciones de las órdenes del cliente
        Notificaciones.iniciarSesion(id)
    }

    suspend fun saveID(id: String) {
        dataStore.edit { it[ID_KEY] = id }
    }

    suspend fun saveDEDOS(id: String) {
        dataStore.edit { it[DEDOS_KEY] = id }
    }

    val idUsuario: Flow<String> = dataStore.data.map { it[ID_KEY] ?: "" }

    val idDedos: Flow<String> = dataStore.data.map { it[DEDOS_KEY] ?: "" }

    /** Token de Laravel Sanctum; se manda en cada petición como "Authorization: Bearer". */
    val token: Flow<String> = dataStore.data.map { it[TOKEN_KEY] ?: "" }

    suspend fun deletePreferences() {
        dataStore.edit {
            it.remove(ID_KEY)
            it.remove(DEDOS_KEY)
            it.remove(TOKEN_KEY)
        }
        Notificaciones.cerrarSesion()
        EstadoCarrito.cantidad.value = 0
    }
}

/** Envuelve un resultado para consumirlo una sola vez (respuestas de red). */
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? =
        if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }

    fun peekContent(): T = content
}
