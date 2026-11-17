package com.tatanstudios.eltuncazometapan.vistas.principal.productos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.eltuncazometapan.componentes.AvisoEstadoLocal
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColor
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1BotonTitulo
import com.tatanstudios.eltuncazometapan.componentes.CustomModal2Botones
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.componentes.navegarAlMenuPrincipal
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.extras.getVersionName
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloInformacionOrdenParaEnviar
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloResumenProductoOrden
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.EnviarOrdenFinalViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.InformacionOrdenParaEnviarViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val colorFondo = Color(0xFFF6F4FA)
private val colorTexto = Color(0xFF1F1B24)
private val colorAviso = Color(0xFFE65100)
private val colorAvisoFondo = Color(0xFFFFF3E0)

@Composable
fun EnviarOrdenScreen(
    navController: NavHostController,
    viewModel: InformacionOrdenParaEnviarViewModel = viewModel { InformacionOrdenParaEnviarViewModel() },
    viewModelEnviarOrden: EnviarOrdenFinalViewModel = viewModel { EnviarOrdenFinalViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val isLoadingEnviarOrden by viewModelEnviarOrden.isLoading.collectAsState()
    val resultadoEnviarOrden by viewModelEnviarOrden.resultado.collectAsState()

    var idusuario by remember { mutableStateOf("") }
    var info by remember { mutableStateOf<ModeloInformacionOrdenParaEnviar?>(null) }
    var nota by remember { mutableStateOf("") }

    val versionLocal = getVersionName(ctx)

    // confirmar envío
    var showModalConfirmar by remember { mutableStateOf(false) }

    // avisos (cerrado, mínimo, etc.)
    var showModalAviso by remember { mutableStateOf(false) }
    var modalTitulo by remember { mutableStateOf("") }
    var modalMensaje by remember { mutableStateOf("") }
    var avisoSinDireccion by remember { mutableStateOf(false) }

    // orden enviada: al cerrar el aviso se va a "Órdenes"
    var showModalEnviada by remember { mutableStateOf(false) }

    // se recarga al volver (por ejemplo, después de cambiar la dirección)
    LifecycleResumeEffect(Unit) {
        scope.launch {
            idusuario = TokenManager.idUsuario.first()
            viewModel.informacionOrdenParaEnviarRetrofit(idusuario)
        }
        onPauseOrDispose { }
    }

    val datos = info
    val localAbierto = datos?.abierto != 0
    val puedeOrdenar = datos?.minimo == 1 && localAbierto

    Scaffold(
        containerColor = colorFondo,
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.enviar_orden),
                AppColors.colorAppPrimary
            )
        },
        bottomBar = {
            if (datos != null) {
                BarraConfirmar(
                    total = datos.total ?: "",
                    habilitado = puedeOrdenar,
                    avisoCerrado = if (!localAbierto) "El local está cerrado" else null,
                    onConfirmar = { showModalConfirmar = true }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (datos != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ── estado del local (igual que en el menú) ──
                    if (!datos.estadoTexto.isNullOrBlank()) {
                        AvisoEstadoLocal(
                            abierto = localAbierto,
                            texto = datos.estadoTexto,
                            detalle = if (localAbierto) null else datos.estadoDetalle
                        )
                    }

                    // ── mínimo de consumo ──
                    if (datos.minimo != 1) {
                        AvisoMinimo(
                            faltante = datos.faltante,
                            minimo = datos.minimoConsumo,
                            mensaje = datos.mensaje,
                            onSeguirComprando = { navegarAlMenuPrincipal(navController) }
                        )
                    }

                    // ── resumen del pedido ──
                    Tarjeta(icono = Icons.Filled.ReceiptLong, titulo = stringResource(Res.string.orden_tu_pedido)) {
                        datos.productos.forEachIndexed { i, producto ->
                            if (i > 0) HorizontalDivider(color = Color(0xFFF0EEF4), modifier = Modifier.padding(vertical = 8.dp))
                            FilaResumen(producto)
                        }

                        HorizontalDivider(color = Color(0xFFE3E0EA), modifier = Modifier.padding(top = 12.dp, bottom = 10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(Res.string.total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorTexto,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = datos.total ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.colorAppPrimary
                            )
                        }
                    }

                    // ── dirección de entrega ──
                    Tarjeta(
                        icono = Icons.Filled.LocationOn,
                        titulo = stringResource(Res.string.direccion_de_entrega),
                        accion = stringResource(Res.string.orden_cambiar),
                        onAccion = {
                            navController.navigate(Routes.VistaMisDirecciones.createRoute(0)) {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Text(
                            text = datos.cliente ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorTexto
                        )
                        if (!datos.telefono.isNullOrBlank()) {
                            Text(
                                text = datos.telefono,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = datos.direccion ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorTexto
                        )
                        if (!datos.referencia.isNullOrBlank()) {
                            Text(
                                text = stringResource(Res.string.punto_referencia) + ": " + datos.referencia,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // ── nota para la orden ──
                    Tarjeta(icono = Icons.AutoMirrored.Filled.Notes, titulo = stringResource(Res.string.orden_nota_titulo)) {
                        OutlinedTextField(
                            value = nota,
                            onValueChange = { nota = it.take(300) },
                            placeholder = { Text(stringResource(Res.string.orden_nota_ejemplo), color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.colorAppPrimary,
                                unfocusedBorderColor = Color(0xFFDAD6E3),
                                cursorColor = AppColors.colorAppPrimary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                            ),
                            supportingText = {
                                Text(
                                    text = "${nota.length}/300",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    color = Color.Gray
                                )
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── modales ──
            if (showModalAviso) {
                CustomModal1BotonTitulo(
                    showModalAviso,
                    modalTitulo,
                    modalMensaje,
                    onDismiss = {
                        showModalAviso = false
                        if (avisoSinDireccion) {
                            avisoSinDireccion = false
                            navController.navigate(Routes.VistaMisDirecciones.createRoute(0)) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            if (showModalConfirmar) {
                CustomModal2Botones(
                    showDialog = true,
                    message = stringResource(Res.string.orden_confirmar_pregunta),
                    onDismiss = { showModalConfirmar = false },
                    onAccept = {
                        showModalConfirmar = false
                        viewModelEnviarOrden.enviarOrdenRetrofit(idusuario, nota.trim(), versionLocal)
                    },
                    stringResource(Res.string.si),
                    stringResource(Res.string.no),
                )
            }

            if (showModalEnviada) {
                CustomModal1BotonTitulo(
                    showModalEnviada,
                    stringResource(Res.string.orden_enviada_titulo),
                    stringResource(Res.string.orden_enviada_mensaje),
                    onDismiss = {
                        showModalEnviada = false
                        navController.navigate(Routes.VistaPrincipal.createRoute("ordenes")) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            if (isLoading && datos == null) LoadingModal(isLoading = true)
            if (isLoadingEnviarOrden) LoadingModal(isLoading = true)
        }
    }

    val stringError = stringResource(Res.string.error_reintentar_de_nuevo)

    // ── cargar información ──
    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // cliente sin dirección
                modalTitulo = "Nota"
                modalMensaje = "Se necesita una dirección de entrega"
                avisoSinDireccion = true
                showModalAviso = true
            }
            2 -> info = result
            else -> {
                CustomToasty(ctx, stringError, ToastType.ERROR)
                navController.popBackStack()
            }
        }
    }

    // ── enviar orden ──
    resultadoEnviarOrden?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            // 1 = cerrado por horario, 2 = cerrado desde el panel: el mensaje dice cuándo abre y el motivo
            1, 2 -> {
                modalTitulo = result.titulo ?: "Local cerrado"
                modalMensaje = result.mensaje ?: "Cerrado"
                showModalAviso = true
                // se recarga para mostrar el aviso y desactivar el botón
                viewModel.informacionOrdenParaEnviarRetrofit(idusuario)
            }
            3, 4 -> { modalTitulo = "Nota"; modalMensaje = result.mensaje ?: ""; showModalAviso = true } // mínimo, carrito/opciones
            5 -> showModalEnviada = true
            else -> CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }
}

// ── piezas de la pantalla ─────────────────────────────────────────────────────

@Composable
private fun Tarjeta(
    icono: ImageVector,
    titulo: String,
    accion: String? = null,
    onAccion: () -> Unit = {},
    contenido: @Composable () -> Unit,
) {
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
                    Icon(icono, contentDescription = null, tint = AppColors.colorAppPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto,
                    modifier = Modifier.weight(1f)
                )
                if (accion != null) {
                    TextButton(onClick = onAccion) {
                        Text(accion, color = AppColors.colorAppPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            contenido()
        }
    }
}

@Composable
private fun FilaResumen(producto: ModeloResumenProductoOrden) {
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
            Text(
                text = producto.nombre ?: "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorTexto
            )
            producto.opciones.forEach {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6D6A75))
            }
            if (!producto.nota.isNullOrBlank()) {
                Text(
                    text = stringResource(Res.string.nota) + ": " + producto.nota,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF6D6A75)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$" + (producto.total ?: ""),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colorTexto
        )
    }
}

@Composable
private fun AvisoMinimo(faltante: String?, minimo: String?, mensaje: String?, onSeguirComprando: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorAvisoFondo),
        border = BorderStroke(1.dp, colorAviso.copy(alpha = 0.35f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = colorAviso)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.minimo_de_consumo),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorAviso
                )
                Text(
                    text = if (!faltante.isNullOrBlank() && !minimo.isNullOrBlank())
                        stringResource(Res.string.orden_te_faltan, faltante, minimo)
                    else mensaje ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto,
                    modifier = Modifier.padding(top = 2.dp)
                )
                TextButton(
                    onClick = onSeguirComprando,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(stringResource(Res.string.orden_seguir_comprando), color = colorAviso, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BarraConfirmar(total: String, habilitado: Boolean, avisoCerrado: String? = null, onConfirmar: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.total_a_pagar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = total,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
            }
            if (avisoCerrado != null) {
                Text(
                    text = avisoCerrado,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC62828),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onConfirmar,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.colorAppPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(Res.string.confirmar_orden),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
