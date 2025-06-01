package com.javier.mappster.ui.screen.monsters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javier.mappster.model.Monster
import com.javier.mappster.ui.screen.calculateModifier
import com.javier.mappster.ui.screen.cleanTraitEntry
import com.javier.mappster.ui.screen.formatModifier

@Composable
fun MonsterCombatStats(monster: Monster, onModifierClick: (String, Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            monster.hp?.let { hp ->
                Text(
                    text = "Hit Points: ${hp.average ?: "Unknown"} (${hp.formula ?: "No formula"})",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val initModValue = monster.initiative?.proficiency?.toString()?.toIntOrNull()
                ?: calculateModifier(monster.dex) ?: 0
            val initMod = formatModifier(initModValue)
            val initAnnotatedText = buildAnnotatedString {
                append("Initiative: ")
                pushStringAnnotation(tag = "initModifier", annotation = initModValue.toString())
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append(initMod)
                }
                pop()
            }
            ClickableText(
                text = initAnnotatedText,
                onClick = { offset ->
                    initAnnotatedText.getStringAnnotations(tag = "initModifier", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            onModifierClick("Initiative", annotation.item.toInt())
                        }
                },
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(top = 4.dp)
            )

            monster.ac?.let { acList ->
                val acText = acList.joinToString(", ") { ac ->
                    buildString {
                        append(ac.ac ?: "Unknown")
                        ac.from?.let { from ->
                            val cleanedFrom = from.map { cleanTraitEntry(it) }.joinToString(", ")
                            append(" (from $cleanedFrom)")
                        }
                        ac.special?.let { special ->
                            val cleanedSpecial = cleanTraitEntry(special)
                            append(" ($cleanedSpecial)")
                        }
                    }
                }
                Text(
                    text = "Armor Class: $acText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}