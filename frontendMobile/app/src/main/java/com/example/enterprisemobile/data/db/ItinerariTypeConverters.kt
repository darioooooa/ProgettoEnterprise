package com.example.enterprisemobile.data.db

import androidx.room.TypeConverter
import com.example.enterprisemobile.model.ViaggioDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ItinerariTypeConverters {
    private val gson = Gson()

    // Trasforma la stringa di testo salvata nel db in una lista di oggetti utilizzabile dall'app
    @TypeConverter
    fun daStringaAViaggi(valore: String?): List<ViaggioDTO> {
        if (valore == null) return emptyList()
        val listType = object : TypeToken<List<ViaggioDTO>>() {}.type
        return gson.fromJson(valore, listType)
    }

    // Trasforma la lista di viaggi in una stringa di testo JSON per poterla salvare nel db
    @TypeConverter
    fun daViaggiAStringa(lista: List<ViaggioDTO>?): String {
        return gson.toJson(lista ?: emptyList<ViaggioDTO>())
    }
}