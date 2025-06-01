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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.javier.mappster.model.Monster
import com.javier.mappster.ui.screen.DiceRollData
import com.javier.mappster.ui.screen.buildDamageAnnotatedString
import com.javier.mappster.ui.screen.cleanTraitEntry
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun MonsterActions(
    monster: Monster,
    onDiceRollClick: (DiceRollData) -> Unit,
    onConditionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Actions:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monster.action.isNullOrEmpty()) {
                Text(
                    text = "No actions available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            } else {
                monster.action.forEach { action ->
                    val actionName = cleanTraitEntry(action.name ?: "Unnamed Action")
                    Text(
                        text = "$actionName",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                    action.entries?.forEach { entry ->
                        when (entry) {
                            is JsonPrimitive -> {
                                val diceDataList = remember { mutableListOf<DiceRollData>() }
                                val cleanedText = cleanTraitEntry(entry.contentOrNull ?: "Unknown")
                                val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)
                                ClickableText(
                                    text = annotatedText,
                                    onClick = { offset ->
                                        annotatedText.getStringAnnotations(tag = "damage", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                val index = annotation.item.toInt()
                                                onDiceRollClick(diceDataList[index])
                                            }
                                        annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                onConditionClick(annotation.item)
                                            }
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                            is JsonObject -> {
                                val type = entry["type"]?.jsonPrimitive?.contentOrNull
                                if (type == "list") {
                                    Text(
                                        text = "List:",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                    )
                                    entry["items"]?.jsonArray?.forEach { item ->
                                        val itemText = if (item is JsonPrimitive) {
                                            item.contentOrNull ?: "Unknown"
                                        } else {
                                            "Unsupported item format"
                                        }
                                        val diceDataList = remember { mutableListOf<DiceRollData>() }
                                        val cleanedText = cleanTraitEntry(itemText)
                                        val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)
                                        ClickableText(
                                            text = annotatedText,
                                            onClick = { offset ->
                                                annotatedText.getStringAnnotations(tag = "damage", start = offset, end = offset)
                                                    .firstOrNull()?.let { annotation ->
                                                        val index = annotation.item.toInt()
                                                        onDiceRollClick(diceDataList[index])
                                                    }
                                                annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                                    .firstOrNull()?.let { annotation ->
                                                        onConditionClick(annotation.item)
                                                    }
                                            },
                                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                        )
                                    }
                                } else {
                                    val text = when {
                                        entry.containsKey("damage") && entry.containsKey("type") -> {
                                            "${entry["damage"]?.jsonPrimitive?.contentOrNull} ${entry["type"]?.jsonPrimitive?.contentOrNull} damage"
                                        }
                                        else -> entry.toString()
                                    }
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = "Unsupported entry format",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}