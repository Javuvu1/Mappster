package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.javier.mappster.model.*

@Composable
private fun SectionTitle(title: String, icon: ImageVector, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

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
                title = { Text(text = spell.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = schoolData.color,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    schoolData.color.copy(alpha = 0.3f)
                )
            )
        )
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp)) // Separación adicional desde la TopAppBar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, schoolData.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionTitle("Detalles", Icons.Default.Info, schoolData.color)
                        Text(
                            text = if (spell.level == 0) "Truco" else "Nivel ${spell.level}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Escuela: ${schoolData.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Fuente: ${sourceMap[spell.source] ?: spell.source}, ${spell.page}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Tiempo de lanzamiento: ${spell.time.joinToString { "${it.number} ${it.unit}" }}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Rango: ${spell.range.type}${spell.range.distance.amount?.let { " ($it ${spell.range.distance.type})" } ?: ""}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        val componentsList = buildList {
                            if (spell.components.v == true) add("V")
                            if (spell.components.s == true) add("S")
                            if (spell.components.m != null) add("M (un poco de esponja)")
                            if (spell.components.r == true) add("R")
                        }
                        Text(
                            text = "Componentes: ${componentsList.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Duración: ${spell.duration.firstOrNull()?.type ?: "Instantánea"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (spell.meta.ritual) {
                            Text(
                                text = "Ritual: Sí",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, schoolData.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionTitle("Descripción", Icons.Default.Description, schoolData.color)
                        spell.entries.forEach { entry ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = entry,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, schoolData.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionTitle("Acceso", Icons.Default.Group, schoolData.color)
                        val classList = spell.classes.fromClassList.joinToString { it.name }
                        val subclassList = spell.classes.fromSubclass.joinToString { "${it.classEntry.name}: ${it.subclass.name}" }
                        Text(
                            text = "Clases: ${if (classList.isNotEmpty()) classList else "Ninguna"}, ${if (subclassList.isNotEmpty()) subclassList else "Ninguna"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            if (spell.entriesHigherLevel.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, schoolData.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            SectionTitle("A nivel superior", Icons.Default.Upgrade, schoolData.color)
                            spell.entriesHigherLevel.forEach { entryHigherLevel ->
                                entryHigherLevel.entries.forEach { entry ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = entry,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
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