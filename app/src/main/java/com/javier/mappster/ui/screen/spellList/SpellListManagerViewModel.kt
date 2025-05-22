package com.javier.mappster.ui.screen.spellList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.SpellList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpellListManagerViewModel(
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager
) : ViewModel() {
    private val _spellLists = MutableStateFlow<List<SpellList>>(emptyList())
    val spellLists: StateFlow<List<SpellList>> = _spellLists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshSpellLists()
    }

    fun refreshSpellLists() {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId() ?: return@launch
            _isLoading.value = true
            try {
                val spellLists = firestoreManager.getSpellLists(userId)
                _spellLists.value = spellLists
            } catch (e: Exception) {
                _error.value = "Error al cargar listas de hechizos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSpellList(name: String, spellIds: List<String>) {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId() ?: run {
                _error.value = "Usuario no autenticado"
                return@launch
            }
            val spellList = SpellList(name = name, userId = userId, spellIds = spellIds)
            val success = firestoreManager.createSpellList(spellList)
            if (success) {
                refreshSpellLists()
            } else {
                _error.value = "Error al crear la lista de hechizos"
            }
        }
    }

    fun updateSpellList(listId: String, name: String, spellIds: List<String>) {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId() ?: run {
                _error.value = "Usuario no autenticado"
                return@launch
            }
            val spellList = SpellList(id = listId, name = name, userId = userId, spellIds = spellIds)
            val success = firestoreManager.updateSpellList(spellList)
            if (success) {
                refreshSpellLists()
            } else {
                _error.value = "Error al actualizar la lista de hechizos"
            }
        }
    }

    fun deleteSpellList(listId: String) {
        viewModelScope.launch {
            val success = firestoreManager.deleteSpellList(listId)
            if (success) {
                refreshSpellLists()
            } else {
                _error.value = "Error al eliminar la lista de hechizos"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

fun provideSpellListManagerViewModel(context: android.content.Context): SpellListManagerViewModel {
    val authManager = AuthManager(context)
    val firestoreManager = FirestoreManager()
    return SpellListManagerViewModel(authManager, firestoreManager)
}