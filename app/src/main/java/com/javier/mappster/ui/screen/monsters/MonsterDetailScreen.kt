package com.javier.mappster.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javier.mappster.model.*
import com.javier.mappster.utils.conditionDescriptions
import com.javier.mappster.utils.sourceMap
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.floor
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterDetailScreen(monster: Monster) {
    var showDiceRollDialog by remember { mutableStateOf(false) }
    var diceRollResult by remember { mutableStateOf(0) }
    var diceRollModifier by remember { mutableStateOf(0) }
    var diceRollTotal by remember { mutableStateOf(0) }
    var currentStatLabel by remember { mutableStateOf("") }
    var diceRollDetails by remember { mutableStateOf<List<Int>>(emptyList()) }
    var showConditionDialog by remember { mutableStateOf(false) }
    var currentCondition by remember { mutableStateOf("") }
    var conditionDescription by remember { mutableStateOf("") }

    // Callback para manejar las tiradas de dados desde MonsterTraits
    val onDiceRollClick: (String, Int, Int) -> Unit = { label, numDice, dieSides ->
        currentStatLabel = "Dice Roll ($label)"
        diceRollModifier = 0
        diceRollDetails = (1..numDice).map { Random.nextInt(1, dieSides + 1) }
        diceRollResult = diceRollDetails.sum()
        diceRollTotal = diceRollResult + diceRollModifier
        showDiceRollDialog = true
    }

    // Callback para manejar clics en condiciones
    val onConditionClick: (String) -> Unit = { condition ->
        currentCondition = condition.replaceFirstChar { it.uppercase() }
        conditionDescription = conditionDescriptions[condition.lowercase()] ?: "No description available for this condition."
        showConditionDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = monster.name ?: "Unknown Monster",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sección: Fuente (Source) debajo del nombre
            monster.source?.let { sourceCode ->
                val sourceName = sourceMap[sourceCode] ?: "Unknown Source"
                Text(
                    text = "Source: $sourceName",
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Sección: Tamaño, Tipo, Alineamiento y CR
            MonsterInfoSection(monster)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: HP, AC e Iniciativa
            MonsterCombatStats(
                monster = monster,
                onModifierClick = { label, modifier ->
                    currentStatLabel = label
                    diceRollModifier = modifier
                    diceRollResult = Random.nextInt(1, 21) // Tirada de d20
                    diceRollTotal = diceRollResult + modifier
                    diceRollDetails = listOf(diceRollResult)
                    showDiceRollDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Velocidad
            MonsterSpeed(monster)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Ability Scores y Saving Throws
            MonsterStats(monster) { label, modifier ->
                currentStatLabel = label
                diceRollModifier = modifier
                diceRollResult = Random.nextInt(1, 21) // Tirada de d20
                diceRollTotal = diceRollResult + modifier
                diceRollDetails = listOf(diceRollResult)
                showDiceRollDialog = true
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Habilidades
            MonsterSkills(
                monster = monster,
                onSkillClick = { label, modifier ->
                    currentStatLabel = label
                    diceRollModifier = modifier
                    diceRollResult = Random.nextInt(1, 21) // Tirada de d20
                    diceRollTotal = diceRollResult + modifier
                    diceRollDetails = listOf(diceRollResult)
                    showDiceRollDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Resistencias e Inmunidades
            MonsterResistancesAndImmunities(monster)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Sentidos
            MonsterSenses(monster)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Lenguajes
            MonsterLanguages(monster)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Traits
            MonsterTraits(monster, onDiceRollClick, onConditionClick)
        }
    }

    // Diálogo para mostrar el resultado de la tirada
    if (showDiceRollDialog) {
        AlertDialog(
            onDismissRequest = { showDiceRollDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$currentStatLabel Roll",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (diceRollDetails.size == 1) {
                        // Tirada simple (como d20)
                        Text(
                            text = "d20: $diceRollResult",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = when (diceRollResult) {
                                20 -> Color.Green
                                1 -> Color.Red
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    } else {
                        // Tirada de múltiples dados (como 5d10)
                        Text(
                            text = "Rolls: ${diceRollDetails.joinToString(" + ")}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Modifier: ${formatModifier(diceRollModifier)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Total: $diceRollTotal",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDiceRollDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close")
                }
            },
            containerColor = Color.Transparent,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        )
    }

    // Diálogo para mostrar la descripción de la condición
    if (showConditionDialog) {
        AlertDialog(
            onDismissRequest = { showConditionDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentCondition,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = conditionDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showConditionDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close")
                }
            },
            containerColor = Color.Transparent,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        )
    }
}

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
            // Tamaño, Tipo y Alineamiento
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

            // CR (Challenge Rating)
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
            // HP
            monster.hp?.let { hp ->
                Text(
                    text = "Hit Points: ${hp.average ?: "Unknown"} (${hp.formula ?: "No formula"})",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Iniciativa
            val initModValue = monster.initiative?.proficiency?.toString()?.toIntOrNull()
                ?: calculateModifier(monster.dex) ?: 0
            val initMod = if (initModValue >= 0) "+$initModValue" else initModValue.toString()
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

            // AC
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

@Composable
fun MonsterSpeed(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Speed:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            var hasSpeedData by remember { mutableStateOf(false) }

            monster.speed?.let { speed ->
                speed.walk?.let { walk ->
                    val walkValue = walk.jsonPrimitive.intOrNull?.toString() ?: walk.jsonPrimitive.contentOrNull ?: "0"
                    Text(
                        text = "Walk: $walkValue ft",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.fly?.let { fly ->
                    val flyValue = fly.jsonPrimitive.intOrNull?.toString() ?: fly.jsonPrimitive.contentOrNull ?: "0"
                    Text(
                        text = "Fly: $flyValue ft${if (speed.canHover == true) " (can hover)" else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.swim?.let { swim ->
                    val swimValue = swim.jsonPrimitive.intOrNull?.toString() ?: swim.jsonPrimitive.contentOrNull ?: "0"
                    Text(
                        text = "Swim: $swimValue ft",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.climb?.let { climb ->
                    val climbValue = climb.jsonPrimitive.intOrNull?.toString() ?: climb.jsonPrimitive.contentOrNull ?: "0"
                    Text(
                        text = "Climb: $climbValue ft",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.burrow?.let { burrow ->
                    val burrowValue = burrow.jsonPrimitive.intOrNull?.toString() ?: burrow.jsonPrimitive.contentOrNull ?: "0"
                    Text(
                        text = "Burrow: $burrowValue ft",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.alternate?.let { alternate ->
                    alternate.walk?.forEach { walk ->
                        walk.number?.let { num ->
                            Text(
                                text = "Walk: $num ft${walk.condition?.let { " ($it)" } ?: ""}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                            hasSpeedData = true
                        }
                    }
                    alternate.fly?.forEach { fly ->
                        fly.number?.let { num ->
                            Text(
                                text = "Fly: $num ft${fly.condition?.let { " ($it)" } ?: ""}${if (speed.canHover == true) " (can hover)" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                            hasSpeedData = true
                        }
                    }
                    alternate.climb?.forEach { climb ->
                        climb.number?.let { num ->
                            Text(
                                text = "Climb: $num ft${climb.condition?.let { " ($it)" } ?: ""}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                            hasSpeedData = true
                        }
                    }
                }
                speed.choose?.let { choose ->
                    choose.from?.forEach { speedType ->
                        Text(
                            text = "$speedType: ${choose.amount ?: 0} ft${choose.note?.let { " ($it)" } ?: ""}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                        hasSpeedData = true
                    }
                }
            }

            if (!hasSpeedData) {
                Text(
                    text = "No speed data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MonsterStats(monster: Monster, onModifierClick: (String, Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Ability Scores & Saves:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    label = "STR",
                    abilityValue = monster.str,
                    saveValue = monster.save?.str,
                    saveModifier = monster.save?.str?.toIntOrNull() ?: calculateModifier(monster.str) ?: 0,
                    isSaveFromBase = monster.save?.str != null,
                    extraModifier = calculateModifier(monster.str) ?: 0,
                    onModifierClick = onModifierClick
                )
                StatColumn(
                    label = "DEX",
                    abilityValue = monster.dex,
                    saveValue = monster.save?.dex,
                    saveModifier = monster.save?.dex?.toIntOrNull() ?: calculateModifier(monster.dex) ?: 0,
                    isSaveFromBase = monster.save?.dex != null,
                    extraModifier = calculateModifier(monster.dex) ?: 0,
                    onModifierClick = onModifierClick
                )
                StatColumn(
                    label = "CON",
                    abilityValue = monster.con,
                    saveValue = monster.save?.con,
                    saveModifier = monster.save?.con?.toIntOrNull() ?: calculateModifier(monster.con) ?: 0,
                    isSaveFromBase = monster.save?.con != null,
                    extraModifier = calculateModifier(monster.con) ?: 0,
                    onModifierClick = onModifierClick
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    label = "INT",
                    abilityValue = monster.int,
                    saveValue = monster.save?.int,
                    saveModifier = monster.save?.int?.toIntOrNull() ?: calculateModifier(monster.int) ?: 0,
                    isSaveFromBase = monster.save?.int != null,
                    extraModifier = calculateModifier(monster.int) ?: 0,
                    onModifierClick = onModifierClick
                )
                StatColumn(
                    label = "WIS",
                    abilityValue = monster.wis,
                    saveValue = monster.save?.wis,
                    saveModifier = monster.save?.wis?.toIntOrNull() ?: calculateModifier(monster.wis) ?: 0,
                    isSaveFromBase = monster.save?.wis != null,
                    extraModifier = calculateModifier(monster.wis) ?: 0,
                    onModifierClick = onModifierClick
                )
                StatColumn(
                    label = "CHA",
                    abilityValue = monster.cha,
                    saveValue = monster.save?.cha,
                    saveModifier = monster.save?.cha?.toIntOrNull() ?: calculateModifier(monster.cha) ?: 0,
                    isSaveFromBase = monster.save?.cha != null,
                    extraModifier = calculateModifier(monster.cha) ?: 0,
                    onModifierClick = onModifierClick
                )
            }
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    abilityValue: Int?,
    saveValue: String?,
    saveModifier: Int,
    isSaveFromBase: Boolean,
    extraModifier: Int,
    onModifierClick: (String, Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = "$label: ${abilityValue?.toString() ?: "–"}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        val saveDisplayText = saveValue ?: formatModifier(saveModifier)
        val saveAnnotatedText = buildAnnotatedString {
            append("Save: ")
            pushStringAnnotation(tag = "saveModifier", annotation = saveModifier.toString())
            withStyle(
                style = SpanStyle(
                    fontWeight = if (isSaveFromBase) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                append(saveDisplayText)
            }
            pop()
        }
        ClickableText(
            text = saveAnnotatedText,
            onClick = { offset ->
                saveAnnotatedText.getStringAnnotations(tag = "saveModifier", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        onModifierClick("$label Save", annotation.item.toInt())
                    }
            },
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
        )
        val extraAnnotatedText = buildAnnotatedString {
            append("Mod: ")
            pushStringAnnotation(tag = "extraModifier", annotation = extraModifier.toString())
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                append(formatModifier(extraModifier))
            }
            pop()
        }
        ClickableText(
            text = extraAnnotatedText,
            onClick = { offset ->
                extraAnnotatedText.getStringAnnotations(tag = "extraModifier", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        onModifierClick("$label Mod", annotation.item.toInt())
                    }
            },
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun MonsterSkills(monster: Monster, onSkillClick: (String, Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Skills:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            val hasSkills = monster.skill?.let { skill ->
                listOfNotNull(
                    skill.perception to "Perception",
                    skill.arcana to "Arcana",
                    skill.nature to "Nature",
                    skill.history to "History",
                    skill.stealth to "Stealth",
                    skill.religion to "Religion",
                    skill.deception to "Deception",
                    skill.intimidation to "Intimidation",
                    skill.persuasion to "Persuasion",
                    skill.insight to "Insight",
                    skill.medicine to "Medicine",
                    skill.survival to "Survival",
                    skill.acrobatics to "Acrobatics",
                    skill.sleightOfHand to "Sleight of Hand",
                    skill.athletics to "Athletics",
                    skill.investigation to "Investigation",
                    skill.performance to "Performance",
                    skill.animalHandling to "Animal Handling"
                ).any { it.first != null } || skill.other?.any { it.oneOf?.let { oneOf ->
                    listOfNotNull(oneOf.arcana, oneOf.history, oneOf.nature, oneOf.religion).isNotEmpty()
                } == true } == true
            } ?: false

            if (hasSkills) {
                monster.skill?.let { skill ->
                    listOfNotNull(
                        skill.perception to "Perception",
                        skill.arcana to "Arcana",
                        skill.nature to "Nature",
                        skill.history to "History",
                        skill.stealth to "Stealth",
                        skill.religion to "Religion",
                        skill.deception to "Deception",
                        skill.intimidation to "Intimidation",
                        skill.persuasion to "Persuasion",
                        skill.insight to "Insight",
                        skill.medicine to "Medicine",
                        skill.survival to "Survival",
                        skill.acrobatics to "Acrobatics",
                        skill.sleightOfHand to "Sleight of Hand",
                        skill.athletics to "Athletics",
                        skill.investigation to "Investigation",
                        skill.performance to "Performance",
                        skill.animalHandling to "Animal Handling"
                    ).forEach { (value, label) ->
                        if (value != null) {
                            val modifier = value.toString().toIntOrNull() ?: 0
                            val skillAnnotatedText = buildAnnotatedString {
                                append("$label: ")
                                pushStringAnnotation(tag = "skillModifier", annotation = modifier.toString())
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    append(value.toString())
                                }
                                pop()
                            }
                            ClickableText(
                                text = skillAnnotatedText,
                                onClick = { offset ->
                                    skillAnnotatedText.getStringAnnotations(tag = "skillModifier", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            onSkillClick(label, annotation.item.toInt())
                                        }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                    skill.other?.forEach { other ->
                        other.oneOf?.let { oneOf ->
                            listOfNotNull(
                                oneOf.arcana to "Arcana",
                                oneOf.history to "History",
                                oneOf.nature to "Nature",
                                oneOf.religion to "Religion"
                            ).forEach { (value, label) ->
                                if (value != null) {
                                    val modifier = value.toString().toIntOrNull() ?: 0
                                    val skillAnnotatedText = buildAnnotatedString {
                                        append("$label: ")
                                        pushStringAnnotation(tag = "skillModifier", annotation = modifier.toString())
                                        withStyle(
                                            style = SpanStyle(
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            append(value.toString())
                                        }
                                        pop()
                                    }
                                    ClickableText(
                                        text = skillAnnotatedText,
                                        onClick = { offset ->
                                            skillAnnotatedText.getStringAnnotations(tag = "skillModifier", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    onSkillClick(label, annotation.item.toInt())
                                                }
                                        },
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No skills available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MonsterResistancesAndImmunities(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Resistances & Immunities:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Resistencias
            var hasResistances = false
            monster.resist?.let { resists ->
                Text(
                    text = "Resists:",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
                resists.forEach { resist ->
                    resist.resist?.let { entries ->
                        entries.joinToString(", ") { it.value?.replaceFirstChar { it.uppercase() } ?: "Unknown" }
                            .let { resistText ->
                                Text(
                                    text = "$resistText${resist.note?.let { " ($it)" } ?: ""}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                                hasResistances = true
                            }
                    } ?: resist.special?.let { special ->
                        special.replaceFirstChar { it.uppercase() }.let { specialText ->
                            Text(
                                text = specialText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                            hasResistances = true
                        }
                    }
                }
            }

            // Inmunidades
            var hasImmunities = false
            monster.immune?.let { immunities ->
                Text(
                    text = "Immune to:",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
                immunities.forEach { immune ->
                    when (immune) {
                        is JsonPrimitive -> {
                            val immuneText = immune.contentOrNull?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                            Text(
                                text = immuneText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                            hasImmunities = true
                        }
                        is JsonObject -> {
                            val immuneList = immune["immune"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                            val note = immune["note"]?.jsonPrimitive?.contentOrNull
                            immuneList?.joinToString(", ") { it.replaceFirstChar { it.uppercase() } }?.let { immuneText ->
                                Text(
                                    text = "$immuneText${note?.let { " ($it)" } ?: ""}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                                hasImmunities = true
                            }
                        }
                        else -> {
                            // Ignorar otros tipos de JsonElement no esperados
                        }
                    }
                }
            }

            // Mensaje por defecto si no hay resistencias ni inmunidades
            if (!hasResistances && !hasImmunities) {
                Text(
                    text = "No resistances or immunities available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MonsterSenses(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Senses:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            monster.senses?.let { senses ->
                senses.joinToString(", ") { it.replaceFirstChar { it.uppercase() } }.let { senseText ->
                    Text(
                        text = senseText.ifEmpty { "No senses available" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            } ?: run {
                Text(
                    text = "No senses available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            monster.passive?.let { passive ->
                val passiveValue = when (passive) {
                    is Passive.Value -> passive.value
                    is Passive.Formula -> passive.formula
                }
                Text(
                    text = "Passive Perception: $passiveValue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MonsterLanguages(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Languages:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            monster.languages?.let { languages ->
                languages.joinToString(", ").let { langText ->
                    Text(
                        text = langText.ifEmpty { "No languages available" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            } ?: run {
                Text(
                    text = "No languages available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MonsterTraits(
    monster: Monster,
    onDiceRollClick: (String, Int, Int) -> Unit,
    onConditionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Traits:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monster.trait.isNullOrEmpty()) {
                Text(
                    text = "No traits available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            } else {
                monster.trait.forEach { trait ->
                    // Nombre del rasgo
                    Text(
                        text = trait.name ?: "Unknown Trait",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                    // Entradas del rasgo
                    trait.entries?.forEach { entry ->
                        when (entry) {
                            is JsonPrimitive -> {
                                // Si es un string, lo mostramos directamente después de limpiar etiquetas
                                val cleanedEntry = cleanTraitEntry(entry.contentOrNull ?: "Unknown")
                                val annotatedText = buildAnnotatedString {
                                    var lastIndex = 0
                                    // Detección de tiradas de dados (por ejemplo, 5d10)
                                    val diceRegex = Regex("(\\d+d\\d+)")
                                    // Detección de condiciones (por ejemplo, charmed, unconscious)
                                    val conditionRegex = Regex("\\b(charmed|unconscious|frightened|restrained|petrified|blinded|deafened|poisoned|paralyzed|stunned|incapacitated|invisible|prone|grappled|exhaustion)\\b")
                                    val allMatches = (diceRegex.findAll(cleanedEntry).map { it to "dice" } +
                                            conditionRegex.findAll(cleanedEntry).map { it to "condition" })
                                        .sortedBy { it.first.range.first }

                                    allMatches.forEach { (match, type) ->
                                        append(cleanedEntry.substring(lastIndex, match.range.first))
                                        if (type == "dice") {
                                            pushStringAnnotation(tag = "diceRoll", annotation = match.value)
                                            withStyle(
                                                style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            ) {
                                                append(match.value)
                                            }
                                            pop()
                                        } else if (type == "condition") {
                                            pushStringAnnotation(tag = "condition", annotation = match.value)
                                            withStyle(
                                                style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            ) {
                                                append(match.value)
                                            }
                                            pop()
                                        }
                                        lastIndex = match.range.last + 1
                                    }
                                    append(cleanedEntry.substring(lastIndex))
                                }
                                ClickableText(
                                    text = annotatedText,
                                    onClick = { offset ->
                                        // Manejar clic en tiradas de dados
                                        annotatedText.getStringAnnotations(tag = "diceRoll", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                val (numDice, dieSides) = annotation.item.split("d").map { it.toInt() }
                                                onDiceRollClick(annotation.item, numDice, dieSides)
                                            }
                                        // Manejar clic en condiciones
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
                                // Si es un objeto, lo interpretamos (por ejemplo, una lista)
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
                                            cleanTraitEntry(item.contentOrNull ?: "Unknown")
                                        } else {
                                            "Unsupported item format"
                                        }
                                        val annotatedText = buildAnnotatedString {
                                            var lastIndex = 0
                                            val diceRegex = Regex("(\\d+d\\d+)")
                                            val conditionRegex = Regex("\\b(charmed|unconscious|frightened|restrained|petrified|blinded|deafened|poisoned|paralyzed|stunned|incapacitated|invisible|prone|grappled|exhaustion)\\b")
                                            val allMatches = (diceRegex.findAll(itemText).map { it to "dice" } +
                                                    conditionRegex.findAll(itemText).map { it to "condition" })
                                                .sortedBy { it.first.range.first }

                                            allMatches.forEach { (match, type) ->
                                                append(itemText.substring(lastIndex, match.range.first))
                                                if (type == "dice") {
                                                    pushStringAnnotation(tag = "diceRoll", annotation = match.value)
                                                    withStyle(
                                                        style = SpanStyle(
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    ) {
                                                        append(match.value)
                                                    }
                                                    pop()
                                                } else if (type == "condition") {
                                                    pushStringAnnotation(tag = "condition", annotation = match.value)
                                                    withStyle(
                                                        style = SpanStyle(
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    ) {
                                                        append(match.value)
                                                    }
                                                    pop()
                                                }
                                                lastIndex = match.range.last + 1
                                            }
                                            append(itemText.substring(lastIndex))
                                        }
                                        ClickableText(
                                            text = annotatedText,
                                            onClick = { offset ->
                                                // Manejar clic en tiradas de dados
                                                annotatedText.getStringAnnotations(tag = "diceRoll", start = offset, end = offset)
                                                    .firstOrNull()?.let { annotation ->
                                                        val (numDice, dieSides) = annotation.item.split("d").map { it.toInt() }
                                                        onDiceRollClick(annotation.item, numDice, dieSides)
                                                    }
                                                // Manejar clic en condiciones
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
                                    // Manejar otros tipos de objetos si es necesario
                                    Text(
                                        text = "Unsupported entry type: $type",
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

// Función para limpiar las etiquetas de las entradas de los rasgos
private fun cleanTraitEntry(entry: String): String {
    var cleaned = entry
    // Reemplazar etiquetas comunes en un orden que evite interferencias
    cleaned = cleaned.replace(Regex("\\{@damage (\\w+)\\}"), "$1") // {@damage 7d6} → 7d6
    cleaned = cleaned.replace(Regex("\\{@actSave (\\w+)\\}")) { match ->
        when (match.groupValues[1].lowercase()) {
            "con" -> "Constitution saving throw"
            "str" -> "Strength saving throw"
            "dex" -> "Dexterity saving throw"
            "int" -> "Intelligence saving throw"
            "wis" -> "Wisdom saving throw"
            "cha" -> "Charisma saving throw"
            else -> match.groupValues[1]
        }
    }
    cleaned = cleaned.replace(Regex("\\{@dc (\\d+)\\}"), "DC $1") // {@dc 14} → DC 14
    cleaned = cleaned.replace(Regex("\\{@variantrule ([^|]+)\\|XPHB(?:\\|[^}]*)?\\}"), "$1") // {@variantrule Hit Points|XPHB} → hit points
    cleaned = cleaned.replace(Regex("\\{@actSaveFail\\}"), "On a failed save,") // {@actSaveFail} → On a failed save,
    cleaned = cleaned.replace(Regex("\\{@spell ([^}]+)\\}"), "$1") // {@spell dispel magic} → dispel magic spell
    cleaned = cleaned.replace(Regex("\\{@dice (\\w+)\\}"), "$1") // {@dice 5d10} → 5d10
    cleaned = cleaned.replace(Regex("\\{@condition (\\w+)\\}"), "$1") // {@condition charmed} → charmed
    cleaned = cleaned.replace(Regex("\\{@i ([^}]+)\\}"), "$1") // {@i placeholder} → placeholder
    // Manejo de {@skill ...}
    cleaned = cleaned.replace(Regex("\\{@skill ([^|}]*)\\|?[^}]*\\}")) { match ->
        match.groupValues[1] // Captura el nombre de la habilidad, ignorando cualquier fuente
    }
    // Manejo específico de {@item ...} para preservar el signo del modificador
    cleaned = cleaned.replace(Regex("\\{@item ([^|}]*(?:\\s[+-]\\d+)?)(?:\\|.*)?\\}")) { match ->
        match.groupValues[1] // Captura todo hasta el |, incluyendo el modificador
    }
    return cleaned
}

private fun calculateModifier(stat: Int?): Int? {
    return stat?.let {
        floor((it - 10) / 2.0).toInt()
    }
}

private fun formatModifier(modifier: Int): String {
    return if (modifier >= 0) "+$modifier" else modifier.toString()
}