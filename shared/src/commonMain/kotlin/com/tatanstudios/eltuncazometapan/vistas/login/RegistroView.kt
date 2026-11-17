package com.tatanstudios.eltuncazometapan.vistas.login

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tatanstudios.eltuncazometapan.resources.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tatanstudios.eltuncazometapan.componentes.BloqueTextFieldLogin
import com.tatanstudios.eltuncazometapan.componentes.BloqueTextFieldPassword
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1Boton
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1BotonTitulo
import com.tatanstudios.eltuncazometapan.componentes.CustomModal2Botones
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.viewmodel.RegistroViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.esCorreoValido
import com.tatanstudios.eltuncazometapan.componentes.BloqueTextFieldCorreo
import kotlinx.coroutines.launch

@Composable
fun RegistroScreen(navController: NavHostController, viewModel: RegistroViewModel = viewModel { RegistroViewModel() }) {

    val ctx = LocalPlatformContext.current
    val usuario by viewModel.usuario.collectAsState()
    val password by viewModel.password.collectAsState()
    val correo by viewModel.correo.collectAsState()

    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val tokenManager = remember { TokenManager }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val loginButtonColor = if (isPressed) {
        AppColors.colorAppPrimary.copy(alpha = 0.8f)
    } else {
        AppColors.colorAppPrimary
    }

    val elevation by animateDpAsState(if (isPressed) 12.dp else 6.dp)
    val scope = rememberCoroutineScope()

    // MODAL 1 BOTON
    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }

    // titulo y mensaje de respuestas
    var textoTituloApi by remember { mutableStateOf("") }
    var textoMensajeApi by remember { mutableStateOf("") }
    var showDialogApi by remember { mutableStateOf(false) }

    var showModal2Boton by remember { mutableStateOf(false) }

    // Strings fuera de lambdas
    val stringUsuarioEsRequerido = stringResource(Res.string.usuario_es_requerido)
    val stringPasswordEsRequerido = stringResource(Res.string.password_es_requerido)
    val texto4CaracteresMinimo = stringResource(Res.string.minimo_4_caracteres)
    val stringErrorReintentar = stringResource(Res.string.error_reintentar_de_nuevo)
    val stringCorreoInvalido = stringResource(Res.string.correo_ingresado_no_es_valido)

    // Estado para navegar después del registro exitoso
    var navegarAPrincipal by remember { mutableStateOf<String?>(null) }
    var tokenRecibido by remember { mutableStateOf("") }

    // LaunchedEffect para navegación segura
    LaunchedEffect(navegarAPrincipal) {
        navegarAPrincipal?.let { id ->
            tokenManager.guardarSesion(id, tokenRecibido)
            // se quita el registro y el login, y se regresa a donde se pidió iniciar sesión
            if (!navController.popBackStack(Routes.VistaLogin.route, inclusive = true)) {
                salirDelLogin(navController)
            }
            navegarAPrincipal = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // deja libre la barra de navegación de Android y, si está abierto, el teclado
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(AppColors.colorAppPrimary),
                contentAlignment = Alignment.Center // 👈 ya centra, pero la status bar lo desplaza
            ) {
                Text(
                    text = stringResource(Res.string.crear_una_cuenta),
                    fontFamily = FontFamily(Font(Res.font.arthura_medium)),
                    color = Color.White,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .statusBarsPadding() // 👈 esto empuja el texto hacia abajo respetando la status bar
                )
            }

            // ── WAVE ───────────────────────────────────────────────
            Image(
                painter = painterResource(Res.drawable.icono_wave),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .offset(y = (-1).dp),
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── CARD ───────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    BloqueTextFieldLogin(
                        text = usuario,
                        onTextChanged = { viewModel.setUsuario(it) },
                        maxLength = 20
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BloqueTextFieldPassword(
                        text = password,
                        onTextChanged = { viewModel.setPassword(it) },
                        isPasswordVisible = isPasswordVisible,
                        onPasswordVisibilityChanged = { isPasswordVisible = it },
                        maxLength = 16
                    )

                    Text(
                        text = stringResource(Res.string.minimo_4_caracteres), // o el string que tengas
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // correo opcional: sirve para recuperar la contraseña
                    BloqueTextFieldCorreo(
                        text = correo,
                        textoPlaceholder = stringResource(Res.string.correo_opcional),
                        onTextChanged = { viewModel.setCorreo(it) },
                        maxLength = 100
                    )

                    Text(
                        text = stringResource(Res.string.correo_recuperacion_descripcion),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón registrarse
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            when {
                                usuario.isBlank() -> {
                                    modalMensajeString = stringUsuarioEsRequerido
                                    showModal1Boton = true
                                }
                                password.isBlank() -> {
                                    modalMensajeString = stringPasswordEsRequerido
                                    showModal1Boton = true
                                }
                                password.length < 4 -> {
                                    modalMensajeString = texto4CaracteresMinimo
                                    showModal1Boton = true
                                }
                                correo.isNotBlank() && !esCorreoValido(correo) -> {
                                    modalMensajeString = stringCorreoInvalido
                                    showModal1Boton = true
                                }
                                else -> {
                                    showModal2Boton = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(elevation = elevation, shape = RoundedCornerShape(30.dp)),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = loginButtonColor,
                            contentColor = Color.White
                        ),
                        interactionSource = interactionSource
                    ) {
                        Text(
                            text = stringResource(Res.string.registrarse).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── MODALES ────────────────────────────────────────────────
        if (showDialogApi) {
            CustomModal1BotonTitulo(
                showDialog = showDialogApi,
                title = textoTituloApi ,
                message = textoMensajeApi,
                onDismiss = { showDialogApi = false }
            )
        }

        if (showModal1Boton) {
            CustomModal1Boton(
                showModal1Boton,
                modalMensajeString,
                onDismiss = { showModal1Boton = false }
            )
        }

        // botón para volver al login (en iOS no hay botón atrás del sistema)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = stringResource(Res.string.atras),
                tint = Color.White
            )
        }

        if (isLoading) {
            LoadingModal(isLoading = isLoading)
        }

        // CONFIRMAR PARA REGISTRARSE
        if (showModal2Boton) {
            CustomModal2Botones(
                showDialog = true,
                message = stringResource(Res.string.registrarse),
                onDismiss = { showModal2Boton = false },
                onAccept = {
                    showModal2Boton = false
                    scope.launch {
                        viewModel.registroRetrofit()
                    }
                },
                stringResource(Res.string.si),
                stringResource(Res.string.no),
            )
        }

        resultado?.getContentIfNotHandled()?.let { result ->
            when (result.success) {
                1 -> {
                    // USUARIO YA REGISTRADO
                    textoTituloApi = result.titulo ?: ""
                    textoMensajeApi = result.mensaje ?: ""
                    showDialogApi = true
                }
                2 -> {
                    // REGISTRADO CORRECTAMENTE
                    tokenRecibido = result.token ?: ""
                    navegarAPrincipal = result.id.toString()
                }
                else -> {
                    CustomToasty(ctx, stringErrorReintentar, ToastType.ERROR)
                }
            }
        }
    }
}


