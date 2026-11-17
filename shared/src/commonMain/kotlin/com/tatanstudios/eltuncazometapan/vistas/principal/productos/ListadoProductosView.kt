package com.tatanstudios.eltuncazometapan.vistas.principal.productos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColor
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosArray
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.viewmodel.ListadoProductosViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosTerceraArray
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.saveable.rememberSaveable
import com.tatanstudios.eltuncazometapan.componentes.ModalIniciarSesion
import com.tatanstudios.eltuncazometapan.componentes.haySesion

@Composable
fun ListadoProductosScreen(navController: NavHostController,
                           idCategoria: Int,
                           viewModel: ListadoProductosViewModel = viewModel { ListadoProductosViewModel() }
) {

    val ctx = LocalPlatformContext.current
    var boolDatosCargados by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val scope = rememberCoroutineScope() // Crea el alcance de coroutine
    val keyboardController = LocalSoftwareKeyboardController.current

    var modeloListaProductosArray: List<ModeloProductosArray> by remember { mutableStateOf(listOf<ModeloProductosArray>()) }
    // 1 = lista, 2 = tienda: lo define el panel para cada servicio
    var tipoVista by remember { mutableStateOf(1) }

    val abrirProducto: (Int) -> Unit = { idProducto ->
        navController.navigate(Routes.VistaInformacionProducto.createRoute(idProducto)) {
            popUpTo(Routes.VistaInformacionProducto.route) { inclusive = true }
            launchSingleTop = true
        }
    }


    // texto del buscador (se conserva al volver de ver un producto)
    var busqueda by rememberSaveable { mutableStateOf("") }
    val sesion = haySesion()
    var mostrarAvisoSesion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // ocultar teclado al entrar (solo una vez, para no cerrarlo mientras se escribe en el buscador)
        keyboardController?.hide()
        scope.launch {
            viewModel.listadoProductosRetrofit(idCategoria)
        }
    }


    Scaffold(
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.productos),
                AppColors.colorAppPrimary,
                acciones = {
                    // carrito: con sesión se abre, sin sesión se ofrece iniciar sesión
                    IconButton(onClick = {
                        if (sesion) {
                            navController.navigate(Routes.VistaCarrito.route) { launchSingleTop = true }
                        } else {
                            mostrarAvisoSesion = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(Res.string.carrito),
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (boolDatosCargados && modeloListaProductosArray.isNotEmpty()) {
                BuscadorProductos(texto = busqueda, onTextoChange = { busqueda = it })
            }

            Box(modifier = Modifier.fillMaxSize()) {
            val filtrados = filtrarProductos(modeloListaProductosArray, busqueda)

            if (boolDatosCargados && modeloListaProductosArray.isEmpty()) {
                Text(
                    text = stringResource(Res.string.sin_productos_disponibles),
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            } else if (boolDatosCargados && tipoVista == 2) {
                VistaTiendaProductos(
                    categorias = modeloListaProductosArray,
                    busqueda = busqueda,
                    onProductoClick = abrirProducto
                )
            } else if (boolDatosCargados && filtrados.isEmpty()) {
                SinResultadosBusqueda(Modifier.align(Alignment.TopCenter))
            } else if (boolDatosCargados) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    filtrados.forEach { categoria ->
                        // Encabezado de categoría (rojo subrayado + fondo crema)
                        item {
                            CategoriaHeader(title = categoria.nombre ?: "")
                        }

                        // Productos de la categoría
                        items(categoria.listaProductoCategoriaTercera) { producto ->
                            ProductoItemCard(
                                producto = producto,
                                onClick = { abrirProducto(producto.id) }
                            )
                        }
                    }
                }
            }
            }
        }

        if (mostrarAvisoSesion) {
            ModalIniciarSesion(
                showDialog = true,
                mensaje = stringResource(Res.string.inicia_sesion_para_carrito),
                navController = navController,
                onDismiss = { mostrarAvisoSesion = false }
            )
        }

        if (isLoading) LoadingModal(isLoading = true)

        resultado?.getContentIfNotHandled()?.let { result ->
            when (result.success) {
                1 -> {
                    modeloListaProductosArray = result.listaProductoCategoria
                    tipoVista = result.tipoVista
                    boolDatosCargados = true
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




}

@Composable
fun SinResultadosBusqueda(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.sin_resultados_busqueda),
        modifier = modifier.fillMaxWidth().padding(32.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = Color.DarkGray,
        textAlign = TextAlign.Center
    )
}

// Encabezado de categoría con estilo solicitado
@Composable
fun CategoriaHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.colorBlanco) // define este color (p.ej. #FFFDF4)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = AppColors.colorAppPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(
            color = AppColors.colorAppPrimary,
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 4.dp)
        )
    }
}

