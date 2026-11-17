package com.tatanstudios.eltuncazometapan.vistas.principal.productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import kotlinx.coroutines.flow.first
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColor
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1BotonTitulo
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloInformacionProductoArray
import com.tatanstudios.eltuncazometapan.viewmodel.EnviarProductoAlCarritoViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.InformacionProductoViewModel
import com.tatanstudios.eltuncazometapan.extras.formatoDosDecimales
import com.tatanstudios.eltuncazometapan.componentes.ModalIniciarSesion
import com.tatanstudios.eltuncazometapan.componentes.SeccionModificadores
import com.tatanstudios.eltuncazometapan.componentes.precioExtraModificadores
import com.tatanstudios.eltuncazometapan.componentes.primerErrorModificadores
import com.tatanstudios.eltuncazometapan.componentes.seleccionInicial
import com.tatanstudios.eltuncazometapan.componentes.seleccionParaEnviar
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloModificadorGrupo

@Composable
fun ElegirProductoScreen(
    navController: NavHostController,
    idProducto: Int,
    viewModel: InformacionProductoViewModel = viewModel { InformacionProductoViewModel() },
    viewModelEnviar: EnviarProductoAlCarritoViewModel = viewModel { EnviarProductoAlCarritoViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val tokenManager = remember { TokenManager }
    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()

    val isLoadingEnviar by viewModelEnviar.isLoading.collectAsState()
    val resultadoEnviar by viewModelEnviar.resultado.collectAsState()

    // MODAL 1 BOTON
    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }
    var modalTituloString by remember { mutableStateOf("") }

    // MOSTRAR MODAL SI NOTA ES REQUERIDA Y NO HE ESCRITO NADA
    var showModalNotaRequerida by remember { mutableStateOf(false) }
    var notaStringQueEsRequerido by remember { mutableStateOf("") }

    // estado UI
    var producto by remember { mutableStateOf<ModeloInformacionProductoArray?>(null) }
    var cantidad by rememberSaveable { mutableStateOf(1) }

    // SI REQUIERE NOTA, ESTO DIRA EL MOTIVO
    var notaInput by rememberSaveable { mutableStateOf("") }
    var errorNotaObligatoria by remember { mutableStateOf(false) }

    // se observa la sesión: si el cliente inicia sesión desde el aviso y regresa aquí, ya puede agregar
    val idusuario by tokenManager.idUsuario.collectAsState(initial = "")
    var showModalIniciarSesion by remember { mutableStateOf(false) }

    // MODIFICADORES (opciones del producto)
    var grupos by remember { mutableStateOf(listOf<ModeloModificadorGrupo>()) }
    var seleccion by remember { mutableStateOf(mapOf<Int, Int>()) }
    var mostrarErroresOpciones by remember { mutableStateOf(false) }

    // cargar datos
    LaunchedEffect(idProducto) {
        viewModel.informacionProductoRetrofit(idProducto)
    }

    // manejar resultado retrofit
    resultado?.getContentIfNotHandled()?.let { result ->
        if (result.success == 1 && result.informacionProducto.isNotEmpty()) {
            producto = result.informacionProducto.first() // asumo 1 ítem por id
            grupos = result.modificadores
            seleccion = seleccionInicial(result.modificadores)
        } else {
            CustomToasty(
                ctx,
                stringResource(Res.string.error_reintentar_de_nuevo),
                ToastType.ERROR
            )
        }
    }

    Scaffold(
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.elegir_cantidad),
                AppColors.colorAppPrimary
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            producto?.let { prod ->
                // parsear precio unitario
                val precioUnit = prod.precio?.toDoubleOrNull() ?: 0.0
                val extraOpciones = precioExtraModificadores(grupos, seleccion)
                val total = (precioUnit + extraOpciones) * cantidad
                notaStringQueEsRequerido = prod.nota ?: ""

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // IMAGEN (o placeholder)
                    item {
                        val tieneImagen = prod.utilizaImagen == 1 && !prod.imagen.isNullOrBlank()
                        if (tieneImagen) {
                            // Cárgala desde tu backend (reemplaza por tu URL base)

                            val imagenUrl = "${ApiConfig.URL_IMAGENES}${prod.imagen}"

                            AsyncImage(
                                model = ImageRequest.Builder(ctx)
                                    .data(imagenUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = prod.nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(Res.drawable.spinloading),
                                error = painterResource(Res.drawable.camaradefecto)
                            )
                        }
                    }

                    // NOMBRE
                    item {
                        Text(
                            text = prod.nombre ?: "",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.colorAppPrimary
                        )
                    }

                    // DESCRIPCIÓN
                    val desc = (prod.descripcion ?: "").replace("\r\n", "\n").replace("\\r\\n", "\n")

                    if (desc.isNotBlank()) {
                        item {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    item { Spacer(Modifier.height(6.dp)) }

                    // PRECIO UNITARIO
                    item {
                        Text(
                            text = stringResource(Res.string.precio) + ": " + formatearUSD(precioUnit) +
                                if (extraOpciones > 0) "  +  ${formatearUSD(extraOpciones)} en opciones" else "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // OPCIONES DEL PRODUCTO
                    if (grupos.isNotEmpty()) {
                        item {
                            SeccionModificadores(
                                grupos = grupos,
                                seleccion = seleccion,
                                mostrarErrores = mostrarErroresOpciones,
                                onCambio = { seleccion = it },
                                onAviso = { CustomToasty(ctx, it, ToastType.INFO) }
                            )
                        }
                    }

                    // STEPPER CANTIDAD
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = { if (cantidad > 1) cantidad-- },
                                enabled = cantidad > 1,
                                modifier = Modifier.size(44.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = AppColors.colorAppPrimary,      // Fondo gris claro
                                    contentColor = Color.White             // Icono negro
                                )
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Menos")
                            }


                            Text(
                                text = cantidad.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                            FilledTonalIconButton(
                                onClick = { if (cantidad < 50) cantidad++ },
                                enabled = cantidad < 50,
                                modifier = Modifier.size(44.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = AppColors.colorAppPrimary,      // Fondo gris claro
                                    contentColor = Color.White             // Icono negro
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Más")
                            }
                        }
                    }

                    // NOTAS (máx 300)
                    item {
                        Column {
                            Text(
                                // si el producto exige nota no se marca como opcional
                                text = if (prod.utilizaNota == 1) stringResource(Res.string.notas)
                                       else stringResource(Res.string.notas_opcional),
                                style = MaterialTheme.typography.labelLarge
                            )
                            TextField(
                                value = notaInput,
                                onValueChange = {
                                    errorNotaObligatoria = false
                                    notaInput = if (it.length <= 300) it else it.take(300)
                                },
                                placeholder = { Text(stringResource(Res.string.nota_para_este_producto)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                maxLines = 3,
                                isError = errorNotaObligatoria,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = AppColors.colorAzul, // línea enfocada
                                    unfocusedIndicatorColor = Color.Gray,                     // línea sin foco
                                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                                    focusedContainerColor = Color.Transparent,                // sin fondo
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent
                                )
                            )
                            if (errorNotaObligatoria) {
                                Text(
                                    text = stringResource(Res.string.nota_es_requerida),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // TOTAL
                    item {
                        Text(
                            text = formatearUSD(total),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                    item { Spacer(Modifier.height(15.dp)) }

                    // BOTÓN AGREGAR
                    item {
                        Button(
                            onClick = {
                                // validar opciones según las reglas de cada bloque
                                primerErrorModificadores(grupos, seleccion)?.let { error ->
                                    mostrarErroresOpciones = true
                                    modalTituloString = "Opciones"
                                    modalMensajeString = error
                                    showModal1Boton = true
                                    return@Button
                                }

                                // validar nota obligatoria si utiliza_nota == 1
                                if (prod.utilizaNota == 1 && notaInput.isBlank()) {
                                    errorNotaObligatoria = true

                                    showModalNotaRequerida = true

                                    return@Button
                                }

                                // sin sesión se pide iniciar sesión; la cantidad y la nota se conservan
                                if (idusuario.isEmpty()) {
                                    showModalIniciarSesion = true
                                    return@Button
                                }

                                val cantidadElegida = cantidad
                                val notaElegida = notaInput.trim()

                                viewModelEnviar.enviarProductoCarritoRetrofit(idusuario, idProducto,
                                    cantidadElegida, notaElegida, seleccionParaEnviar(seleccion))

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.colorAppPrimary, // 🔴 fondo del botón
                                contentColor = Color.White                         // ⚪ texto/icono
                            )
                        ) {
                            Text(
                                text = stringResource(Res.string.agregar_a_la_orden),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    item { Spacer(Modifier.height(15.dp)) }

                }
            }

            if (isLoading) LoadingModal(isLoading = true)
            if (isLoadingEnviar) LoadingModal(isLoading = true)


            if(showModal1Boton){
                CustomModal1BotonTitulo(showModal1Boton, modalTituloString, modalMensajeString, onDismiss = {showModal1Boton = false})
            }

            if (showModalIniciarSesion) {
                ModalIniciarSesion(
                    showDialog = true,
                    mensaje = stringResource(Res.string.inicia_sesion_para_agregar),
                    navController = navController,
                    onDismiss = { showModalIniciarSesion = false }
                )
            }

            if(showModalNotaRequerida){
                CustomModal1BotonTitulo(showModalNotaRequerida, "Nota Requerida", notaStringQueEsRequerido, onDismiss = {showModalNotaRequerida = false})
            }
        }
    }

    resultadoEnviar?.getContentIfNotHandled()?.let { result ->
        if(result.success == 1){
            CustomToasty(
                ctx,
                stringResource(Res.string.agregado_al_carrito),
                ToastType.SUCCESS
            )
            navController.popBackStack()
        }else if(result.success == 2){
            // el servidor rechazó las opciones (cambiaron mientras elegía)
            modalTituloString = result.titulo ?: "Opciones"
            modalMensajeString = result.mensaje ?: ""
            showModal1Boton = true
            viewModel.informacionProductoRetrofit(idProducto)
        }else{
            CustomToasty(
                ctx,
                stringResource(Res.string.error_reintentar_de_nuevo),
                ToastType.ERROR
            )
        }
    }
}

/** ===== Utilidades ===== **/

private fun formatearUSD(monto: Double): String {
    // Formato simple tipo $10.25
    return "$" + formatoDosDecimales(monto)
}