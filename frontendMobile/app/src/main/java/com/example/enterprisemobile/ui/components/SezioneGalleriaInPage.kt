package com.example.enterprisemobile.ui.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.enterprisemobile.ui.theme.SuccessGreen
import com.example.enterprisemobile.viewmodels.GalleriaViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SezioneGalleriaInPage(
    viewModel: GalleriaViewModel,
    viaggioId: Long,
    isMioViaggio: Boolean,
    context: Context
) {
    val immagini by viewModel.immagini.collectAsState()

    // Effetto per caricare le immagini non appena l'id viaggio è disponibile
    LaunchedEffect(viaggioId) {
        if (viaggioId != -1L) {
            viewModel.caricaImmagini(viaggioId)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titolo della sezione integrata
        Text(
            text = "Galleria fotografica",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Banner di avviso temporaneo interno della galleria
        viewModel.messaggioAvviso?.let { avviso ->
            val coloreBanner = if (viewModel.tipoAvviso == "successo") SuccessGreen else MaterialTheme.colorScheme.error
            Surface(
                color = coloreBanner.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(avviso, color = coloreBanner, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(
                        text = "×",
                        color = coloreBanner,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.messaggioAvviso = null }
                    )
                }
            }
        }

        // Form di aggiunta per l'organizzatore
        if (isMioViaggio) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Aggiungi un'immagine al viaggio", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = viewModel.nuovaImmagineUrl,
                        onValueChange = { viewModel.nuovaImmagineUrl = it },
                        placeholder = { Text("Incolla l'URL di un'immagine pubblica...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.nuovaImmaginePubblica = !viewModel.nuovaImmaginePubblica }
                        ) {
                            Text(
                                text = if (viewModel.nuovaImmaginePubblica) "👁️ Pubblica" else "🔒 Privata",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.aggiungiImmagine(context, viaggioId) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("Invia foto", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Slider interattivo a trascinamento
        if (immagini.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nessuna immagine presente in galleria.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { immagini.size })

            // Aggiorna l'indice del ViewModel quando l'utente fa uno swipe manuale
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { pagina ->
                    viewModel.immagineCorrenteIndex = pagina
                }
            }

            // Sincronizzazione inversa (se l'indice del ViewModel cambia dall'esterno)
            LaunchedEffect(viewModel.immagineCorrenteIndex) {
                if (viewModel.immagineCorrenteIndex in immagini.indices && viewModel.immagineCorrenteIndex != pagerState.currentPage) {
                    pagerState.scrollToPage(viewModel.immagineCorrenteIndex)
                }
            }
            val fotoAttiva = immagini.getOrNull(pagerState.currentPage)
            if (fotoAttiva != null) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Componente di scorrimento orizzontale a trascinamento touch
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { pagina ->
                                val immagineRenderizzata = immagini[pagina]
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(immagineRenderizzata.url)
                                            .crossfade(true) // Transizione morbida
                                            .size(800, 600) // Abbassa la risoluzione massima in memoria
                                            .build(),
                                        contentDescription = "Immagine del viaggio",
                                        modifier = Modifier.fillMaxSize(),
                                        // Si adatta mantenendo le proporzioni
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            // Contatore dell'indice corrente (Sempre in primo piano sopra il pager)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1} / ${immagini.size}",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }

                            // Badge modifica visibilità per l'organizzatore
                            if (isMioViaggio) {
                                val coloreBadge = if (fotoAttiva.pubblica) SuccessGreen else MaterialTheme.colorScheme.outline
                                Surface(
                                    color = coloreBadge,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .clickable { viewModel.cambiaVisibilita(context, viaggioId, fotoAttiva) }
                                ) {
                                    Text(
                                        text = if (fotoAttiva.pubblica) "👁️ Pubblica" else "🔒 Privata",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Sistema di sicurezza a doppio click (4 secondi di timeout) ritornato nativamente
                    if (isMioViaggio) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val inAttesaConferma = viewModel.idImmagineDaEliminare == fotoAttiva.id

                        Button(
                            onClick = { viewModel.cancellaImmagine(context, viaggioId, fotoAttiva.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inAttesaConferma) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = if (inAttesaConferma) "Conferma eliminazione" else "Elimina questa foto",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}