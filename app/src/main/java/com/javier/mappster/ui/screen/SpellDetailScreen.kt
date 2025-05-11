package com.javier.mappster.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.javier.mappster.ui.navigation.SpellDetailDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellDetailScreen(
    spellDetails: SpellDetailDestination,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spellDetails.spellName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Tarjeta con la información básica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Nombre del hechizo
                    Text(
                        text = spellDetails.spellName,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Escuela y nivel
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Escuela: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getSchoolName(spellDetails.spellSchool),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Nivel: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (spellDetails.spellLevel == 0) "Truco"
                            else "Nivel ${spellDetails.spellLevel}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Fuente
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fuente: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getSourceName(spellDetails.spellSource),
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            // Aquí puedes añadir más secciones con otros detalles del hechizo
        }
    }
}

// Función de ayuda para obtener el nombre completo de la escuela
private fun getSchoolName(schoolCode: String): String {
    return when (schoolCode.uppercase()) {
        "A" -> "Abjuración"
        "C" -> "Conjuración"
        "D" -> "Adivinación"
        "E" -> "Encantamiento"
        "V" -> "Evocación"
        "I" -> "Ilusión"
        "N" -> "Nigromancia"
        "T" -> "Transmutación"
        else -> schoolCode
    }
}

// Función de ayuda para obtener el nombre completo de la fuente
private fun getSourceName(sourceCode: String): String {
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
    return sourceMap[sourceCode.uppercase()] ?: sourceCode
}