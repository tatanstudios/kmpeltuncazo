package com.tatanstudios.eltuncazometapan.vistas.principal.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosArray
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

/** Campo de búsqueda que se usa arriba de la vista lista y de la vista tienda. */
@Composable
fun BuscadorProductos(texto: String, onTextoChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = texto,
        onValueChange = { if (it.length <= 50) onTextoChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        placeholder = { Text(stringResource(Res.string.buscar_producto), color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTextoChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.limpiar_busqueda),
                        tint = Color.Gray
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.colorAppPrimary,
            unfocusedBorderColor = Color.LightGray,
            cursorColor = AppColors.colorAppPrimary,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        )
    )
}

/**
 * Deja solo los productos cuyo nombre o descripción contienen el texto
 * (sin importar mayúsculas ni tildes). Las categorías que quedan vacías se quitan.
 */
fun filtrarProductos(categorias: List<ModeloProductosArray>, texto: String): List<ModeloProductosArray> {
    val buscado = normalizarBusqueda(texto)
    if (buscado.isEmpty()) return categorias

    return categorias.mapNotNull { categoria ->
        val productos = categoria.listaProductoCategoriaTercera.filter { producto ->
            normalizarBusqueda(producto.nombre ?: "").contains(buscado) ||
                normalizarBusqueda(producto.descripcion ?: "").contains(buscado)
        }
        if (productos.isEmpty()) null else categoria.copy(listaProductoCategoriaTercera = productos)
    }
}

private fun normalizarBusqueda(texto: String): String {
    val sinTildes = mapOf(
        'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n'
    )
    return texto.trim().lowercase().map { sinTildes[it] ?: it }.joinToString("")
}
