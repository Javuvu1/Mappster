package com.javier.mappster.ui.screen.monsters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.javier.mappster.model.Monster
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun MonsterInfoSection(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeText = monster.size?.firstOrNull()?.let { size ->
                    when (size.uppercase()) {
                        "M" -> "Medium"
                        "L" -> "Large"
                        "S" -> "Small"
                        "T" -> "Tiny"
                        "H" -> "Huge"
                        "G" -> "Gargantuan"
                        else -> size
                    }
                } ?: "Unknown"

                val typeText = monster.type?.type?.jsonPrimitive?.contentOrNull?.removeSurrounding("\"")?.replaceFirstChar { it.uppercase() } ?: "Unknown"

                val alignmentText = monster.alignment?.flatMap { it.values.orEmpty() }?.joinToString(" ") { align ->
                    when (align.uppercase()) {
                        "L" -> "Lawful"
                        "N" -> "Neutral"
                        "C" -> "Chaotic"
                        "G" -> "Good"
                        "E" -> "Evil"
                        "A" -> "Any alignment"
                        else -> align
                    }
                }?.let { if (it.isNotEmpty()) " $it" else "" } ?: ""

                Text(
                    text = "$sizeText $typeText$alignmentText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            monster.cr?.let { cr ->
                val crValue = cr.value?.toDoubleOrNull() ?: 0.0
                val crText = when {
                    crValue == 0.5 -> "1/2"
                    crValue == 0.25 -> "1/4"
                    crValue == 0.125 -> "1/8"
                    else -> crValue.toString()
                }
                Text(
                    text = "CR: $crText",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}