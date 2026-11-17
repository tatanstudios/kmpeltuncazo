package com.tatanstudios.eltuncazometapan.vistas.opciones.direcciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.eltuncazometapan.componentes.BarraToolbarColor
import com.tatanstudios.eltuncazometapan.componentes.CustomModal1Boton
import com.tatanstudios.eltuncazometapan.componentes.CustomModal2Botones
import com.tatanstudios.eltuncazometapan.componentes.CustomToasty
import com.tatanstudios.eltuncazometapan.componentes.LoadingModal
import com.tatanstudios.eltuncazometapan.componentes.ToastType
import com.tatanstudios.eltuncazometapan.extras.TokenManager
import com.tatanstudios.eltuncazometapan.model.modelos.ModeloDireccionesArray
import com.tatanstudios.eltuncazometapan.resources.*
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import com.tatanstudios.eltuncazometapan.viewmodel.BorrarDireccionViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.ListadoDireccionesViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.RegistroNuevaDireccionViewModel
import com.tatanstudios.eltuncazometapan.viewmodel.SeleccionarDireccionViewModel
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

private val colorFondo = Color(0xFFF6F4FA)
private val colorTexto = Color(0xFF1F1B24)
private val colorSecundario = Color(0xFF6D6A75)
private val colorElegida = Color(0xFF2E7D32)
private val colorBorrar = Color(0xFFC62828)

