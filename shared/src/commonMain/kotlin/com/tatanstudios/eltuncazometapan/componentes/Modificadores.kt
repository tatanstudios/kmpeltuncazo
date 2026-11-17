package com.tatanstudios.eltuncazometapan.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tatanstudios.eltuncazometapan.extras.formatoDosDecimales
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloModificadorGrupo
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloModificadorOpcion
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloSeleccionOpcion
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors

/*
 * MODIFICADORES DE PRODUCTO
 * Mismas reglas que valida el servidor (app/Services/Modificadores.php):
 *  - Requerido: debe elegir al menos [ModeloModificadorGrupo.minimo] unidades.
 *  - Opcional: puede no elegir nada; si elige, debe cumplir el mínimo.
 *  - Máximo del bloque 1: tocar otra opción la cambia.
 *  - Máximo de la opción: veces que se puede elegir la misma opción.
 *  - Obligatorio: viene marcada y no se puede quitar.
 *  - Una sola: no se combina con otras opciones del bloque.
 *
 * La selección es un mapa idOpcion -> cantidad.
 */

private val colorRojoAviso = Color(0xFFD32F2F)

/** Opciones obligatorias ya marcadas. */
fun seleccionInicial(grupos: List<ModeloModificadorGrupo>): Map<Int, Int> =
    grupos.flatMap { it.opciones }.filter { it.obligatorio == 1 }.associate { it.id to 1 }

/** Selección guardada en el carrito, quitando opciones que ya no existen y agregando las obligatorias. */
fun seleccionGuardada(grupos: List<ModeloModificadorGrupo>, guardada: List<ModeloSeleccionOpcion>): Map<Int, Int> {
    val existentes = grupos.flatMap { it.opciones }.associateBy { it.id }
    val seleccion = guardada
        .filter { existentes.containsKey(it.idOpcion) && it.cantidad > 0 }
        .associate { it.idOpcion to it.cantidad.coerceAtMost(existentes.getValue(it.idOpcion).maximo.coerceAtLeast(1)) }
    return seleccionInicial(grupos) + seleccion
}

/** Precio extra por unidad del producto. */
fun precioExtraModificadores(grupos: List<ModeloModificadorGrupo>, seleccion: Map<Int, Int>): Double =
    grupos.flatMap { it.opciones }.sumOf { op ->
        (seleccion[op.id] ?: 0) * (op.precio.toDoubleOrNull() ?: 0.0)
    }

/** Formato que espera la API: "idOpcion:cantidad,idOpcion:cantidad". */
fun seleccionParaEnviar(seleccion: Map<Int, Int>): String =
    seleccion.filterValues { it > 0 }.entries.joinToString(",") { "${it.key}:${it.value}" }

fun reglaModificador(grupo: ModeloModificadorGrupo): String {
    val min = grupo.minimo
    val max = grupo.maximo.coerceAtLeast(1)
    return when {
        min == max -> "Elige $max"
        min <= 0 -> "Elige hasta $max"
        else -> "Elige de $min a $max"
    }
}

/** Error del bloque o null si cumple las reglas. */
fun errorModificador(grupo: ModeloModificadorGrupo, seleccion: Map<Int, Int>): String? {
    val elegidas = grupo.opciones.filter { (seleccion[it.id] ?: 0) > 0 }
    val total = elegidas.sumOf { seleccion[it.id] ?: 0 }
    val max = grupo.maximo.coerceAtLeast(1)

    grupo.opciones.firstOrNull { it.obligatorio == 1 && (seleccion[it.id] ?: 0) == 0 }?.let {
        return "«${it.nombre}» es obligatorio"
    }
    if (elegidas.size > 1) {
        elegidas.firstOrNull { it.unaSola == 1 }?.let { return "«${it.nombre}» no se combina con otras opciones" }
    }
    if (total > max) return "Máximo $max"
    if ((grupo.requerido == 1 || total > 0) && total < grupo.minimo) return reglaModificador(grupo)
    return null
}

/** Primer bloque con error, con el nombre del bloque, para mostrarlo en un aviso. */
fun primerErrorModificadores(grupos: List<ModeloModificadorGrupo>, seleccion: Map<Int, Int>): String? =
    grupos.firstNotNullOfOrNull { g -> errorModificador(g, seleccion)?.let { "${g.nombre}: $it" } }

/**
 * Cambia la cantidad de una opción aplicando las reglas.
 * Devuelve la nueva selección, o un aviso si el cambio no se puede hacer.
 */
fun cambiarOpcionModificador(
    grupo: ModeloModificadorGrupo,
    opcion: ModeloModificadorOpcion,
    nuevaCantidad: Int,
    seleccion: Map<Int, Int>,
): Pair<Map<Int, Int>, String?> {
    val cantidad = nuevaCantidad.coerceIn(0, opcion.maximo.coerceAtLeast(1))
    val nueva = seleccion.toMutableMap()
    val idsGrupo = grupo.opciones.map { it.id }.toSet()

    if (cantidad == 0) {
        if (opcion.obligatorio == 1) return seleccion to null
        nueva.remove(opcion.id)
        return nueva to null
    }

    val otras = grupo.opciones.filter { it.id != opcion.id && it.obligatorio == 0 }
    if (opcion.unaSola == 1) {
        otras.forEach { nueva.remove(it.id) } // "una sola" quita las demás
    } else {
        otras.filter { it.unaSola == 1 }.forEach { nueva.remove(it.id) } // y otras quitan a "una sola"
    }
    nueva[opcion.id] = cantidad

    val max = grupo.maximo.coerceAtLeast(1)
    val total = nueva.filterKeys { it in idsGrupo }.values.sum()
    if (total > max) {
        if (max == 1) {
            otras.forEach { nueva.remove(it.id) } // tocar otra la cambia
        } else {
            return seleccion to "${grupo.nombre}: máximo $max"
        }
    }
    return nueva to null
}

