package com.javier.mappster.model

import android.util.Log
import kotlin.math.floor

// Modelo intermedio para unificar Monster y CustomMonster en la lista
data class UnifiedMonster(
    val id: String, // ID único para distinguir entre Monster y CustomMonster
    val name: String, // Nombre del monstruo
    val cr: String?, // Challenge Rating como String
    val size: String?, // Tamaño
    val type: String?, // Tipo
    val alignment: String?, // Alineamiento
    val source: String?, // Fuente
    val isCustom: Boolean, // Indica si es un CustomMonster (true) o Monster (false)
    val ac: String?, // Clase de armadura (String para manejar "15 (leather)" o número)
    val hp: Int?, // Puntos de vida
    val initiative: Int?, // Modificador de iniciativa para CustomMonster
    val dex: Int?, // Destreza para calcular iniciativa en Monster
    val public: Boolean = false, // Visibilidad
    val userId: String? = null // ID del usuario que creó el monstruo, nullable for non-custom monsters
)

fun CustomMonster.toUnifiedMonster(): UnifiedMonster {
    Log.d("UnifiedMonster", "Converting CustomMonster: name=$name, id=$id, userId=$userId, public=$public, initiative=$initiative (before conversion)")
    val unifiedUserId = if (userId.isNotEmpty()) userId else null // Convert empty string to null if no valid userId
    val unified = UnifiedMonster(
        id = id ?: "custom_${name}_${hashCode()}",
        name = name,
        cr = cr,
        size = size,
        type = type?.joinToString(", "),
        alignment = alignment,
        source = source,
        isCustom = true,
        ac = ac,
        hp = hp,
        initiative = initiative,
        dex = null, // CustomMonster no usa DEX
        public = public,
        userId = unifiedUserId // Use the processed userId
    )
    Log.d("UnifiedMonster", "Converted to UnifiedMonster: name=${unified.name}, id=${unified.id}, userId=${unified.userId}, public=${unified.public}, isCustom=${unified.isCustom}")
    return unified
}

fun Monster.toUnifiedMonster(): UnifiedMonster {
    Log.d("UnifiedMonster", "Converting Monster: name=$name, dex=$dex")
    return UnifiedMonster(
        id = name ?: "monster_${hashCode()}",
        name = name ?: "Unknown",
        cr = cr?.value,
        size = size?.firstOrNull(),
        type = type?.type?.toString(),
        alignment = alignment?.joinToString(", "),
        source = source,
        isCustom = false,
        ac = ac?.firstOrNull()?.ac?.toString(),
        hp = hp?.average,
        initiative = null, // Monster no usa initiative
        dex = dex,
        public = false, // Monstruos no personalizados no tienen visibilidad configurable
        userId = null // No aplica a Monster
    )
}