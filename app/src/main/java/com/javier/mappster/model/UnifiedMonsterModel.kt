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
    val dex: Int? // Destreza para calcular iniciativa en Monster
)

fun CustomMonster.toUnifiedMonster(): UnifiedMonster {
    Log.d("UnifiedMonster", "Converting CustomMonster ${name}, Initiative: $initiative")
    return UnifiedMonster(
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
        dex = null // CustomMonster no usa DEX
    )
}

fun Monster.toUnifiedMonster(): UnifiedMonster {
    Log.d("UnifiedMonster", "Converting Monster ${name}, DEX: $dex")
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
        dex = dex // Pasamos DEX para calcular iniciativa
    )
}