/**
 * Bloques de opciones del producto.
 * [mostrarErrores] se activa después de intentar agregar, para marcar en rojo lo que falta.
 */
@Composable
fun SeccionModificadores(
    grupos: List<ModeloModificadorGrupo>,
    seleccion: Map<Int, Int>,
    mostrarErrores: Boolean,
    onCambio: (Map<Int, Int>) -> Unit,
    onAviso: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        grupos.forEach { grupo ->
            BloqueModificador(
                grupo = grupo,
                seleccion = seleccion,
                error = if (mostrarErrores) errorModificador(grupo, seleccion) else null,
                onCambiar = { opcion, cantidad ->
                    val (nueva, aviso) = cambiarOpcionModificador(grupo, opcion, cantidad, seleccion)
                    if (aviso != null) onAviso(aviso) else onCambio(nueva)
                }
            )
        }
    }
}

@Composable
private fun BloqueModificador(
    grupo: ModeloModificadorGrupo,
    seleccion: Map<Int, Int>,
    error: String?,
    onCambiar: (ModeloModificadorOpcion, Int) -> Unit,
) {
    val requerido = grupo.requerido == 1
    val esRadio = grupo.maximo <= 1
    val elegidas = grupo.opciones.sumOf { seleccion[it.id] ?: 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (error != null) BorderStroke(1.5.dp, colorRojoAviso) else null
    ) {
        // encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F4FA))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grupo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1B24)
                )
                Text(
                    text = error ?: reglaModificador(grupo),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error != null) colorRojoAviso else Color.Gray
                )
            }
            Text(
                text = if (requerido) "Requerido" else "Opcional",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (requerido) Color.White else Color.DarkGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        when {
                            !requerido -> Color(0xFFE6E4EC)
                            elegidas >= grupo.minimo && error == null -> Color(0xFF43A047) // ya cumple
                            else -> Color(0xFFE53935)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        grupo.opciones.forEachIndexed { index, opcion ->
            if (index > 0) HorizontalDivider(color = Color(0xFFF0EEF4))
            FilaOpcion(
                opcion = opcion,
                cantidad = seleccion[opcion.id] ?: 0,
                esRadio = esRadio,
                onCambiar = { onCambiar(opcion, it) }
            )
        }
    }
}

@Composable
private fun FilaOpcion(
    opcion: ModeloModificadorOpcion,
    cantidad: Int,
    esRadio: Boolean,
    onCambiar: (Int) -> Unit,
) {
    val obligatorio = opcion.obligatorio == 1
    val multiple = opcion.maximo > 1
    val precio = opcion.precio.toDoubleOrNull() ?: 0.0
    val marcada = cantidad > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !obligatorio && !multiple) { onCambiar(if (marcada && !esRadio) 0 else 1) }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            multiple -> Spacer(Modifier.width(12.dp))
            esRadio -> RadioButton(
                selected = marcada,
                onClick = if (obligatorio) null else ({ onCambiar(1) }),
                enabled = !obligatorio,
                colors = RadioButtonDefaults.colors(selectedColor = AppColors.colorAppPrimary)
            )
            else -> Checkbox(
                checked = marcada,
                onCheckedChange = if (obligatorio) null else ({ onCambiar(if (it) 1 else 0) }),
                enabled = !obligatorio,
                colors = CheckboxDefaults.colors(checkedColor = AppColors.colorAppPrimary)
            )
        }

        Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp)) {
            Text(
                text = opcion.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (marcada) FontWeight.SemiBold else FontWeight.Normal
            )
            val detalle = buildList {
                if (obligatorio) add("Incluido")
                if (opcion.unaSola == 1) add("No se combina")
                if (multiple) add("Hasta ${opcion.maximo}")
            }
            if (detalle.isNotEmpty()) {
                Text(
                    text = detalle.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        if (precio > 0) {
            Text(
                text = "+$" + formatoDosDecimales(precio),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.colorAppPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if (multiple) {
            StepperOpcion(
                cantidad = cantidad,
                maximo = opcion.maximo,
                minimo = if (obligatorio) 1 else 0,
                onCambiar = onCambiar
            )
        }
    }
}

@Composable
private fun StepperOpcion(cantidad: Int, maximo: Int, minimo: Int, onCambiar: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 6.dp)) {
        BotonCircular(enabled = cantidad > minimo, onClick = { onCambiar(cantidad - 1) }) {
            Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(18.dp))
        }
        Text(
            text = cantidad.toString(),
            modifier = Modifier.widthIn(min = 28.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
        BotonCircular(enabled = cantidad < maximo, onClick = { onCambiar(cantidad + 1) }) {
            Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun BotonCircular(enabled: Boolean, onClick: () -> Unit, contenido: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) AppColors.colorAppPrimary.copy(alpha = 0.12f) else Color(0xFFF0F0F0))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                if (enabled) AppColors.colorAppPrimary else Color.LightGray
        ) { contenido() }
    }
}
