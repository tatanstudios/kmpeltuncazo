package com.tatanstudios.eltuncazometapan.model.modelos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModeloDatosBasicos(
    @SerialName("success") val success: Int = 0,
    @SerialName("id") val id: Int = 0,
    @SerialName("titulo") val titulo: String? = null,
    @SerialName("mensaje") val mensaje: String? = null,
    @SerialName("usuario") val usuario: String? = null,
    @SerialName("correo") val correo: String? = null,
    @SerialName("resta") val resta: String? = null,
    @SerialName("idorden") val idorden: Int = 0,
    @SerialName("token") val token: String? = null,
)



@Serializable
data class ModeloMenuPrincipal(
    @SerialName("success") val success: Int = 0,
    @SerialName("titulo") val titulo: String? = null,
    @SerialName("mensaje") val mensaje: String? = null,
    @SerialName("activo") val activo: Int = 0,
    @SerialName("servicios") val arrayCategorias: List<ModeloMenuPrincipalCategoriasArray> = emptyList(),
    // estado del local (horario / cerrado desde el panel)
    @SerialName("abierto") val abierto: Int = 1,
    @SerialName("estado_texto") val estadoTexto: String? = null,
    @SerialName("estado_detalle") val estadoDetalle: String? = null,
    // unidades en el carrito del cliente (0 sin sesión)
    @SerialName("cantidad_carrito") val cantidadCarrito: Int = 0,
)


@Serializable
data class ModeloMenuPrincipalCategoriasArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("tiposervicio_id") val tiposervicio_id: Int = 0,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("activo") val activo: Int = 0,
)





@Serializable
data class ModeloHistorialOrdenes(
    @SerialName("success") val success: Int = 0,
    @SerialName("hayordenes") val hayordenes: Int = 0,
    @SerialName("ordenes") val lista: List<ModeloHistorialOrdenesArray> = emptyList(),
)


@Serializable
data class ModeloHistorialOrdenesArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_cliente") val idcliente: Int = 0,
    @SerialName("id_servicio") val idservicio: Int = 0,
    @SerialName("id_zona") val idzona: Int = 0,
    @SerialName("nota_orden") val notaOrden: String? = null,
    @SerialName("totalformat") val totalFormat: String? = null,
    @SerialName("estado") val estado: String? = null,
    @SerialName("fecha_orden") val fechaOrden: String? = null,
    @SerialName("fecha_cancelada") val fechaCancelada: String? = null,
    @SerialName("haycupon") val haycupon: Int = 0,
    @SerialName("cliente") val cliente: String? = null,
    @SerialName("direccion") val direccion: String? = null,
    @SerialName("telefono") val telefono: String? = null,
    @SerialName("referencia") val referencia: String? = null,
    @SerialName("haypremio") val haypremio: Int = 0,
    @SerialName("textopremio") val textopremio: String? = null,
    @SerialName("mensaje_cupon") val mensajeCupon: String? = null,
)





@Serializable
data class ModeloProductoHistorialOrdenes(
    @SerialName("success") val success: Int = 0,
    @SerialName("productos") val lista: List<ModeloProductoHistorialOrdenesArray> = emptyList(),
)

@Serializable
data class ModeloProductoHistorialOrdenesArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_ordenes") val idordenes: Int = 0,
    @SerialName("id_producto") val idproducto: Int = 0,
    @SerialName("cantidad") val cantidad: Int = 0,
    @SerialName("nota") val nota: String? = null,
    @SerialName("precio") val precio: String? = null,
    @SerialName("nombreproducto") val nombreProducto: String? = null,
    @SerialName("idordendescrip") val idOrdenDescrip: Int = 0,
    @SerialName("utiliza_imagen") val utilizaImagen: Int = 0,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("multiplicado") val multiplicado: String? = null,
)




@Serializable
data class ModeloInfoProducto(
    @SerialName("success") val success: Int = 0,
    @SerialName("productos") val lista: List<ModeloInfoProductoArray> = emptyList(),
)

