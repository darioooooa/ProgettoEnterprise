package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.data.model.RichiestaPromozioneDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ViaggiatoreApiService {

    @Multipart
    @POST("viaggiatori/richieste-promozione")
    suspend fun inviaRichiestaPromozione(
        @Part("richiesta") richiesta: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<RichiestaPromozioneDTO>

    @GET("viaggiatori/{id}/richiesta-pendente")
    suspend fun getRichiestaPendente(@Path("id") id: Long): Response<RichiestaPromozioneDTO>
}