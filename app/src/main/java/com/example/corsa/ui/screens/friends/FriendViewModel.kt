package com.example.corsa.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class UserRankEntry(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val weekKm: Double,
    val level: Int,
)

enum class SortBy { Kilometers, Level }

class FriendViewModel : ViewModel() {
    private val _entries = MutableStateFlow<List<UserRankEntry>>(emptyList())
    val entries: StateFlow<List<UserRankEntry>> = _entries
    fun loadRanking(sortBy: SortBy) {
        viewModelScope.launch {
            val raw = listOf(
            UserRankEntry("1", "J. Donahue", null, 64.2, 5),
            UserRankEntry("2", "A. Smith",   null, 58.9, 4),
            UserRankEntry("3", "M. Tanaka",  null, 45.1, 3),
        )   // your DB/network call
            _entries.value = when (sortBy) {
                SortBy.Kilometers -> raw.sortedByDescending { it.weekKm }
                SortBy.Level      -> raw.sortedByDescending { it.level }
            }
        }
    }
}