@Serializable
data class ModeloInfoProductoArray(
    @SerialName("id") val success: Int = 0,
    @SerialName("id_ordenes") val idordenes: Int = 0,
    @SerialName("id_producto") val idproducto: Int = 0,
    @SerialName("cantidad") val cantidad: Int = 0,
    @SerialName("nota") val nota: String? = null,
    @SerialName("precio") val precio: String? = null,
    @SerialName("nombreproducto") val nombreProducto: String? = null,
    @SerialName("utiliza_imagen") val utilizaImagen: Int = 0,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("descripcion") val descripcion: String? = null,
    @SerialName("multiplicado") val multiplicado: String? = null,
)

@Serializable
data class ModeloHorario(
    @SerialName("success") val success: Int = 0,
    @SerialName("restaurante") val restaurante: String = "",
    @SerialName("horario") val lista: List<ModeloHorarioArray> = emptyList(),
)


@Serializable
data class ModeloHorarioArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_servicios") val idServicio: Int = 0,
    @SerialName("hora1") val hora1: String? = null,
    @SerialName("hora2") val hora2: String? = null,
    @SerialName("dia") val dia: Int = 0,
    @SerialName("cerrado") val cerrado: Int = 0,
    @SerialName("horario") val horario: String? = null,
    @SerialName("fechaformat") val fechaformat: String? = null,
)




@Serializable
data class ModeloPremios(
    @SerialName("success") val success: Int = 0,
    @SerialName("conteo") val conteo: Int = 0,
    @SerialName("nota") val nota: String? = null,
    @SerialName("puntos") val puntos: String? = null,
    @SerialName("listado") val lista: List<ModeloPremiosArray> = emptyList(),
)


@Serializable
data class ModeloPremiosArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_servicio") val idServicio: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("puntos") val puntos: Int = 0,
    @SerialName("activo") val activo: Int = 0,
    @SerialName("seleccionado") var seleccionado: Int = 0,
)




@Serializable
data class ModeloDirecciones(
    @SerialName("success") val success: Int = 0,
    @SerialName("titulo") val titulo: String? = null,
    @SerialName("mensaje") val mensaje: String? = null,
    @SerialName("direcciones") val lista: List<ModeloDireccionesArray> = emptyList(),
)

@Serializable
data class ModeloDireccionesArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("direccion") val direccion: String? = null,
    @SerialName("numero_casa") val numero_casa: String? = null,
    @SerialName("punto_referencia") val punto_referencia: String? = null,
    @SerialName("telefono") val telefono: String? = null,
    @SerialName("seleccionado") var seleccionado: Int = 0,
    @SerialName("minimocompra") val minimocompra: String? = null,
)



@Serializable
data class ModeloPoligonos(
    @SerialName("success") val success: Int = 0,
    @SerialName("poligono") val lista: List<ModeloPoligonosArray> = emptyList(),
)

@Serializable
data class ModeloPoligonosArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("nombreZona") val nombre: String? = null,
    @SerialName("poligonos") val listado: List<ModeloPoligonosLatitudLongitudArray> = emptyList(),
)

@Serializable
data class ModeloPoligonosLatitudLongitudArray(
    @SerialName("latitudPoligono") val latitud: String = "",
    @SerialName("longitudPoligono") val longitud: String = "",
)



@Serializable
data class ModeloMenuPrincipalPopularesArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("utiliza_imagen") val utilizaImagen: Int = 0,
    @SerialName("precio") val precio: String? = null,
)





@Serializable
data class ModeloProductos(
    @SerialName("success") val success: Int = 0,
    // 1 = lista, 2 = tienda (se elige en el panel, por servicio)
    @SerialName("tipo_vista") val tipoVista: Int = 1,
    @SerialName("productos") val listaProductoCategoria: List<ModeloProductosArray> = emptyList(),
)


@Serializable
data class ModeloProductosArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_categorias") val idCategorias: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("productos") val listaProductoCategoriaTercera: List<ModeloProductosTerceraArray> = emptyList(),
)

@Serializable
data class ModeloProductosTerceraArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_subcategorias") val idSubCategoria: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("descripcion") val descripcion: String? = null,
    @SerialName("precio") val precio: String? = null,
    @SerialName("utiliza_nota") val utilizaNota: Int = 0,
    @SerialName("nota") val nota: String? = null,
    @SerialName("utiliza_imagen") val utilizaImagen: Int = 0,
)


