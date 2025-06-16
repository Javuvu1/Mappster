package com.javier.mappster.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.CustomMonster
import com.javier.mappster.model.Monster
import com.javier.mappster.model.UnifiedMonster
import com.javier.mappster.model.toUnifiedMonster
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class DataState(
    val monsters: List<UnifiedMonster> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MonsterListViewModel(
    private val dataManager: LocalDataManager,
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager = FirestoreManager()
) : ViewModel() {

    private val _state = MutableStateFlow(DataState(isLoading = true))
    val state: StateFlow<DataState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null
    private var allMonsters: List<UnifiedMonster> = emptyList()

    init {
        loadMonsters()
    }

    private fun loadMonsters() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Cargar monstruos desde JSON local
                val localResult = dataManager.loadMonsters()
                val localMonsters = localResult.getOrDefault(emptyList())
                val unifiedLocalMonsters = localMonsters.mapIndexed { index, monster ->
                    //Log.d("MonsterList", "Converting local monster ${monster.name}, DEX: ${monster.dex}")
                    monster.toUnifiedMonster() // Usar el método existente
                }

                // Cargar monstruos personalizados desde Firestore
                val userId = authManager.getCurrentUserId()
                val customMonsters = if (userId != null) {
                    firestoreManager.getCustomMonsters(userId)
                } else {
                    emptyList()
                }
                val unifiedCustomMonsters = customMonsters.map { customMonster ->
                    Log.d("MonsterList", "Converting custom monster ${customMonster.name}, Initiative: ${customMonster.initiative}, userId: ${customMonster.userId}")
                    customMonster.toUnifiedMonster() // Usar el método existente
                }

                // Combinar y eliminar duplicados por nombre
                allMonsters = (unifiedLocalMonsters + unifiedCustomMonsters)
                    .distinctBy { it.name.lowercase() }
                    .sortedBy { it.name.trim().lowercase() }

                _state.update {
                    it.copy(
                        isLoading = false,
                        monsters = allMonsters.filter {
                            it.name.contains(_searchQuery.value, ignoreCase = true)
                        },
                        error = localResult.exceptionOrNull()?.message
                    )
                }
                Log.d("MonsterListViewModel", "Obtenidos ${allMonsters.size} monstruos (locales + personalizados)")
            } catch (e: Exception) {
                Log.e("MonsterListViewModel", "Error al cargar monstruos: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        monsters = emptyList(),
                        error = "Error al cargar monstruos: ${e.message}"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        _searchQuery.value = query
        searchJob = viewModelScope.launch {
            delay(300.milliseconds)
            if (_searchQuery.value == query) {
                filterMonsters(query)
            }
        }
    }

    private fun filterMonsters(query: String) {
        Log.d("MonsterListViewModel", "Filtrando monstruos con query='$query'")
        val filtered = if (query.isBlank()) {
            allMonsters
        } else {
            allMonsters.filter {
                it.name.contains(query, ignoreCase = true)
            }.sortedBy { it.name.lowercase() }
        }
        _state.update { it.copy(monsters = filtered) }
        Log.d("MonsterListViewModel", "Filtrados ${filtered.size} monstruos")
    }

    fun refreshCustomMonsters(): Job {
        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Solo cargar monstruos personalizados si hay usuario autenticado
                val userId = authManager.getCurrentUserId()
                val customMonsters = if (userId != null) {
                    firestoreManager.getCustomMonsters(userId).map { it.toUnifiedMonster() }
                } else {
                    emptyList()
                }

                val localMonsters = allMonsters.filter { !it.isCustom }

                allMonsters = (localMonsters + customMonsters)
                    .distinctBy { it.name.lowercase() }
                    .sortedBy { it.name.trim().lowercase() }

                _state.update {
                    it.copy(
                        isLoading = false,
                        monsters = allMonsters.filter {
                            it.name.contains(_searchQuery.value, ignoreCase = true)
                        },
                        error = null
                    )
                }
                Log.d("MonsterListViewModel", "Monsters refreshed. Total: ${allMonsters.size}")
            } catch (e: Exception) {
                Log.e("MonsterListViewModel", "Refresh error", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Refresh error: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteCustomMonster(monster: UnifiedMonster) {
        if (!monster.isCustom || monster.id == null) {
            Log.e("MonsterListViewModel", "Cannot delete non-custom monster or monster without ID")
            return
        }

        viewModelScope.launch {
            try {
                firestoreManager.deleteCustomMonster(monster.id)
                Log.d("MonsterListViewModel", "Monster ${monster.name} deleted successfully")
                refreshCustomMonsters() // Recargar la lista después de eliminar
            } catch (e: Exception) {
                Log.e("MonsterListViewModel", "Error deleting monster: ${e.message}", e)
                _state.update {
                    it.copy(error = "Error al eliminar el monstruo: ${e.message}")
                }
            }
        }
    }

    fun updateMonsterVisibility(monster: UnifiedMonster, isPublic: Boolean) {
        viewModelScope.launch {
            try {
                if (monster.isCustom && monster.id != null) {
                    firestoreManager.updateMonsterVisibility(monster.id, isPublic)
                    val updatedMonster = monster.copy(public = isPublic)
                    _state.value = state.value.copy(monsters = state.value.monsters.map { if (it.id == monster.id) updatedMonster else it })
                }
            } catch (e: Exception) {
                _state.value = state.value.copy(error = "Error al actualizar visibilidad: ${e.message}")
            }
        }
    }
}

class MonsterListViewModelFactory(
    private val dataManager: LocalDataManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonsterListViewModel::class.java)) {
            return MonsterListViewModel(dataManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}