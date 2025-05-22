package com.javier.mappster.utils

fun normalizeSpellName(name: String): String {
    return name
        .trim() // Eliminar espacios iniciales y finales
        .lowercase() // Convertir a minúsculas
        .replace("[^a-z0-9]+".toRegex(), "-") // Reemplazar caracteres no alfanuméricos por guiones
        .trim('-') // Eliminar guiones iniciales o finales
}