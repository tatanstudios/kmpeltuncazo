package com.tatanstudios.eltuncazometapan.vistas.principal.carrito

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
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloInformacionProductoEditarArray
import com.tatanstudios.eltuncazometapan.viewmodel.ActualizarProductoEditadoViewModel

import com.tatanstudios.eltuncazometapan.viewmodel.InformacionProductoEditadoViewModel
import com.tatanstudios.eltuncazometapan.extras.formatoDosDecimales
import com.tatanstudios.eltuncazometapan.componentes.SeccionModificadores
import com.tatanstudios.eltuncazometapan.componentes.precioExtraModificadores
import com.tatanstudios.eltuncazometapan.componentes.primerErrorModificadores
import com.tatanstudios.eltuncazometapan.componentes.seleccionGuardada
import com.tatanstudios.eltuncazometapan.componentes.seleccionParaEnviar
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloModificadorGrupo

@Composable
fun EditarProductoScreen(
    navController: NavHostController,
    idFilaCarrito: Int,
    viewModel: InformacionProductoEditadoViewModel = viewModel { InformacionProductoEditadoViewModel() },
    viewModelGuardar: ActualizarProductoEditadoViewModel = viewModel { ActualizarProductoEditadoViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val tokenManager = remember { TokenManager }

    // Carga de info del producto a editar
    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()

    // Guardar cambios
    val isLoadingGuardar by viewModelGuardar.isLoading.collectAsState()
    val resultadoGuardar by viewModelGuardar.resultado.collectAsState()

    // Estado de datos
    var producto by remember { mutableStateOf<ModeloInformacionProductoEditarArray?>(null) }
    var idusuario by remember { mutableStateOf("") }

    // Estado UI (inicializados al recibir el producto)
    var cantidad by remember { mutableStateOf(1) }
    var nota by remember { mutableStateOf("") }
    var errorNotaObligatoria by remember { mutableStateOf(false) }
    var datosInicializados by remember { mutableStateOf(false) }

    // MODIFICADORES (opciones del producto)
    var grupos by remember { mutableStateOf(listOf<ModeloModificadorGrupo>()) }
    var seleccion by remember { mutableStateOf(mapOf<Int, Int>()) }
    var mostrarErroresOpciones by remember { mutableStateOf(false) }
    var modalOpcionesMensaje by remember { mutableStateOf<String?>(null) }


    // MOSTRAR MODAL SI NOTA ES REQUERIDA Y NO HE ESCRITO NADA
    var showModalNotaRequerida by remember { mutableStateOf(false) }
    var notaStringQueEsRequerido by remember { mutableStateOf("") }
    val stringErrorReintentar = stringResource(Res.string.error_reintentar_de_nuevo)


    // cargar datos
    LaunchedEffect(idFilaCarrito) {
        idusuario = tokenManager.idUsuario.first()
        viewModel.informacionProductoEditarRetrofit(idusuario, idFilaCarrito)
    }


    Scaffold(
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.editar_cantidad),
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
                // precio unitario (String -> Double)
                val precioUnit = prod.precio?.toDoubleOrNull() ?: 0.0
                val extraOpciones = precioExtraModificadores(grupos, seleccion)
                val total = (precioUnit + extraOpciones) * cantidad

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // IMAGEN
                    item {
                        val tieneImagen = prod.utilizaImagen == 1 && !prod.imagen.isNullOrBlank()
                        if (tieneImagen) {
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

                    // OPCIONES DEL PRODUCTO (inician con las que eligió)
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

                    // STEPPER CANTIDAD (inicia con la del carrito)
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
                                    containerColor = AppColors.colorAppPrimary,
                                    contentColor = Color.White
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
                                    containerColor = AppColors.colorAppPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Más")
                            }
                        }
                    }

                    // NOTAS
                    item {
                        Column {
                            Text(
                                // si el producto exige nota no se marca como opcional
                                text = if (prod.utilizaNota == 1) stringResource(Res.string.notas)
                                       else stringResource(Res.string.notas_opcional),
                                style = MaterialTheme.typography.labelLarge
                            )
                            TextField(
                                value = nota,
                                onValueChange = {
                                    errorNotaObligatoria = false
                                    nota = if (it.length <= 300) it else it.take(300)
                                },
                                placeholder = { Text(stringResource(Res.string.nota_para_este_producto)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                maxLines = 3,
                                isError = errorNotaObligatoria,
                                textStyle = MaterialTheme.typography.bodyLarge, // ← agregar esto
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = AppColors.colorAzul,
                                    unfocusedIndicatorColor = Color.Gray,
                                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                                    focusedContainerColor = Color.Transparent,
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

                    // BOTÓN GUARDAR CAMBIOS
                    item {
                        Button(
                            onClick = {
                                // validar opciones según las reglas de cada bloque
                                primerErrorModificadores(grupos, seleccion)?.let { error ->
                                    mostrarErroresOpciones = true
                                    modalOpcionesMensaje = error
                                    return@Button
                                }

                                // validar nota obligatoria si utiliza_nota == 1
                                if (prod.utilizaNota == 1 && nota.isBlank()) {
                                    errorNotaObligatoria = true

                                    showModalNotaRequerida = true
                                    return@Button
                                }

                                // Llama tu endpoint de actualización/edición del carrito
                                // Ajusta parámetros según tu API:
                                viewModelGuardar.actualizarProductoEditadoRetrofit(
                                    idusuario,
                                    cantidad,
                                    idFilaCarrito,
                                    nota = nota.trim(),
                                    opciones = seleccionParaEnviar(seleccion)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.colorAppPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = stringResource(Res.string.actualizar),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    item { Spacer(Modifier.height(15.dp)) }
                }
            }

            if (isLoading) LoadingModal(isLoading = true)
            if (isLoadingGuardar) LoadingModal(isLoading = true)

            if(showModalNotaRequerida){
                CustomModal1BotonTitulo(showModalNotaRequerida, "Nota Requerida", notaStringQueEsRequerido, onDismiss = {showModalNotaRequerida = false})
            }

            modalOpcionesMensaje?.let { mensaje ->
                CustomModal1BotonTitulo(true, "Opciones", mensaje, onDismiss = { modalOpcionesMensaje = null })
            }
        }
    }


    // manejar resultado retrofit (carga)
    resultado?.getContentIfNotHandled()?.let { res ->
        if (res.success == 1) {
            producto = res.producto
            grupos = res.modificadores
            // Setear estados iniciales sólo una vez
            res.producto?.let { p ->
                if (!datosInicializados) {
                    cantidad = (p.cantidad).coerceAtLeast(1)
                    nota = p.notaProducto ?: ""
                    seleccion = seleccionGuardada(res.modificadores, res.seleccion)
                    datosInicializados = true
                    notaStringQueEsRequerido = p.nota ?: ""
                }
            }
        } else {
            CustomToasty(
                ctx,
                stringErrorReintentar,
                ToastType.ERROR
            )
        }
    }

    // manejar resultado de GUARDAR
    resultadoGuardar?.getContentIfNotHandled()?.let { res ->
        // Ajusta según tu backend:
        if (res.success == 1 || res.success == 2) {
            CustomToasty(
                ctx,
                "Actualizado",
                ToastType.SUCCESS
            )
            navController.popBackStack()
        } else if (res.success == 3) {
            // el servidor rechazó las opciones
            mostrarErroresOpciones = true
            modalOpcionesMensaje = res.mensaje ?: stringErrorReintentar
        } else {
            CustomToasty(
                ctx,
                stringErrorReintentar,
                ToastType.ERROR
            )
        }
    }
}

/** ===== Utilidades ===== **/

private fun formatearUSD(monto: Double): String {
    return "$" + formatoDosDecimales(monto)
}