package com.javier.mappster.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.serializer

@Serializable
data class Monster(
    val name: String? = null,
    val source: String? = null,
    val page: Int? = null,
    @SerialName("otherSources")
    val alternativeSources: List<MonsterSource>? = null,
    val reprintedAs: List<String>? = null,
    val size: List<String>? = null,
    val str: Int? = null,
    val dex: Int? = null,
    val con: Int? = null,
    @SerialName("int")
    val int: Int? = null,
    val wis: Int? = null,
    val cha: Int? = null,
    val environment: List<String>? = null,
    val languages: List<String>? = null,
    val hasToken: Boolean? = null,
    val hasFluff: Boolean? = null,
    val hasFluffImages: Boolean? = null,
    val languageTags: List<String>? = null,
    val damageTags: List<String>? = null,
    val miscTags: List<String>? = null,
    val srd: Boolean? = null,
    val srd52: Boolean? = null,
    val basicRules2024: Boolean? = null,
    val familiar: Boolean? = null,
    val isNpc: Boolean? = null,
    val isNamedCreature: Boolean? = null,
    val basicRules: Boolean? = null,
    val group: List<String>? = null,
    val dragonAge: String? = null,
    val dragonCastingColor: String? = null,
    val alias: List<String>? = null,
    val level: Int? = null,
    val summonedByClass: String? = null,
    val sizeNote: String? = null,
    val actionNote: String? = null,
    @Serializable(with = ChallengeRatingSerializer::class)
    val cr: ChallengeRating? = null,
    val speed: Speed? = null,
    val hp: Hp? = null,
    @Serializable(with = AcListSerializer::class)
    val ac: List<Ac>? = null,
    val initiative: Initiative? = null,
    val skill: Skill? = null,
    @Serializable(with = ResistanceListSerializer::class)
    val resist: List<Resistance>? = null,
    val immune: List<JsonElement>? = null,
    val vulnerable: List<JsonElement>? = null,
    @Serializable(with = ConditionImmuneListSerializer::class)
    val conditionImmune: List<ConditionImmune>? = null,
    val senses: List<String>? = null,
    @Serializable(with = MonsterTypeSerializer::class)
    val type: MonsterType? = null,
    @Serializable(with = AlignmentListSerializer::class)
    val alignment: List<Alignment>? = null,
    val alignmentPrefix: String? = null,
    @Serializable(with = PassiveSerializer::class)
    val passive: Passive? = null,
    val save: Save? = null,
    val trait: List<Trait>? = null
)

@Serializable
data class MonsterSource(
    val source: String? = null,
    val page: Int? = null
)

@Serializable
data class ChallengeRating(
    val value: String? = null,
    val coven: String? = null,
    val lair: String? = null,
    val xp: Int? = null,
    val xpLair: Int? = null
)

object ChallengeRatingSerializer : KSerializer<ChallengeRating> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChallengeRating")

    override fun serialize(encoder: Encoder, value: ChallengeRating) {
        if (value.value != null && value.coven == null && value.lair == null && value.xp == null && value.xpLair == null) {
            encoder.encodeString(value.value)
        } else {
            encoder.beginStructure(descriptor).apply {
                if (value.value != null) encodeStringElement(descriptor, 0, value.value)
                if (value.coven != null) encodeStringElement(descriptor, 1, value.coven)
                if (value.lair != null) encodeStringElement(descriptor, 2, value.lair)
                if (value.xp != null) encodeIntElement(descriptor, 3, value.xp)
                if (value.xpLair != null) encodeIntElement(descriptor, 4, value.xpLair)
                endStructure(descriptor)
            }
        }
    }

    override fun deserialize(decoder: Decoder): ChallengeRating {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("Only JsonDecoder is supported")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonPrimitive -> {
                ChallengeRating(value = jsonElement.content)
            }
            jsonElement is JsonObject -> {
                val obj = jsonElement
                ChallengeRating(
                    value = obj["cr"]?.jsonPrimitive?.content,
                    coven = obj["coven"]?.jsonPrimitive?.content,
                    lair = obj["lair"]?.jsonPrimitive?.content,
                    xp = obj["xp"]?.jsonPrimitive?.content?.toIntOrNull(),
                    xpLair = obj["xpLair"]?.jsonPrimitive?.content?.toIntOrNull()
                )
            }
            else -> throw IllegalArgumentException("Unsupported JSON format for ChallengeRating")
        }
    }
}

@Serializable
data class Speed(
    val walk: JsonElement? = null,
    val fly: JsonElement? = null,
    val climb: JsonElement? = null,
    val canHover: Boolean? = null,
    val swim: JsonElement? = null,
    val burrow: JsonElement? = null,
    val alternate: AlternateSpeed? = null,
    val choose: ChooseSpeed? = null
)

@Serializable
data class AlternateSpeed(
    val walk: List<WalkSpeed>? = null,
    val climb: List<ClimbSpeed>? = null,
    val fly: List<FlySpeed>? = null
)

