package com.tatanstudios.eltuncazometapan.vistas.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.savedstate.read
import androidx.navigation.navArgument
import com.tatanstudios.eltuncazometapan.componentes.RobotoMediumFont
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.extras.Notificaciones
import com.tatanstudios.eltuncazometapan.componentes.AvisoSesionTerminada
import com.tatanstudios.eltuncazometapan.extras.migrarPreferenciasAntiguas
import com.tatanstudios.eltuncazometapan.vistas.opciones.direcciones.MisDireccionesScreen
import com.tatanstudios.eltuncazometapan.vistas.opciones.direcciones.RegistrarNuevaDireccionScreen
import com.tatanstudios.eltuncazometapan.vistas.opciones.direcciones.SeleccionarDireccionScreen
import com.tatanstudios.eltuncazometapan.vistas.opciones.password.ActualizarPasswordScreen
import com.tatanstudios.eltuncazometapan.vistas.opciones.perfil.PerfilScreen
import com.tatanstudios.eltuncazometapan.vistas.opciones.correo.CorreoRecuperacionScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.PrincipalScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.carrito.CarritoComprasScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.carrito.EditarProductoScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.ordenes.EstadoOrdenScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.productos.ElegirProductoScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.productos.EnviarOrdenScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.productos.ListadoProductosDeUnaOrdenScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.productos.ListadoProductosScreen


