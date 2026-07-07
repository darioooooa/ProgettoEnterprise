package com.example.enterprisemobile.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    // Genera una chiave master memorizzata nel KeyStore sicuro di Android
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Inizializza le SharedPreferences cifrate a livello di file XML (AES256)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "enterprise_secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun salvaTokenAccesso(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    fun ottieniTokenAccesso(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun salvaRefreshToken(refreshToken: String) {
        sharedPreferences.edit().putString("refresh_token", refreshToken).apply()
    }

    fun ottieniRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun salvaDatiUtente(username: String, ruolo: String, userId: String) {
        sharedPreferences.edit().apply {
            putString("username", username)
            putString("ruolo", ruolo)
            putString("user_id", userId)
        }.apply()
    }

    fun ottieniUsername(): String? = sharedPreferences.getString("username", null)
    fun ottieniRuolo(): String? = sharedPreferences.getString("ruolo", null)
    fun ottieniIdUtente(): String? = sharedPreferences.getString("user_id", null)

    fun isLoggato(): Boolean {
        return ottieniTokenAccesso() != null
    }

    fun cancellaSessione() {
        sharedPreferences.edit().clear().apply()
    }


}