package com.tatanstudios.eltuncazometapan.network

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Prueba de humo contra el servidor real (solo endpoints de lectura, sin cuenta).
 * Verifica que Ktor + kotlinx.serialization interpretan las respuestas de Laravel.
 * Ejecutar:  ./gradlew :shared:testAndroidHostTest
 */
class ApiServiceIntegrationTest {

    private val api = ApiService()

    @Test
    fun loginConUsuarioInexistenteDevuelveCodigo4() = runBlocking {
        val r = api.verificarUsuarioPassword("usuario_inexistente_kmp_test", "x")
        assertEquals(4, r.success)
    }

    @Test
    fun menuPrincipalTraeCategorias() = runBlocking {
        val r = api.listadoMenuPrincipal("1")
        assertEquals(1, r.success)
        assertTrue(r.arrayCategorias.isNotEmpty())
        assertTrue(r.arrayCategorias.all { !it.nombre.isNullOrBlank() })
    }

    @Test
    fun listadoProductosTraeProductosConPrecio() = runBlocking {
        val r = api.listadoProductos(1)
        assertEquals(1, r.success)
        val productos = r.listaProductoCategoria.flatMap { it.listaProductoCategoriaTercera }
        assertTrue(productos.isNotEmpty())
        assertTrue(productos.all { it.precio != null })
    }

    @Test
    fun informacionProductoIndividual() = runBlocking {
        val r = api.informacionProducto(1)
        assertEquals(1, r.success)
        assertEquals(1, r.informacionProducto.first().id)
    }
}
