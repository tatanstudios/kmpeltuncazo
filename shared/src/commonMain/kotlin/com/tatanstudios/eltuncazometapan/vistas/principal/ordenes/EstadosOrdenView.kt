package com.tatanstudios.eltuncazometapan.vistas.principal.ordenes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColorOrdenesEstado
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1BotonTitulo
import com.tatanstudios.eltuncazometapan.componentes.CustomModal2Botones
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloOrdenesIndividualArray
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloResumenProductoOrden
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.CancelarOrdenViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.CompletarOrdenViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.InformacionDeUnaOrdenViewModel
import org.jetbrains.compose.resources.stringResource

private val colorFondo = Color(0xFFF6F4FA)
private val colorTexto = Color(0xFF1F1B24)
private val colorSecundario = Color(0xFF6D6A75)
private val colorHecho = Color(0xFF2E7D32)
private val colorEspera = Color(0xFFB26A00)
private val colorCancelado = Color(0xFFC62828)
private val colorPendienteGris = Color(0xFFD6D3DC)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EstadoOrdenScreen(
    navController: NavHostController,
    idorden: Int,
    viewModel: InformacionDeUnaOrdenViewModel = viewModel { InformacionDeUnaOrdenViewModel() },
    viewModelCancelar: CancelarOrdenViewModel = viewModel { CancelarOrdenViewModel() },
    viewModelCompletar: CompletarOrdenViewModel = viewModel { CompletarOrdenViewModel() },
) {
    val ctx = LocalPlatformContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val isLoadingCancelar by viewModelCancelar.isLoading.collectAsState()
    val resultadoCancelar by viewModelCancelar.resultado.collectAsState()
    val isLoadingCompletar by viewModelCompletar.isLoading.collectAsState()
    val resultadoCompletar by viewModelCompletar.resultado.collectAsState()

    var orden by remember { mutableStateOf<ModeloOrdenesIndividualArray?>(null) }
    var cargaFallida by remember { mutableStateOf(false) }

    var showModalCancelarOrden by rememberSaveable { mutableStateOf(false) }
    var showModalCompletarOrden by rememberSaveable { mutableStateOf(false) }
    var showModalAviso by rememberSaveable { mutableStateOf(false) }
    var tituloAviso by rememberSaveable { mutableStateOf("") }
    var mensajeAviso by rememberSaveable { mutableStateOf("") }

    var refreshing by remember { mutableStateOf(false) }
    fun recargar() {
        refreshing = true
        viewModel.informacionOrdenIndividualRetrofit(idorden)
    }
    val pullRefreshState = rememberPullRefreshState(refreshing, { recargar() })

    LaunchedEffect(idorden) { viewModel.informacionOrdenIndividualRetrofit(idorden) }

    val datos = orden
    val cancelada = datos?.estadoCancelada == 1
    val iniciada = datos?.estadoIniciada == 1

    Scaffold(
        containerColor = colorFondo,
        topBar = {
            BarraToolbarColorOrdenesEstado(
                navController,
                stringResource(Res.string.estado_de_orden),
                AppColors.colorAppPrimary
            )
        },
        bottomBar = {
            if (datos != null) {
                // pendiente: se puede cancelar. Iniciada o cancelada: solo ocultarla de la lista
                BarraAccion(
                    puedeCancelar = !iniciada && !cancelada,
                    onCancelar = { showModalCancelarOrden = true },
                    onOcultar = { showModalCompletarOrden = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            if (datos != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Encabezado(datos)

                    Tarjeta(Icons.Filled.Timeline, "Seguimiento") {
                        LineaDeTiempo(datos)
                    }

                    if (datos.productos.isNotEmpty()) {
                        Tarjeta(Icons.AutoMirrored.Filled.ReceiptLong, "Tu pedido") {
                            datos.productos.forEachIndexed { i, p ->
                                if (i > 0) HorizontalDivider(color = Color(0xFFF0EEF4), modifier = Modifier.padding(vertical = 8.dp))
                                FilaProducto(p)
                            }
                            if (!datos.nota.isNullOrBlank()) {
                                Text(
                                    text = "Nota de la orden: ${datos.nota}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = colorSecundario,
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colorFondo)
                                        .padding(10.dp)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFE3E0EA), modifier = Modifier.padding(top = 12.dp, bottom = 10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorTexto,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    datos.total ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.colorAppPrimary
                                )
                            }
                        }
                    } else {
                        // respuesta de una versión vieja de la API: se ofrece la lista aparte
                        OutlinedButton(
                            onClick = {
                                navController.navigate(Routes.VistaListaProductosDeOrden.createRoute(datos.id)) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Ver productos") }
                    }

                    if (!datos.direccion.isNullOrBlank()) {
                        Tarjeta(Icons.Filled.LocationOn, stringResource(Res.string.direccion_de_entrega)) {
                            if (!datos.cliente.isNullOrBlank()) {
                                Text(datos.cliente, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colorTexto)
                            }
                            if (!datos.telefono.isNullOrBlank()) {
                                Text(datos.telefono, style = MaterialTheme.typography.bodyMedium, color = colorSecundario)
                            }
                            Text(
                                datos.direccion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorTexto,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            if (!datos.referencia.isNullOrBlank()) {
                                Text(
                                    "${stringResource(Res.string.punto_referencia)}: ${datos.referencia}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorSecundario,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Desliza hacia abajo para actualizar el estado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                    )
                }
            } else if (cargaFallida) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(Res.string.error_reintentar_de_nuevo), color = colorSecundario)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { recargar() }) { Text("Reintentar") }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (isLoading && !refreshing && datos == null) LoadingModal(isLoading = true)
            if (isLoadingCancelar || isLoadingCompletar) LoadingModal(isLoading = true)

            if (showModalCancelarOrden) {
                CustomModal2Botones(
                    showDialog = true,
                    message = stringResource(Res.string.cancelar_orden),
                    onDismiss = { showModalCancelarOrden = false },
                    onAccept = {
                        showModalCancelarOrden = false
                        viewModelCancelar.cancelarOrdenRetrofit(idorden)
                    },
                    stringResource(Res.string.si),
                    stringResource(Res.string.no),
                )
            }

            if (showModalCompletarOrden) {
                CustomModal2Botones(
                    showDialog = true,
                    message = "¿Ocultar esta orden? Ya no aparecerá en tu lista de órdenes.",
                    onDismiss = { showModalCompletarOrden = false },
                    onAccept = {
                        showModalCompletarOrden = false
                        viewModelCompletar.completarOrdenRetrofit(idorden)
                    },
                    stringResource(Res.string.si),
                    stringResource(Res.string.no),
                )
            }

            if (showModalAviso) {
                CustomModal1BotonTitulo(
                    showModalAviso,
                    tituloAviso,
                    mensajeAviso,
                    onDismiss = {
                        showModalAviso = false
                        viewModel.informacionOrdenIndividualRetrofit(idorden)
                    }
                )
            }
        }
    }

    val stringError = stringResource(Res.string.error_reintentar_de_nuevo)
    val stringCancelada = stringResource(Res.string.orden_cancelada)

    // cargar orden
    resultado?.getContentIfNotHandled()?.let { result ->
        refreshing = false
        if (result.success == 1 && result.ordenes.isNotEmpty()) {
            orden = result.ordenes.first()
            cargaFallida = false
        } else {
            cargaFallida = orden == null
            CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }

    // cancelar orden
    resultadoCancelar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> { // el local ya la inició
                tituloAviso = result.titulo ?: ""
                mensajeAviso = result.mensaje ?: ""
                showModalAviso = true
            }
            2 -> {
                CustomToasty(ctx, stringCancelada, ToastType.SUCCESS)
                navController.popBackStack()
            }
            else -> CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }

    // orden recibida
    resultadoCompletar?.getContentIfNotHandled()?.let { result ->
        if (result.success == 1) {
            CustomToasty(ctx, "Orden ocultada", ToastType.SUCCESS)
            navController.navigate(Routes.VistaPrincipal.createRoute("ordenes")) {
                popUpTo(Routes.VistaEstadoOrden.route) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }
}

// ── encabezado ────────────────────────────────────────────────────────────────

@Composable
private fun Encabezado(orden: ModeloOrdenesIndividualArray) {
    val (texto, color) = when {
        orden.estadoCancelada == 1 -> "Cancelada" to colorCancelado
        orden.estadoIniciada == 1 -> "Iniciada" to colorHecho
        else -> "Pendiente" to colorEspera
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Orden #${orden.id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = texto,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(color.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
            Text(
                text = "Enviada: ${orden.fechaOrden ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                color = colorSecundario,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ── línea de tiempo ───────────────────────────────────────────────────────────

private enum class EstadoPaso { HECHO, ACTUAL, PENDIENTE, CANCELADO }

private data class Paso(val titulo: String, val detalle: String?, val estado: EstadoPaso)

private fun pasosDe(orden: ModeloOrdenesIndividualArray): List<Paso> {
    val pasos = mutableListOf(Paso("Orden enviada", orden.fechaOrden, EstadoPaso.HECHO))

    if (orden.estadoCancelada == 1) {
        if (orden.estadoIniciada == 1) {
            pasos += Paso("Orden iniciada", orden.fechaIniciada, EstadoPaso.HECHO)
        }
        val quien = if (orden.canceladaPor == 1) "Cancelaste la orden" else "El local canceló la orden"
        val motivo = orden.mensajeCancelada?.takeIf { it.isNotBlank() }
        pasos += Paso(
            "Cancelada",
            listOfNotNull(quien, motivo?.let { "Motivo: $it" }, orden.fechaCancelada).joinToString("\n"),
            EstadoPaso.CANCELADO
        )
        return pasos
    }

    // el local solo inicia la orden: ese es el último estado
    if (orden.estadoIniciada == 1) {
        pasos += Paso("Orden iniciada", orden.fechaIniciada, EstadoPaso.HECHO)
    } else {
        pasos += Paso("Esperando que el local inicie tu orden", null, EstadoPaso.ACTUAL)
        pasos += Paso("Orden iniciada", null, EstadoPaso.PENDIENTE)
    }
    return pasos
}

@Composable
private fun LineaDeTiempo(orden: ModeloOrdenesIndividualArray) {
    val pasos = pasosDe(orden)
    Column {
        pasos.forEachIndexed { i, paso ->
            FilaPaso(paso, esUltimo = i == pasos.lastIndex, siguienteHecho = pasos.getOrNull(i + 1)?.estado != EstadoPaso.PENDIENTE)
        }
    }
}

@Composable
private fun FilaPaso(paso: Paso, esUltimo: Boolean, siguienteHecho: Boolean) {
    val color = when (paso.estado) {
        EstadoPaso.HECHO -> colorHecho
        EstadoPaso.ACTUAL -> colorEspera
        EstadoPaso.CANCELADO -> colorCancelado
        EstadoPaso.PENDIENTE -> colorPendienteGris
    }

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // círculo + línea hacia el siguiente paso
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (paso.estado == EstadoPaso.ACTUAL) color.copy(alpha = 0.18f) else color),
                contentAlignment = Alignment.Center
            ) {
                when (paso.estado) {
                    EstadoPaso.HECHO -> Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    EstadoPaso.CANCELADO -> Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    EstadoPaso.ACTUAL -> Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                    EstadoPaso.PENDIENTE -> Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                }
            }
            if (!esUltimo) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(if (siguienteHecho) color else colorPendienteGris)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(bottom = if (esUltimo) 0.dp else 18.dp)) {
            Text(
                text = paso.titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (paso.estado == EstadoPaso.PENDIENTE) FontWeight.Normal else FontWeight.SemiBold,
                color = when (paso.estado) {
                    EstadoPaso.PENDIENTE -> Color.Gray
                    EstadoPaso.CANCELADO -> colorCancelado
                    else -> colorTexto
                }
            )
            if (!paso.detalle.isNullOrBlank()) {
                Text(
                    text = paso.detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorSecundario
                )
            }
        }
    }
}

// ── piezas ────────────────────────────────────────────────────────────────────

@Composable
private fun Tarjeta(icono: ImageVector, titulo: String, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AppColors.colorAppPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, null, tint = AppColors.colorAppPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorTexto)
            }
            Spacer(Modifier.height(14.dp))
            contenido()
        }
    }
}

@Composable
private fun FilaProducto(producto: ModeloResumenProductoOrden) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "${producto.cantidad}x",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.colorAppPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AppColors.colorAppPrimary.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(producto.nombre ?: "", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = colorTexto)
            producto.opciones.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = colorSecundario)
            }
            if (!producto.nota.isNullOrBlank()) {
                Text(
                    "Nota: ${producto.nota}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = colorSecundario
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text("$" + (producto.total ?: ""), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = colorTexto)
    }
}

@Composable
private fun BarraAccion(puedeCancelar: Boolean, onCancelar: () -> Unit, onOcultar: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            if (!puedeCancelar) {
                OutlinedButton(
                    onClick = onOcultar,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.5.dp, AppColors.colorAppPrimary)
                ) {
                    Icon(Icons.Filled.VisibilityOff, null, tint = AppColors.colorAppPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ocultar orden",
                        color = AppColors.colorAppPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Puedes cancelar mientras el local no la acepte.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorSecundario,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.5.dp, colorCancelado)
                ) {
                    Text("Cancelar orden", color = colorCancelado, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
