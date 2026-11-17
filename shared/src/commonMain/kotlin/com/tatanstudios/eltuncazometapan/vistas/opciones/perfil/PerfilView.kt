package com.tatanstudios.eltuncazometapan.vistas.opciones.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColor
import com.tatanstudios.eltuncazometapan.componentes.CustomModalCerrarSesion
import com.tatanstudios.eltuncazometapan.componentes.navegarAlMenuPrincipal
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.extras.getVersionName
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.network.ApiService
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.VerCorreoViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val colorFondo = Color(0xFFF6F4FA)
private val colorTexto = Color(0xFF1F1B24)
private val colorSecundario = Color(0xFF6D6A75)
private val colorAviso = Color(0xFFE65100)
private val colorSalir = Color(0xFFC62828)

@Composable
fun PerfilScreen(
    navController: NavHostController,
    viewModelCorreo: VerCorreoViewModel = viewModel { VerCorreoViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    val resultadoCorreo by viewModelCorreo.resultado.collectAsState()
    var usuario by remember { mutableStateOf<String?>(null) }
    var correo by remember { mutableStateOf<String?>(null) }
    var datosCuenta by remember { mutableStateOf(false) }

    var showModalCerrarSesion by remember { mutableStateOf(false) }
    val version = getVersionName(ctx)

    // se recarga al volver (por si agregó o cambió el correo)
    LifecycleResumeEffect(Unit) {
        viewModelCorreo.verCorreoRetrofit()
        onPauseOrDispose { }
    }

    LaunchedEffect(resultadoCorreo) {
        val r = resultadoCorreo?.getContentIfNotHandled() ?: return@LaunchedEffect
        if (r.success == 1) {
            usuario = r.usuario
            correo = r.correo?.takeIf { it.isNotBlank() }
            datosCuenta = true
        }
    }

    fun ir(ruta: String) = navController.navigate(ruta) { launchSingleTop = true }

    Scaffold(
        containerColor = colorFondo,
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.opciones),
                AppColors.colorAppPrimary
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── cuenta ──
            EncabezadoCuenta(usuario = usuario, correo = correo, cargado = datosCuenta)

            Spacer(Modifier.height(8.dp))
            TituloSeccion("Mi cuenta")

            Grupo {
                FilaOpcion(
                    icono = Icons.Filled.LocationOn,
                    titulo = "Mis direcciones",
                    subtitulo = "Dónde entregamos tus órdenes",
                    onClick = { ir(Routes.VistaMisDirecciones.createRoute(0)) }
                )
                Separador()
                FilaOpcion(
                    icono = Icons.Filled.Lock,
                    titulo = "Cambiar contraseña",
                    subtitulo = "Actualiza la contraseña de tu cuenta",
                    onClick = { ir(Routes.VistaActualizarContrasena.route) }
                )
                Separador()
                FilaOpcion(
                    icono = Icons.Filled.Email,
                    titulo = "Correo de recuperación",
                    subtitulo = when {
                        !datosCuenta -> "Para recuperar tu contraseña"
                        correo != null -> correo!!
                        else -> "Agrégalo para poder recuperar tu contraseña"
                    },
                    subtituloColor = if (datosCuenta && correo == null) colorAviso else colorSecundario,
                    alerta = datosCuenta && correo == null,
                    onClick = { ir(Routes.VistaCorreo.route) }
                )
            }

            Spacer(Modifier.height(8.dp))
            TituloSeccion("Sesión")

            Grupo {
                FilaOpcion(
                    icono = Icons.AutoMirrored.Filled.Logout,
                    titulo = stringResource(Res.string.cerrar_sesion),
                    subtitulo = "Podrás seguir viendo el menú",
                    colorPrincipal = colorSalir,
                    mostrarFlecha = false,
                    onClick = { showModalCerrarSesion = true }
                )
            }

            Text(
                text = "El Tuncazo · versión $version",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (showModalCerrarSesion) {
            CustomModalCerrarSesion(
                showModalCerrarSesion,
                stringResource(Res.string.cerrar_sesion),
                onDismiss = { showModalCerrarSesion = false },
                onAccept = {
                    scope.launch {
                        // se revoca el token en el servidor; si no hay internet igual se cierra la sesión local
                        try {
                            ApiService.instance.cerrarSesion()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                        }
                        TokenManager.deletePreferences()
                        showModalCerrarSesion = false

                        // sin sesión se sigue viendo el menú; el login se pide al querer ordenar
                        navegarAlMenuPrincipal(navController)
                    }
                })
        }
    }
}

// ── piezas ────────────────────────────────────────────────────────────────────

@Composable
private fun EncabezadoCuenta(usuario: String?, correo: String?, cargado: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.colorAppPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usuario?.firstOrNull()?.uppercase() ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hola,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = usuario ?: if (cargado) "" else "…",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (correo != null) {
                    Text(
                        text = correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = colorSecundario,
        modifier = Modifier.padding(start = 6.dp)
    )
}

@Composable
private fun Grupo(contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column { contenido() }
    }
}

@Composable
private fun Separador() {
    HorizontalDivider(color = Color(0xFFF0EEF4), modifier = Modifier.padding(start = 68.dp))
}

@Composable
private fun FilaOpcion(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    colorPrincipal: Color = AppColors.colorAppPrimary,
    subtituloColor: Color = colorSecundario,
    alerta: Boolean = false,
    mostrarFlecha: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorPrincipal.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = colorPrincipal, modifier = Modifier.size(22.dp))
            if (alerta) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorAviso)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (colorPrincipal == colorSalir) colorSalir else colorTexto
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = subtituloColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (mostrarFlecha) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFBDB8C7)
            )
        }
    }
}
