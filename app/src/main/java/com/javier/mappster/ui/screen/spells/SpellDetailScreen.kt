package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Masks
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.javier.mappster.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellDetailScreen(spell: Spell) {
    val sourceMap = mapOf(
        "AAG" to "Astral Adventurer's Guide",
        "AI" to "Aquisitions Incorporated",
        "AITFR-AVT" to "A Verdant Tomb",
        "BMT" to "The Book of Many Things",
        "DODK" to "Dungeons of Drakkenheim",
        "EGW" to "Explorer's Guide to Wildemount",
        "FTD" to "Fizban's Treasury of Dragons",
        "GGR" to "Guildmasters' Guide to Ravnica",
        "GHLOE" to "Grim Hollow",
        "IDROTF" to "Icewind Dale: Rime of the Frostmaiden",
        "LLK" to "Lost Laboratory of Kwalish",
        "PHB" to "Player's Handbook",
        "SATO" to "Sigil and the Outlands",
        "SCC" to "Strixhaven: Curriculum of Chaos",
        "SCAG" to "Sword Coast Adventurer's Guide",
        "TCE" to "Tasha's Cauldron of Everything",
        "TDCSR" to "Tal'Dorei Campaign Setting",
        "XGE" to "Xanathar's Guide to Everything"
    )

    val schoolData = when (spell.school.uppercase()) {
        "A" -> SchoolData("Abjuración", Color(0xFF4CAF50), Icons.Default.Shield)
        "C" -> SchoolData("Conjuración", Color(0xFF9C27B0), Icons.Default.CallMerge)
        "D" -> SchoolData("Adivinación", Color(0xFF00ACC1), Icons.Default.Visibility)
        "E" -> SchoolData("Encantamiento", Color(0xFFE91E63), Icons.Default.Favorite)
        "V" -> SchoolData("Evocación", Color(0xFFFF5722), Icons.Default.Whatshot)
        "I" -> SchoolData("Ilusión", Color(0xFF7C4DFF), Icons.Default.Masks)
        "N" -> SchoolData("Nigromancia", Color(0xFF607D8B), Icons.Default.Coronavirus)
        "T" -> SchoolData("Transmutación", Color(0xFFFFC107), Icons.Default.AutoAwesome)
        else -> SchoolData(spell.school, Color.Gray, Icons.Default.AutoFixHigh)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = spell.name) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = schoolData.color)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                Text("Nivel: ${spell.level}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = schoolData.icon, contentDescription = "School Icon", tint = schoolData.color)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escuela: ${schoolData.name}", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text("Fuente: ${sourceMap[spell.source] ?: spell.source}", style = MaterialTheme.typography.bodyLarge)
                Text("Página: ${spell.page}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // Ritual
                if (spell.meta.ritual) {
                    Text("Ritual: Sí", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Componentes
                val componentsList = buildList {
                    if (spell.components.v == true) add("Verbal")
                    if (spell.components.s == true) add("Somático")
                    if (spell.components.r == true) add("Ritual")
                    if (!spell.components.m.isNullOrBlank()) add("Material: ${spell.components.m}")
                }
                if (componentsList.isNotEmpty()) {
                    Text("Componentes: ${componentsList.joinToString()}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tiempo de lanzamiento
                Text("Tiempo de lanzamiento: ${spell.time.joinToString { "${it.number} ${it.unit}" }}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // Duración
                spell.duration.forEach { duration ->
                    Text("Duración: ${duration.type}", style = MaterialTheme.typography.bodyLarge)
                    duration.duration?.let {
                        Text("Duración detallada: ${it.amount} ${it.type}", style = MaterialTheme.typography.bodyLarge)
                    }
                    if (duration.concentration) {
                        Text("Concentración: Sí", style = MaterialTheme.typography.bodyLarge)
                    }
                    if (duration.ends.isNotEmpty()) {
                        Text("Finaliza en: ${duration.ends.joinToString()}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Rango y Área
                Text("Rango: ${spell.range.type}${spell.range.distance.amount?.let { " ($it ${spell.range.distance.type})" } ?: ""}", style = MaterialTheme.typography.bodyLarge)
                if (spell.areaTags.isNotEmpty()) {
                    Text("Área: ${spell.areaTags.joinToString()}", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Tiradas de salvación
                if (spell.savingThrow.isNotEmpty()) {
                    Text("Tiradas de salvación: ${spell.savingThrow.joinToString()}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Ataque de hechizo
                if (spell.spellAttack.isNotEmpty()) {
                    Text("Ataque de hechizo: ${spell.spellAttack.joinToString()}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Clases
                if (spell.classes.fromClassList.isNotEmpty()) {
                    Text("Clases: ${spell.classes.fromClassList.joinToString { it.name }}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (spell.classes.fromSubclass.isNotEmpty()) {
                    Text("Subclases: ${spell.classes.fromSubclass.joinToString { "${it.classEntry.name}: ${it.subclass.name}" }}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Hazañas
                if (spell.feats.isNotEmpty()) {
                    Text("Hazañas: ${spell.feats.joinToString { it.name }}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Trasfondos
                if (spell.backgrounds.isNotEmpty()) {
                    Text("Trasfondos: ${spell.backgrounds.joinToString { it.name }}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Razas
                if (spell.races.isNotEmpty()) {
                    Text("Razas: ${spell.races.joinToString { it.name + (it.baseName?.let { base -> " ($base)" } ?: "") }}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Características opcionales
                if (spell.optionalFeatures.isNotEmpty()) {
                    Text("Características opcionales: ${spell.optionalFeatures.joinToString { it.name }}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text("Descripción:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Entries
            items(spell.entries) { entry ->
                Text(text = "• $entry", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // EntriesHigherLevel
            if (spell.entriesHigherLevel.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hechizos a nivel superior:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(spell.entriesHigherLevel) { entryHigherLevel ->
                    Text("Tipo: ${entryHigherLevel.type}", style = MaterialTheme.typography.bodyMedium)
                    Text("Nombre: ${entryHigherLevel.name}", style = MaterialTheme.typography.bodyMedium)
                    entryHigherLevel.entries.forEach { entry ->
                        Text(text = "• $entry", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

data class SchoolData(
    val name: String,
    val color: Color,
    val icon: ImageVector
)