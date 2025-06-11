package com.javier.mappster.ui.screen.spells

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.Spell
import com.javier.mappster.utils.normalizeSpellName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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

    private var allSpells: List<Spell> = emptyList() // Cache de todos los hechizos
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            authManager.userStateFlow().collectLatest { userId ->
                if (userId != null) {
                    refreshSpells(userId)
                } else {
                    allSpells = emptyList()
                    _spells.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun refreshSpells(userId: String) {
        _isLoading.value = true
        try {
            Log.d("SpellListViewModel", "Fetching spells for userId=$userId")
            val spells = firestoreManager.getSpells(userId)
            allSpells = spells.sortedBy { it.name.lowercase() }
            Log.d("SpellListViewModel", "Fetched ${allSpells.size} spells")
            filterSpells()
        } catch (e: Exception) {
            Log.e("SpellListViewModel", "Error fetching spells", e)
            _error.value = "Error al cargar hechizos: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearState() {
        allSpells = emptyList()
        _spells.value = emptyList()
        _searchQuery.value = ""
        _isLoading.value = false
        _error.value = null
    }

    private fun filterSpells() {
        val query = _searchQuery.value
        Log.d("SpellListViewModel", "Filtering spells with query='$query'")
        _spells.value = allSpells.filter {
            it.name.contains(query, ignoreCase = true)
        }.sortedBy { it.name.lowercase() }
        Log.d("SpellListViewModel", "Filtered ${_spells.value.size} spells")
    }

    fun getSpellByName(name: String): Spell? {
        val normalizedName = name.trim()
        Log.d("SpellListViewModel", "Searching for spell: '$normalizedName' in allSpells: ${allSpells.map { it.name }}")
        return allSpells.find { it.name.equals(normalizedName, ignoreCase = true) }
    }

    fun refreshSpellsPublic() {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId() ?: return@launch
            refreshSpells(userId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        _searchQuery.value = query
        searchJob = viewModelScope.launch {
            delay(300.milliseconds) // Debounce de 300ms
            Log.d("SpellListViewModel", "Search query changed to '$query' after debounce")
            filterSpells()
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
                val userId = authManager.getCurrentUserId() ?: return@launch
                refreshSpells(userId)
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
                val userId = authManager.getCurrentUserId() ?: return@launch
                refreshSpells(userId)
            } else {
                _error.value = "Error al actualizar la visibilidad"
            }
        }
    }
}

@Composable
fun provideSpellListViewModel(context: android.content.Context): SpellListViewModel {
    val authManager = AuthManager.getInstance(context)
    val firestoreManager = FirestoreManager()
    return remember {
        SpellListViewModel(authManager, firestoreManager)
    }
}