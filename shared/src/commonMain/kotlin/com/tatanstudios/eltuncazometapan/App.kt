package com.tatanstudios.eltuncazometapan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.componentes.ToastHost
import com.tatanstudios.eltuncazometapan.vistas.login.AppNavigation

/** Punto de entrada común: lo llaman MainActivity (Android) y MainViewController (iOS). */
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }

    Box(Modifier.fillMaxSize()) {
        AppNavigation()
        ToastHost()
    }
}
