package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.data.model.PrenotazioneDTO
import com.example.enterprisemobile.model.PagamentoDTO
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PagamentoApiService {

    @POST("pagamento/crea-intent/{idPrenotazione}")
    suspend fun creaPaymentIntent(@Path("idPrenotazione") idPrenotazione: Long): Map<String, String>

    @POST("pagamento/conferma")
    suspend fun confermaPagamento(@Body pagamento: PagamentoDTO): PrenotazioneDTO
}