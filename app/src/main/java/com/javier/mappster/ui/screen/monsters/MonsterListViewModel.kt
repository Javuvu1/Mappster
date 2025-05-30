package com.javier.mappster.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.Monster
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MonsterListViewModel(private val dataManager: LocalDataManager) : ViewModel() {

    private val _monsters = MutableStateFlow<List<Monster>>(emptyList())
    val monsters: StateFlow<List<Monster>> = _monsters.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allMonsters: List<Monster> = emptyList() // Cache de todos los monstruos
    private var searchJob: Job? = null

    init {
        loadMonsters()
    }

    private fun loadMonsters() {
        viewModelScope.launch {
            val monsterList = dataManager.loadMonsters()
            allMonsters = monsterList
            filterMonsters()
            Log.d("MonsterListViewModel", "Fetched ${monsterList.size} monsters locally")
        }
    }

    private fun filterMonsters() {
        val query = _searchQuery.value
        Log.d("MonsterListViewModel", "Filtering monsters with query='$query'")
        _monsters.value = if (query.isBlank()) {
            allMonsters
        } else {
            allMonsters.filter {
                it.name?.contains(query, ignoreCase = true) == true
            }.sortedBy { it.name?.lowercase() }
        }
        Log.d("MonsterListViewModel", "Filtered ${_monsters.value.size} monsters")
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        _searchQuery.value = query
        searchJob = viewModelScope.launch {
            delay(300.milliseconds) // Debounce de 300ms
            Log.d("MonsterListViewModel", "Search query changed to '$query' after debounce")
            filterMonsters()
        }
    }
}