package com.javier.mappster.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomMonster(
    val id: String? = null,
    val userId: String = "",
    val name: String = "",
    val source: String = "Custom",
    val initiative: Int? = null,
    val size: String? = null,
    val type: List<String>? = null,
    val alignment: String? = null,
    val cr: String? = null,
    val hp: Int? = null,
    val ac: String? = null,
    val speed: Map<String, Int>? = null,
    val str: Int? = null,
    val dex: Int? = null,
    val con: Int? = null,
    val int: Int? = null,
    val wis: Int? = null,
    val cha: Int? = null,
    val proficiencyBonus: Int = 2,
    val saves: Map<String, String?>? = null,
    val skills: Map<String, String>? = null,
    val passive: Int? = null,
    val resist: List<String>? = null,
    val immune: List<String>? = null,
    val senses: List<String>? = null,
    val languages: List<String>? = null,
    val traits: List<TraitEntry>? = null,
    val actions: List<ActionEntry>? = null,
    val bonus: List<ActionEntry>? = null,
    val reactions: List<ActionEntry>? = null,
    val legendary: List<ActionEntry>? = null,
    val spellcasting: List<SpellcastingEntry>? = null,  // Campo añadido para lanzamiento de hechizos
    val public: Boolean = false
)

@Serializable
data class TraitEntry(
    val name: String = "",
    val entries: List<String> = emptyList()
)

@Serializable
data class ActionEntry(
    val name: String = "",
    val entries: List<String> = emptyList()
)

@Serializable
data class SpellcastingEntry(
    val name: String = "",
    val type: String = "spellcasting",
    val headerEntries: List<String> = emptyList(),
    val spells: Map<String, CustomSpellLevel> = emptyMap(),
    val ability: String = ""
)

@Serializable
data class CustomSpellLevel(
    val slots: Int? = null,
    val spells: List<String> = emptyList()
)