@Serializable
data class ModeloInformacionProducto(
    @SerialName("success") val success: Int = 0,
    @SerialName("producto") val informacionProducto: List<ModeloInformacionProductoArray> = emptyList(),
    @SerialName("modificadores") val modificadores: List<ModeloModificadorGrupo> = emptyList(),
)

/** Bloque de opciones de un producto (ej. "Preparación de huevo"). */
@Serializable
data class ModeloModificadorGrupo(
    @SerialName("id") val id: Int = 0,
    @SerialName("nombre") val nombre: String = "",
    @SerialName("requerido") val requerido: Int = 0,
    @SerialName("minimo") val minimo: Int = 0,
    @SerialName("maximo") val maximo: Int = 1,
    @SerialName("opciones") val opciones: List<ModeloModificadorOpcion> = emptyList(),
)

@Serializable
data class ModeloModificadorOpcion(
    @SerialName("id") val id: Int = 0,
    @SerialName("nombre") val nombre: String = "",
    @SerialName("precio") val precio: String = "0.00", // precio extra por unidad
    @SerialName("maximo") val maximo: Int = 1,         // veces que se puede elegir
    @SerialName("obligatorio") val obligatorio: Int = 0,
    @SerialName("una_sola") val unaSola: Int = 0,
)

@Serializable
data class ModeloSeleccionOpcion(
    @SerialName("id_opcion") val idOpcion: Int = 0,
    @SerialName("cantidad") val cantidad: Int = 1,
)

@Serializable
data class ModeloInformacionProductoArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_subcategorias") val idSubCategoria: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("descripcion") val descripcion: String? = null,
    @SerialName("precio") val precio: String? = null,
    @SerialName("activo") val activo: Int = 0,
    @SerialName("utiliza_nota") val utilizaNota: Int = 0,
    @SerialName("nota") val nota: String? = null,
    @SerialName("utiliza_imagen") val utilizaImagen: Int = 0,
    )



@Serializable
data class ModeloCarrito(
    @SerialName("success") val success: Int = 0,
    @SerialName("subtotal") val subTotal: String? = null,
    @SerialName("estadoProductoGlobal") val estadoProductoGlobal: Int = 0,
    @SerialName("producto") val listadoCarritoTemporal: List<ModeloCarritoTemporal> = emptyList(),
    @SerialName("hayDireccionRegistrada") val hayDireccionRegistrada: Int = 0,
)

@Serializable
data class ModeloCarritoTemporal(
    @SerialName("productoID") val id: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("cantidad") val cantidad: Int = 0,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("precio") val precio: String? = null,

    @SerialName("activo") val activo: Int = 0,
    @SerialName("carritoid") val carritoid: Int = 0,
    @SerialName("utiliza_imagen") val utilizaImagen: Int = 0,
    @SerialName("id_subcategorias") val idSubCategorias: Int = 0,
    @SerialName("estadoLocal") val estadoLocal: Int = 0,

    @SerialName("titulo") val titulo: String? = null,
    @SerialName("mensaje") val mensaje: String? = null,
    @SerialName("precioformat") val precioformat: String? = null,
    @SerialName("preciounitario") val precioUnitario: String? = null, // con opciones
    @SerialName("preciobase") val precioBase: String? = null,         // sin opciones
    @SerialName("nota") val nota: String? = null,
    @SerialName("opciones") val opciones: List<String> = emptyList(),
)



@Serializable
data class ModeloInformacionProductoEditar(
    @SerialName("success") val success: Int = 0,
    @SerialName("producto") val producto: ModeloInformacionProductoEditarArray? = null,
    @SerialName("modificadores") val modificadores: List<ModeloModificadorGrupo> = emptyList(),
    @SerialName("seleccion") val seleccion: List<ModeloSeleccionOpcion> = emptyList(),
)

