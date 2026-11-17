package com.tatanstudios.eltuncazometapan.vistas.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tatanstudios.eltuncazometapan.resources.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.tatanstudios.eltuncazometapan.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import com.tatanstudios.eltuncazometapan.model.rutas.Routes
import com.tatanstudios.eltuncazometapan.componentes.ModalIniciarSesion
import com.tatanstudios.eltuncazometapan.componentes.haySesion
import com.tatanstudios.eltuncazometapan.vistas.opciones.menu.MenuPrincipalScreen
import com.tatanstudios.eltuncazometapan.vistas.principal.ordenes.ListadoOrdenesScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.tatanstudios.eltuncazometapan.extras.EstadoCarrito
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PrincipalScreen(navController: NavHostController, selectedScreenVar: String) {
    // ✅ Cambio principal: usar selectedScreenVar directamente como valor inicial
    // sin usar selectedScreenVar como key en rememberSaveable
    var selectedScreen by rememberSaveable { mutableStateOf(selectedScreenVar) }

    // ✅ Agregar LaunchedEffect para sincronizar cuando cambie selectedScreenVar
    LaunchedEffect(selectedScreenVar) {
        selectedScreen = selectedScreenVar
    }

    val scope = rememberCoroutineScope()
    var canClickCart by remember { mutableStateOf(true) }

    // sin sesión, el carrito y las órdenes piden iniciar sesión
    val sesion = haySesion()
    var mensajeIniciarSesion by remember { mutableStateOf<String?>(null) }
    val stringSesionCarrito = stringResource(Res.string.inicia_sesion_para_carrito)
    val stringSesionOrdenes = stringResource(Res.string.inicia_sesion_para_ordenes)

    // si se cierra sesión estando en "ordenes", se vuelve al menú
    LaunchedEffect(sesion) {
        if (!sesion && selectedScreen == "ordenes") selectedScreen = "menu"
    }

    val openCart = {
        if (!sesion) {
            mensajeIniciarSesion = stringSesionCarrito
        } else if (canClickCart) {
            canClickCart = false
            navController.navigate(Routes.VistaCarrito.route) {
                launchSingleTop = true
            }
            scope.launch {
                delay(500)
                canClickCart = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBarWithCart(
                selectedScreen = selectedScreen,
                onMenuClick = { selectedScreen = "menu" },
                onOrdersClick = {
                    if (sesion) selectedScreen = "ordenes" else mensajeIniciarSesion = stringSesionOrdenes
                },
                onCartClick = openCart
            )
        },
        floatingActionButton = {
            // contador de productos en el carrito (se actualiza al cargar el menú)
            val cantidadCarrito by EstadoCarrito.cantidad.collectAsState()

            Box {
                FloatingActionButton(
                    onClick = openCart,
                    backgroundColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = if (cantidadCarrito > 0) "Carrito, $cantidadCarrito productos" else "Carrito",
                        tint = AppColors.colorAppPrimary
                    )
                }
                if (sesion && cantidadCarrito > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cantidadCarrito > 99) "99+" else cantidadCarrito.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) { innerPadding ->
        when (selectedScreen) {
            "menu" -> MenuPrincipalScreen(
                navController = navController,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 72.dp
                )
            )
            "ordenes" -> {
                ListadoOrdenesScreen(navController)
            }
        }
    }

    mensajeIniciarSesion?.let { mensaje ->
        ModalIniciarSesion(
            showDialog = true,
            mensaje = mensaje,
            navController = navController,
            onDismiss = { mensajeIniciarSesion = null }
        )
    }
}

@Composable
fun BottomAppBarWithCart(
    selectedScreen: String,
    onMenuClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onCartClick: () -> Unit
) {

    BottomAppBar(
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)                     // fondo blanco
            .windowInsetsPadding(WindowInsets.navigationBars) // y lo extiende a la zona del gesto
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onMenuClick() }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isSelected = selectedScreen == "menu"
                Icon(
                    painter = painterResource(Res.drawable.icono_comida),
                    contentDescription = stringResource(Res.string.menu),
                    tint = if (isSelected)
                        AppColors.colorAppPrimary
                    else
                        Color.Gray
                )
                Text(
                    text = stringResource(Res.string.menu),
                    color = if (isSelected) AppColors.colorAppPrimary else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // espacio del FAB

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOrdersClick() }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isSelected = selectedScreen == "ordenes"
                Icon(
                    painter = painterResource(Res.drawable.icono_comida),
                    contentDescription = stringResource(Res.string.ordenes),
                    tint = if (isSelected) AppColors.colorAppPrimary else Color.Gray
                )
                Text(
                    text = stringResource(Res.string.ordenes),
                    color = if (isSelected) AppColors.colorAppPrimary else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}



