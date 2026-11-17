package com.tatanstudios.eltuncazometapan.vistas.principal.carrito

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloCarritoTemporal
import com.tatanstudios.eltuncazometapan.network.ApiConfig
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val formaItem = RoundedCornerShape(16.dp)
private val colorTexto = Color(0xFF1F1B24)
private val colorRojo = Color(0xFFE53935)
private val colorNaranja = Color(0xFFF57C00)

// ── Fila del carrito con "deslizar para quitar" ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCarritoSwipeAction(
    p: ModeloCarritoTemporal,
    onClick: (ModeloCarritoTemporal) -> Unit,
    onSwipeAction: (ModeloCarritoTemporal) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onSwipeAction(p)
            false // la fila se quita cuando el servidor responde y se recarga la lista
        },
        positionalThreshold = { distance -> distance * 0.4f }
    )

    SwipeToDismissBox(
        modifier = Modifier.fillMaxWidth().clip(formaItem),
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(formaItem)
                    .background(colorRojo)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Text(
                        text = stringResource(Res.string.quitar),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        content = { ItemCarritoCard(p = p, onClick = onClick) }
    )
}

// ── Tarjeta del producto ──────────────────────────────────────────────────────

@Composable
private fun ItemCarritoCard(
    p: ModeloCarritoTemporal,
    onClick: (ModeloCarritoTemporal) -> Unit
) {
    val noDisponible = p.activo == 0

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(p) },
        shape = formaItem,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // imagen
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F0F7)),
                contentAlignment = Alignment.Center
            ) {
                if (p.utilizaImagen == 1 && !p.imagen.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data("${ApiConfig.URL_IMAGENES}${p.imagen}")
                            .crossfade(true)
                            .build(),
                        contentDescription = p.nombre,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(Res.drawable.spinloading),
                        error = painterResource(Res.drawable.camaradefecto),
                        contentScale = ContentScale.Crop,
                        alpha = if (noDisponible) 0.4f else 1f
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.camaradefecto),
                        contentDescription = p.nombre,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 0.6f
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // nombre, precio unitario, nota y aviso
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = p.nombre ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (noDisponible) Color.Gray else colorTexto,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val unitario = p.precioUnitario
                if (!unitario.isNullOrBlank()) {
                    // con opciones se muestra también el precio base: "Base $0.75 · $4.25 c/u"
                    val base = p.precioBase
                    val textoUnitario = stringResource(Res.string.carrito_precio_unitario, unitario)
                    Text(
                        text = if (!base.isNullOrBlank() && base != unitario)
                            stringResource(Res.string.carrito_precio_base, base) + " · " + textoUnitario
                        else textoUnitario,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // opciones elegidas, una línea por bloque
                p.opciones.forEach { linea ->
                    Text(
                        text = linea,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4A4458),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val nota = p.nota
                if (!nota.isNullOrBlank()) {
                    Text(
                        text = stringResource(Res.string.nota) + ": " + nota,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF6D6A75),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (noDisponible) {
                    val aviso = listOf(p.titulo, p.mensaje).filter { !it.isNullOrBlank() }.joinToString(" · ")
                    Text(
                        text = aviso,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorRojo)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // cantidad y total de la fila
            Column(horizontalAlignment = Alignment.End) {
                CantidadBadge(cantidad = p.cantidad)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$" + (p.precioformat ?: p.precio.orEmpty()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (noDisponible) Color.Gray else AppColors.colorAppPrimary,
                    textDecoration = if (noDisponible) TextDecoration.LineThrough else null
                )
            }
        }
    }
}

@Composable
private fun CantidadBadge(cantidad: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.colorAppPrimary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = "x$cantidad",
            color = AppColors.colorAppPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

// ── Resumen inferior: cantidad, subtotal y botón continuar ────────────────────

@Composable
internal fun BarraSubtotal(
    subtotal: String,
    cantidadProductos: Int,
    sinDireccion: Boolean,
    hayNoDisponible: Boolean,
    onClick: () -> Unit
) {
    val pedirDireccion = sinDireccion && !hayNoDisponible

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.carrito_productos_cantidad, cantidadProductos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(Res.string.subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$$subtotal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
            }

            // avisos que impiden continuar
            val aviso = when {
                hayNoDisponible -> stringResource(Res.string.carrito_quitar_no_disponibles)
                sinDireccion -> stringResource(Res.string.agregar_direccion_para_continuar)
                else -> null
            }
            if (aviso != null) {
                Text(
                    text = aviso,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hayNoDisponible) colorRojo else colorNaranja,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onClick,
                enabled = !hayNoDisponible,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pedirDireccion) colorNaranja else AppColors.colorAppPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color.White
                )
            ) {
                if (pedirDireccion) {
                    Icon(Icons.Filled.AddLocation, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = stringResource(
                        if (pedirDireccion) Res.string.carrito_agregar_direccion else Res.string.carrito_continuar
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!pedirDireccion) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
