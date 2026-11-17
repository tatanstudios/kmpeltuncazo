package com.tatanstudios.eltuncazometapan.vistas.opciones.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.ImeAction
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
import com.tatanstudios.eltuncazometapan.componentes.RequisitoCuenta
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.componentes.colorFondoCuenta
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.ActualizarPasswordViewModel
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

private const val MINIMO = 4
private const val MAXIMO = 16

@Composable
fun ActualizarPasswordScreen(
    navController: NavHostController,
    viewModel: ActualizarPasswordViewModel = viewModel { ActualizarPasswordViewModel() }
) {
    val ctx = LocalPlatformContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val password by viewModel.passowrd.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var confirmar by remember { mutableStateOf("") }
    var validar by remember { mutableStateOf(false) }
    var idusuario by remember { mutableStateOf("") }

    val stringActualizado = stringResource(Res.string.contrasena_actualizada)
    val stringError = stringResource(Res.string.error_reintentar_de_nuevo)

    LaunchedEffect(Unit) { idusuario = TokenManager.idUsuario.first() }

    LaunchedEffect(resultado) {
        val r = resultado?.getContentIfNotHandled() ?: return@LaunchedEffect
        if (r.success == 1) {
            CustomToasty(ctx, stringActualizado, ToastType.SUCCESS)
            navController.popBackStack()
        } else {
            CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }

    val largoOk = password.length >= MINIMO
    val coinciden = password.isNotEmpty() && password == confirmar

    val errorPassword = when {
        !validar -> null
        password.isBlank() -> "Escribe la nueva contraseña"
        !largoOk -> "Debe tener al menos $MINIMO caracteres"
        else -> null
    }
    val errorConfirmar = when {
        !validar -> null
        confirmar.isBlank() -> "Vuelve a escribir la contraseña"
        !coinciden -> "Las contraseñas no coinciden"
        else -> null
    }

    Scaffold(
        containerColor = colorFondoCuenta,
        topBar = {
            BarraToolbarColor(navController, stringResource(Res.string.actualizar_contrasena), AppColors.colorAppPrimary)
        },
        bottomBar = {
            BotonInferiorCuenta(texto = "Guardar contraseña") {
                validar = true
                if (largoOk && coinciden) {
                    keyboardController?.hide()
                    viewModel.actualizarContrasenaRetrofit(idusuario)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EncabezadoCuentaPantalla(
                    icono = Icons.Filled.LockReset,
                    titulo = "Nueva contraseña",
                    descripcion = "Elige una contraseña que recuerdes. La usarás la próxima vez que inicies sesión."
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CampoCuenta(
                            valor = password,
                            onCambio = { viewModel.setPassword(it.take(MAXIMO)) },
                            etiqueta = "Nueva contraseña",
                            icono = Icons.Filled.Lock,
                            error = errorPassword,
                            esContrasena = true
                        )
                        CampoCuenta(
                            valor = confirmar,
                            onCambio = { confirmar = it.take(MAXIMO) },
                            etiqueta = "Confirmar contraseña",
                            icono = Icons.Filled.Lock,
                            error = errorConfirmar,
                            esContrasena = true,
                            imeAction = ImeAction.Done
                        )

                        // requisitos en vivo
                        RequisitoCuenta("Al menos $MINIMO caracteres", largoOk)
                        RequisitoCuenta("Las dos contraseñas coinciden", coinciden)
                    }
                }
            }

            if (isLoading) LoadingModal(isLoading = true)
        }
    }
}
