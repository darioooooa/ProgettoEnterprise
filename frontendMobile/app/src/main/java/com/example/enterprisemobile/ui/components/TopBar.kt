package com.example.enterprisemobile.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.AmiciziaActivity
import com.example.enterprisemobile.DiventaOrganizzatoreActivity
import com.example.enterprisemobile.MainActivity
import com.example.enterprisemobile.MiePrenotazioniActivity
import com.example.enterprisemobile.ProfiloActivity
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.security.SessionManager
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
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (mostraFrecciaIndietro) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .padding(end = 12.dp)
                )
            }
            Text(
                text = titolo,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val intent = Intent(context, ProfiloActivity::class.java)
                        context.startActivity(intent)
                    }
                    .padding(4.dp)
            ) {
                Text(
                    text = nomeUtente,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profilo",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onMenuClick() }) {
                BadgedBox(
                    badge = {
                        if (notificheMenu > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(notificheMenu.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
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
                        // Gestione silenziosa dell'errore di rete
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
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = nomeUtente,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onSurface) },
                    label = { Text("Il mio profilo", fontSize = 16.sp) },
                    selected = titolo == "IL MIO PROFILO",
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (titolo != "IL MIO PROFILO") {
                            context.startActivity(Intent(context, ProfiloActivity::class.java))
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                if (isViaggiatore) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Group, null, tint = MaterialTheme.colorScheme.onSurface) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Amici", fontSize = 16.sp)

                                if (notificheAmici > 0) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
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
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedContainerColor = Color.Transparent,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.List, null, tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Le Mie Prenotazioni", fontSize = 16.sp) },
                        selected = titolo == "I MIEI VIAGGI",
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (titolo != "I MIEI VIAGGI") {
                                context.startActivity(Intent(context, MiePrenotazioniActivity::class.java))
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedContainerColor = Color.Transparent,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Work, null, tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Diventa Organizzatore", fontSize = 16.sp) },
                        selected = titolo == "DIVENTA ORGANIZZATORE",
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (titolo != "DIVENTA ORGANIZZATORE") {
                                context.startActivity(Intent(context, DiventaOrganizzatoreActivity::class.java))
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedContainerColor = Color.Transparent,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Disconnetti", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = MaterialTheme.colorScheme.error
                    ),
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