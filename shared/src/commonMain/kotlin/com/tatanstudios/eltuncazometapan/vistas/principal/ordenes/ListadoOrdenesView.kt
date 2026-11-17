package com.tatanstudios.eltuncazometapan.vistas.principal.ordenes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import kotlinx.coroutines.flow.first
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColorParaListaOrdenes
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloOrdenesArray
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.viewmodel.ListadoOrdenesViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.OcultarOrdenViewModel
import com.tatanstudios.eltuncazometapan.componentes.CustomModal2Botones

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListadoOrdenesScreen(
    navController: NavHostController,
    viewModel: ListadoOrdenesViewModel = viewModel { ListadoOrdenesViewModel() },
    viewModelOcultarOrden: OcultarOrdenViewModel = viewModel { OcultarOrdenViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()

    val isLoadingOcultar by viewModelOcultarOrden.isLoading.collectAsState()
    val resultadoOcultar by viewModelOcultarOrden.resultado.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    var idusuario by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }

    var modeloOrdenesArray: List<ModeloOrdenesArray> by remember { mutableStateOf(listOf()) }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.listadoOrdenesRetrofit(idusuario)
        }
    )

    LaunchedEffect(Unit) {
        idusuario = TokenManager.idUsuario.first()
        viewModel.listadoOrdenesRetrofit(idusuario)
    }

    LaunchedEffect(Unit) { keyboardController?.hide() }

    var ordenABorrar by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = Color(0xFFF6F4FA),
        topBar = {
            BarraToolbarColorParaListaOrdenes(
                stringResource(Res.string.ordenes),
                AppColors.colorAppPrimary,
            )
        },
    ) { innerPadding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            if (datosCargados) {
                if (modeloOrdenesArray.isEmpty()) {
                    // Estado vacío
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes órdenes",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tus pedidos aparecerán aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = innerPadding.calculateBottomPadding() + 72.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = modeloOrdenesArray,
                            key = { it.id }
                        ) { orden ->
                            OrdenCard(
                                orden = orden,
                                onVer = {
                                    navController.navigate(
                                        Routes.VistaEstadoOrden.createRoute(orden.id)
                                    ) { launchSingleTop = true }
                                },
                                onBorrar = { ordenABorrar = orden.id }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // al deslizar para actualizar ya se ve el indicador; el modal solo en la primera carga
            if (isLoading && !isRefreshing && !datosCargados) LoadingModal(true)
            if (isLoadingOcultar) LoadingModal(true)

            ordenABorrar?.let { id ->
                CustomModal2Botones(
                    showDialog = true,
                    message = "¿Ocultar la orden #$id? Ya no aparecerá en tu lista.",
                    onDismiss = { ordenABorrar = null },
                    onAccept = {
                        ordenABorrar = null
                        viewModelOcultarOrden.ocultarOrdenRetrofit(id)
                    },
                    stringResource(Res.string.si),
                    stringResource(Res.string.no),
                )
            }
        }
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                datosCargados = true
                modeloOrdenesArray = result.ordenes
                isRefreshing = false
            }
            else -> {
                isRefreshing = false
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
            }
        }
    }

    resultadoOcultar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                viewModel.listadoOrdenesRetrofit(idusuario)
            }
            else -> {
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
            }
        }
    }
}