// *** RUTAS DE NAVEGACION ***
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Antes de mostrar el menú se migran las preferencias de versiones anteriores (tarda milisegundos).
    // Así el menú no lee una sesión vieja que el servidor rechazaría.
    var preferenciasListas by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        migrarPreferenciasAntiguas()
        // una sesión guardada sin token es de una versión anterior de la app (antes de Sanctum):
        // se borra y el cliente iniciará sesión de nuevo cuando quiera ordenar
        if (TokenManager.idUsuario.first().isNotEmpty() && TokenManager.token.first().isEmpty()) {
            TokenManager.deletePreferences()
        }
        // si ya había sesión (ej. se instaló la versión con notificaciones), se vincula el teléfono
        Notificaciones.iniciarSesion(TokenManager.idUsuario.first())
        preferenciasListas = true
    }

    if (!preferenciasListas) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White))
        return
    }

    // si el servidor rechaza la sesión en cualquier pantalla, se avisa y se ofrece iniciar sesión
    AvisoSesionTerminada(navController)

    // La app abre directo en el menú: sin sesión se pueden ver los productos y el login se pide
    // al agregar a la orden, abrir el carrito, las órdenes o el perfil.
    NavHost(navController = navController, startDestination = Routes.VistaPrincipal.route) {

        // LOGIN
        composable(Routes.VistaLogin.route) {
            LoginScreen(navController)
        }

        // REGISTRO
        composable(Routes.VistaRegistro.route) {
            RegistroScreen(navController)
        }

        // RECUPERAR CONTRASEÑA (código por correo)
        composable(Routes.VistaRecuperarPassword.route) {
            RecuperarPasswordScreen(navController)
        }

        // CORREO DE RECUPERACION (perfil)
        composable(Routes.VistaCorreo.route) { CorreoRecuperacionScreen(navController) }

        // PRINCIPAL
        composable(
            route = Routes.VistaPrincipal.route, // "principal/{selectedScreenVar}"
            arguments = listOf(
                navArgument("selectedScreenVar") {
                    type = NavType.StringType
                    defaultValue = "menu"
                }
            )
        ) { backStackEntry ->
            val selected = backStackEntry.arguments?.read { getStringOrNull("selectedScreenVar") } ?: "menu"

            PrincipalScreen(
                navController = navController,
                selectedScreenVar = selected
            )
        }

        // VISTA OPCIONES PERFIL
        composable(Routes.VistaPerfil.route) { PerfilScreen(navController) }

        // VISTA ACTUALIZAR CONTRASENA
        composable(Routes.VistaActualizarContrasena.route) { ActualizarPasswordScreen(navController) }

        // VISTA MIS DIRECCIONES
        composable(
            route = Routes.VistaMisDirecciones.route,
            arguments = listOf(
                navArgument("estadoBoton") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val estadoBoton = backStackEntry.arguments?.read { getIntOrNull("estadoBoton") } ?: 0

            MisDireccionesScreen(
                navController = navController,
                estadoBotonAtras = estadoBoton
            )
        }


        // VISTA MAPA

        // VISTA REGISTRAR NUEVA DIRECCION
        composable(Routes.VistaRegistroDireccion.route) { backStackEntry ->

            val id = backStackEntry.arguments?.read { getStringOrNull("id") }?.toIntOrNull() ?: 0
            val latitud = backStackEntry.arguments?.read { getStringOrNull("latitud") } ?: "0"
            val longitud = backStackEntry.arguments?.read { getStringOrNull("longitud") } ?: "0"
            val latitudrealStr = backStackEntry.arguments?.read { getStringOrNull("latitudreal") }
            val longitudrealStr = backStackEntry.arguments?.read { getStringOrNull("longitudreal") }

            val latitudreal = if (latitudrealStr == "none") null else latitudrealStr
            val longitudreal = if (longitudrealStr == "none") null else longitudrealStr

            RegistrarNuevaDireccionScreen(
                navController = navController,
                idzona = id,
                latitud = latitud,
                longitud = longitud,
                latitudreal = latitudreal,
                longitudreal = longitudreal
            )
        }

        // VISTA SELECCIONAR DIRECCION
        composable(Routes.VistaSeleccionarDireccion.route) { backStackEntry ->

            val id = backStackEntry.arguments?.read { getStringOrNull("id") }?.toIntOrNull() ?: 0
            val nombre = backStackEntry.arguments?.read { getStringOrNull("nombre") } ?: ""
            val telefono = backStackEntry.arguments?.read { getStringOrNull("telefono") } ?: ""
            val direccion = backStackEntry.arguments?.read { getStringOrNull("direccion") } ?: ""
            val referencia = backStackEntry.arguments?.read { getStringOrNull("referencia") } ?: ""

            SeleccionarDireccionScreen(
                navController = navController,
                id = id,
                nombre,
                telefono,
                direccion,
                referencia
            )
        }

        // VISTA LISTADO DE PRODUCTOS
        composable(
            route = Routes.VistaListadoProductos.route,
            arguments = listOf(
                navArgument("idcategoria") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val idCategoria = backStackEntry.arguments?.read { getIntOrNull("idcategoria") } ?: 0

            ListadoProductosScreen(
                navController = navController,
                idCategoria = idCategoria
            )
        }

        // VISTA INFORMACION PRODUCTO
        composable(Routes.VistaInformacionProducto.route) { backStackEntry ->
            val idproductoStr = backStackEntry.arguments?.read { getStringOrNull("idproducto") } ?: "0"
            val idproducto = idproductoStr.toIntOrNull() ?: 0

            ElegirProductoScreen(navController = navController, idProducto = idproducto)
        }

        // VISTA CARRITO DE COMPRAS
        composable(Routes.VistaCarrito.route) { CarritoComprasScreen(navController) }

        // VISTA INFORMACION PRODUCTO PARA EDITARLO
        composable(Routes.VistaEditarProducto.route) { backStackEntry ->
            val idfilaCarritoStr = backStackEntry.arguments?.read { getStringOrNull("idfilacarrito") } ?: "0"
            val idfilaCarrito = idfilaCarritoStr.toIntOrNull() ?: 0

            EditarProductoScreen(navController = navController, idfilaCarrito)
        }

        // VISTA ENVIAR ORDEN
        composable(Routes.VistaEnviarOrden.route) { EnviarOrdenScreen(navController) }


        // VISTA ESTADO ORDENES
        composable(Routes.VistaEstadoOrden.route) { backStackEntry ->

            val idorden = backStackEntry.arguments?.read { getStringOrNull("idorden") }?.toIntOrNull() ?: 0

            EstadoOrdenScreen(
                navController = navController,
                idorden = idorden,
            )
        }

        // VISTA LISTADO PRODUCTOS DE UNA ORDEN
        composable(Routes.VistaListaProductosDeOrden.route) { backStackEntry ->

            val idorden = backStackEntry.arguments?.read { getStringOrNull("idorden") }?.toIntOrNull() ?: 0

            ListadoProductosDeUnaOrdenScreen(
                navController = navController,
                idorden = idorden,
            )
        }

    }
}


