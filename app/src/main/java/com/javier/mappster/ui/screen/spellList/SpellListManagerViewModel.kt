package com.javier.mappster.ui.screen.spellList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.SpellList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SpellListManagerViewModel(
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager
) : ViewModel() {
    private val _spellLists = MutableStateFlow<List<SpellList>>(emptyList())
    val spellLists: StateFlow<List<SpellList>> = _spellLists.asStateFlow()

    private val _isLoading = MutableStateFlow(true) // Iniciar en true
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.userStateFlow().collectLatest { userId ->
                if (userId != null) {
                    refreshSpellLists(userId)
                } else {
                    _spellLists.value = emptyList()
                    _isLoading.value = false
                    _error.value = null
                }
            }
        }
    }

    private suspend fun refreshSpellLists(userId: String) {
        _isLoading.value = true
        try {
            val spellLists = firestoreManager.getSpellLists(userId)
            _spellLists.value = spellLists
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al cargar listas de hechizos: ${e.message}"
            _spellLists.value = emptyList()
        } finally {
            _isLoading.value = false
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
                refreshSpellLists(userId)
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
                refreshSpellLists(userId)
            } else {
                _error.value = "Error al actualizar la lista de hechizos"
            }
        }
    }

    fun deleteSpellList(listId: String) {
        viewModelScope.launch {
            val success = firestoreManager.deleteSpellList(listId)
            if (success) {
                val userId = authManager.getCurrentUserId() ?: return@launch
                refreshSpellLists(userId)
            } else {
                _error.value = "Error al eliminar la lista de hechizos"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

@Composable
fun provideSpellListManagerViewModel(context: android.content.Context): SpellListManagerViewModel {
    val authManager = AuthManager.getInstance(context)
    val firestoreManager = FirestoreManager()
    return remember {
        SpellListManagerViewModel(authManager, firestoreManager)
    }
}