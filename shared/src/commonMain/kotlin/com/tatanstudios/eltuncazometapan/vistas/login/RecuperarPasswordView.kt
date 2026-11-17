package com.tatanstudios.eltuncazometapan.vistas.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColorDireccion
import com.tatanstudios.eltuncazometapan.componentes.BloqueEntradaGeneral
import com.tatanstudios.eltuncazometapan.componentes.BloqueTextFieldCorreo
import com.tatanstudios.eltuncazometapan.componentes.BloqueTextFieldPassword
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1Boton
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.RecuperarPasswordViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.esCorreoValido
import org.jetbrains.compose.resources.stringResource

private enum class AccionRecuperar { ENVIAR, VERIFICAR, CAMBIAR }

/**
 * Recuperar contraseña: correo -> código de 6 dígitos -> nueva contraseña.
 * Al terminar se inicia sesión con el token que devuelve el servidor.
 */
@Composable
fun RecuperarPasswordScreen(
    navController: NavHostController,
    viewModel: RecuperarPasswordViewModel = viewModel { RecuperarPasswordViewModel() },
) {
    val paso by viewModel.paso.collectAsState()
    val correo by viewModel.correo.collectAsState()
    val codigo by viewModel.codigo.collectAsState()
    val password by viewModel.password.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    var accion by remember { mutableStateOf(AccionRecuperar.ENVIAR) }

    var showModal by remember { mutableStateOf(false) }
    var modalMensaje by remember { mutableStateOf("") }
    fun mostrarModal(mensaje: String) { modalMensaje = mensaje; showModal = true }

    // Strings fuera de lambdas
    val stringCorreoRequerido = stringResource(Res.string.correo_es_requerido)
    val stringCorreoInvalido = stringResource(Res.string.correo_ingresado_no_es_valido)
    val stringCodigo6 = stringResource(Res.string.codigo_debe_tener_6_digitos)
    val stringPasswordRequerido = stringResource(Res.string.password_es_requerido)
    val stringMinimo4 = stringResource(Res.string.minimo_4_caracteres)
    val stringCodigoEnviado = stringResource(Res.string.codigo_enviado)
    val stringPasswordActualizada = stringResource(Res.string.contrasena_actualizada)
    val stringError = stringResource(Res.string.error_reintentar_de_nuevo)

    fun volver() {
        if (paso > 1) viewModel.irAPaso(paso - 1) else navController.popBackStack()
    }

    LaunchedEffect(resultado) {
        val result = resultado?.getContentIfNotHandled() ?: return@LaunchedEffect

        when (accion) {
            AccionRecuperar.ENVIAR -> when (result.success) {
                1 -> {
                    CustomToasty(null, stringCodigoEnviado, ToastType.SUCCESS)
                    viewModel.setCodigo("")
                    viewModel.irAPaso(2)
                }
                else -> mostrarModal(result.mensaje ?: stringError)
            }

            AccionRecuperar.VERIFICAR -> when (result.success) {
                1 -> viewModel.irAPaso(3)
                3 -> {
                    // vencido o demasiados intentos: hay que pedir otro código
                    mostrarModal(result.mensaje ?: stringError)
                    viewModel.irAPaso(1)
                }
                else -> mostrarModal(result.mensaje ?: stringError)
            }

            AccionRecuperar.CAMBIAR -> when (result.success) {
                1 -> {
                    CustomToasty(null, stringPasswordActualizada, ToastType.SUCCESS)
                    TokenManager.guardarSesion(result.id.toString(), result.token ?: "")
                    // se quita recuperar y login, y se regresa a donde se pidió iniciar sesión
                    if (!navController.popBackStack(Routes.VistaLogin.route, inclusive = true)) {
                        salirDelLogin(navController)
                    }
                }
                3 -> {
                    mostrarModal(result.mensaje ?: stringError)
                    viewModel.irAPaso(1)
                }
                2 -> {
                    mostrarModal(result.mensaje ?: stringError)
                    viewModel.irAPaso(2)
                }
                else -> mostrarModal(result.mensaje ?: stringError)
            }
        }
    }

    Scaffold(
        topBar = {
            BarraToolbarColorDireccion(
                titulo = stringResource(Res.string.recuperacion_de_contrasena),
                backgroundColor = AppColors.colorAppPrimary,
                onBackClick = { volver() },
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {

                        val descripcion = when (paso) {
                            1 -> stringResource(Res.string.recuperar_paso_correo)
                            2 -> stringResource(Res.string.recuperar_paso_codigo, correo.trim())
                            else -> stringResource(Res.string.recuperar_paso_password)
                        }

                        Text(
                            text = descripcion,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        when (paso) {
                            1 -> BloqueTextFieldCorreo(
                                text = correo,
                                textoPlaceholder = stringResource(Res.string.correo_electronico),
                                onTextChanged = { viewModel.setCorreo(it) },
                                maxLength = 100
                            )

                            2 -> BloqueEntradaGeneral(
                                text = codigo,
                                onTextChanged = { viewModel.setCodigo(it) },
                                maxLength = 6,
                                placeholderResId = Res.string.codigo_de_6_digitos,
                                icon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Number
                            )

                            else -> {
                                BloqueTextFieldPassword(
                                    text = password,
                                    onTextChanged = { viewModel.setPassword(it) },
                                    isPasswordVisible = isPasswordVisible,
                                    onPasswordVisibilityChanged = { isPasswordVisible = it },
                                    maxLength = 16
                                )
                                Text(
                                    text = stringResource(Res.string.minimo_4_caracteres),
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val textoBoton = when (paso) {
                            1 -> stringResource(Res.string.enviar_codigo)
                            2 -> stringResource(Res.string.verificar_codigo)
                            else -> stringResource(Res.string.cambiar_contrasena)
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                when (paso) {
                                    1 -> when {
                                        correo.isBlank() -> mostrarModal(stringCorreoRequerido)
                                        !esCorreoValido(correo) -> mostrarModal(stringCorreoInvalido)
                                        else -> {
                                            accion = AccionRecuperar.ENVIAR
                                            viewModel.enviarCodigo()
                                        }
                                    }

                                    2 -> if (codigo.length != 6) {
                                        mostrarModal(stringCodigo6)
                                    } else {
                                        accion = AccionRecuperar.VERIFICAR
                                        viewModel.verificarCodigo()
                                    }

                                    else -> when {
                                        password.isBlank() -> mostrarModal(stringPasswordRequerido)
                                        password.length < 4 -> mostrarModal(stringMinimo4)
                                        else -> {
                                            accion = AccionRecuperar.CAMBIAR
                                            viewModel.cambiarPassword()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.colorAppPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = textoBoton.uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        if (paso == 2) {
                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = stringResource(Res.string.reenviar_codigo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        accion = AccionRecuperar.ENVIAR
                                        viewModel.enviarCodigo()
                                    },
                                textAlign = TextAlign.Center,
                                color = AppColors.colorAppPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(Res.string.cambiar_correo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.irAPaso(1) },
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            if (showModal) {
                CustomModal1Boton(showModal, modalMensaje, onDismiss = { showModal = false })
            }

            if (isLoading) {
                LoadingModal(isLoading = isLoading)
            }
        }
    }
}
