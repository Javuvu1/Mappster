package com.javier.mappster.ui.screen.initiativeTracker

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.model.InitiativeEntry
import com.javier.mappster.model.InitiativeTrackerUiState
import com.javier.mappster.model.UnifiedMonster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class InitiativeTrackerViewModel : ViewModel() {
    private val _entries = mutableStateOf<List<InitiativeEntry>>(emptyList())
    private val _uiState = MutableStateFlow<InitiativeTrackerUiState>(InitiativeTrackerUiState.Success(emptyList()))
    val uiState: StateFlow<InitiativeTrackerUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun addMonster(monster: UnifiedMonster) {
        viewModelScope.launch {
            val acInt = monster.ac?.let { acStr: String ->
                acStr.split(" ")[0].toIntOrNull()
            }

            // Count existing entries with the same base name (case-insensitive)
            val baseName = monster.name.trim()
            val sameNameCount = _entries.value.count {
                it.name.startsWith(baseName, ignoreCase = true) &&
                        (it.name == baseName || it.name.matches(Regex("^${Regex.escape(baseName)} #\\d+$")))
            }
            // Append suffix if there's at least one existing entry
            val displayName = if (sameNameCount > 0) {
                "$baseName #${sameNameCount + 1}"
            } else {
                baseName
            }

            val newEntry = InitiativeEntry.MonsterEntry(
                id = "monster_${monster.id}_${UUID.randomUUID()}",
                name = displayName,
                monster = monster,
                count = 1,
                hp = monster.hp,
                ac = acInt,
                baseInitiative = monster.initiative,
                initiative = null
            )
            _entries.value = _entries.value + newEntry
            updateUiState()
        }
    }

    fun addPlayer(name: String) {
        viewModelScope.launch {
            val newEntry = InitiativeEntry.PlayerEntry(
                id = "player_${UUID.randomUUID()}",
                name = name,
                initiative = null
            )
            _entries.value = _entries.value + newEntry
            updateUiState()
        }
    }

    fun removeEntry(id: String) {
        viewModelScope.launch {
            _entries.value = _entries.value.filterNot { it.id == id }
            updateUiState()
        }
    }

    fun updateEntryInitiative(id: String, initiative: Int?) {
        viewModelScope.launch {
            _entries.value = _entries.value.map { entry ->
                if (entry.id == id) {
                    when (entry) {
                        is InitiativeEntry.MonsterEntry -> entry.copy(initiative = initiative)
                        is InitiativeEntry.PlayerEntry -> entry.copy(initiative = initiative)
                    }
                } else {
                    entry
                }
            }
            updateUiState()
        }
    }

    fun rollInitiatives() {
        viewModelScope.launch {
            _entries.value = _entries.value.map { entry ->
                when (entry) {
                    is InitiativeEntry.MonsterEntry -> {
                        val roll = (1..20).random()
                        entry.copy(initiative = (entry.baseInitiative ?: 0) + roll)
                    }
                    is InitiativeEntry.PlayerEntry -> entry
                }
            }
            _uiState.update {
                InitiativeTrackerUiState.Success(
                    entries = _entries.value.sortedByDescending { it.initiative ?: Int.MIN_VALUE }
                )
            }
        }
    }

    private fun updateUiState() {
        _uiState.update {
            InitiativeTrackerUiState.Success(
                entries = _entries.value
            )
        }
    }

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
        }
    }
}