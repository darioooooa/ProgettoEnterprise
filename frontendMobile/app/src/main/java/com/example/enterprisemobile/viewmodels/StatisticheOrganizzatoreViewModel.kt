package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.PrenotazioneDTO
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class RigaViaggioStat(
    val titolo: String,
    val data: String,
    val postiVenduti: Int,
    val ricavo: Double,
    val dataOggetto: LocalDate? = null
)

class StatisticheOrganizzatoreViewModel(application: Application) : AndroidViewModel(application) {

    private val viaggioApi = RetrofitClient.ottieniViaggioService(application)
    private val prenotazioneApi = RetrofitClient.ottieniPrenotazioneService(application)
    private val sessionManager = SessionManager(application)

    var totaleViaggi by mutableStateOf(0)
    var totaleRecensioni by mutableStateOf(0)
    var mediaRecensioni by mutableStateOf(0.0)

    var filtroGuadagni by mutableStateOf("MESE")
    var guadagni by mutableStateOf(mapOf("SETTIMANA" to 0.0, "MESE" to 0.0, "ANNO" to 0.0, "TOTALE" to 0.0))

    var viaggiRecenti by mutableStateOf<List<RigaViaggioStat>>(emptyList())
    var isLoading by mutableStateOf(true)

    init {
        caricaStatistiche()
    }

    fun caricaStatistiche() {
        viewModelScope.launch {
            isLoading = true
            try {
                val orgId = sessionManager.ottieniIdUtente()?.toLongOrNull() ?: return@launch

                android.util.Log.d("STATS_DEBUG", "Organizzatore ID: $orgId")

                // 1. Scarica i Viaggi dell'Organizzatore
                val viaggiResponse = viaggioApi.getViaggiByOrganizzatore(orgId)
                val viaggi = if (viaggiResponse.isSuccessful) viaggiResponse.body() ?: emptyList() else emptyList()

                android.util.Log.d("STATS_DEBUG", "Viaggi trovati: ${viaggi.size}")

                // 2. Scarica TUTTE le prenotazioni con loop paginazione
                val tuttePrenotazioni = mutableListOf<PrenotazioneDTO>()
                var paginaCorrente = 0
                var continuaLoop = true

                while (continuaLoop) {
                    try {
                        val response = prenotazioneApi.getMiePrenotazioni(page = paginaCorrente)
                        if (response.isSuccessful && response.body() != null) {
                            val pageData = response.body()!!
                            val content = pageData.content ?: emptyList()
                            tuttePrenotazioni.addAll(content)

                            android.util.Log.d("STATS_DEBUG", "Pagina $paginaCorrente: ${content.size} prenotazioni")

                            if (content.size < 10 || paginaCorrente >= (pageData.totalPages - 1)) {
                                continuaLoop = false
                            } else {
                                paginaCorrente++
                            }
                        } else {
                            android.util.Log.e("STATS_ERROR", "Errore fetch prenotazioni pagina $paginaCorrente: ${response.code()}")
                            continuaLoop = false
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("STATS_ERROR", "Eccezione fetch prenotazioni pagina $paginaCorrente", e)
                        continuaLoop = false
                    }
                }

                android.util.Log.d("STATS_DEBUG", "Totale prenotazioni caricate: ${tuttePrenotazioni.size}")

                // 3. Elaborazione dei Numeri Base
                totaleViaggi = viaggi.size


                var sommaVotiPonderata = 0.0
                var totaleRecensioniGlobali = 0

                viaggi.forEach { v ->
                    val numRec = v.numeroRecensioni ?: 0
                    val mediaV = v.mediaRecensioni ?: 0.0

                    if (numRec > 0) {
                        totaleRecensioniGlobali += numRec
                        sommaVotiPonderata += (mediaV * numRec)

                        android.util.Log.d("STATS_DEBUG", "Viaggio '${v.titolo}': $numRec recensioni, media $mediaV")
                    }
                }

                totaleRecensioni = totaleRecensioniGlobali
                mediaRecensioni = if (totaleRecensioniGlobali > 0) {
                    val mediaCalcolata = sommaVotiPonderata / totaleRecensioniGlobali
                    // Arrotonda a 1 decimale
                    String.format("%.1f", mediaCalcolata).replace(",", ".").toDouble()
                } else 0.0

                android.util.Log.d("STATS_DEBUG", "Recensioni: totale=$totaleRecensioni, media=$mediaRecensioni")

                // 4. Elaborazione Incassi
                var guadagnoTot = 0.0
                var guadagnoAnno = 0.0
                var guadagnoMese = 0.0
                var guadagnoSettimana = 0.0

                val oggi = LocalDate.now()
                val annoCorrente = oggi.year
                val meseCorrente = oggi.monthValue
                val unaSettimanaFa = oggi.minusDays(7)

                val mappaPosti = mutableMapOf<Long, Int>()
                val mappaRicavi = mutableMapOf<Long, Double>()

                tuttePrenotazioni.filter { it.stato == "CONFERMATA" }.forEach { p ->
                    val viaggio = viaggi.find { it.id == p.viaggioId }
                    if (viaggio != null) {
                        val incasso = p.numeroPersone * viaggio.prezzo
                        val vId = viaggio.id!!
                        mappaPosti[vId] = (mappaPosti[vId] ?: 0) + p.numeroPersone
                        mappaRicavi[vId] = (mappaRicavi[vId] ?: 0.0) + incasso

                        guadagnoTot += incasso

                        val dataP = p.dataPrenotazione?.take(10)?.let {
                            try { LocalDate.parse(it) } catch(e: Exception) { null }
                        }

                        if (dataP != null) {
                            if (dataP.year == annoCorrente) {
                                guadagnoAnno += incasso
                                if (dataP.monthValue == meseCorrente) {
                                    guadagnoMese += incasso
                                }
                            }
                            if (!dataP.isBefore(unaSettimanaFa) && !dataP.isAfter(oggi)) {
                                guadagnoSettimana += incasso
                            }
                        }
                    }
                }

                guadagni = mapOf(
                    "SETTIMANA" to guadagnoSettimana,
                    "MESE" to guadagnoMese,
                    "ANNO" to guadagnoAnno,
                    "TOTALE" to guadagnoTot
                )

                android.util.Log.d("STATS_DEBUG", "Guadagni: Totale=$guadagnoTot, Mese=$guadagnoMese, Anno=$guadagnoAnno, Settimana=$guadagnoSettimana")

                // 5. Preparazione della Lista Viaggi Recenti per l'interfaccia
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val lista = viaggi.mapNotNull { v ->
                    if (v.dataInizio.isNotEmpty()) {
                        val dataObj = try { LocalDate.parse(v.dataInizio) } catch(e: Exception) { LocalDate.now() }
                        val dataStr = try { dataObj.format(formatter) } catch(e: Exception) { v.dataInizio }
                        RigaViaggioStat(
                            titolo = v.titolo,
                            data = dataStr,
                            postiVenduti = mappaPosti[v.id ?: 0L] ?: 0,
                            ricavo = mappaRicavi[v.id ?: 0L] ?: 0.0,
                            dataOggetto = dataObj
                        )
                    } else null
                }.sortedWith(compareByDescending<RigaViaggioStat> { it.ricavo }.thenByDescending { it.dataOggetto })

                viaggiRecenti = lista

            } catch (e: Exception) {
                android.util.Log.e("STATS_ERROR", "Eccezione generale", e)
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}