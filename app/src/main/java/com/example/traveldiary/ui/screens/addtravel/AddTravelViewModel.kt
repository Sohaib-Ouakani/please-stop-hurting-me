package com.example.traveldiary.ui.screens.addtravel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traveldiary.data.database.Trip
import com.example.traveldiary.data.repositories.TripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTravelState(
    val destination: String = "",
    val date: String = "",
    val description: String = "",
    val imageUri: Uri? = null,

    val showLocationDisabledAlert: Boolean = false,
    val showPermissionDeniedAlert: Boolean = false,
    val showPermissionPermanentlyDeniedSnackbar: Boolean = false,
    val showNoConnectivitySnackbar: Boolean = false
) {
    val canSubmit get() = destination.isNotBlank() && date.isNotBlank() && description.isNotBlank()

    fun toTrip() = Trip(
        name = destination,
        description = description,
        date = date,
        imageUri = imageUri?.toString()
    )
}

data class AddTravelActions(
    val setDestination: (String) -> Unit,
    val setDate: (String) -> Unit,
    val setDescription: (String) -> Unit,
    val setImageUri: (Uri?) -> Unit,

    val setShowLocationDisabledAlert: (Boolean) -> Unit,
    val setShowPermissionDeniedAlert: (Boolean) -> Unit,
    val setShowPermissionPermanentlyDeniedSnackbar: (Boolean) -> Unit,
    val setShowNoConnectivitySnackbar: (Boolean) -> Unit,

    val submit: () -> Unit
)

class AddTravelViewModel(repository: TripsRepository) : ViewModel() {
    private val _state = MutableStateFlow(AddTravelState())
    val state = _state.asStateFlow()

    val actions = AddTravelActions(
        { destination -> _state.update { it.copy(destination = destination) } },
        { date -> _state.update { it.copy(date = date) } },
        { description -> _state.update { it.copy(description = description) } },
        { imageUri -> _state.update { it.copy(imageUri = imageUri) } },

        { show -> _state.update { it.copy(showLocationDisabledAlert = show) } },
        { show -> _state.update { it.copy(showPermissionDeniedAlert = show) } },
        { show -> _state.update { it.copy(showPermissionPermanentlyDeniedSnackbar = show) } },
        { show -> _state.update { it.copy(showNoConnectivitySnackbar = show) } },

        {
            if (state.value.canSubmit) {
                viewModelScope.launch { repository.upsert(state.value.toTrip()) }
            }
        }
    )
}
