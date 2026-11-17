package com.tatanstudios.eltuncazometapan.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val colorAbierto = Color(0xFF2E7D32)
private val colorCerrado = Color(0xFFC62828)

/**
 * Estado del local (lo calcula la API con el horario y "Cerrar App" del panel).
 * Verde con un punto si está abierto; rojo con un reloj si está cerrado.
 * Se usa en el Menú y en Enviar orden.
 */
@Composable
fun AvisoEstadoLocal(abierto: Boolean, texto: String, detalle: String?, modifier: Modifier = Modifier) {
    val color = if (abierto) colorAbierto else colorCerrado

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (abierto) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            } else {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (!detalle.isNullOrBlank()) {
                    Text(
                        text = detalle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D6A75)
                    )
                }
            }
        }
    }
}
