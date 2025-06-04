package com.javier.mappster.model

// Modelo intermedio para unificar Monster y CustomMonster en la lista
data class UnifiedMonster(
    val id: String, // ID único para distinguir entre Monster y CustomMonster
    val name: String, // Nombre del monstruo
    val cr: String?, // Challenge Rating como String
    val size: String?, // Tamaño
    val type: String?, // Tipo
    val alignment: String?, // Alineamiento
    val source: String?, // Fuente
    val isCustom: Boolean // Indica si es un CustomMonster (true) o Monster (false)
)