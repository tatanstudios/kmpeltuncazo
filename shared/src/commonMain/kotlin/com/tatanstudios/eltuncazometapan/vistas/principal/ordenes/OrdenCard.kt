package com.tatanstudios.eltuncazometapan.vistas.principal.ordenes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloOrdenesArray
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors

private val colorTexto = Color(0xFF1F1B24)
private val colorSecundario = Color(0xFF6D6A75)

/** Estado que se muestra en la tarjeta según los datos de la orden. */
private enum class EstadoOrden(val texto: String, val color: Color, val fondo: Color, val ayuda: String?) {
    PENDIENTE("Pendiente", Color(0xFFB26A00), Color(0xFFFFF3E0), "Esperando que el local inicie tu orden"),
    INICIADA("Iniciada", Color(0xFF2E7D32), Color(0xFFE8F5E9), "El local inició tu orden"),
    CANCELADA("Cancelada", Color(0xFFC62828), Color(0xFFFFEBEE), null),
}

private fun estadoDe(orden: ModeloOrdenesArray) = when {
    orden.estadoCancelada == 1 -> EstadoOrden.CANCELADA
    orden.estadoIniciada == 1 -> EstadoOrden.INICIADA
    else -> EstadoOrden.PENDIENTE
}

@Composable
fun OrdenCard(
    orden: ModeloOrdenesArray,
    onVer: () -> Unit,
    onBorrar: () -> Unit,
) {
    val estado = estadoDe(orden)
    val cancelada = estado == EstadoOrden.CANCELADA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = !cancelada, onClick = onVer),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── número y estado ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Orden #${orden.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto,
                    modifier = Modifier.weight(1f)
                )
                ChipEstado(estado)
            }

            Spacer(Modifier.padding(top = 6.dp))
            Detalle(Icons.Filled.Schedule, orden.fechaOrden ?: "-")

            // ── productos ──
            if (!orden.resumenProductos.isNullOrBlank()) {
                Text(
                    text = orden.resumenProductos,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Detalle(Icons.Filled.LocationOn, orden.direccion ?: "-", Modifier.padding(top = 8.dp))

            if (!orden.notaOrden.isNullOrBlank()) {
                Text(
                    text = "Nota: ${orden.notaOrden}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = colorSecundario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // ── aviso según estado ──
            val aviso = if (cancelada) {
                val motivo = orden.mensajeCancelado?.takeIf { it.isNotBlank() }
                if (motivo != null) "Motivo: $motivo" else "La orden fue cancelada"
            } else estado.ayuda

            if (aviso != null) {
                Text(
                    text = aviso,
                    style = MaterialTheme.typography.bodySmall,
                    color = estado.color,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(estado.fondo)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            HorizontalDivider(color = Color(0xFFF0EEF4), modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))

            // ── total y acción ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (orden.cantidadProductos > 0)
                            "Total · ${orden.cantidadProductos} producto${if (orden.cantidadProductos == 1) "" else "s"}"
                        else "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorSecundario
                    )
                    Text(
                        text = orden.total ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (cancelada) Color.Gray else AppColors.colorAppPrimary
                    )
                }

                // iniciada o cancelada: ya no cambia, el cliente la puede ocultar de su lista
                if (estado != EstadoOrden.PENDIENTE) {
                    OutlinedButton(
                        onClick = onBorrar,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFD6D3DC)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = colorSecundario,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Ocultar", color = colorSecundario)
                    }
                }
                if (!cancelada) {
                    TextButton(onClick = onVer) {
                        Text("Ver detalle", color = AppColors.colorAppPrimary, fontWeight = FontWeight.SemiBold)
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = AppColors.colorAppPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipEstado(estado: EstadoOrden) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(estado.fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(estado.color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = estado.texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = estado.color
        )
    }
}

@Composable
private fun Detalle(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.Top, modifier = modifier) {
        Icon(icono, contentDescription = null, tint = colorSecundario, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = colorSecundario,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
