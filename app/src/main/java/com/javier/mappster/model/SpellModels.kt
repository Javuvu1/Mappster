package com.javier.mappster.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

data class Spells(
    val spell: List<Spell> = emptyList()
)

@Serializable
data class Spell(
    val name: String = "",
    val level: Int = 0,
    val school: String = "",
    val source: String = "",
    val page: Int = 0,

    val basicRules: Boolean = false,

    val components: Components = Components(),

    val time: List<Time> = emptyList(),
    val duration: List<Duration> = emptyList(),

    val range: Range = Range(),
    val areaTags: List<String> = emptyList(),

    val meta: Meta = Meta(),
    val miscTags: List<String> = emptyList(),
    val hasFluff: Boolean = false,
    val hasFluffImages: Boolean = false,

    val entries: List<String> = emptyList(),
    val entriesHigherLevel: List<EntriesHigherLevel> = emptyList(),
    val savingThrow: List<String> = emptyList(),
    val spellAttack: List<String> = emptyList(),
    val abilityCheck: List<String> = emptyList(),

    val damageInflict: List<String> = emptyList(),
    val damageResist: List<String> = emptyList(),
    val damageVulnerable: List<String> = emptyList(),
    val damageImmune: List<String> = emptyList(),
    val conditionInflict: List<String> = emptyList(),
    val conditionImmune: List<String> = emptyList(),
    val affectsCreatureType: List<String> = emptyList(),

    val subschools: List<String> = emptyList(),
    val reprintedAs: List<String> = emptyList(),
    val additionalSources: List<AdditionalSource> = emptyList(),
    val otherSources: List<OtherSource> = emptyList(),

    val classes: Classes = Classes(),
    val feats: List<Feat> = emptyList(),
    val backgrounds: List<Background> = emptyList(),
    val races: List<Race> = emptyList(),
    val optionalFeatures: List<OptionalFeature> = emptyList(),
    val customAccess: String = "",
    val customHigherLevel: String = "",

    @PropertyName("custom")
    private val _custom: Boolean? = null,
    val userId: String? = null,
    @PropertyName("public")
    private val _public: Boolean? = null
) {
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

@Serializable
data class Classes(
    val fromClassList: List<ClassEntry> = emptyList(),
    val fromSubclass: List<SubclassEntry> = emptyList()
)

@Serializable
data class ClassEntry(
    val name: String = "",
    val source: String = ""
)

@Serializable
data class SubclassEntry(
    @PropertyName("class")
    val classEntry: ClassEntry = ClassEntry(),
    val subclass: SubclassDetail = SubclassDetail()
)

@Serializable
data class SubclassDetail(
    val name: String = "",
    val shortName: String? = null,
    val source: String = ""
)

@Serializable
data class Feat(
    val name: String = "",
    val source: String = ""
)

@Serializable
data class Background(
    val name: String = "",
    val source: String = ""
)

@Serializable
data class Race(
    val name: String = "",
    val source: String = "",
    val baseName: String? = null,
    val baseSource: String? = null
)

@Serializable
data class OptionalFeature(
    val name: String = "",
    val source: String = "",
    val featureType: List<String> = emptyList()
)