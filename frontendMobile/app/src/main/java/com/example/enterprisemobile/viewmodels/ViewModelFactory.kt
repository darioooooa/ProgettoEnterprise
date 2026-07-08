package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.enterprisemobile.data.repository.AdminRepository
import com.example.enterprisemobile.data.repository.PrenotazioneRepository
import com.example.enterprisemobile.data.repository.ViaggioRepository

class ViewModelFactory(
    private val repository: ViaggioRepository,
    private val prenotazioneRepository: PrenotazioneRepository,
    private val adminRepository: AdminRepository

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        //  Diciamo alla Factory come costruire l'HomeOrganizzatoreViewModel
        if (modelClass.isAssignableFrom(HomeOrganizzatoreViewModel::class.java)) {
            return HomeOrganizzatoreViewModel(repository,prenotazioneRepository,adminRepository) as T
        }

        //Quando verranno create le altre schermate, gli altripezzi di codice vanno aggiunti qui sotto
        // }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}