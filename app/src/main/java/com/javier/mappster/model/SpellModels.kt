package com.javier.mappster.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

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
    val otherSources: List<OtherSource> = emptyList(),

    // Custom field added by script
    @PropertyName("custom")
    private val _custom: Boolean? = null,
    val userId: String? = null,
    @PropertyName("public")
    private val _public: Boolean? = null
) {
    val srd: Boolean
        get() = when (_srd) {
            is Boolean -> _srd
            is String -> _srd.equals("true", ignoreCase = true)
            else -> true
        }

    val custom: Boolean
        get() = _custom ?: false

    val public: Boolean
        get() = _public ?: false
}

/* ===== Submodels ===== */
@Serializable
data class Components(
    val v: Boolean? = null,
    val s: Boolean? = null,
    val r: Boolean? = null,
    val m: String? = null
)

@Serializable
data class Time(
    val number: Int = 0,
    val unit: String = "",
    val condition: String? = null
)

@Serializable
data class Duration(
    val type: String = "",
    val duration: DurationX? = null,
    val concentration: Boolean = false,
    val ends: List<String> = emptyList()
)

@Serializable
data class DurationX(
    val type: String = "",
    val amount: Int = 0,
    val upTo: Boolean = false
)

@Serializable
data class Range(
    val type: String = "",
    val distance: Distance = Distance()
)

@Serializable
data class Distance(
    val type: String = "",
    val amount: Int? = null
)

@Serializable
data class EntriesHigherLevel(
    val type: String = "",
    val name: String = "",
    val entries: List<String> = emptyList()
)

@Serializable
data class AdditionalSource(
    val source: String = "",
    val page: Int = 0
)

@Serializable
data class OtherSource(
    val source: String = "",
    val page: Int = 0
)

@Serializable
data class Meta(
    val ritual: Boolean = false
)