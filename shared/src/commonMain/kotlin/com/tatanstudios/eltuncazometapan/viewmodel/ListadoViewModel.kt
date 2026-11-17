package com.tatanstudios.eltuncazometapan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tatanstudios.eltuncazometapan.extras.Event
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
import com.tatanstudios.eltuncazometapan.network.ApiService
import com.tatanstudios.eltuncazometapan.network.SesionVencidaException
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.ToastType

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MENSAJE_ERROR_CONEXION = "No se pudo conectar. Revisa tu internet e intenta de nuevo"

/**
 * Base para todas las peticiones: reemplaza RxJava (Single + Disposable) y LiveData.
 *
 * - [resultado]: respuesta envuelta en [Event] para consumirla una sola vez.
 * - [isLoading]: estado de carga.
 * - Evita peticiones duplicadas mientras una está en curso.
 * - Reintenta ante fallos de red (antes era `.retry()` infinito; ahora con límite y espera).
 * - Si se define [resultadoError], se emite cuando todos los intentos fallan.
 */
abstract class PeticionViewModel<T>(
    private val reintentos: Int = 3,
    private val resultadoError: (() -> T)? = null,
) : ViewModel() {

    protected val api: ApiService get() = ApiService.instance

    private val _resultado = MutableStateFlow<Event<T>?>(null)
    val resultado: StateFlow<Event<T>?> = _resultado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** true si la última petición falló (sin conexión, error del servidor). Sirve para mostrar "Reintentar". */
    private val _fallo = MutableStateFlow(false)
    val fallo: StateFlow<Boolean> = _fallo.asStateFlow()

    private var job: Job? = null

    protected fun ejecutar(llamada: suspend ApiService.() -> T) {
        if (job?.isActive == true) return
        _isLoading.value = true
        _fallo.value = false
        job = viewModelScope.launch {
            try {
                val respuesta = conReintentos { api.llamada() }
                _resultado.value = Event(respuesta)
            } catch (e: CancellationException) {
                throw e
            } catch (e: SesionVencidaException) {
                // el aviso de "inicia sesión" lo muestra AvisoSesionTerminada
                _fallo.value = true
            } catch (e: Exception) {
                _fallo.value = true
                if (resultadoError != null) {
                    _resultado.value = Event(resultadoError.invoke())
                } else {
                    // antes el error se perdía y el botón parecía no hacer nada
                    CustomToasty(null, MENSAJE_ERROR_CONEXION, ToastType.ERROR)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun <R> conReintentos(bloque: suspend () -> R): R {
        var espera = 1_000L
        repeat(reintentos) {
            try {
                return bloque()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SesionVencidaException) {
                throw e // reintentar no sirve: hay que iniciar sesión de nuevo
            } catch (_: Exception) {
                delay(espera)
                espera *= 2
            }
        }
        return bloque()
    }
}

// ============================ LOGIN / REGISTRO ============================

class LoginViewModel : PeticionViewModel<ModeloDatosBasicos>(
    reintentos = 0,
    resultadoError = { ModeloDatosBasicos(success = 0) },
) {
    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun setUsuario(usuario: String) { _usuario.value = usuario }
    fun setPassword(password: String) { _password.value = password }

    fun verificarUsuarioPasssword() = ejecutar {
        verificarUsuarioPassword(_usuario.value, _password.value)
    }
}

class RegistroViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _correo = MutableStateFlow("")
    val correo: StateFlow<String> = _correo.asStateFlow()

    fun setUsuario(usuario: String) { _usuario.value = usuario }
    fun setPassword(password: String) { _password.value = password }
    fun setCorreo(correo: String) { _correo.value = correo }

    fun registroRetrofit() = ejecutar {
        registrarme(_usuario.value, _password.value, _correo.value.trim().ifBlank { null })
    }
}

/** Validación simple de correo, igual para registro, perfil y recuperación. */
fun esCorreoValido(correo: String): Boolean =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(correo.trim())

/**
 * Recuperar contraseña en 3 pasos (una sola petición a la vez):
 *  1 = escribir el correo, 2 = escribir el código, 3 = escribir la nueva contraseña.
 * La pantalla interpreta [resultado] según el paso en que se hizo la petición.
 */
class RecuperarPasswordViewModel : PeticionViewModel<ModeloDatosBasicos>(reintentos = 0) {
    private val _paso = MutableStateFlow(1)
    val paso: StateFlow<Int> = _paso.asStateFlow()

    private val _correo = MutableStateFlow("")
    val correo: StateFlow<String> = _correo.asStateFlow()

    private val _codigo = MutableStateFlow("")
    val codigo: StateFlow<String> = _codigo.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun setCorreo(correo: String) { _correo.value = correo }
    fun setCodigo(codigo: String) { _codigo.value = codigo.filter { it.isDigit() }.take(6) }
    fun setPassword(password: String) { _password.value = password }
    fun irAPaso(paso: Int) { _paso.value = paso }

    fun enviarCodigo() = ejecutar { enviarCodigoRecuperacion(_correo.value.trim()) }
    fun verificarCodigo() = ejecutar { verificarCodigoRecuperacion(_correo.value.trim(), _codigo.value) }
    fun cambiarPassword() = ejecutar {
        cambiarPasswordRecuperacion(_correo.value.trim(), _codigo.value, _password.value)
    }
}

class VerCorreoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun verCorreoRetrofit() = ejecutar { verCorreo() }
}

class ActualizarCorreoViewModel : PeticionViewModel<ModeloDatosBasicos>(reintentos = 0) {
    private val _correo = MutableStateFlow("")
    val correo: StateFlow<String> = _correo.asStateFlow()

    fun setCorreo(correo: String) { _correo.value = correo }

    fun actualizarCorreoRetrofit() = ejecutar { actualizarCorreo(_correo.value.trim()) }
}

// ============================ MENU / PERFIL ============================

class ListadoMenuPrincipal : PeticionViewModel<ModeloMenuPrincipal>() {
    fun listadoMenuPrincipalRetrofit(id: String) = ejecutar { listadoMenuPrincipal(id) }
}

class ActualizarPasswordViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    private val _password = MutableStateFlow("")
    val passowrd: StateFlow<String> = _password.asStateFlow()

    fun setPassword(password: String) { _password.value = password }

    fun actualizarContrasenaRetrofit(idusuario: String) = ejecutar {
        actualizarPassword(idusuario, _password.value)
    }
}

// ============================ DIRECCIONES ============================

class ListadoDireccionesViewModel : PeticionViewModel<ModeloDirecciones>() {
    fun listadoDireccionesRetrofit(idusuario: String) = ejecutar { listadoDirecciones(idusuario) }
}

class RegistroNuevaDireccionViewModel : PeticionViewModel<ModeloDatosBasicos>(
    reintentos = 0,
    resultadoError = { ModeloDatosBasicos(success = 0) },
) {
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion: StateFlow<String> = _direccion.asStateFlow()

    private val _puntoReferencia = MutableStateFlow("")
    val puntoReferencia: StateFlow<String> = _puntoReferencia.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    fun setNombre(nombre: String) { _nombre.value = nombre }
    fun setDireccion(direccion: String) { _direccion.value = direccion }
    fun setPuntoReferencia(puntoReferencia: String) { _puntoReferencia.value = puntoReferencia }
    fun setTelefono(telefono: String) { _telefono.value = telefono }

    fun registrarNuevaDireccionRetrofit(idusuario: String) = ejecutar {
        registrarNuevaDireccion(
            idusuario,
            _nombre.value,
            _direccion.value,
            _puntoReferencia.value,
            _telefono.value,
        )
    }
}

class SeleccionarDireccionViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun seleccionarDireccionRetrofit(idusuario: String, dirid: Int) = ejecutar {
        seleccionarDireccion(idusuario, dirid)
    }
}

class BorrarDireccionViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun borrarDireccionRetrofit(idusuario: String, dirid: Int) = ejecutar {
        borrarDireccion(idusuario, dirid)
    }
}

// ============================ PRODUCTOS ============================

class ListadoProductosViewModel : PeticionViewModel<ModeloProductos>() {
    fun listadoProductosRetrofit(idcategoria: Int) = ejecutar { listadoProductos(idcategoria) }
}

class InformacionProductoViewModel : PeticionViewModel<ModeloInformacionProducto>() {
    fun informacionProductoRetrofit(idproducto: Int) = ejecutar { informacionProducto(idproducto) }
}

class EnviarProductoAlCarritoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun enviarProductoCarritoRetrofit(clienteid: String, productoid: Int, cantidad: Int, notaproducto: String?, opciones: String) =
        ejecutar { enviarProductosAlCarrito(clienteid, productoid, cantidad, notaproducto, opciones) }
}

// ============================ CARRITO ============================

class ListadoCarritoComprasViewModel : PeticionViewModel<ModeloCarrito>() {
    fun listadoCarritoComprasRetrofit(idcliente: String) = ejecutar { listadoCarritoCompras(idcliente) }
}

class BorrarCarritoComprasViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun eliminarCarritoComprasRetrofit(idcliente: String) = ejecutar { borrarCarritoCompras(idcliente) }
}

class BorrarFilaCarritoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun eliminarFilaCarritoRetrofit(idcliente: String, idcarritofila: Int) = ejecutar {
        eliminarFilaCarrito(idcliente, idcarritofila)
    }
}

class InformacionProductoEditadoViewModel : PeticionViewModel<ModeloInformacionProductoEditar>() {
    fun informacionProductoEditarRetrofit(idcliente: String, idfilacarrito: Int) = ejecutar {
        informacionProductoParaEditar(idcliente, idfilacarrito)
    }
}

class ActualizarProductoEditadoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun actualizarProductoEditadoRetrofit(idcliente: String, cantidad: Int, idcarritofila: Int, nota: String?, opciones: String) =
        ejecutar { actualizarProductoEditado(idcliente, cantidad, idcarritofila, nota, opciones) }
}

class InformacionOrdenParaEnviarViewModel : PeticionViewModel<ModeloInformacionOrdenParaEnviar>() {
    fun informacionOrdenParaEnviarRetrofit(idcliente: String) = ejecutar {
        informacionOrdenParaEnviar(idcliente)
    }
}

class EnviarOrdenFinalViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun enviarOrdenRetrofit(idcliente: String, nota: String?, version: String?) = ejecutar {
        enviarOrdenFinal(idcliente, nota, version)
    }
}

// ============================ ORDENES ============================

class InformacionDeUnaOrdenViewModel : PeticionViewModel<ModeloOrdenesIndividual>() {
    fun informacionOrdenIndividualRetrofit(ordenid: Int) = ejecutar { informacionOrdenIndividual(ordenid) }
}

class CancelarOrdenViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun cancelarOrdenRetrofit(ordenid: Int) = ejecutar { cancelarOrden(ordenid) }
}

class ListadoOrdenesViewModel : PeticionViewModel<ModeloOrdenes>() {
    fun listadoOrdenesRetrofit(idcliente: String) = ejecutar { listadoOrdenes(idcliente) }
}

class OcultarOrdenViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun ocultarOrdenRetrofit(ordenid: Int) = ejecutar { completarOrden(ordenid) }
}

class CompletarOrdenViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun completarOrdenRetrofit(ordenid: Int) = ejecutar { completarOrden(ordenid) }
}

class ListadoProductosDeUnaOrdenViewModel : PeticionViewModel<ModeloProductosDeOrden>() {
    fun listadoProductosDeUnaOrdenRetrofit(idorden: Int) = ejecutar { listadoProductosOrden(idorden) }
}
