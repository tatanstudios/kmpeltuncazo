package com.tatanstudios.eltuncazometapan.vistas.opciones.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.componentes.AvisoEstadoLocal
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColorMenuPrincipal
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.extras.EstadoCarrito
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloMenuPrincipalCategoriasArray
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.ListadoMenuPrincipal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MenuPrincipalScreen(
    navController: NavHostController,
    viewModel: ListadoMenuPrincipal = viewModel { ListadoMenuPrincipal() },
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current

    val isLoading by viewModel.isLoading.collectAsState()
    val fallo by viewModel.fallo.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    var errorCarga by remember { mutableStateOf(false) }
    var menuCargado by remember { mutableStateOf(false) }

    var categorias by remember { mutableStateOf(listOf<ModeloMenuPrincipalCategoriasArray>()) }
    var abierto by remember { mutableStateOf(true) }
    var estadoTexto by remember { mutableStateOf<String?>(null) }
    var estadoDetalle by remember { mutableStateOf<String?>(null) }

    var refrescando by remember { mutableStateOf(false) }
    val stringErrorReintentar = stringResource(Res.string.error_reintentar_de_nuevo)

    fun cargar() {
        errorCarga = false
        scope.launch {
            // la API exige "id" aunque no lo usa; sin sesión se envía 0
            val id = TokenManager.idUsuario.first().ifEmpty { "0" }
            viewModel.listadoMenuPrincipalRetrofit(id)
        }
    }

    // se recarga al volver al menú: así el estado del local y el contador del carrito están al día
    LifecycleResumeEffect(Unit) {
        cargar()
        onPauseOrDispose { }
    }

    val pullRefreshState = rememberPullRefreshState(refrescando, { refrescando = true; cargar() })

    LaunchedEffect(resultado) {
        resultado?.getContentIfNotHandled()?.let { result ->
            refrescando = false
            when (result.success) {
                1 -> {
                    categorias = result.arrayCategorias
                    abierto = result.abierto == 1
                    estadoTexto = result.estadoTexto
                    estadoDetalle = result.estadoDetalle
                    EstadoCarrito.cantidad.value = result.cantidadCarrito
                    menuCargado = true
                }
                else -> {
                    errorCarga = true
                    CustomToasty(ctx, stringErrorReintentar, ToastType.ERROR)
                }
            }
        }
    }
    // si falla la red tampoco debe quedarse girando el indicador
    LaunchedEffect(fallo) { if (fallo) refrescando = false }

    Scaffold(
        topBar = {
            BarraToolbarColorMenuPrincipal(
                navController,
                stringResource(Res.string.menu),
                AppColors.colorAppPrimary
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->

        val totalPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.colorCremaV1)
                .padding(top = totalPadding.calculateTopPadding())
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 10.dp,
                    bottom = totalPadding.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── estado del local ──
                if (menuCargado && !estadoTexto.isNullOrBlank()) {
                    item { AvisoEstadoLocal(abierto, estadoTexto!!, estadoDetalle, Modifier.padding(horizontal = 16.dp)) }
                }

                items(categorias, key = { it.id }) { categoria ->
                    TarjetaCategoria(categoria) {
                        navController.navigate(Routes.VistaListadoProductos.createRoute(categoria.id)) {
                            popUpTo(Routes.VistaListadoProductos.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }

            // ── cargando / error (mientras no hay categorías) ──
            if (categorias.isEmpty() && !menuCargado) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isLoading || (!fallo && !errorCarga)) {
                        CircularProgressIndicator(color = AppColors.colorAppPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(Res.string.cargando_menu),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.no_se_pudo_cargar_menu),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cargar() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.colorAppPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(stringResource(Res.string.reintentar))
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refrescando,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = AppColors.colorAppPrimary
            )
        }
    }
}

/** Bloque del menú: imagen y una barra delgada con el nombre. */
@Composable
private fun TarjetaCategoria(categoria: ModeloMenuPrincipalCategoriasArray, onClick: () -> Unit) {
    val ctx = LocalPlatformContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f) // un poco más baja que antes (16:9) para ver más bloques
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            var cargando by remember { mutableStateOf(true) }

            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data("${ApiConfig.URL_IMAGENES}${categoria.imagen}")
                    .crossfade(true)
                    .build(),
                contentDescription = categoria.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onLoading = { cargando = true },
                onSuccess = { cargando = false },
                onError = { cargando = false },
                error = painterResource(Res.drawable.camaradefecto),
            )

            if (cargando) {
                Image(
                    painter = painterResource(Res.drawable.spinloading),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoria.nombre ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1B24),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Ver productos",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.colorAppPrimary
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.colorAppPrimary
            )
        }
    }
}
