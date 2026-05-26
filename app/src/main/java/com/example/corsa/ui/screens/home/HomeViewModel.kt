package com.example.corsa.ui.screens.home

import androidx.lifecycle.ViewModel

data class HomeState(
    val goalKm: Double,
    val currentKm: Double,
    val progress: Double,
    val locationName: String
)

class HomeViewModel: ViewModel() {
    companion object {
        val state = HomeState(
            50.0,
            25.0,
            0.5,
            "Galeata"
        )
    }
}