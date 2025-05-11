package com.javier.mappster.model

import com.google.firebase.firestore.PropertyName

data class Spells(
    val spell: List<Spell> = emptyList()
)

data class Spell(
    // Basic Information
    val name: String = "",
    val level: Int = 0,
    val school: String = "",
    val source: String = "",
    val page: Int = 0,

    @PropertyName("srd")
    private val _srd: Any? = null,

    val basicRules: Boolean = false,

    // Components
    val components: Components = Components(),

    // Time & Duration
    val time: List<Time> = emptyList(),
    val duration: List<Duration> = emptyList(),

    // Range & Area
    val range: Range = Range(),
    val areaTags: List<String> = emptyList(),

    // Metadata
    val meta: Meta = Meta(),
    val miscTags: List<String> = emptyList(),
    val hasFluff: Boolean = false,
    val hasFluffImages: Boolean = false,

    // Game Mechanics
    val entries: List<Any> = emptyList(),
    val entriesHigherLevel: List<EntriesHigherLevel> = emptyList(),
    val savingThrow: List<String> = emptyList(),
    val spellAttack: List<String> = emptyList(),
    val abilityCheck: List<String> = emptyList(),
    //val scalingLevelDice: ScalingLevelDice? = null,

    // Damage & Conditions
    val damageInflict: List<String> = emptyList(),
    val damageResist: List<String> = emptyList(),
    val damageVulnerable: List<String> = emptyList(),
    val damageImmune: List<String> = emptyList(),
    val conditionInflict: List<String> = emptyList(),
    val conditionImmune: List<String> = emptyList(),
    val affectsCreatureType: List<String> = emptyList(),

    // Additional Content
    val subschools: List<String> = emptyList(),
    val reprintedAs: List<String> = emptyList(),
    val additionalSources: List<AdditionalSource> = emptyList(),
    val otherSources: List<OtherSource> = emptyList()
) {
    val srd: Boolean
        get() = when (_srd) {
            is Boolean -> _srd
            is String -> _srd.equals("true", ignoreCase = true)
            else -> true
        }
}

/* ===== Submodels (se mantienen igual) ===== */
data class Components(
    val v: Boolean = false,
    val s: Boolean = false,
    val r: Boolean = false,
    val m: Any? = null
)

data class M(
    val text: String = "",
    val cost: Int? = null,
    val consume: Boolean = false
)

data class Time(
    val number: Int = 0,
    val unit: String = "",  // "action", "hour", etc.
    val condition: String? = null
)

data class Duration(
    val type: String = "",           // "timed", "instant", etc.
    val duration: DurationX? = null, // Details
    val concentration: Boolean = false,
    val ends: List<String> = emptyList()
)

data class DurationX(
    val type: String = "",  // "hour", "day", etc.
    val amount: Int = 0,
    val upTo: Boolean = false
)

data class Range(
    val type: String = "",  // "point", "radius", etc.
    val distance: Distance = Distance()
)

data class Distance(
    val type: String = "",  // "feet", "mile", "self", etc.
    val amount: Int? = null
)

data class ScalingLevelDice(
    val label: String = "",
    val scaling: Map<String, String> = emptyMap()  // Mapa dinámico para los niveles
) {
    // Propiedades computadas para acceder fácilmente a los niveles comunes
    val level1: String? get() = scaling["1"]
    val level5: String? get() = scaling["5"]
    val level11: String? get() = scaling["11"]
    val level17: String? get() = scaling["17"]
}

data class EntriesHigherLevel(
    val type: String = "",
    val name: String = "",
    val entries: List<String> = emptyList()
)

data class AdditionalSource(
    val source: String = "",
    val page: Int = 0
)

data class OtherSource(
    val source: String = "",
    val page: Int = 0
)

data class Meta(
    val ritual: Boolean = false
)