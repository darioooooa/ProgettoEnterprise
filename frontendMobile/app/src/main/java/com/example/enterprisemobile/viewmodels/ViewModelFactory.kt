package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.enterprisemobile.data.repository.ViaggioRepository

class HomeOrganizzatoreViewModelFactory(
    private val repository: ViaggioRepository,
    private val isOnline: Boolean
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeOrganizzatoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeOrganizzatoreViewModel(repository, isOnline) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}