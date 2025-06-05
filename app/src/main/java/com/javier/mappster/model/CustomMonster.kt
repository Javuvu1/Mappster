package com.javier.mappster.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomMonster(
    val id: String? = null, // Identificador único para Firestore
    val userId: String = "", // ID del usuario que creó el monstruo, por defecto vacío
    val name: String = "", // Nombre del monstruo, por defecto vacío
    val source: String = "Custom", // Fuente, por defecto "Custom"
    val initiative: Int? = null, // Iniciativa
    val size: String? = null, // Tamaño (ej. "Medium")
    val type: List<String>? = null, // Tipo (ej. ["Beast", "Humanoid"])
    val alignment: String? = null, // Alineamiento (ej. "Chaotic Evil")
    val cr: String? = null, // Challenge Rating (ej. "1/2")
    val hp: Int? = null, // Puntos de vida
    val ac: String? = null, // Clase de armadura (ej. "15 (natural armor)")
    val speed: Map<String, Int>? = null, // Velocidad (ej. {"walk": 20, "fly": 50})
    val str: Int? = null, // Fuerza
    val dex: Int? = null, // Destreza
    val con: Int? = null, // Constitución
    val int: Int? = null, // Inteligencia
    val wis: Int? = null, // Sabiduría
    val cha: Int? = null, // Carisma
    val proficiencyBonus: Int = 2, // Bonificador de competencia, obligatorio
    val saves: Map<String, String?>? = null, // Tiradas de salvación (ej. {"dex": "+6", "con": "+10"})
    val skills: Map<String, String>? = null, // Habilidades (ej. {"arcana": "+5", "perception": "+7"})
    val passive: Int? = null, // Percepción pasiva
    val resist: List<String>? = null, // Resistencias (ej. ["Fire", "Cold"])
    val idiomas: List<String>? = null, // Lenguajes (ej. ["Common", "Draconic"])
    val traits: List<TraitEntry>? = null, // Rasgos
    val actions: List<ActionEntry>? = null, // Acciones
    val bonus: List<ActionEntry>? = null, // Acciones bonus
    val reactions: List<ActionEntry>? = null, // Reacciones
    val legendary: List<ActionEntry>? = null, // Acciones legendarias
    val spellcasting: List<SpellcastingEntry>? = null, // Lanzamiento de hechizos
    val public: Boolean = false // Indicador de visibilidad pública
)

@Serializable
data class TraitEntry(
    val name: String, // Nombre del rasgo (ej. "Spider Climb")
    val entries: List<String> // Descripción del rasgo (ej. ["The aartuk can climb difficult surfaces..."])
)

@Serializable
data class ActionEntry(
    val name: String, // Nombre de la acción (ej. "Multiattack")
    val entries: List<String> // Descripción de la acción (ej. ["The slaad makes two Chaos Claw attacks."])
)

@Serializable
data class SpellcastingEntry(
    val name: String, // Nombre del tipo de lanzamiento (ej. "Spellcasting")
    val type: String = "spellcasting", // Tipo, por defecto "spellcasting"
    val headerEntries: List<String>, // Entradas de cabecera (ej. ["The aarakocra is a 9th-level spellcaster..."])
    val spells: Map<String, CustomSpellLevel>, // Hechizos por nivel (ej. "0", "1", etc.)
    val ability: String // Habilidad asociada (ej. "int")
)

@Serializable
data class CustomSpellLevel(
    val slots: Int? = null, // Número de ranuras (opcional)
    val spells: List<String> // Lista de hechizos (ej. ["{@spell fire bolt}", "{@spell light}"])
)