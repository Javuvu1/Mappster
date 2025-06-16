package com.javier.mappster.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.serializer

@Serializable
data class Monster(
    val name: String? = null,
    val userId: String? = null,
    val custom: Boolean = false,
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
    val alignment: List<String>? = null,
    val alignmentPrefix: String? = null,
    @Serializable(with = PassiveSerializer::class)
    val passive: Passive? = null,
    val save: Save? = null,
    val trait: List<Trait>? = null,
    @Serializable(with = ActionListSerializer::class)
    val action: List<Action>? = null,
    @Serializable(with = BonusListSerializer::class)
    val bonus: List<Bonus>? = null,
    @Serializable(with = ReactionListSerializer::class)
    val reaction: List<Reaction>? = null,
    @Serializable(with = SpellcastingListSerializer::class)
    val spellcasting: List<Spellcasting>? = null,
    @Serializable(with = LegendaryListSerializer::class)
    val legendary: List<Legendary>? = null
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
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChallengeRating") {
        element<String>("cr", isOptional = true)
        element<String>("coven", isOptional = true)
        element<String>("lair", isOptional = true)
        element<Int>("xp", isOptional = true)
        element<Int>("xpLair", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: ChallengeRating) {
        encoder.encodeString(value.value ?: "")
    }

    override fun deserialize(decoder: Decoder): ChallengeRating {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("Only JsonDecoder is supported")

        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> ChallengeRating(value = element.content)
            is JsonObject -> ChallengeRating(
                value = element["cr"]?.jsonPrimitive?.content,
                coven = element["coven"]?.jsonPrimitive?.content,
                lair = element["lair"]?.jsonPrimitive?.content,
                xp = element["xp"]?.jsonPrimitive?.intOrNull,
                xpLair = element["xpLair"]?.jsonPrimitive?.intOrNull
            )
            else -> throw IllegalArgumentException("Invalid CR format")
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
    override val descriptor: SerialDescriptor = listSerialDescriptor<Ac>()

    override fun serialize(encoder: Encoder, value: List<Ac>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<Ac>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<Ac>>(), value)
        }
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
    override val descriptor: SerialDescriptor = listSerialDescriptor<Resistance>()

    override fun serialize(encoder: Encoder, value: List<Resistance>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<Resistance>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<Resistance>>(), value)
        }
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
    override val descriptor: SerialDescriptor = listSerialDescriptor<ConditionImmune>()

    override fun serialize(encoder: Encoder, value: List<ConditionImmune>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<ConditionImmune>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<ConditionImmune>>(), value)
        }
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
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MonsterType") {
        element<JsonElement>("type", isOptional = true)
        element<List<JsonElement>>("tags", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: MonsterType) {
        if (value.tags.isNullOrEmpty() && value.type is JsonPrimitive) {
            encoder.encodeSerializableValue(JsonPrimitive.serializer(), value.type)
        } else {
            encoder.beginStructure(descriptor).apply {
                if (value.type != null) {
                    encodeSerializableElement(descriptor, 0, serializer<JsonElement>(), value.type)
                }
                if (!value.tags.isNullOrEmpty()) {
                    encodeSerializableElement(descriptor, 1, serializer<List<JsonElement>>(), value.tags)
                }
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

object AlignmentListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<String>()

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeSerializableValue(serializer<List<String>>(), value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonArray -> {
                jsonElement.mapNotNull { element ->
                    when (element) {
                        is JsonPrimitive -> element.contentOrNull
                        else -> null
                    }
                }
            }
            jsonElement is JsonPrimitive -> listOf(jsonElement.content)
            else -> emptyList()
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
    val entries: List<JsonElement>? = null
)

@Serializable
data class Action(
    val name: String? = null,
    val entries: List<JsonElement>? = null
)

object ActionListSerializer : KSerializer<List<Action>> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<Action>()

    override fun serialize(encoder: Encoder, value: List<Action>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<Action>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<Action>>(), value)
        }
    }

    override fun deserialize(decoder: Decoder): List<Action> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonArray -> {
                jsonElement.map { element ->
                    when {
                        element is JsonObject -> {
                            val name = element["name"]?.jsonPrimitive?.contentOrNull
                            val entries = element["entries"]?.let { entriesElement ->
                                when {
                                    entriesElement is JsonArray -> entriesElement.toList()
                                    entriesElement is JsonPrimitive -> listOf(entriesElement)
                                    entriesElement is JsonObject -> listOf(entriesElement)
                                    else -> emptyList()
                                }
                            } ?: emptyList()
                            Action(name = name, entries = entries)
                        }
                        else -> throw IllegalStateException("Unexpected JSON element in action list: $element")
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected JSON format for action list: $jsonElement")
        }
    }
}

@Serializable
data class Bonus(
    val name: String? = null,
    val entries: List<JsonElement>? = null
)

object BonusListSerializer : KSerializer<List<Bonus>> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<Bonus>()

    override fun serialize(encoder: Encoder, value: List<Bonus>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<Bonus>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<Bonus>>(), value)
        }
    }

    override fun deserialize(decoder: Decoder): List<Bonus> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonArray -> {
                jsonElement.map { element ->
                    when {
                        element is JsonObject -> {
                            val name = element["name"]?.jsonPrimitive?.contentOrNull
                            val entries = element["entries"]?.let { entriesElement ->
                                when {
                                    entriesElement is JsonArray -> entriesElement.toList()
                                    entriesElement is JsonPrimitive -> listOf(entriesElement)
                                    entriesElement is JsonObject -> listOf(entriesElement)
                                    else -> emptyList()
                                }
                            } ?: emptyList()
                            Bonus(name = name, entries = entries)
                        }
                        else -> throw IllegalStateException("Unexpected JSON element in bonus list: $element")
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected JSON format for bonus list: $jsonElement")
        }
    }
}

