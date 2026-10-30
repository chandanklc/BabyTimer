package com.example.babytimer.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.babytimer.data.BabyPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplashViewModel(
    private val repository: BabyPreferencesRepository
) : ViewModel() {

    val babyName: StateFlow<String> = repository.babyName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "Baby"
        )

    val photoUri: StateFlow<String?> = repository.photoUri
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun updateBabyName(name: String) {
        viewModelScope.launch {
            repository.updateBabyName(name)
        }
    }

    fun updatePhotoUri(uri: String?) {
        viewModelScope.launch {
            repository.updatePhotoUri(uri)
        }
    }
}