@Serializable
data class ModeloInformacionProductoEditarArray(
    @SerialName("productoID") val productoID: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("descripcion") val descripcion: String? = null,
    @SerialName("cantidad") val cantidad: Int = 0,
    @SerialName("nota_producto") val notaProducto: String? = null,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("precio") val precio: String? = null,         // viene como String ("10.25")
    @SerialName("utiliza_nota") val utilizaNota: Int = 0,
    @SerialName("nota") val nota: String? = null,             // está en tu JSON de ejemplo
    @SerialName("utiliza_imagen") val utilizaImagen: Int = 0,
)


@Serializable
data class ModeloInformacionOrdenParaEnviar(
    @SerialName("success") val success: Int = 0,
    @SerialName("total") val total: String? = null,
    @SerialName("direccion") val direccion: String? = null,
    @SerialName("cliente") val cliente: String? = null,
    @SerialName("minimo") val minimo: Int = 0,
    @SerialName("mensaje") val mensaje: String? = null,
    @SerialName("usacupon") val usacupon: Int = 0,
    @SerialName("usapremio") val usapremio: Int = 0,
    @SerialName("textopremio") val textopremio: String? = null,
    @SerialName("telefono") val telefono: String? = null,
    @SerialName("referencia") val referencia: String? = null,
    @SerialName("minimoconsumo") val minimoConsumo: String? = null,
    @SerialName("faltante") val faltante: String? = null,
    // estado del local; 1 por defecto para no bloquear si la API es anterior
    @SerialName("abierto") val abierto: Int = 1,
    @SerialName("estado_texto") val estadoTexto: String? = null,
    @SerialName("estado_detalle") val estadoDetalle: String? = null,
    @SerialName("productos") val productos: List<ModeloResumenProductoOrden> = emptyList(),
)

/** Fila del resumen del pedido antes de enviarlo. */
@Serializable
data class ModeloResumenProductoOrden(
    @SerialName("cantidad") val cantidad: Int = 0,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("opciones") val opciones: List<String> = emptyList(),
    @SerialName("nota") val nota: String? = null,
    @SerialName("total") val total: String? = null,
)




@Serializable
data class ModeloOrdenes(
    @SerialName("success") val success: Int = 0,
    @SerialName("ordenes") val ordenes: List<ModeloOrdenesArray> = emptyList(),
)

@Serializable
data class ModeloOrdenesArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_cliente") val idCliente: Int = 0,
    @SerialName("id_servicio") val idServicio: Int = 0,
    @SerialName("id_zona") val idZona: Int = 0,
    @SerialName("nota") val notaOrden: String? = null,
    @SerialName("total") val total: String? = null,
    @SerialName("fecha_orden") val fechaOrden: String? = null,
    @SerialName("fecha_estimada") val fechaEstimada: String? = null,
    @SerialName("estado_iniciada") val estadoIniciada: Int = 0,
    @SerialName("fecha_iniciada") val fechaIniciada: String? = null,
    @SerialName("estado_preparada") val estadoPreparada: Int = 0,
    @SerialName("fecha_preparada") val fechaPreparada: String? = null,
    @SerialName("estado_camino") val estadoCamino: Int = 0,
    @SerialName("fecha_camino") val fechaCamino: String? = null,
    @SerialName("estado_entregada") val estadoEntregada: Int = 0,
    @SerialName("fecha_entregada") val fechaEntregada: String? = null,
    @SerialName("estado_cancelada") val estadoCancelada: Int = 0,
    @SerialName("fecha_cancelada") val fechaCancelada: String? = null,
    @SerialName("mensaje_cancelada") val mensajeCancelado: String? = null,
    @SerialName("cantidad_productos") val cantidadProductos: Int = 0,
    @SerialName("resumen_productos") val resumenProductos: String? = null,
    @SerialName("id_cupones") val idCupones: Int? = null,
    @SerialName("total_cupon") val totalCupon: String? = null,
    @SerialName("mensaje_cupon") val mensajeCupon: String? = null,
    @SerialName("visible") val visible: Int = 0,
    @SerialName("id_cupones_copia") val idCuponesCopia: Int? = null,
    @SerialName("cancelado_por") val canceladoPor: Int = 0,
    @SerialName("direccion") val direccion: String? = null,
    @SerialName("totalformat") val totalFormat: String? = null,
    @SerialName("haycupon") val hayCupon: Int = 0,
    @SerialName("estado") val estado: String? = null,
    @SerialName("haypremio") val hayPremio: Int = 0,
    @SerialName("textopremio") val textoPremio: String? = null,
)




