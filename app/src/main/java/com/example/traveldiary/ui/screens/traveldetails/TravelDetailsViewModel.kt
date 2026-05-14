package com.example.traveldiary.ui.screens.traveldetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traveldiary.data.repositories.TripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TravelDetailsViewModel(repository: TripsRepository) : ViewModel() {
    val tripId = MutableStateFlow<Int?>(null)

    val trip = combine(tripId, repository.trips) { id, trips ->
        trips.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
