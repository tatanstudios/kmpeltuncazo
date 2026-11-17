package com.tatanstudios.eltuncazometapan.componentes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.navigation.NavController
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.resources.*
import org.jetbrains.compose.resources.stringResource

/*
 * La app se puede usar sin sesión para ver el menú y los productos.
 * Agregar al carrito, ver el carrito, las órdenes o el perfil pide iniciar sesión.
 */

/** true si hay un cliente con sesión iniciada. Se actualiza solo al iniciar o cerrar sesión. */
@Composable
fun haySesion(): Boolean {
    val idusuario by TokenManager.idUsuario.collectAsState(initial = null)
    // mientras se lee DataStore (null) se asume que hay sesión, para no mostrar el aviso por error
    return idusuario == null || idusuario!!.isNotEmpty()
}

/** Abre el login encima de la pantalla actual; al iniciar sesión se regresa a ella. */
fun navegarALogin(navController: NavController) {
    navController.navigate(Routes.VistaLogin.route) {
        launchSingleTop = true
    }
}

/** Vuelve al menú principal dejando una pila de navegación limpia (cerrar sesión, login sin pantalla previa). */
fun navegarAlMenuPrincipal(navController: NavController) {
    navController.navigate(Routes.VistaPrincipal.createRoute("menu")) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

/** Aviso con "Ahora no" / "Iniciar sesión". */
@Composable
fun ModalIniciarSesion(
    showDialog: Boolean,
    mensaje: String,
    navController: NavController,
    onDismiss: () -> Unit
) {
    CustomModal2Botones(
        showDialog = showDialog,
        message = mensaje,
        onDismiss = onDismiss,
        onAccept = {
            onDismiss()
            navegarALogin(navController)
        },
        txtBtnAceptar = stringResource(Res.string.iniciar_sesion),
        txtBtnCancelar = stringResource(Res.string.ahora_no)
    )
}


/**
 * Avisa cuando el servidor rechaza el token (401) en cualquier petición.
 * Lo dispara ApiService y lo muestra [AvisoSesionTerminada] en la raíz de la navegación,
 * así ningún botón queda "sin hacer nada" si la sesión ya no es válida.
 */
object SesionEventos {
    private val _sesionTerminada = MutableStateFlow(false)
    val sesionTerminada = _sesionTerminada.asStateFlow()

    fun avisarSesionTerminada() { _sesionTerminada.value = true }
    fun ocultar() { _sesionTerminada.value = false }
}

@Composable
fun AvisoSesionTerminada(navController: NavController) {
    val mostrar by SesionEventos.sesionTerminada.collectAsState()
    ModalIniciarSesion(
        showDialog = mostrar,
        mensaje = stringResource(Res.string.sesion_terminada),
        navController = navController,
        onDismiss = { SesionEventos.ocultar() }
    )
}
