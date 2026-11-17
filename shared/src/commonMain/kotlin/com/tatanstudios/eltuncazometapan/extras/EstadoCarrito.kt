package com.tatanstudios.eltuncazometapan.extras

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Cantidad de productos en el carrito, para el contador del botón del carrito.
 * Se actualiza cada vez que se carga el menú (al abrir la app y al volver a él).
 */
object EstadoCarrito {
    val cantidad = MutableStateFlow(0)
}
