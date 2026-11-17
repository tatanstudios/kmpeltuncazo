package com.tatanstudios.eltuncazometapan.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

private data class ToastData(val id: Long, val message: String, val type: ToastType)

private object ToastManager {
    private var contador = 0L
    private val _actual = MutableStateFlow<ToastData?>(null)
    val actual = _actual.asStateFlow()

    fun mostrar(message: String, type: ToastType) {
        _actual.value = ToastData(++contador, message, type)
    }

    fun ocultar(id: Long) {
        if (_actual.value?.id == id) _actual.value = null
    }
}

/**
 * Reemplazo multiplataforma de Toasty. Se mantiene la firma original
 * (el contexto ya no es necesario, pero se acepta para no tocar las pantallas).
 */
@Suppress("UNUSED_PARAMETER")
fun CustomToasty(context: Any?, message: String, type: ToastType) {
    ToastManager.mostrar(message, type)
}

/** Debe colocarse una vez en la raíz de la app (ver App.kt). */
@Composable
fun ToastHost(modifier: Modifier = Modifier) {
    val toast by ToastManager.actual.collectAsState()
    var visible by remember { mutableStateOf<ToastData?>(null) }

    LaunchedEffect(toast) {
        val t = toast ?: return@LaunchedEffect
        visible = t
        delay(2_200)
        ToastManager.ocultar(t.id)
    }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.navigationBarsPadding().padding(bottom = 64.dp, start = 24.dp, end = 24.dp),
        ) {
            visible?.let { ToastContenido(it) }
        }
    }
}

@Composable
private fun ToastContenido(data: ToastData) {
    val (color, icono) = when (data.type) {
        ToastType.SUCCESS -> Color(0xFF388E3C) to Icons.Filled.CheckCircle
        ToastType.ERROR -> Color(0xFFD32F2F) to Icons.Filled.Error
        ToastType.INFO -> Color(0xFF3F51B5) to Icons.Filled.Info
        ToastType.WARNING -> Color(0xFFFFA900) to Icons.Filled.Warning
    }
    val forma = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .shadow(6.dp, forma)
            .background(color, forma)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(data.message, color = Color.White, fontSize = 14.sp)
    }
}