/**
 * Mis direcciones: tocar una tarjeta la marca como la dirección de entrega,
 * el menú ⋮ permite eliminarla y el botón de abajo abre el formulario de nueva dirección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisDireccionesScreen(
    navController: NavHostController,
    estadoBotonAtras: Int = 0,
    viewModel: ListadoDireccionesViewModel = viewModel { ListadoDireccionesViewModel() },
) {
    val ctx = LocalPlatformContext.current

    val registroViewModel: RegistroNuevaDireccionViewModel = viewModel { RegistroNuevaDireccionViewModel() }
    val seleccionarViewModel: SeleccionarDireccionViewModel = viewModel { SeleccionarDireccionViewModel() }
    val borrarViewModel: BorrarDireccionViewModel = viewModel { BorrarDireccionViewModel() }

    val isLoading by viewModel.isLoading.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val isLoadingRegistro by registroViewModel.isLoading.collectAsState()
    val resultadoRegistro by registroViewModel.resultado.collectAsState()
    val isLoadingSeleccionar by seleccionarViewModel.isLoading.collectAsState()
    val resultadoSeleccionar by seleccionarViewModel.resultado.collectAsState()
    val isLoadingBorrar by borrarViewModel.isLoading.collectAsState()
    val resultadoBorrar by borrarViewModel.resultado.collectAsState()

    var idusuario by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }
    var direcciones by remember { mutableStateOf(listOf<ModeloDireccionesArray>()) }
    var recargar by remember { mutableStateOf(0) }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var direccionABorrar by remember { mutableStateOf<ModeloDireccionesArray?>(null) }
    var mensajeAviso by remember { mutableStateOf<String?>(null) }

    val stringDireccionOk = stringResource(Res.string.direccion_registrada)
    val stringDireccionBorrada = stringResource(Res.string.direccion_borrada)
    val stringMinimoUna = stringResource(Res.string.no_se_puede_eliminar_direccion_ultima)
    val stringError = stringResource(Res.string.error_reintentar_de_nuevo)

    // cargar (y recargar después de cada cambio)
    LaunchedEffect(recargar) {
        if (idusuario.isEmpty()) idusuario = TokenManager.idUsuario.first()
        viewModel.listadoDireccionesRetrofit(idusuario)
    }

    LaunchedEffect(resultado) {
        val r = resultado?.getContentIfNotHandled() ?: return@LaunchedEffect
        if (r.success == 1) direcciones = r.lista
        datosCargados = true
    }

    LaunchedEffect(resultadoRegistro) {
        val r = resultadoRegistro?.getContentIfNotHandled() ?: return@LaunchedEffect
        if (r.success == 1) {
            CustomToasty(ctx, stringDireccionOk, ToastType.SUCCESS)
            mostrarFormulario = false
            registroViewModel.setNombre("")
            registroViewModel.setTelefono("")
            registroViewModel.setDireccion("")
            registroViewModel.setPuntoReferencia("")
            recargar++
        } else {
            CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }

    LaunchedEffect(resultadoSeleccionar) {
        val r = resultadoSeleccionar?.getContentIfNotHandled() ?: return@LaunchedEffect
        if (r.success != 1) CustomToasty(ctx, stringError, ToastType.ERROR)
        recargar++
    }

    LaunchedEffect(resultadoBorrar) {
        val r = resultadoBorrar?.getContentIfNotHandled() ?: return@LaunchedEffect
        when (r.success) {
            1 -> { CustomToasty(ctx, stringDireccionBorrada, ToastType.SUCCESS); recargar++ }
            2 -> mensajeAviso = stringMinimoUna
            else -> CustomToasty(ctx, stringError, ToastType.ERROR)
        }
    }

    Scaffold(
        containerColor = colorFondo,
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.mis_direcciones),
                AppColors.colorAppPrimary,
                estadoBotonAtras = estadoBotonAtras
            )
        },
        bottomBar = {
            if (datosCargados && direcciones.isNotEmpty()) {
                Surface(color = Color.White, shadowElevation = 12.dp) {
                    Button(
                        onClick = { mostrarFormulario = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.colorAppPrimary)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar nueva dirección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                !datosCargados -> Unit

                direcciones.isEmpty() -> SinDirecciones(onAgregar = { mostrarFormulario = true })

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Toca una dirección para usarla en tus órdenes",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorSecundario,
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                    }
                    items(direcciones, key = { it.id }) { dir ->
                        TarjetaDireccion(
                            direccion = dir,
                            onElegir = {
                                if (dir.seleccionado != 1) {
                                    // se marca de una vez; el servidor confirma al recargar
                                    direcciones = direcciones.map { it.copy(seleccionado = if (it.id == dir.id) 1 else 0) }
                                    seleccionarViewModel.seleccionarDireccionRetrofit(idusuario, dir.id)
                                }
                            },
                            onEliminar = { direccionABorrar = dir }
                        )
                    }
                }
            }

            if (isLoading && !datosCargados) LoadingModal(isLoading = true)
            if (isLoadingRegistro || isLoadingBorrar || isLoadingSeleccionar) LoadingModal(isLoading = true)

            direccionABorrar?.let { dir ->
                CustomModal2Botones(
                    showDialog = true,
                    message = "¿Eliminar la dirección de ${dir.nombre ?: ""}?",
                    onDismiss = { direccionABorrar = null },
                    onAccept = {
                        direccionABorrar = null
                        borrarViewModel.borrarDireccionRetrofit(idusuario, dir.id)
                    },
                    stringResource(Res.string.si),
                    stringResource(Res.string.no),
                )
            }

            mensajeAviso?.let {
                CustomModal1Boton(showDialog = true, message = it, onDismiss = { mensajeAviso = null })
            }
        }
    }

    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = { mostrarFormulario = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            // sin arrastre: al hacer scroll del formulario la hoja no intenta cerrarse
            sheetGesturesEnabled = false,
            dragHandle = null,
            containerColor = Color.White,
        ) {
            FormularioNuevaDireccion(
                viewModel = registroViewModel,
                onCerrar = { mostrarFormulario = false },
                onGuardar = { registroViewModel.registrarNuevaDireccionRetrofit(idusuario) }
            )
        }
    }
}

// ── tarjeta ───────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaDireccion(
    direccion: ModeloDireccionesArray,
    onElegir: () -> Unit,
    onEliminar: () -> Unit,
) {
    val elegida = direccion.seleccionado == 1
    var menuAbierto by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onElegir),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (elegida) 3.dp else 1.dp),
        border = if (elegida) BorderStroke(2.dp, colorElegida) else BorderStroke(1.dp, Color(0xFFE8E5EE))
    ) {
        Row(modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = if (elegida) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (elegida) "Dirección elegida" else "Elegir esta dirección",
                tint = if (elegida) colorElegida else Color(0xFFBDB8C7),
                modifier = Modifier.size(24.dp).padding(top = 1.dp)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = direccion.nombre ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorTexto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (elegida) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Para entregas",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorElegida,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(colorElegida.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (!direccion.telefono.isNullOrBlank()) {
                    Detalle(Icons.Filled.Phone, direccion.telefono)
                }
                Detalle(Icons.Filled.LocationOn, direccion.direccion ?: "", colorTexto)
                if (!direccion.punto_referencia.isNullOrBlank()) {
                    Detalle(Icons.Filled.Place, "Ref: ${direccion.punto_referencia}")
                }
            }

            Box {
                IconButton(onClick = { menuAbierto = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = colorSecundario)
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = colorBorrar) },
                        leadingIcon = { Icon(Icons.Filled.DeleteOutline, null, tint = colorBorrar) },
                        onClick = { menuAbierto = false; onEliminar() }
                    )
                }
            }
        }
    }
}

@Composable
private fun Detalle(icono: ImageVector, texto: String, color: Color = colorSecundario) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp)) {
        Icon(icono, contentDescription = null, tint = colorSecundario, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun SinDirecciones(onAgregar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(AppColors.colorAppPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = AppColors.colorAppPrimary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(Res.string.no_hay_direccion_registrada),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorTexto,
            textAlign = TextAlign.Center
        )
        Text(
            "Agrega la dirección donde quieres recibir tus órdenes.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAgregar,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.colorAppPrimary),
            modifier = Modifier.height(50.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar dirección", fontWeight = FontWeight.Bold)
        }
    }
}

// ── formulario nueva dirección ────────────────────────────────────────────────

@Composable
private fun FormularioNuevaDireccion(
    viewModel: RegistroNuevaDireccionViewModel,
    onCerrar: () -> Unit,
    onGuardar: () -> Unit,
) {
    val nombre by viewModel.nombre.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val direccion by viewModel.direccion.collectAsState()
    val referencia by viewModel.puntoReferencia.collectAsState()

    // los errores se muestran después de intentar guardar
    var validar by remember { mutableStateOf(false) }
    val errorNombre = if (validar && nombre.isBlank()) "Escribe el nombre de quien recibe" else null
    val errorTelefono = when {
        !validar -> null
        telefono.isBlank() -> "Escribe un teléfono"
        telefono.length < 8 -> "El teléfono debe tener 8 dígitos"
        else -> null
    }
    val errorDireccion = if (validar && direccion.isBlank()) "Escribe la dirección" else null

    // título fijo con botón cerrar
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(Res.string.nueva_direccion),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
            Text(
                "¿A dónde llevamos tu orden?",
                style = MaterialTheme.typography.bodySmall,
                color = colorSecundario
            )
        }
        IconButton(onClick = onCerrar) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.cerrar))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding() // el teclado no tapa los campos ni el botón
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Campo(
            valor = nombre,
            onCambio = { viewModel.setNombre(it.take(100)) },
            etiqueta = "Nombre de quien recibe",
            icono = Icons.Filled.Person,
            error = errorNombre,
            capitalizar = KeyboardCapitalization.Words
        )
        Campo(
            valor = telefono,
            onCambio = { viewModel.setTelefono(it.filter(Char::isDigit).take(8)) },
            etiqueta = "Teléfono",
            icono = Icons.Filled.Phone,
            error = errorTelefono,
            ayuda = "8 dígitos, sin guiones",
            teclado = KeyboardType.Phone
        )
        Campo(
            valor = direccion,
            onCambio = { viewModel.setDireccion(it.take(400)) },
            etiqueta = "Dirección",
            icono = Icons.Filled.Home,
            error = errorDireccion,
            ayuda = "Colonia, calle o avenida, número de casa",
            unaLinea = false
        )
        Campo(
            valor = referencia ?: "",
            onCambio = { viewModel.setPuntoReferencia(it.take(400)) },
            etiqueta = "Punto de referencia (opcional)",
            icono = Icons.Filled.Place,
            ayuda = "Ej. frente al parque, portón negro",
            unaLinea = false
        )

        Spacer(Modifier.height(6.dp))

        Button(
            onClick = {
                validar = true
                if (nombre.isNotBlank() && telefono.length == 8 && direccion.isNotBlank()) onGuardar()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.colorAppPrimary)
        ) {
            Text("Guardar dirección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Campo(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String? = null,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizar: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    unaLinea: Boolean = true,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        isError = error != null,
        supportingText = (error ?: ayuda)?.let { texto -> { Text(texto) } },
        singleLine = unaLinea,
        maxLines = if (unaLinea) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = teclado, capitalization = capitalizar),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.colorAppPrimary,
            focusedLabelColor = AppColors.colorAppPrimary,
            focusedLeadingIconColor = AppColors.colorAppPrimary,
            cursorColor = AppColors.colorAppPrimary,
            unfocusedBorderColor = Color(0xFFD6D3DC),
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
