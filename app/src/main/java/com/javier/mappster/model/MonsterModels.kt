package com.javier.mappster.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Bestiary(
    val monster: List<Monster> = emptyList()
)

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
    val actionNote: String? = null
)

@Serializable
data class MonsterSource(
    val source: String? = null,
    val page: Int? = null
)