package com.tatanstudios.eltuncazometapan.vistas.principal.carrito

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColorCarritoCompras
import com.tatanstudios.eltuncazometapan.componentes.CustomModal2Botones
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloCarritoTemporal
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.viewmodel.BorrarCarritoComprasViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.BorrarFilaCarritoViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.ListadoCarritoComprasViewModel
import kotlinx.coroutines.launch

@Composable
fun CarritoComprasScreen(
    navController: NavHostController,
    viewModel: ListadoCarritoComprasViewModel = viewModel { ListadoCarritoComprasViewModel() },
    viewModelBorrarCarrito: BorrarCarritoComprasViewModel = viewModel { BorrarCarritoComprasViewModel() },
    viewModelBorrarFila: BorrarFilaCarritoViewModel = viewModel { BorrarFilaCarritoViewModel() }
) {
    val ctx = LocalPlatformContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()

    val isLoadingBorrar by viewModelBorrarCarrito.isLoading.collectAsState()
    val resultadoBorrar by viewModelBorrarCarrito.resultado.collectAsState()

    val isLoadingFilaBorrar by viewModelBorrarFila.isLoading.collectAsState()
    val resultadoFilaBorrar by viewModelBorrarFila.resultado.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var idusuario by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }
    var hayProductoNoDisponible by remember { mutableStateOf(false) }
    var hayDireccionRegistrada by remember { mutableStateOf(true) }

    var subtotal: String by remember { mutableStateOf("") }
    var modeloListaCarritoArray by remember {
        mutableStateOf(listOf<ModeloCarritoTemporal>())
    }

    var showModal2BotonParaBorrarCarrito by remember { mutableStateOf(false) }
    var recargarListado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        idusuario = TokenManager.idUsuario.first()
        viewModel.listadoCarritoComprasRetrofit(idusuario)
    }

    LaunchedEffect(recargarListado) {
        if (recargarListado && idusuario.isNotEmpty()) {
            recargarListado = false
            viewModel.listadoCarritoComprasRetrofit(idusuario)
        }
    }

    LaunchedEffect(Unit) { keyboardController?.hide() }

    Scaffold(
        containerColor = Color(0xFFF6F4FA),
        topBar = {
            BarraToolbarColorCarritoCompras(
                navController,
                stringResource(Res.string.carrito),
                AppColors.colorAppPrimary,
                onDeleteClick = {
                    if (datosCargados) {
                        if (modeloListaCarritoArray.isNotEmpty()) {
                            showModal2BotonParaBorrarCarrito = true
                        } else {
                            CustomToasty(ctx, "No hay Productos", ToastType.INFO)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (datosCargados && modeloListaCarritoArray.isNotEmpty()) {
                val carritoVacio = false
                BarraSubtotal(
                    subtotal = subtotal,
                    cantidadProductos = modeloListaCarritoArray.sumOf { it.cantidad },
                    sinDireccion = !hayDireccionRegistrada,
                    hayNoDisponible = hayProductoNoDisponible,
                    onClick = {
                        when {
                            carritoVacio -> { /* no hace nada */ }
                            !hayDireccionRegistrada -> {
                                // Redirigir a agregar dirección
                                navController.navigate(
                                    Routes.VistaMisDirecciones.createRoute(0)
                                ) {
                                    launchSingleTop = true
                                }
                            }
                            !hayProductoNoDisponible -> {
                                navController.navigate(Routes.VistaEnviarOrden.route) {
                                    popUpTo(Routes.VistaEnviarOrden.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (datosCargados) {
                if (modeloListaCarritoArray.isEmpty()) {
                    // ── Carrito vacío ──────────────────────────────────────
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(Res.string.carrito_vacio),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // ── Lista de productos ─────────────────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(Res.string.carrito_ayuda),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                            )
                        }
                        items(
                            items = modeloListaCarritoArray,
                            key = { it.carritoid }
                        ) { p ->
                            ItemCarritoSwipeAction(
                                p = p,
                                onClick = { item ->
                                    navController.navigate(
                                        Routes.VistaEditarProducto.createRoute(item.carritoid)
                                    ) {
                                        popUpTo(Routes.VistaInformacionProducto.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onSwipeAction = { item ->
                                    scope.launch {
                                        viewModelBorrarFila.eliminarFilaCarritoRetrofit(idusuario, item.carritoid)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (isLoading) LoadingModal(true)
            if (isLoadingBorrar) LoadingModal(true)
            if (isLoadingFilaBorrar) LoadingModal(true)

            if (showModal2BotonParaBorrarCarrito) {
                CustomModal2Botones(
                    showDialog = true,
                    message = stringResource(Res.string.borrar_carrito),
                    onDismiss = { showModal2BotonParaBorrarCarrito = false },
                    onAccept = {
                        showModal2BotonParaBorrarCarrito = false
                        scope.launch {
                            viewModelBorrarCarrito.eliminarCarritoComprasRetrofit(idusuario)
                        }
                    },
                    stringResource(Res.string.si),
                    stringResource(Res.string.no),
                )
            }
        }
    }

    // ── Resultado listado ──────────────────────────────────────────────────
    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                modeloListaCarritoArray = result.listadoCarritoTemporal
                hayProductoNoDisponible = (result.estadoProductoGlobal == 1)
                hayDireccionRegistrada = (result.hayDireccionRegistrada == 1)
                subtotal = result.subTotal ?: ""
                datosCargados = true
            }
            2 -> {
                modeloListaCarritoArray = emptyList()
                datosCargados = true
                subtotal = "0.00"
            }
            else -> {
                CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
            }
        }
    }

    // ── Resultado borrar carrito completo ──────────────────────────────────
    resultadoBorrar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1, 2 -> {
                CustomToasty(ctx, stringResource(Res.string.carrito_borrado), ToastType.SUCCESS)
                navController.popBackStack()
            }
            else -> {
                CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
            }
        }
    }

    // ── Resultado borrar fila ──────────────────────────────────────────────
    resultadoFilaBorrar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                datosCargados = false
                CustomToasty(ctx, stringResource(Res.string.carrito_borrado), ToastType.SUCCESS)
                navController.popBackStack()
            }
            2, 3 -> {
                datosCargados = false
                CustomToasty(ctx, stringResource(Res.string.producto_eliminado), ToastType.SUCCESS)
                recargarListado = true
            }
            else -> {
                CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
            }
        }
    }
}