@Serializable
data class ChooseSpeed(
    val from: List<String>? = null,
    val amount: Int? = null,
    val note: String? = null
)

@Serializable
data class WalkSpeed(
    val number: Int? = null,
    val condition: String? = null
)

@Serializable
data class ClimbSpeed(
    val number: Int? = null,
    val condition: String? = null
)

@Serializable
data class FlySpeed(
    val number: Int? = null,
    val condition: String? = null
)

@Serializable
data class Hp(
    val average: Int? = null,
    val formula: String? = null,
    val special: String? = null
)

@Serializable
data class Ac(
    val ac: Int? = null,
    val from: List<String>? = null,
    val special: String? = null
)

object AcListSerializer : KSerializer<List<Ac>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AcList")

    override fun serialize(encoder: Encoder, value: List<Ac>) {
        val output = encoder.beginStructure(descriptor)
        output.encodeSerializableElement(descriptor, 0, serializer<List<Ac>>(), value)
        output.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<Ac> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonArray = jsonDecoder.decodeJsonElement().jsonArray

        return jsonArray.map { element ->
            when {
                element is JsonPrimitive && element.intOrNull != null -> {
                    Ac(ac = element.intOrNull)
                }
                element is JsonObject -> {
                    Ac(
                        ac = element["ac"]?.jsonPrimitive?.intOrNull,
                        from = element["from"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull },
                        special = element["special"]?.jsonPrimitive?.contentOrNull
                    )
                }
                else -> throw IllegalStateException("Unexpected JSON element in Ac list: $element")
            }
        }
    }
}

@Serializable
data class Initiative(
    val proficiency: Int? = null,
    val advantageMode: String? = null
)

@Serializable
data class Skill(
    val perception: String? = null,
    val arcana: String? = null,
    val nature: String? = null,
    val history: String? = null,
    val stealth: String? = null,
    val religion: String? = null,
    val deception: String? = null,
    val intimidation: String? = null,
    val persuasion: String? = null,
    val insight: String? = null,
    val medicine: String? = null,
    val survival: String? = null,
    val other: List<OtherSkill>? = null,
    val acrobatics: String? = null,
    @SerialName("sleight of hand")
    val sleightOfHand: String? = null,
    val athletics: String? = null,
    val investigation: String? = null,
    val performance: String? = null,
    @SerialName("animal handling")
    val animalHandling: String? = null
)

@Serializable
data class OtherSkill(
    val oneOf: OneOfSkill? = null
)

@Serializable
data class OneOfSkill(
    val arcana: String? = null,
    val history: String? = null,
    val nature: String? = null,
    val religion: String? = null
)

@Serializable
data class Resistance(
    val special: String? = null,
    val resist: List<ResistanceEntry>? = null,
    val note: String? = null,
    val cond: Boolean? = null,
    val preNote: String? = null
)

@Serializable
data class ResistanceEntry(
    val value: String? = null,
    val details: JsonElement? = null
)

object ResistanceListSerializer : KSerializer<List<Resistance>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ResistanceList")

    override fun serialize(encoder: Encoder, value: List<Resistance>) {
        val output = encoder.beginStructure(descriptor)
        output.encodeSerializableElement(descriptor, 0, serializer<List<Resistance>>(), value)
        output.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<Resistance> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonArray = jsonDecoder.decodeJsonElement().jsonArray

        return jsonArray.map { element ->
            when {
                element is JsonPrimitive -> {
                    Resistance(special = element.contentOrNull)
                }
                element is JsonObject -> {
                    Resistance(
                        special = element["special"]?.jsonPrimitive?.contentOrNull,
                        resist = element["resist"]?.jsonArray?.map { resistElement ->
                            when {
                                resistElement is JsonPrimitive -> ResistanceEntry(value = resistElement.contentOrNull)
                                resistElement is JsonObject -> {
                                    val firstKey = resistElement.keys.firstOrNull()
                                    ResistanceEntry(
                                        value = firstKey,
                                        details = if (firstKey != null) resistElement[firstKey] else null
                                    )
                                }
                                else -> throw IllegalStateException("Unexpected JSON element in resist nested list: $resistElement")
                            }
                        },
                        note = element["note"]?.jsonPrimitive?.contentOrNull,
                        cond = element["cond"]?.jsonPrimitive?.booleanOrNull,
                        preNote = element["preNote"]?.jsonPrimitive?.contentOrNull
                    )
                }
                else -> throw IllegalStateException("Unexpected JSON element in resist list: $element")
            }
        }
    }
}

@Serializable
data class ConditionImmune(
    val conditionImmune: List<ConditionImmuneEntry>? = null,
    val note: String? = null,
    val cond: Boolean? = null
)

@Serializable
data class ConditionImmuneEntry(
    val value: String? = null,
    val details: JsonElement? = null
)

