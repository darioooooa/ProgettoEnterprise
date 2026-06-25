package com.example.enterprisemobile.data.api

import android.content.Context
import com.example.enterprisemobile.data.security.AuthInterceptor
import com.example.enterprisemobile.data.security.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private const val CURRENT_IP = "10.0.2.2"

    private const val BASE_URL = "https://$CURRENT_IP:8443/api/v1/"
    private const val KEYCLOAK_BASE_URL = "http://$CURRENT_IP:8081/"

    private var retrofit: Retrofit? = null
    private var retrofitKeycloak: Retrofit? = null

    // Configura un client OkHttp in grado di accettare certificati SSL auto-firmati
    private fun ottieniOkHttpClientSicuro(context: Context, usaInterceptor: Boolean): OkHttpClient {
        try {
            // Crea un trust manager che si fida di qualsiasi certificato
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            // Inizializza il contesto SSL usando il nostro trust manager permissivo
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { hostname, _ ->
                    // Accetta la connessione sia che arrivi da localhost, sia dall'ip dell'emulatore 10.0.2.2
                    hostname == CURRENT_IP || hostname == "localhost" || hostname == "127.0.0.1"
                }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)

            // Aggiunge l'AuthInterceptor solo se richiesto (per il backend, non per Keycloak)
            if (usaInterceptor) {
                val sessionManager = SessionManager(context)
                builder.addInterceptor(AuthInterceptor(sessionManager))
            }

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // Fornisce l'istanza di Retrofit configurata per parlare con il backend Spring Boot
    fun ottieniClientBackend(context: Context): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(ottieniOkHttpClientSicuro(context, usaInterceptor = true)) //  Usa il client SSL permissivo + Interceptor
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    // Fornisce l'istanza di Retrofit configurata per parlare con Keycloak
    fun ottieniClientKeycloak(context: Context): Retrofit {
        if (retrofitKeycloak == null) {
            retrofitKeycloak = Retrofit.Builder()
                .baseUrl(KEYCLOAK_BASE_URL)
                .client(ottieniOkHttpClientSicuro(context, usaInterceptor = false)) // Usa il client SSL permissivo senza Interceptor
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofitKeycloak!!
    }

    // Funzione per istanziare i servizi di autenticazione di Keycloak
    fun ottieniAuthService(context: Context): AuthApiService {
        return ottieniClientKeycloak(context).create(AuthApiService::class.java)
    }
}