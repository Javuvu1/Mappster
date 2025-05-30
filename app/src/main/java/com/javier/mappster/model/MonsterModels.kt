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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
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
    val initiative: Initiative? = null // Añadido el campo initiative
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