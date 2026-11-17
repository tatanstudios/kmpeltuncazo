package com.tatanstudios.eltuncazometapan.extras

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import coil3.PlatformContext

/** Contexto de aplicación, inicializado desde MainActivity con [AndroidPlatform.init]. */
@SuppressLint("StaticFieldLeak")
object AndroidPlatform {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

/** Misma ubicación que usaba `preferencesDataStore(name = "user_prefs")`: se conserva la sesión. */
internal actual fun dataStoreFilePath(): String =
    AndroidPlatform.appContext.filesDir.resolve("datastore/$DATASTORE_FILE_NAME").absolutePath

actual fun getVersionName(context: PlatformContext): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "N/A"
} catch (_: Exception) {
    "N/A"
}

actual fun redireccionarAjustes(context: PlatformContext) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

actual suspend fun migrarPreferenciasAntiguas() {
    val sharedPrefs = AndroidPlatform.appContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    if (!sharedPrefs.getBoolean("hasMigratedToDataStore", false)) {
        TokenManager.deletePreferences()
        sharedPrefs.edit().putBoolean("hasMigratedToDataStore", true).apply()
    }
}
