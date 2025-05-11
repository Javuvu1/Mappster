package com.javier.mappster.ui.navigation

import kotlinx.serialization.Serializable


@Serializable
object SpellListDestination

@Serializable
data class SpellDetailDestination(
    val spellName: String,
    val spellSchool: String,
    val spellLevel: Int,
    val spellSource: String
)