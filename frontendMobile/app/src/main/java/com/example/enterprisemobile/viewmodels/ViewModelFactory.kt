package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.enterprisemobile.data.repository.ViaggioRepository

class ViewModelFactory(
    private val repository: ViaggioRepository

    //se servono altri repository nella factory vanno  aggiunti semplicemente qui
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        //  Diciamo alla Factory come costruire l'HomeOrganizzatoreViewModel
        if (modelClass.isAssignableFrom(HomeOrganizzatoreViewModel::class.java)) {
            return HomeOrganizzatoreViewModel(repository) as T
        }

        //Quando verranno create le altre schermate, gli altripezzi di codice vanno aggiunti qui sotto
        // }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}