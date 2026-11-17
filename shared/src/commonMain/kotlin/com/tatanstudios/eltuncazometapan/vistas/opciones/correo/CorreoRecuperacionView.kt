package com.tatanstudios.eltuncazometapan.vistas.opciones.correo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColor
import com.tatanstudios.eltuncazometapan.componentes.BotonInferiorCuenta
import com.tatanstudios.eltuncazometapan.componentes.CampoCuenta
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.EncabezadoCuentaPantalla
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.componentes.colorFondoCuenta
import com.tatanstudios.eltuncazometapan.componentes.colorSecundarioCuenta
import com.tatanstudios.eltuncazometapan.componentes.colorTextoCuenta
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.ActualizarCorreoViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.VerCorreoViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.esCorreoValido
import org.jetbrains.compose.resources.stringResource

private val colorOk = Color(0xFF2E7D32)
private val colorAviso = Color(0xFFE65100)

/** Perfil: ver y cambiar el correo que se usa para recuperar la contraseña. */
@Composable
fun CorreoRecuperacionScreen(
    navController: NavHostController,
    verViewModel: VerCorreoViewModel = viewModel { VerCorreoViewModel() },
    viewModel: ActualizarCorreoViewModel = viewModel { ActualizarCorreoViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val correo by viewModel.correo.collectAsState()
    val resultadoVer by verViewModel.resultado.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val cargandoVer by verViewModel.isLoading.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // correo guardado en el servidor (null = no tiene)
    var correoGuardado by remember { mutableStateOf<String?>(null) }
    var cargado by remember { mutableStateOf(false) }
    var validar by remember { mutableStateOf(false) }
    var errorServidor by remember { mutableStateOf<String?>(null) }

    val stringOtroCorreo = stringResource(Res.string.otro_correo_ya_tiene)
    val stringGuardado = stringResource(Res.string.correo_guardado)
    val stringError = stringResource(Res.string.error_reintentar_de_nuevo)

    LaunchedEffect(Unit) { verViewModel.verCorreoRetrofit() }

    LaunchedEffect(resultadoVer) {
        val r = resultadoVer?.getContentIfNotHandled() ?: return@LaunchedEffect
        if (r.success == 1) {
            correoGuardado = r.correo?.takeIf { it.isNotBlank() }
            viewModel.setCorreo(r.correo ?: "")
        }
        cargado = true
    }

    LaunchedEffect(resultado) {
        val r = resultado?.getContentIfNotHandled() ?: return@LaunchedEffect
        when (r.success) {
            1 -> {
                correoGuardado = r.correo ?: correo.trim()
                validar = false
                CustomToasty(ctx, stringGuardado, ToastType.SUCCESS)
            }
            2 -> errorServidor = stringOtroCorreo
            else -> CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }

    val limpio = correo.trim()
    val sinCambios = correoGuardado != null && limpio.equals(correoGuardado, ignoreCase = true)
    val errorCorreo = errorServidor ?: when {
        !validar -> null
        limpio.isEmpty() -> "Escribe tu correo"
        !esCorreoValido(limpio) -> "Ese correo no es válido"
        else -> null
    }

    Scaffold(
        containerColor = colorFondoCuenta,
        topBar = {
            BarraToolbarColor(navController, stringResource(Res.string.correo_recuperacion), AppColors.colorAppPrimary)
        },
        bottomBar = {
            if (cargado) {
                BotonInferiorCuenta(
                    texto = if (correoGuardado == null) "Guardar correo" else "Actualizar correo",
                    habilitado = !sinCambios
                ) {
                    validar = true
                    errorServidor = null
                    if (esCorreoValido(limpio)) {
                        keyboardController?.hide()
                        viewModel.actualizarCorreoRetrofit()
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (cargado) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EncabezadoCuentaPantalla(
                        icono = Icons.Filled.AlternateEmail,
                        titulo = "Correo de recuperación",
                        descripcion = "Si olvidas tu contraseña, te enviaremos un código a este correo para cambiarla."
                    )

                    EstadoCorreo(correoGuardado)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CampoCuenta(
                                valor = correo,
                                onCambio = {
                                    viewModel.setCorreo(it.trim().take(100))
                                    errorServidor = null
                                },
                                etiqueta = if (correoGuardado == null) "Tu correo" else "Nuevo correo",
                                icono = Icons.Filled.Email,
                                error = errorCorreo,
                                ayuda = if (sinCambios) "Este es tu correo actual" else "Ej. nombre@gmail.com",
                                teclado = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            )
                        }
                    }
                }
            }

            if (cargandoVer || isLoading) LoadingModal(isLoading = true)
        }
    }
}

/** Aviso verde si ya tiene correo, naranja si todavía no. */
@Composable
private fun EstadoCorreo(correoGuardado: String?) {
    val tiene = correoGuardado != null
    val color = if (tiene) colorOk else colorAviso

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (tiene) Icons.Filled.CheckCircle else Icons.Filled.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    if (tiene) "Recuperación activada" else "Sin correo de recuperación",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    if (tiene) correoGuardado!! else "Agrégalo para no perder tu cuenta si olvidas la contraseña.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tiene) colorTextoCuenta else colorSecundarioCuenta
                )
            }
        }
    }
}
