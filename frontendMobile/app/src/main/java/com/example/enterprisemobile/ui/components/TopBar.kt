package com.example.enterprisemobile.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.AmiciziaActivity
import com.example.enterprisemobile.DiventaOrganizzatoreActivity
import com.example.enterprisemobile.MainActivity
import com.example.enterprisemobile.MiePrenotazioniActivity
import com.example.enterprisemobile.ProfiloActivity
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    titolo: String,
    nomeUtente: String,
    mostraFrecciaIndietro: Boolean = false,
    notificheMenu: Int = 0,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavy)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (mostraFrecciaIndietro) {
                Icon(
                    Icons.Filled.ArrowBack, "Indietro", tint = WhiteText,
                    modifier = Modifier.clickable { onBackClick() }.padding(end = 12.dp)
                )
            }
            Text(titolo, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(nomeUtente, color = WhiteText, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
            Icon(Icons.Filled.AccountCircle, "Profilo", tint = WhiteText, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { onMenuClick() }) {
                BadgedBox(
                    badge = {
                        if (notificheMenu > 0) {
                            Badge(containerColor = DangerRed, contentColor = WhiteText) {
                                Text(notificheMenu.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Menu, "Menu", tint = WhiteText)
                }
            }
        }
    }
}

@Composable
fun EnterpriseScaffold(
    titolo: String,
    nomeUtente: String,
    mostraFrecciaIndietro: Boolean = false,
    badgeAmiciOverride: Int? = null,
    onBackClick: () -> Unit = {},
    gesturesEnabled: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sessionManager = SessionManager(context)
    val ruoloUtente = sessionManager.ottieniRuolo() ?: ""
    val isViaggiatore = ruoloUtente.contains("VIAGGIATORE", ignoreCase = true)
    val isAdmin = ruoloUtente.contains("ADMIN", ignoreCase = true)

    var notificheAmiciFetch by remember { mutableIntStateOf(0) }
    val notificheAmici = badgeAmiciOverride ?: notificheAmiciFetch

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, badgeAmiciOverride) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && badgeAmiciOverride == null) {
                scope.launch {
                    try {
                        val api = RetrofitClient.ottieniAmiciziaService(context)
                        notificheAmiciFetch = api.getRichiesteRicevute().size
                    } catch (e: Exception) {
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled || drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = DarkNavy, modifier = Modifier.width(300.dp)) {
                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = WhiteText, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$nomeUtente", color = WhiteText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Person, null, tint = WhiteText) },
                    label = { Text("Il mio profilo", color = WhiteText, fontSize = 16.sp) },
                    selected = titolo == "IL MIO PROFILO",
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (titolo != "IL MIO PROFILO") {
                            context.startActivity(Intent(context, ProfiloActivity::class.java))
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CardOverlay, unselectedContainerColor = Color.Transparent)
                )


                if (isViaggiatore) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Group, null, tint = WhiteText) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Amici", color = WhiteText, fontSize = 16.sp)

                                if (notificheAmici > 0) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Badge(containerColor = DangerRed, contentColor = WhiteText) {
                                        Text(notificheAmici.toString(), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        },
                        selected = titolo == "AMICI",
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (titolo != "AMICI") {
                                context.startActivity(Intent(context, AmiciziaActivity::class.java))
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CardOverlay, unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.List, null, tint = WhiteText) },
                        label = { Text("Le Mie Prenotazioni", color = WhiteText, fontSize = 16.sp) },
                        selected = titolo == "I MIEI VIAGGI",
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (titolo != "I MIEI VIAGGI") {
                                context.startActivity(Intent(context, MiePrenotazioniActivity::class.java))
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CardOverlay, unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Work, null, tint = WhiteText) },
                        label = { Text("Diventa Organizzatore", color = WhiteText, fontSize = 16.sp) },
                        selected = titolo == "DIVENTA ORGANIZZATORE",
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (titolo != "DIVENTA ORGANIZZATORE") {
                                context.startActivity(Intent(context, DiventaOrganizzatoreActivity::class.java))
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CardOverlay, unselectedContainerColor = Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = DangerRed) },
                    label = { Text("Disconnetti", color = DangerRed, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }

                        val sessionManager = SessionManager(context)
                        sessionManager.cancellaSessione()

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    titolo = titolo,
                    nomeUtente = nomeUtente,
                    mostraFrecciaIndietro = mostraFrecciaIndietro,
                    notificheMenu = notificheAmici,
                    onBackClick = onBackClick,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }
}