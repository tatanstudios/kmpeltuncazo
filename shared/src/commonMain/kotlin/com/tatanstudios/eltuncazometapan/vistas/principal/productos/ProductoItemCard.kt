package com.tatanstudios.eltuncazometapan.vistas.principal.productos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosTerceraArray
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource

private val tamanoImagen = 84.dp

/**
 * Producto en la vista "lista": nombre, descripción corta y precio a la izquierda,
 * imagen cuadrada a la derecha centrada verticalmente con el texto.
 */
@Composable
fun ProductoItemCard(
    producto: ModeloProductosTerceraArray,
    onClick: () -> Unit
) {
    val traeImagen = producto.utilizaImagen == 1 && !producto.imagen.isNullOrBlank()
    val descripcion = producto.descripcion
        ?.replace("\\r\\n|\\n|\r\n".toRegex(), " ")
        ?.trim()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = tamanoImagen + 24.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // texto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = producto.nombre ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F1B24),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!descripcion.isNullOrBlank()) {
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!producto.precio.isNullOrBlank()) {
                    Text(
                        text = "$${producto.precio}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorAppPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // imagen (o marcador gris si no tiene)
            Box(
                modifier = Modifier
                    .size(tamanoImagen)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F0F7)),
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
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 0.6f
                    )
                }
            }
        }
    }
}
