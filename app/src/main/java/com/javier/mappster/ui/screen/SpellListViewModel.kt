package com.javier.mappster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.Spell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpellListViewModel(
    private val firestoreManager: FirestoreManager = FirestoreManager()
) : ViewModel() {

    // Estados
    private val _allSpells = MutableStateFlow<List<Spell>>(emptyList())
    private val _filteredSpells = MutableStateFlow<List<Spell>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    // Flujos públicos
    val spells: StateFlow<List<Spell>> = _filteredSpells
    val searchQuery: StateFlow<String> = _searchQuery
    val isLoading: StateFlow<Boolean> = _isLoading
    val error: StateFlow<String?> = _error

    init {
        loadSpells()
        setupSearch()
    }

    private fun loadSpells() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                _allSpells.value = firestoreManager.getSpells()
                _filteredSpells.value = _allSpells.value
            } catch (e: Exception) {
                _error.value = "Error al cargar hechizos: ${e.localizedMessage}"
                _allSpells.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun setupSearch() {
        _searchQuery
            .debounce(300) // Espera 300ms tras el último cambio
            .onEach { query ->
                _filteredSpells.update { _ ->
                    if (query.isEmpty()) {
                        _allSpells.value
                    } else {
                        _allSpells.value.filter { spell ->
                            spell.name.contains(query, ignoreCase = true)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearError() {
        _error.value = null
    }
}