package com.javier.mappster.ui.screen.initiativeTracker

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.model.InitiativeEntry
import com.javier.mappster.model.InitiativeTrackerStore
import com.javier.mappster.model.InitiativeTrackerUiState
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
            Log.d("AddMonsterDebug", """
                Añadiendo monstruo: ${monster.name}
                Es custom: ${monster.isCustom}
                Iniciativa: ${monster.initiative}
                DEX: ${monster.dex ?: "N/A"}
            """.trimIndent())

            val acInt = monster.ac?.let { acStr ->
                acStr.split(" ").first().toIntOrNull()
            }

            // Calcular baseInitiative
            val baseInitiative = if (monster.isCustom) {
                monster.initiative ?: 0
            } else {
                monster.dex?.let { dex ->
                    val modifier = floor((dex - 10) / 2.0).toInt()
                    Log.d("AddMonsterDebug", "Calculando iniciativa para ${monster.name}, DEX: $dex, Modifier: $modifier")
                    modifier
                } ?: 0.also {
                    Log.w("AddMonsterDebug", "DEX es null para ${monster.name}, usando 0")
                }
            }

            val newEntry = InitiativeEntry.MonsterEntry(
                id = "monster_${monster.id}_${UUID.randomUUID()}",
                name = generateDisplayName(monster.name),
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

    private fun generateDisplayName(baseName: String): String {
        val trimmedName = baseName.trim()
        val sameNameCount = _entries.value.count {
            it.name.startsWith(trimmedName, ignoreCase = true) &&
                    (it.name == trimmedName || it.name.matches(Regex("^${Regex.escape(trimmedName)} #\\d+$")))
        }
        return if (sameNameCount > 0) "$trimmedName #${sameNameCount + 1}" else trimmedName
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