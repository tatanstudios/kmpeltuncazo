package com.tatanstudios.eltuncazometapan.network

import com.tatanstudios.eltuncazometapan.model.modelos.ModeloCarrito
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloDatosBasicos
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloDirecciones
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloInformacionOrdenParaEnviar
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloInformacionProducto
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloInformacionProductoEditar
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloMenuPrincipal
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloOrdenes
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloOrdenesIndividual
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductos
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloProductosDeOrden
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.componentes.SesionEventos
import kotlinx.coroutines.flow.first
import io.ktor.http.ParametersBuilder
import io.ktor.http.parameters

/**
 * Endpoints de la API Laravel (routes/api.php). Todos son POST form-urlencoded,
 * igual que en la versión Retrofit, pero como funciones suspend multiplataforma.
 */
class ApiService(private val client: HttpClient = createHttpClient()) {

    /**
     * Si hay sesión se manda el token de Sanctum. Si el servidor responde 401 (token vencido o
     * revocado), se borra la sesión guardada: la app vuelve a modo sin sesión y pide iniciar sesión.
     */
    private suspend inline fun <reified T> post(
        path: String,
        crossinline campos: ParametersBuilder.() -> Unit,
    ): T {
        val token = TokenManager.token.first()
        try {
            return client.submitForm(url = path, formParameters = parameters { campos() }) {
                if (token.isNotEmpty()) header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                val teniaSesion = token.isNotEmpty()
                TokenManager.deletePreferences()
                if (teniaSesion) SesionEventos.avisarSesionTerminada()
                throw SesionVencidaException()
            }
            throw e
        }
    }

    /** Retrofit omitía los @Field con valor null; replicamos ese comportamiento. */
    private fun ParametersBuilder.opcional(nombre: String, valor: String?) {
        if (valor != null) append(nombre, valor)
    }

    // ---------------- CLIENTE ----------------

    suspend fun verificarUsuarioPassword(telefono: String, password: String): ModeloDatosBasicos =
        post("cliente/login") { append("usuario", telefono); append("password", password) }

    suspend fun registrarme(telefono: String, password: String, correo: String?): ModeloDatosBasicos =
        post("cliente/registro") {
            append("usuario", telefono)
            append("password", password)
            opcional("correo", correo)
        }

    // ---------------- RECUPERAR CONTRASEÑA ----------------

    suspend fun enviarCodigoRecuperacion(correo: String): ModeloDatosBasicos =
        post("cliente/recuperar/enviar-codigo") { append("correo", correo) }

    suspend fun verificarCodigoRecuperacion(correo: String, codigo: String): ModeloDatosBasicos =
        post("cliente/recuperar/verificar-codigo") { append("correo", correo); append("codigo", codigo) }

    suspend fun cambiarPasswordRecuperacion(correo: String, codigo: String, password: String): ModeloDatosBasicos =
        post("cliente/recuperar/cambiar-password") {
            append("correo", correo)
            append("codigo", codigo)
            append("password", password)
        }

    suspend fun verCorreo(): ModeloDatosBasicos =
        post("cliente/perfil/ver-correo") { }

    suspend fun actualizarCorreo(correo: String): ModeloDatosBasicos =
        post("cliente/perfil/actualizar-correo") { append("correo", correo) }

    /** Revoca el token en el servidor. */
    suspend fun cerrarSesion(): ModeloDatosBasicos =
        post("cliente/logout") { }

    // ---------------- PERFIL ----------------

    suspend fun listadoMenuPrincipal(id: String): ModeloMenuPrincipal =
        post("cliente/lista/servicios-bloque") { append("id", id) }

    suspend fun actualizarPassword(id: String, password: String): ModeloDatosBasicos =
        post("cliente/actualizar/password") { append("id", id); append("password", password) }

    suspend fun listadoDirecciones(id: String): ModeloDirecciones =
        post("cliente/listado/direcciones") { append("id", id) }

    suspend fun registrarNuevaDireccion(
        idusuario: String,
        nombre: String,
        direccion: String,
        puntoReferencia: String?,
        telefono: String,
    ): ModeloDatosBasicos = post("cliente/nueva/direccion") {
        append("id", idusuario)
        append("nombre", nombre)
        append("direccion", direccion)
        opcional("punto_referencia", puntoReferencia)
        append("telefono", telefono)
    }

