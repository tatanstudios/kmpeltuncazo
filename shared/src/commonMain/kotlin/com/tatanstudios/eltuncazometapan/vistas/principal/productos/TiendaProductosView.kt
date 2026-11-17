package com.tatanstudios.eltuncazometapan.vistas.principal.productos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosArray
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosTerceraArray
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Vista "tienda" de un servicio (tipo_vista = 2 en el panel):
 * filtros por categoría arriba y los productos en cuadrícula de 2 columnas.
 */
@Composable
fun VistaTiendaProductos(
    categorias: List<ModeloProductosArray>,
    busqueda: String,
    onProductoClick: (Int) -> Unit
) {
    // id de la categoría elegida; 0 = todas
    var categoriaElegida by rememberSaveable { mutableStateOf(0) }
    // los chips muestran todas las categorías; la búsqueda filtra solo los productos
    val filtradas = filtrarProductos(categorias, busqueda)
    val visibles = if (categoriaElegida == 0) filtradas else filtradas.filter { it.id == categoriaElegida }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── FILTROS POR CATEGORÍA ──
        if (categorias.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().background(Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ChipCategoria(stringResource(Res.string.todos), categoriaElegida == 0) { categoriaElegida = 0 }
                }
                items(categorias) { categoria ->
                    ChipCategoria(categoria.nombre ?: "", categoriaElegida == categoria.id) {
                        categoriaElegida = categoria.id
                    }
                }
            }
        }

        if (visibles.isEmpty()) {
            SinResultadosBusqueda()
            return@Column
        }

        // ── PRODUCTOS EN CUADRÍCULA ──
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().background(AppColors.colorCremaV1),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            visibles.forEach { categoria ->
                // título de la categoría ocupando todo el ancho (solo con "Todos" y varias categorías)
                if (categoriaElegida == 0 && categorias.size > 1) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CategoriaHeader(title = categoria.nombre ?: "")
                    }
                }
                items(categoria.listaProductoCategoriaTercera, key = { "${categoria.id}-${it.id}" }) { producto ->
                    ProductoTiendaCard(producto) { onProductoClick(producto.id) }
                }
            }
        }
    }
}

@Composable
private fun ChipCategoria(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto, maxLines = 1) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.colorAppPrimary,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun ProductoTiendaCard(producto: ModeloProductosTerceraArray, onClick: () -> Unit) {
    val traeImagen = producto.utilizaImagen == 1 && !producto.imagen.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (traeImagen) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data("${ApiConfig.URL_IMAGENES}${producto.imagen}")
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(Res.drawable.spinloading),
                        error = painterResource(Res.drawable.camaradefecto),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.camaradefecto),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = producto.nombre ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!producto.precio.isNullOrBlank()) {
                    Text(
                        text = "$${producto.precio}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorAppPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
