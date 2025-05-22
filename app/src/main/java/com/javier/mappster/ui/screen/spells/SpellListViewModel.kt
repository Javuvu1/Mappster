package com.javier.mappster.ui.screen.spells

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.Spell
import com.javier.mappster.utils.normalizeSpellName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpellListViewModel(
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager
) : ViewModel() {
    private val _spells = MutableStateFlow<List<Spell>>(emptyList())
    val spells: StateFlow<List<Spell>> = _spells.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshSpells()
    }

    fun refreshSpells() {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId() ?: return@launch
            _isLoading.value = true
            try {
                val spells = firestoreManager.getSpells(userId)
                _spells.value = spells // Ya ordenados alfabéticamente por FirestoreManager
            } catch (e: Exception) {
                _error.value = "Error al cargar hechizos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId() ?: return@launch
            _isLoading.value = true
            try {
                val spells = firestoreManager.getSpells(userId)
                _spells.value = spells
                    .filter { it.name.contains(query, ignoreCase = true) }
                    // Mantener orden alfabético después del filtro
                    .sortedBy { it.name.lowercase() }
            } catch (e: Exception) {
                _error.value = "Error al buscar hechizos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun deleteSpell(spell: Spell) {
        viewModelScope.launch {
            val id = if (spell.custom) normalizeSpellName(spell.name) else spell.name
            val success = firestoreManager.deleteSpell(id)
            if (success) {
                refreshSpells()
            } else {
                _error.value = "Error al eliminar el hechizo"
            }
        }
    }

    fun updateSpellVisibility(spell: Spell, public: Boolean) {
        viewModelScope.launch {
            val id = if (spell.custom) normalizeSpellName(spell.name) else spell.name
            val success = firestoreManager.updateSpellVisibility(id, public)
            if (success) {
                refreshSpells()
            } else {
                _error.value = "Error al actualizar la visibilidad"
            }
        }
    }
}

fun provideSpellListViewModel(context: Context): SpellListViewModel {
    val authManager = AuthManager(context)
    val firestoreManager = FirestoreManager()
    return SpellListViewModel(authManager, firestoreManager)
}