@Serializable
data class Reaction(
    val name: String? = null,
    val entries: List<JsonElement>? = null
)

object ReactionListSerializer : KSerializer<List<Reaction>> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<Reaction>()

    override fun serialize(encoder: Encoder, value: List<Reaction>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<Reaction>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<Reaction>>(), value)
        }
    }

    override fun deserialize(decoder: Decoder): List<Reaction> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonArray -> {
                jsonElement.map { element ->
                    when {
                        element is JsonObject -> {
                            val name = element["name"]?.jsonPrimitive?.contentOrNull
                            val entries = element["entries"]?.let { entriesElement ->
                                when {
                                    entriesElement is JsonArray -> entriesElement.toList()
                                    entriesElement is JsonPrimitive -> listOf(entriesElement)
                                    entriesElement is JsonObject -> listOf(entriesElement)
                                    else -> emptyList()
                                }
                            } ?: emptyList()
                            Reaction(name = name, entries = entries)
                        }
                        else -> throw IllegalStateException("Unexpected JSON element in reaction list: $element")
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected JSON format for reaction list: $jsonElement")
        }
    }
}

@Serializable
data class Spellcasting(
    val name: String? = null,
    val type: String? = null,
    val headerEntries: List<String>? = null,
    val spells: Map<String, SpellLevel>? = null,
    val daily: Map<String, List<DailySpell>>? = null,
    val will: List<DailySpell>? = null,
    val ability: String? = null,
    val displayAs: String? = null,
    val hidden: List<String>? = null
)

@Serializable
data class SpellLevel(
    val slots: Int? = null,
    val spells: List<String>? = null
)

@Serializable
data class DailySpell(
    val entry: String? = null,
    val hidden: Boolean? = null
)