@Serializable
data class ModeloOrdenesIndividual(
    @SerialName("success") val success: Int = 0,
    @SerialName("ordenes") val ordenes: List<ModeloOrdenesIndividualArray> = emptyList(),
)

@Serializable
data class ModeloOrdenesIndividualArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("id_cliente") val idCliente: Int = 0,
    @SerialName("id_servicio") val idServicio: Int = 0,
    @SerialName("id_zona") val idZona: Int = 0,
    @SerialName("nota_orden") val notaOrden: String? = null,
    @SerialName("total_orden") val totalOrden: String? = null,
    @SerialName("fecha_orden") val fechaOrden: String? = null,
    @SerialName("fecha_estimada") val fechaEstimada: String? = null,
    @SerialName("estado_iniciada") val estadoIniciada: Int = 0,
    @SerialName("fecha_iniciada") val fechaIniciada: String? = null,
    @SerialName("estado_preparada") val estadoPreparada: Int = 0,
    @SerialName("fecha_preparada") val fechaPreparada: String? = null,
    @SerialName("estado_camino") val estadoCamino: Int = 0,
    @SerialName("fecha_camino") val fechaCamino: String? = null,
    @SerialName("estado_entregada") val estadoEntregada: Int = 0,
    @SerialName("fecha_entregada") val fechaEntregada: String? = null,
    @SerialName("estado_cancelada") val estadoCancelada: Int = 0,
    @SerialName("fecha_cancelada") val fechaCancelada: String? = null,
    @SerialName("nota_cancelada") val notaCancelada: String? = null,
    @SerialName("id_cupones") val idCupones: Int? = null,
    @SerialName("total_cupon") val totalCupon: String? = null,
    @SerialName("mensaje_cupon") val mensajeCupon: String? = null,
    @SerialName("visible") val visible: Int = 0,
    @SerialName("id_cupones_copia") val idCuponesCopia: Int? = null,
    @SerialName("cancelado_por") val canceladoPor: Int = 0,
    // datos de la pantalla de seguimiento
    @SerialName("total") val total: String? = null,
    @SerialName("nota") val nota: String? = null,
    @SerialName("mensaje_cancelada") val mensajeCancelada: String? = null,
    @SerialName("cancelada_por") val canceladaPor: Int = 0, // 1 = cliente, 2 = local
    @SerialName("cliente") val cliente: String? = null,
    @SerialName("telefono") val telefono: String? = null,
    @SerialName("direccion") val direccion: String? = null,
    @SerialName("referencia") val referencia: String? = null,
    @SerialName("productos") val productos: List<ModeloResumenProductoOrden> = emptyList(),
    @SerialName("textoiniciada") val textoIniciada: String? = null,
    @SerialName("fechaestimada") val fechaEstimadaTxt: String? = null,
    @SerialName("textocamino") val textoCamino: String? = null,
    @SerialName("fechacamino") val fechaCaminoTxt: String? = null,
    @SerialName("textoentregada") val textoEntregada: String? = null,
    @SerialName("fechaentregada") val fechaEntregadaTxt: String? = null,
)



@Serializable
data class ModeloProductosDeOrden(
    @SerialName("success") val success: Int = 0,
    @SerialName("productos") val productos: List<ModeloProductosDeOrdenArray> = emptyList(),
)

@Serializable
data class ModeloProductosDeOrdenArray(
    @SerialName("id") val id: Int = 0,
    @SerialName("cantidad") val cantidad: Int = 0,
    @SerialName("nota") val nota: String? = null,
    @SerialName("precio") val precio: String? = null,
    @SerialName("nombreproducto") val nombreproducto: String? = null,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("idordendescrip") val idordendescrip: Int = 0,
    @SerialName("utiliza_imagen") val utiliza_imagen: Int = 0,
    @SerialName("imagen") val imagen: String? = null,
    @SerialName("multiplicado") val multiplicado: String? = null,
    @SerialName("opciones") val opciones: List<String> = emptyList(),
)
