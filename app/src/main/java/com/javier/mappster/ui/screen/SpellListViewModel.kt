package com.javier.mappster.viewmodel

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.javier.mappster.data.AuthManager
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
    private val firestoreManager: FirestoreManager = FirestoreManager(),
    private val authManager: AuthManager
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
        viewModelScope.launch {
            authManager.userStateFlow().collect { userId ->
                if (userId == null) {
                    _error.value = "Usuario no autenticado. Por favor, inicia sesión."
                    _allSpells.value = emptyList()
                    _filteredSpells.value = emptyList()
                    _isLoading.value = false
                } else {
                    _error.value = null
                    loadSpells(userId)
                    setupSearch()
                }
            }
        }
    }

    private fun loadSpells(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                _allSpells.value = firestoreManager.getSpells(userId)
                _filteredSpells.value = _allSpells.value
            } catch (e: Exception) {
                _error.value = "Error al cargar hechizos: ${e.localizedMessage}"
                _allSpells.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSpells() {
        val userId = authManager.getCurrentUserId()
        if (userId != null) {
            loadSpells(userId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun setupSearch() {
        _searchQuery
            .debounce(300)
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

    fun deleteSpell(spellName: String) {
        viewModelScope.launch {
            val success = firestoreManager.deleteSpell(spellName)
            if (success) {
                _allSpells.update { spells -> spells.filter { it.name != spellName } }
                _filteredSpells.update { spells -> spells.filter { it.name != spellName } }
            } else {
                _error.value = "Error al borrar el hechizo: $spellName"
            }
        }
    }

    fun updateSpellVisibility(spellName: String, public: Boolean) {
        viewModelScope.launch {
            val success = firestoreManager.updateSpellVisibility(spellName, public)
            if (success) {
                _allSpells.update { spells ->
                    spells.map { spell ->
                        if (spell.name == spellName) spell.copy(_public = public)
                        else spell
                    }
                }
                _filteredSpells.update { spells ->
                    spells.map { spell ->
                        if (spell.name == spellName) spell.copy(_public = public)
                        else spell
                    }
                }
            } else {
                _error.value = "Error al actualizar la visibilidad del hechizo: $spellName"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

@Composable
fun provideSpellListViewModel(context: Context): SpellListViewModel {
    val authManager = remember { AuthManager(context) }
    return viewModel(factory = SpellListViewModelFactory(firestoreManager = FirestoreManager(), authManager = authManager))
}

class SpellListViewModelFactory(
    private val firestoreManager: FirestoreManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpellListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpellListViewModel(firestoreManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}