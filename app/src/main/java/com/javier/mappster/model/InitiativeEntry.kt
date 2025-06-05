package com.javier.mappster.model

sealed class InitiativeEntry {
    abstract val id: String
    abstract val name: String
    abstract var initiative: Int?
    abstract val isPlayer: Boolean

    data class MonsterEntry(
        override val id: String,
        override val name: String,
        val monster: UnifiedMonster,
        val count: Int = 1,
        val hp: Int?,
        val ac: Int?,
        val baseInitiative: Int?,
        override var initiative: Int? = null,
        override val isPlayer: Boolean = false
    ) : InitiativeEntry() {
        val displayName: String
            get() = if (count > 1) "$name (x$count)" else name
    }

    data class PlayerEntry(
        override val id: String,
        override val name: String,
        override var initiative: Int? = null,
        override val isPlayer: Boolean = true
    ) : InitiativeEntry()
}

sealed class InitiativeTrackerUiState {
    object Loading : InitiativeTrackerUiState()
    data class Success(val entries: List<InitiativeEntry>) : InitiativeTrackerUiState()
    data class Error(val message: String) : InitiativeTrackerUiState()
}