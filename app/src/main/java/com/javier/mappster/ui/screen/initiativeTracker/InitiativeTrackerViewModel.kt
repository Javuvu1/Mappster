package com.javier.mappster.ui.screen.initiativeTracker

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.model.InitiativeEntry
import com.javier.mappster.model.InitiativeTrackerStore
import com.javier.mappster.model.InitiativeTrackerUiState
import com.javier.mappster.model.Monster
import com.javier.mappster.model.UnifiedMonster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.floor

class InitiativeTrackerViewModel : ViewModel() {
    private val _entries = mutableStateOf(InitiativeTrackerStore.getEntries())
    private val _uiState = MutableStateFlow<InitiativeTrackerUiState>(InitiativeTrackerUiState.Success(_entries.value))
    val uiState: StateFlow<InitiativeTrackerUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        updateUiState()
    }

    fun addMonster(monster: UnifiedMonster) {
        viewModelScope.launch {
            val acInt = monster.ac?.let { acStr: String ->
                acStr.split(" ")[0].toIntOrNull()
            }

            // Calculate initiative modifier
            val baseInitiative = when {
                monster.isCustom -> monster.initiative
                else -> {
                    // For non-custom Monster, prefer initiative.proficiency or calculate from DEX
                    val nonCustomMonster = monster as? Monster
                    nonCustomMonster?.initiative?.proficiency
                        ?: nonCustomMonster?.dex?.let { dex ->
                            floor((dex - 10) / 2.0).toInt()
                        } ?: 0
                }
            }

            val baseName = monster.name.trim()
            val sameNameCount = _entries.value.count {
                it.name.startsWith(baseName, ignoreCase = true) &&
                        (it.name == baseName || it.name.matches(Regex("^${Regex.escape(baseName)} #\\d+$")))
            }
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
                baseInitiative = baseInitiative,
                initiative = null
            )
            _entries.value = _entries.value + newEntry
            InitiativeTrackerStore.setEntries(_entries.value)
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
            InitiativeTrackerStore.setEntries(_entries.value)
            updateUiState()
        }
    }

    fun removeEntry(id: String) {
        viewModelScope.launch {
            _entries.value = _entries.value.filterNot { it.id == id }
            InitiativeTrackerStore.setEntries(_entries.value)
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
            InitiativeTrackerStore.setEntries(_entries.value)
            updateUiState()
        }
    }

    fun rollMonsterInitiatives() {
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
            InitiativeTrackerStore.setEntries(_entries.value)
            updateUiState()
        }
    }

    fun sortEntries() {
        viewModelScope.launch {
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

    fun clearEntries() {
        viewModelScope.launch {
            _entries.value = emptyList()
            InitiativeTrackerStore.clearEntries()
            updateUiState()
        }
    }
}