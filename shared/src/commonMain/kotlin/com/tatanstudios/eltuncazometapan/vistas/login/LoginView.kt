package com.tatanstudios.eltuncazometapan.vistas.login

import com.tatanstudios.eltuncazometapan.componentes.navegarAlMenuPrincipal
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton



import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
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
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.viewmodel.LoginViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip



@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = viewModel { LoginViewModel() }) {

    val ctx = LocalPlatformContext.current
    val usuario by viewModel.usuario.collectAsState()
    val password by viewModel.password.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isPasswordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val tokenManager = remember { TokenManager }
    val scope = rememberCoroutineScope()

    val loginButtonColor = if (isPressed) {
        AppColors.colorAppPrimary.copy(alpha = 0.8f)
    } else {
        AppColors.colorAppPrimary
    }
    val elevation by animateDpAsState(if (isPressed) 12.dp else 6.dp)

    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }

    // Strings fuera de lambdas
    val stringUsuarioRequerido = stringResource(Res.string.usuario_es_requerido)
    val stringPasswordRequerido = stringResource(Res.string.password_es_requerido)
    val stringDatosIncorrectos = stringResource(Res.string.datos_incorrectos)

    // Estado para navegación segura
    var navegarAPrincipal by remember { mutableStateOf<String?>(null) }
    var tokenRecibido by remember { mutableStateOf("") }

    LaunchedEffect(navegarAPrincipal) {
        navegarAPrincipal?.let { id ->
            tokenManager.guardarSesion(id, tokenRecibido)
            // se regresa a la pantalla desde donde se pidió iniciar sesión (ej. elegir producto)
            salirDelLogin(navController)
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

            // ── BLOQUE SUPERIOR MORADO ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(AppColors.colorAppPrimary),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .shadow(8.dp, shape = CircleShape)
                            .background(Color.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.logoapp),
                            contentDescription = stringResource(Res.string.descripcion_negocio),
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                        )
                    }
                }
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

            // ── TÍTULO ─────────────────────────────────────────────
            Text(
                text = stringResource(Res.string.descripcion_negocio),
                fontFamily = FontFamily(Font(Res.font.arthura_medium)),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-8).dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── CARD DE LOGIN ──────────────────────────────────────
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón ingresar
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            when {
                                usuario.isBlank() -> {
                                    modalMensajeString = stringUsuarioRequerido
                                    showModal1Boton = true
                                }
                                password.isBlank() -> {
                                    modalMensajeString = stringPasswordRequerido
                                    showModal1Boton = true
                                }
                                else -> {
                                    viewModel.verificarUsuarioPasssword()
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
                            text = stringResource(Res.string.iniciar_sesion).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Olvidé mi contraseña
                    Text(
                        text = stringResource(Res.string.olvidaste_tu_contrasena),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Routes.VistaRecuperarPassword.route) {
                                    launchSingleTop = true
                                }
                            },
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Registrarse
                    Text(
                        text = stringResource(Res.string.registrarse),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Routes.VistaRegistro.route) {
                                    popUpTo(Routes.VistaRegistro.route) { inclusive = true }
                                }
                            },
                        textAlign = TextAlign.Center,
                        color = AppColors.colorAppPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── MODALES ────────────────────────────────────────────────
        // botón para salir sin iniciar sesión (en iOS no hay botón atrás del sistema)
        IconButton(
            onClick = { salirDelLogin(navController) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.cerrar),
                tint = Color.White
            )
        }

        if (showModal1Boton) {
            CustomModal1Boton(
                showModal1Boton,
                modalMensajeString,
                onDismiss = { showModal1Boton = false }
            )
        }

        if (isLoading) {
            LoadingModal(isLoading = isLoading)
        }

        // ── RESULTADO API ──────────────────────────────────────────
        resultado?.getContentIfNotHandled()?.let { result ->

            when (result.success) {
                1 -> {
                    // INICIO DE SESION
                    tokenRecibido = result.token ?: ""
                    navegarAPrincipal = result.id.toString()
                }
                else -> {
                    modalMensajeString = stringDatosIncorrectos
                    showModal1Boton = true
                }
            }
        }
    }
}


/**
 * Sale del login: regresa a la pantalla desde donde se abrió (ej. elegir producto).
 * Si el login quedó sin pantalla anterior, abre el menú principal.
 */
internal fun salirDelLogin(navController: NavHostController) {
    if (navController.previousBackStackEntry != null) {
        navController.popBackStack()
    } else {
        navegarAlMenuPrincipal(navController)
    }
}
