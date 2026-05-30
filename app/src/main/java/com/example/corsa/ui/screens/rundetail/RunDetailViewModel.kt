package com.example.corsa.ui.screens.rundetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.RunsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RunDetailUiState {
    data object Loading : RunDetailUiState
    data class Error(val message: String) : RunDetailUiState
    data class Success(
        val run: Run,
        val isMapExpanded: Boolean = false
    ) : RunDetailUiState
}

data class RunDetailActions(
    val setMapExpanded: (Boolean) -> Unit,
    val retry: () -> Unit
)

class RunDetailViewModel(
    private val repository: RunsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val runId: String = checkNotNull(savedStateHandle["runId"])

    private val _isMapExpanded = MutableStateFlow(false)
    private val _run = MutableStateFlow<Run?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val state = combine(_run, _error, _isMapExpanded) { run, error, expanded ->
        when {
            error != null -> RunDetailUiState.Error(error)
            run != null -> RunDetailUiState.Success(run = run, isMapExpanded = expanded)
            else -> RunDetailUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RunDetailUiState.Loading
    )

    val actions = RunDetailActions(
        setMapExpanded = { expanded -> _isMapExpanded.update { expanded } },
        retry = { loadRun() }
    )

    init {
        loadRun()
    }

    private fun loadRun() {
        _run.update { null }
        _error.update { null }
        Log.d("RunDetailVM", "Loading run with id: '$runId'")  // ← check this first
        viewModelScope.launch {
            runCatching { repository.getRunById(runId) }
                .onSuccess { run ->
                    Log.d("RunDetailVM", "Loaded run: ${run.id}")
                    _run.update { run }
                }
                .onFailure { throwable ->
                    Log.e("RunDetailVM", "Failed to load run", throwable)  // logs full stack trace
                    _error.update { throwable.message ?: "Unknown error" }
                }
        }
    }
}