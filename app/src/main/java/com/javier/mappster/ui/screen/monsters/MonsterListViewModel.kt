package com.javier.mappster.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.Monster
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class DataState(
    val monsters: List<Monster> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MonsterListViewModel(private val dataManager: LocalDataManager) : ViewModel() {

    private val _state = MutableStateFlow(DataState(isLoading = true))
    val state: StateFlow<DataState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null
    private var allMonsters: List<Monster> = emptyList() // Lista original de monstruos

    init {
        loadMonsters()
    }

    private fun loadMonsters() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = dataManager.loadMonsters()
            val monsters = result.getOrDefault(emptyList())
            allMonsters = monsters // Guardar la lista original
            _state.update {
                it.copy(
                    isLoading = false,
                    monsters = monsters, // Mostrar todos los monstruos inicialmente
                    error = result.exceptionOrNull()?.message
                )
            }
            Log.d("MonsterListViewModel", "Fetched ${monsters.size} monsters locally")
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        _searchQuery.value = query
        searchJob = viewModelScope.launch {
            delay(300.milliseconds)
            if (_searchQuery.value == query) { // Solo filtrar si el query no cambió
                filterMonsters(query)
            }
        }
    }

    private fun filterMonsters(query: String) {
        Log.d("MonsterListViewModel", "Filtering monsters with query='$query'")
        val filtered = if (query.isBlank()) {
            allMonsters // Usar la lista original si el query está vacío
        } else {
            allMonsters.filter { // Filtrar desde la lista original
                it.name?.contains(query, ignoreCase = true) == true
            }.sortedBy { it.name?.lowercase() }
        }
        _state.update { it.copy(monsters = filtered) }
        Log.d("MonsterListViewModel", "Filtered ${filtered.size} monsters")
    }
}

class MonsterListViewModelFactory(private val dataManager: LocalDataManager) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonsterListViewModel::class.java)) {
            return MonsterListViewModel(dataManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}