package com.tatanstudios.eltuncazometapan.vistas.principal.productos

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.draw.clip
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosDeOrdenArray
import com.tatanstudios.eltuncazometapan.viewmodel.ListadoProductosDeUnaOrdenViewModel

@Composable
fun ListadoProductosDeUnaOrdenScreen(
    navController: NavHostController,
    idorden: Int,
    viewModel: ListadoProductosDeUnaOrdenViewModel = viewModel { ListadoProductosDeUnaOrdenViewModel() }
) {
    val ctx = LocalPlatformContext.current
    var boolDatosCargados by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var modeloListaProductosArray: List<ModeloProductosDeOrdenArray> by remember { mutableStateOf(listOf()) }

    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.listadoProductosDeUnaOrdenRetrofit(idorden)
        }
    }

    keyboardController?.hide()

    Scaffold(
        topBar = {
            BarraToolbarColor(
                navController = navController,
                titulo = stringResource(Res.string.productos),
                backgroundColor = AppColors.colorAppPrimary,
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (boolDatosCargados) {
                if (modeloListaProductosArray.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay productos",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(
                            items = modeloListaProductosArray,
                            key = { index, item -> "${item.id}_$index" }
                        ) { _, producto ->
                            ProductoItemCardDeOrden(
                                producto = producto,
                                onClick = { }
                            )
                        }
                    }
                }
            }

            if (isLoading) LoadingModal(isLoading = true)

            resultado?.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    1 -> {
                        modeloListaProductosArray = result.productos
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
}


@Composable
fun ProductoItemCardDeOrden(
    producto: ModeloProductosDeOrdenArray,
    onClick: () -> Unit
) {
    val baseUrlImagenes = "${ApiConfig.URL_IMAGENES}${producto.imagen}"
    var traeImagen = (producto.utiliza_imagen == 1)
    if (producto.imagen.isNullOrBlank()) traeImagen = false

    val imageSlot = 96.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .padding(end = imageSlot),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = producto.nombre ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.colorNegro
                )

                // opciones elegidas, una línea por bloque
                producto.opciones.forEach { linea ->
                    Text(
                        text = linea,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4A4458)
                    )
                }

                Text(
                    text = "Precio: $${producto.precio}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorNegro
                )

                Text(
                    text = "Cantidad: ${producto.cantidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorNegro
                )

                Text(
                    text = "Total: ${producto.multiplicado}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorNegro
                )

                if (!producto.nota.isNullOrBlank()) {
                    Text(
                        text = "Nota: ${producto.nota}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorRojo
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(72.dp)
            ) {
                if (traeImagen) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(baseUrlImagenes)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(Res.drawable.spinloading),
                        error = painterResource(Res.drawable.camaradefecto),
                        contentDescription = producto.nombreproducto,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.camaradefecto),
                        contentDescription = producto.nombreproducto,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}