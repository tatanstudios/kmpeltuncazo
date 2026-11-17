package com.tatanstudios.eltuncazometapan.vistas.opciones.direcciones

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColorDireccion
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1Boton
import com.tatanstudios.eltuncazometapan.componentes.CustomModal2Botones
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.viewmodel.BorrarDireccionViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.SeleccionarDireccionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SeleccionarDireccionScreen(navController: NavHostController,
                                  id: Int, // id direccion
                                  _nombre: String,
                                  _telefono: String?,
                                  _direccion: String?,
                                  _puntoReferencia: String?,
                                  viewModelSeleccionar: SeleccionarDireccionViewModel = viewModel { SeleccionarDireccionViewModel() },
                                  viewModelBorrar: BorrarDireccionViewModel = viewModel { BorrarDireccionViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    val resultadoSeleccionar by viewModelSeleccionar.resultado.collectAsState()
    val isLoadingSeleccionar by viewModelSeleccionar.isLoading.collectAsState()

    val resultadoBorrar by viewModelBorrar.resultado.collectAsState()
    val isLoadingBorrar by viewModelBorrar.isLoading.collectAsState()

    val tokenManager = remember { TokenManager }
    var idusuario by remember { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (isPressed) 12.dp else 6.dp)

    val keyboardController = LocalSoftwareKeyboardController.current


    var showModal2BotonBorrar by remember { mutableStateOf(false) }
    var showModal1Boton by remember { mutableStateOf(false) }

    val textoNosePuedeBorrar = stringResource(Res.string.no_se_puede_eliminar_direccion_ultima)


    // Definir el color del fondo al presionar
    val loginButtonColor = if (isPressed) {
        AppColors.colorAppPrimary.copy(alpha = 0.8f) // más oscuro al presionar
    } else {
        AppColors.colorAppPrimary
    }


    // Lanzar la solicitud cuando se carga la pantalla
    LaunchedEffect(Unit) {
        scope.launch {
            idusuario = tokenManager.idUsuario.first()
        }
    }


    Scaffold(
        topBar = {
            BarraToolbarColorDireccion(
                titulo = stringResource(Res.string.direccion),
                backgroundColor = AppColors.colorAppPrimary,
                onBackClick = {

                    showModal2BotonBorrar = true

                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
                    .align(Alignment.TopCenter), // <-- alinear arriba
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tu Card y Button aquí

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Nombre: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = _nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {

                                Text(
                                    text = "Teléfono: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )

                                _telefono.nullSiLiteral()?.let { limpio ->
                                    Text(
                                        text = limpio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Dirección: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )

                                _direccion.nullSiLiteral()?.let { limpio ->
                                    Text(
                                        text = limpio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.House, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {

                                Text(
                                    text = "Referencia: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )

                                _puntoReferencia.nullSiLiteral()?.let { limpio ->
                                    Text(
                                        text = limpio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()

                        scope.launch {
                           viewModelSeleccionar.seleccionarDireccionRetrofit(
                               idusuario,
                               id
                           )
                       }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 24.dp, end = 24.dp)
                        .shadow(
                            elevation = elevation, // Cambia la sombra cuando se presiona
                            shape = RoundedCornerShape(25.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = loginButtonColor,  // Cambia color al presionar
                        contentColor = AppColors.colorBlanco,
                    ),
                    interactionSource = interactionSource // Para detectar la interacción
                ) {
                    Text(
                        text = stringResource(Res.string.seleccionar),
                        fontSize = 18.sp,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    )
                }


                if (isLoadingSeleccionar) {
                    LoadingModal(isLoading = isLoadingSeleccionar)
                }

                if (isLoadingBorrar) {
                    LoadingModal(isLoading = isLoadingBorrar)
                }

                if(showModal1Boton){
                    CustomModal1Boton(showModal1Boton, textoNosePuedeBorrar, onDismiss = {showModal1Boton = false})
                }

                resultadoSeleccionar?.getContentIfNotHandled()?.let { result ->
                    when (result.success) {

                        1 -> {
                            CustomToasty(
                                ctx,
                                stringResource(Res.string.direccion_seleccionada),
                                ToastType.SUCCESS
                            )

                            navController.popBackStack()
                        }

                        else -> {
                            // Error, mostrar Toast
                            CustomToasty(
                                ctx,
                                stringResource(Res.string.error_reintentar_de_nuevo),
                                ToastType.ERROR
                            )
                        }
                    }
                }


                resultadoBorrar?.getContentIfNotHandled()?.let { result ->
                    when (result.success) {

                        1 -> {
                            // DIRECCION BORRADA
                            CustomToasty(
                                ctx,
                                stringResource(Res.string.direccion_borrada),
                                ToastType.SUCCESS
                            )

                            navController.popBackStack()
                        }
                        2 -> {
                            // NO SE PUEDE BORRAR, MINIMO 1 DIRECCION
                            showModal1Boton = true
                        }
                        else -> {
                            // Error, mostrar Toast
                            CustomToasty(
                                ctx,
                                stringResource(Res.string.error_reintentar_de_nuevo),
                                ToastType.ERROR
                            )
                        }
                    }
                }

            } // end-card

            // CONFIRMAR PARA BORRAR DIRECCION
            if (showModal2BotonBorrar) {
                CustomModal2Botones(
                    showDialog = true,
                    message = stringResource(Res.string.borrar_direccion),
                    onDismiss = { showModal2BotonBorrar = false },
                    onAccept = {
                        showModal2BotonBorrar = false

                        scope.launch {
                            viewModelBorrar.borrarDireccionRetrofit(
                                idusuario,
                                id
                            )
                        }
                    },
                    stringResource(Res.string.si),
                    stringResource(Res.string.no),
                )
            }
        }
    }
}

fun String?.nullSiLiteral(): String? =
    this?.trim()?.let { if (it.isEmpty() || it.equals("null", true)) null else it }