object SpellcastingListSerializer : KSerializer<List<Spellcasting>> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<Spellcasting>()

    override fun serialize(encoder: Encoder, value: List<Spellcasting>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<Spellcasting>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<Spellcasting>>(), value)
        }
    }

    override fun deserialize(decoder: Decoder): List<Spellcasting> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonArray -> {
                jsonElement.map { element ->
                    if (element !is JsonObject) {
                        throw IllegalStateException("Expected a JsonObject in spellcasting list, but found: $element")
                    }

                    fun extractString(fieldName: String, jsonElement: JsonElement?): String? {
                        return when (jsonElement) {
                            is JsonPrimitive -> jsonElement.contentOrNull
                            is JsonObject -> throw IllegalStateException("Expected a JsonPrimitive for '$fieldName', but found a JsonObject: $jsonElement")
                            else -> null
                        }
                    }

                    fun extractStringList(fieldName: String, jsonElement: JsonElement?): List<String>? {
                        return when (jsonElement) {
                            is JsonArray -> jsonElement.map { item ->
                                if (item is JsonPrimitive) {
                                    item.contentOrNull
                                } else {
                                    throw IllegalStateException("Expected a JsonPrimitive in '$fieldName' list, but found: $item")
                                }
                            }.filterNotNull()
                            is JsonObject -> throw IllegalStateException("Expected a JsonArray for '$fieldName', but found a JsonObject: $jsonElement")
                            else -> null
                        }
                    }

                    fun extractInt(fieldName: String, jsonElement: JsonElement?): Int? {
                        return when (jsonElement) {
                            is JsonPrimitive -> jsonElement.intOrNull
                            is JsonObject -> throw IllegalStateException("Expected a JsonPrimitive for '$fieldName', but found a JsonObject: $jsonElement")
                            else -> null
                        }
                    }

                    fun extractDailySpell(fieldName: String, jsonElement: JsonElement?): DailySpell? {
                        return when (jsonElement) {
                            is JsonPrimitive -> DailySpell(entry = jsonElement.contentOrNull)
                            is JsonObject -> DailySpell(
                                entry = jsonElement["entry"]?.jsonPrimitive?.contentOrNull,
                                hidden = jsonElement["hidden"]?.jsonPrimitive?.booleanOrNull
                            )
                            else -> null
                        }
                    }

                    val spellsMap = element["spells"]?.let { spellsElement ->
                        if (spellsElement !is JsonObject) {
                            throw IllegalStateException("Expected a JsonObject for 'spells', but found: $spellsElement")
                        }
                        spellsElement.jsonObject.mapValues { (key, value) ->
                            if (value !is JsonObject) {
                                throw IllegalStateException("Expected a JsonObject for spell level '$key', but found: $value")
                            }
                            SpellLevel(
                                slots = extractInt("slots", value.jsonObject["slots"]),
                                spells = extractStringList("spells", value.jsonObject["spells"])
                            )
                        }
                    }

                    val dailyMap = element["daily"]?.let { dailyElement ->
                        if (dailyElement !is JsonObject) {
                            throw IllegalStateException("Expected a JsonObject for 'daily', but found: $dailyElement")
                        }
                        dailyElement.jsonObject.mapValues { (key, value) ->
                            if (value !is JsonArray) {
                                throw IllegalStateException("Expected a JsonArray for daily spells under '$key', but found: $value")
                            }
                            value.jsonArray.mapNotNull { extractDailySpell("daily[$key]", it) }
                        }
                    }

                    val willList = element["will"]?.let { willElement ->
                        if (willElement !is JsonArray) {
                            throw IllegalStateException("Expected a JsonArray for 'will', but found: $willElement")
                        }
                        willElement.jsonArray.mapNotNull { extractDailySpell("will", it) }
                    }

                    Spellcasting(
                        name = extractString("name", element["name"]),
                        type = extractString("type", element["type"]),
                        headerEntries = extractStringList("headerEntries", element["headerEntries"]),
                        spells = spellsMap,
                        daily = dailyMap,
                        will = willList,
                        ability = extractString("ability", element["ability"]),
                        displayAs = extractString("displayAs", element["displayAs"]),
                        hidden = extractStringList("hidden", element["hidden"])
                    )
                }
            }
            else -> throw IllegalStateException("Unexpected JSON format for spellcasting list: $jsonElement")
        }
    }
}

@Serializable
data class Legendary(
    val name: String? = null,
    val entries: List<JsonElement>? = null
)

object LegendaryListSerializer : KSerializer<List<Legendary>> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<Legendary>()

    override fun serialize(encoder: Encoder, value: List<Legendary>) {
        if (value.isEmpty()) {
            encoder.encodeSerializableValue(serializer<List<Legendary>>(), emptyList())
        } else {
            encoder.encodeSerializableValue(serializer<List<Legendary>>(), value)
        }
    }

    override fun deserialize(decoder: Decoder): List<Legendary> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonArray -> {
                jsonElement.map { element ->
                    when {
                        element is JsonObject -> {
                            val name = element["name"]?.jsonPrimitive?.contentOrNull
                            val entries = element["entries"]?.let { entriesElement ->
                                when {
                                    entriesElement is JsonArray -> entriesElement.toList()
                                    entriesElement is JsonPrimitive -> listOf(entriesElement)
                                    entriesElement is JsonObject -> listOf(entriesElement)
                                    else -> emptyList()
                                }
                            } ?: emptyList()
                            Legendary(name = name, entries = entries)
                        }
                        else -> throw IllegalStateException("Unexpected JSON element in legendary list: $element")
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected JSON format for legendary list: $jsonElement")
        }
    }
}
