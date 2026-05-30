package com.example.corsa.ui.screens.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.UserEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class HomeState(
    val goalKm: Float,
    val currentKm: Float,
    val progress: Float,
    val locationName: String
)

data class StopWatchStatus(
    val elapsedTime: Long = 0L,
    val isRunning: Boolean = false
) {
    // Derive a ready‑to‑display string (HH:MM:SS or MM:SS)
    val formattedTime: String
        get() {
            val totalSeconds = elapsedTime / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
}

data class StopWatchAction(
    val start: () -> Unit,
    val pause: () -> Unit,
    val stop: () -> Unit,
)

class HomeViewModel(
    private val runsRepository: RunsRepository,
    private val profilesRepository: ProfilesRepository,
    private val locationProvider: LocationProvider
): ViewModel() {
    val stopWatchActions = StopWatchAction(
        { start() },
        { pause() },
        { reset() }
    )
    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    // Derived from profile reactively
    private val _state = MutableStateFlow<HomeState?>(null)
    val state: StateFlow<HomeState?> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val loaded = profilesRepository.getMyProfile()
            val weeklyKm = profilesRepository.weeklyKmByUserId(loaded.id)
            val goalKm = loaded.level * 10f

            _profile.value = loaded
            _state.value = HomeState(
                goalKm = goalKm,
                currentKm = weeklyKm,
                progress = weeklyKm / goalKm,
                locationName = "Galeata"
            )
        }
    }
    data class DebugLocation(
        val lat: Double, val lng: Double, val accuracy: Float
    )

    private val _liveLocation = MutableStateFlow<DebugLocation?>(null)
    val liveLocation: StateFlow<DebugLocation?> = _liveLocation

    fun startLocationUpdates() {
        viewModelScope.launch {
            locationProvider.locationFlow(intervalMs = 5_000L)   // 5 s for testing
                .collect { location ->
                    _liveLocation.value = DebugLocation(
                        lat      = location.latitude,
                        lng      = location.longitude,
                        accuracy = location.accuracy
                    )
                }
        }
    }

    private val _timerState = MutableStateFlow(StopWatchStatus())
    val timerState: StateFlow<StopWatchStatus> = _timerState

    private var timerJob: Job? = null

    private fun start() {
        if (_timerState.value.isRunning) return
        _timerState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(200L)                    // tick every second
                _timerState.update { current ->
                    current.copy(
                        elapsedTime = current.elapsedTime + 200L
                    )
                }
            }
        }
    }

    private fun pause() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
    }

    private fun reset() {
        pause()
        _timerState.update { current ->
            current.copy(
                elapsedTime = 0L
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}