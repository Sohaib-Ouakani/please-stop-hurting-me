package com.example.traveldiary.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traveldiary.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    // We are using Compose state directly inside the ViewModel
    // Pro: no need to use .collectAsStateWithLifecycle() in the UI
    // Cons: not thread-safe, ties the ViewModel to Jetpack Compose
    var username by mutableStateOf((""))
        private set

    fun updateUsername(value: String) {
        username = value
        viewModelScope.launch {
            repository.setUsername(value)
        }
    }

    init {
        viewModelScope.launch {
            username = repository.username.first()
        }
    }
}