object ConditionImmuneListSerializer : KSerializer<List<ConditionImmune>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ConditionImmuneList")

    override fun serialize(encoder: Encoder, value: List<ConditionImmune>) {
        val output = encoder.beginStructure(descriptor)
        output.encodeSerializableElement(descriptor, 0, serializer<List<ConditionImmune>>(), value)
        output.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<ConditionImmune> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonArray = jsonDecoder.decodeJsonElement().jsonArray

        return jsonArray.map { element ->
            when {
                element is JsonPrimitive -> {
                    ConditionImmune(conditionImmune = listOf(ConditionImmuneEntry(value = element.contentOrNull)))
                }
                element is JsonObject -> {
                    ConditionImmune(
                        conditionImmune = element["conditionImmune"]?.jsonArray?.map { immuneElement ->
                            when {
                                immuneElement is JsonPrimitive -> ConditionImmuneEntry(value = immuneElement.contentOrNull)
                                immuneElement is JsonObject -> {
                                    val firstKey = immuneElement.keys.firstOrNull()
                                    ConditionImmuneEntry(
                                        value = firstKey,
                                        details = if (firstKey != null) immuneElement[firstKey] else null
                                    )
                                }
                                else -> throw IllegalStateException("Unexpected JSON element in conditionImmune nested list: $immuneElement")
                            }
                        },
                        note = element["note"]?.jsonPrimitive?.contentOrNull,
                        cond = element["cond"]?.jsonPrimitive?.booleanOrNull
                    )
                }
                else -> throw IllegalStateException("Unexpected JSON element in conditionImmune list: $element")
            }
        }
    }
}

@Serializable
data class MonsterType(
    val type: JsonElement? = null,
    val tags: List<JsonElement>? = null
)

object MonsterTypeSerializer : KSerializer<MonsterType> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MonsterType")

    override fun serialize(encoder: Encoder, value: MonsterType) {
        if (value.tags.isNullOrEmpty() && value.type is JsonPrimitive) {
            encoder.encodeString(value.type.jsonPrimitive.content)
        } else {
            encoder.beginStructure(descriptor).apply {
                if (value.type != null) encodeSerializableElement(descriptor, 0, serializer<JsonElement>(), value.type)
                if (!value.tags.isNullOrEmpty()) encodeSerializableElement(descriptor, 1, serializer<List<JsonElement>>(), value.tags)
                endStructure(descriptor)
            }
        }
    }

    override fun deserialize(decoder: Decoder): MonsterType {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("Only JsonDecoder is supported")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonPrimitive -> {
                MonsterType(type = jsonElement)
            }
            jsonElement is JsonObject -> {
                MonsterType(
                    type = jsonElement["type"],
                    tags = jsonElement["tags"]?.jsonArray?.toList()
                )
            }
            else -> throw IllegalArgumentException("Unsupported JSON format for MonsterType: $jsonElement")
        }
    }
}

@Serializable
data class Alignment(
    val values: List<String>? = null
)

object AlignmentListSerializer : KSerializer<List<Alignment>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AlignmentList")

    override fun serialize(encoder: Encoder, value: List<Alignment>) {
        val output = encoder.beginStructure(descriptor)
        output.encodeSerializableElement(descriptor, 0, serializer<List<Alignment>>(), value)
        output.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<Alignment> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonArray = jsonDecoder.decodeJsonElement().jsonArray

        return jsonArray.map { element ->
            when {
                element is JsonPrimitive -> {
                    val content = element.contentOrNull
                    Alignment(values = if (content != null) listOf(content) else emptyList())
                }
                element is JsonObject -> {
                    Alignment(
                        values = element["alignment"]?.jsonArray?.mapNotNull { it.jsonPrimitive?.contentOrNull }
                    )
                }
                else -> throw IllegalStateException("Unexpected JSON element in alignment list: $element")
            }
        }
    }
}

@Serializable
sealed class Passive {
    @Serializable
    data class Value(val value: Int) : Passive()

    @Serializable
    data class Formula(val formula: String) : Passive()
}

object PassiveSerializer : KSerializer<Passive> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Passive")

    override fun serialize(encoder: Encoder, value: Passive) {
        when (value) {
            is Passive.Value -> encoder.encodeInt(value.value)
            is Passive.Formula -> encoder.encodeString(value.formula)
        }
    }

    override fun deserialize(decoder: Decoder): Passive {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("Only JsonDecoder is supported")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonPrimitive && jsonElement.intOrNull != null -> {
                Passive.Value(jsonElement.intOrNull!!)
            }
            jsonElement is JsonPrimitive -> {
                Passive.Formula(jsonElement.content)
            }
            else -> throw IllegalArgumentException("Unsupported JSON format for Passive: $jsonElement")
        }
    }
}

@Serializable
data class Save(
    val str: String? = null,
    val dex: String? = null,
    val con: String? = null,
    @SerialName("int")
    val int: String? = null,
    val wis: String? = null,
    val cha: String? = null
)

@Serializable
data class Trait(
    val name: String? = null,
    val entries: List<JsonElement>? = null // Cambiado de List<String> a List<JsonElement>
)