    suspend fun seleccionarDireccion(id: String, dirid: Int): ModeloDatosBasicos =
        post("cliente/seleccionar/direccion") { append("id", id); append("dirid", dirid.toString()) }

    suspend fun borrarDireccion(id: String, dirid: Int): ModeloDatosBasicos =
        post("cliente/eliminar/direccion") { append("id", id); append("dirid", dirid.toString()) }

    // ---------------- PRODUCTOS ----------------

    suspend fun listadoProductos(idcategoria: Int): ModeloProductos =
        post("cliente/servicios/listado/menu") { append("categoria", idcategoria.toString()) }

    suspend fun informacionProducto(idproducto: Int): ModeloInformacionProducto =
        post("cliente/informacion/producto") { append("productoid", idproducto.toString()) }

    suspend fun enviarProductosAlCarrito(
        clienteid: String,
        productoid: Int,
        cantidad: Int,
        notaproducto: String?,
        opciones: String, // "idOpcion:cantidad,idOpcion:cantidad"
    ): ModeloDatosBasicos = post("cliente/carrito/producto/agregar") {
        append("clienteid", clienteid)
        append("productoid", productoid.toString())
        append("cantidad", cantidad.toString())
        opcional("notaproducto", notaproducto)
        append("opciones", opciones)
    }

    // ---------------- CARRITO ----------------

    suspend fun listadoCarritoCompras(idcliente: String): ModeloCarrito =
        post("cliente/carrito/ver/orden") { append("clienteid", idcliente) }

    suspend fun borrarCarritoCompras(idcliente: String): ModeloDatosBasicos =
        post("cliente/carrito/borrar/orden") { append("clienteid", idcliente) }

    suspend fun eliminarFilaCarrito(idcliente: String, idcarrito: Int): ModeloDatosBasicos =
        post("cliente/carrito/eliminar/producto") {
            append("clienteid", idcliente); append("carritoid", idcarrito.toString())
        }

    suspend fun informacionProductoParaEditar(idcliente: String, idcarrito: Int): ModeloInformacionProductoEditar =
        post("cliente/carrito/ver/producto") {
            append("clienteid", idcliente); append("carritoid", idcarrito.toString())
        }

    suspend fun actualizarProductoEditado(
        idcliente: String,
        cantidad: Int,
        idcarrito: Int,
        nota: String?,
        opciones: String,
    ): ModeloDatosBasicos = post("cliente/carrito/cambiar/cantidad") {
        append("clienteid", idcliente)
        append("cantidad", cantidad.toString())
        append("carritoid", idcarrito.toString())
        opcional("nota", nota)
        append("opciones", opciones)
    }

    suspend fun informacionOrdenParaEnviar(idcliente: String): ModeloInformacionOrdenParaEnviar =
        post("cliente/carrito/ver/proceso-orden") { append("clienteid", idcliente) }

    suspend fun enviarOrdenFinal(idcliente: String, nota: String?, version: String?): ModeloDatosBasicos =
        post("cliente/proceso/orden/estado-1") {
            append("clienteid", idcliente)
            opcional("nota", nota)
            opcional("version", version)
        }

    // ---------------- ORDENES ----------------

    suspend fun listadoOrdenes(clienteid: String): ModeloOrdenes =
        post("cliente/ver/ordenes-activas") { append("clienteid", clienteid) }

    suspend fun informacionOrdenIndividual(ordenid: Int): ModeloOrdenesIndividual =
        post("cliente/ver/estado-orden") { append("ordenid", ordenid.toString()) }

    suspend fun listadoProductosOrden(ordenid: Int): ModeloProductosDeOrden =
        post("cliente/listado/productos/ordenes") { append("ordenid", ordenid.toString()) }

    suspend fun cancelarOrden(idorden: Int): ModeloDatosBasicos =
        post("cliente/proceso/orden/cancelar") { append("ordenid", idorden.toString()) }

    suspend fun completarOrden(idorden: Int): ModeloDatosBasicos =
        post("cliente/proceso/completar/orden") { append("ordenid", idorden.toString()) }

    companion object {
        /** Instancia única compartida por toda la app (como RetrofitBuilder.getApiService()). */
        val instance: ApiService by lazy { ApiService() }
    }
}

/** El servidor rechazó el token (401). La sesión local ya se borró. */
class SesionVencidaException : Exception("Sesión vencida")
