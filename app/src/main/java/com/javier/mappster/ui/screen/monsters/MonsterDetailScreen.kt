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
    var diceRollDetails by remember { mutableStateOf<List<Int>>(emptyList()) }
    var diceRollTotal by remember { mutableStateOf(0) }
    var currentDiceData by remember { mutableStateOf<DiceRollData?>(null) }
    var showConditionDialog by remember { mutableStateOf(false) }
    var currentCondition by remember { mutableStateOf("") }

    // Callback para manejar las tiradas de dados
    val onDiceRollClick: (DiceRollData) -> Unit = { diceData ->
        currentDiceData = diceData
        val (rolls, total) = rollDice(diceData)
        diceRollDetails = rolls
        diceRollTotal = total
        showDiceRollDialog = true
    }

    // Callback para manejar clics en condiciones
    val onConditionClick: (String) -> Unit = { condition ->
        currentCondition = condition.replaceFirstChar { it.uppercase() }
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
                    currentDiceData = DiceRollData(numDice = 1, dieSides = 20, bonus = modifier, type = "hit")
                    val (rolls, total) = rollDice(currentDiceData!!)
                    diceRollDetails = rolls
                    diceRollTotal = total
                    showDiceRollDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Velocidad
            MonsterSpeed(monster)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Ability Scores y Saving Throws
            MonsterStats(monster) { label, modifier ->
                currentDiceData = DiceRollData(numDice = 1, dieSides = 20, bonus = modifier, type = "hit")
                val (rolls, total) = rollDice(currentDiceData!!)
                diceRollDetails = rolls
                diceRollTotal = total
                showDiceRollDialog = true
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Habilidades
            MonsterSkills(
                monster = monster,
                onSkillClick = { label, modifier ->
                    currentDiceData = DiceRollData(numDice = 1, dieSides = 20, bonus = modifier, type = "hit")
                    val (rolls, total) = rollDice(currentDiceData!!)
                    diceRollDetails = rolls
                    diceRollTotal = total
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

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Actions
            MonsterActions(monster, onDiceRollClick, onConditionClick)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Bonus Actions
            MonsterBonusActions(monster, onDiceRollClick, onConditionClick)

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Reactions
            MonsterReactions(monster, onDiceRollClick, onConditionClick)
        }
    }

    // Diálogo para mostrar el resultado de la tirada
    if (showDiceRollDialog && currentDiceData != null) {
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
                        text = "Dice Roll",
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
                    Text(
                        text = buildString {
                            append("Roll: ")
                            append(currentDiceData!!.numDice.toString())
                            append("d")
                            append(currentDiceData!!.dieSides.toString())
                            currentDiceData!!.bonus?.let { bonus ->
                                if (bonus != 0) append(" + $bonus")
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Breakdown: ",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = buildDiceRollBreakdown(
                            rolls = diceRollDetails,
                            dieSides = currentDiceData!!.dieSides,
                            bonus = currentDiceData!!.bonus,
                            defaultColor = MaterialTheme.colorScheme.onSurface
                        ).text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                        text = conditionDescriptions[currentCondition.lowercase()] ?: "No description available for this condition.",
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
                    val walkValue = when (walk) {
                        is JsonPrimitive -> walk.intOrNull?.toString() ?: walk.contentOrNull ?: "0"
                        is JsonObject -> walk["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                        else -> "0"
                    }
                    Text(
                        text = "Walk: $walkValue ft",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.fly?.let { fly ->
                    val flyValue = when (fly) {
                        is JsonPrimitive -> fly.intOrNull?.toString() ?: fly.contentOrNull ?: "0"
                        is JsonObject -> {
                            val number = fly["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                            val condition = fly["condition"]?.jsonPrimitive?.contentOrNull
                            if (condition != null) "$number ft ($condition)" else "$number ft"
                        }
                        else -> "0"
                    }
                    Text(
                        text = "Fly: $flyValue${if (speed.canHover == true) " (can hover)" else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.swim?.let { swim ->
                    val swimValue = when (swim) {
                        is JsonPrimitive -> swim.intOrNull?.toString() ?: swim.contentOrNull ?: "0"
                        is JsonObject -> swim["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                        else -> "0"
                    }
                    Text(
                        text = "Swim: $swimValue ft",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.climb?.let { climb ->
                    val climbValue = when (climb) {
                        is JsonPrimitive -> climb.intOrNull?.toString() ?: climb.contentOrNull ?: "0"
                        is JsonObject -> climb["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                        else -> "0"
                    }
                    Text(
                        text = "Climb: $climbValue ft",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    hasSpeedData = true
                }
                speed.burrow?.let { burrow ->
                    val burrowValue = when (burrow) {
                        is JsonPrimitive -> burrow.intOrNull?.toString() ?: burrow.contentOrNull ?: "0"
                        is JsonObject -> burrow["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                        else -> "0"
                    }
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
                        else -> {}
                    }
                }
            }

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
                    val traitName = cleanTraitEntry(trait.name ?: "Unknown Trait")
                    Text(
                        text = traitName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                    trait.entries?.forEach { entry ->
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

@Composable
fun MonsterBonusActions(
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
                text = "Bonus Actions:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monster.bonus.isNullOrEmpty()) {
                Text(
                    text = "No bonus actions available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            } else {
                monster.bonus.forEach { bonus ->
                    val bonusName = cleanTraitEntry(bonus.name ?: "Unnamed Bonus Action")
                    Text(
                        text = "$bonusName",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                    bonus.entries?.forEach { entry ->
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

@Composable
fun MonsterReactions(
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
                text = "Reactions:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monster.reaction.isNullOrEmpty()) {
                Text(
                    text = "No reactions available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            } else {
                monster.reaction.forEach { reaction ->
                    val reactionName = cleanTraitEntry(reaction.name ?: "Unnamed Reaction")
                    Text(
                        text = "$reactionName",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                    reaction.entries?.forEach { entry ->
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

// Función para limpiar las etiquetas de las entradas de los rasgos
private fun cleanTraitEntry(entry: String): String {
    var cleaned = entry
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
    cleaned = cleaned.replace(Regex("\\{@dc (\\d+)\\}"), "DC $1")
    cleaned = cleaned.replace(Regex("\\{@variantrule ([^|]+)\\|XPHB(?:\\|[^}]*)?\\}"), "$1")
    cleaned = cleaned.replace(Regex("\\{@actSaveFail\\}"), "On a failed save,")
    cleaned = cleaned.replace(Regex("\\{@spell ([^}]+)\\}"), "$1")
    cleaned = cleaned.replace(Regex("\\{@skill ([^|}]*)\\|?[^}]*\\}"), "$1")
    cleaned = cleaned.replace(Regex("\\{@item ([^|}]*(?:\\s[+-]\\d+)?)(?:\\|.*)?\\}"), "$1")
    cleaned = cleaned.replace(Regex("\\{@atk mw\\}"), "Melee Weapon Attack: ")
    cleaned = cleaned.replace(Regex("\\{@atk rw\\}"), "Ranged Weapon Attack: ")
    cleaned = cleaned.replace(Regex("\\{@h\\}"), "Hit: ")
    cleaned = cleaned.replace(Regex("\\{@recharge (\\d+)\\}"), "(Recharge $1–6)")
    cleaned = cleaned.replace(Regex("\\{@i ([^}]+)\\}"), "$1")
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

// Calcular el promedio de una tirada de dados
private fun calculateDiceAverage(numDice: Int, dieSides: Int, bonus: Int? = null): Int {
    val averagePerDie = (dieSides + 1) / 2.0
    val totalAverage = (numDice * averagePerDie).toInt()
    return totalAverage + (bonus ?: 0)
}

// Datos de la tirada de dados
data class DiceRollData(
    val numDice: Int,
    val dieSides: Int,
    val bonus: Int? = null,
    val type: String = "damage", // "damage", "dice", "hit", "scaledice", "scaledamage"
    val scaledDie: String? = null,
    val levelRange: String? = null
)

// Construye un AnnotatedString con partes clicables
@Composable
fun buildDamageAnnotatedString(
    text: String,
    diceDataList: MutableList<DiceRollData>
): AnnotatedString {
    val pattern = Regex(
        "\\{@damage\\s*(\\d+d\\d+\\s*[+\\s]\\s*\\d+|\\d+d\\d+)\\}|" +
                "\\{@hit\\s*(\\d+)\\}\\s*to hit|" +
                "\\{@hit\\s*(\\d+)\\}|" +
                "\\{@dice\\s*(\\d*d\\d+)\\}|" +
                "\\{@scaledice\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" +
                "\\{@scaledamage\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" +
                "\\{@condition\\s*(charmed|unconscious|frightened|restrained|petrified|blinded|deafened|poisoned|paralyzed|stunned|incapacitated|invisible|prone|grappled|exhaustion|deafened\\|\\|deaf|blinded\\|\\|blind)\\}"
    )
    val matches = pattern.findAll(text).toList()
    diceDataList.clear()

    return buildAnnotatedString {
        var lastIndex = 0
        var hitAdded = false // Reset for each call

        matches.forEachIndexed { index, matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            // Append the text before the match
            append(text.substring(lastIndex, start))

            when {
                matchResult.value.startsWith("{@damage") -> {
                    val damage = matchResult.groupValues[1].trim().replace("\\s+".toRegex(), " ")
                    val parts = damage.split("[+\\s]".toRegex()).filter { it.isNotEmpty() }
                    if (parts.size == 1) {
                        val (numDice, dieSides) = parts[0].split("d").map { it.toIntOrNull() ?: 0 }
                        if (numDice > 0 && dieSides > 0) {
                            diceDataList.add(DiceRollData(numDice, dieSides, type = "damage"))
                            pushStringAnnotation(tag = "damage", annotation = index.toString())
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                append(damage)
                            }
                            pop()
                        }
                    } else if (parts.size == 2) {
                        val (dicePart, bonusPart) = parts
                        val (numDice, dieSides) = dicePart.split("d").map { it.toIntOrNull() ?: 0 }
                        val bonus = bonusPart.toIntOrNull() ?: 0
                        if (numDice > 0 && dieSides > 0) {
                            diceDataList.add(DiceRollData(numDice, dieSides, bonus, "damage"))
                            pushStringAnnotation(tag = "damage", annotation = index.toString())
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                append(damage)
                            }
                            pop()
                        }
                    }
                }
                matchResult.value.startsWith("{@hit") -> {
                    val bonus = if (matchResult.groupValues[2].isNotEmpty()) {
                        matchResult.groupValues[2].toIntOrNull() ?: 0
                    } else {
                        matchResult.groupValues[3].toIntOrNull() ?: 0
                    }
                    if (!hitAdded) {
                        diceDataList.add(DiceRollData(numDice = 1, dieSides = 20, bonus = bonus, type = "hit"))
                        pushStringAnnotation(tag = "damage", annotation = index.toString())
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append("${formatModifier(bonus)} to hit")
                        }
                        pop()
                        hitAdded = true
                    }
                }
                matchResult.value.startsWith("{@dice") -> {
                    val dice = matchResult.groupValues[4].replace("\\s+".toRegex(), "")
                    val parts = dice.split("d")
                    val numDice = if (parts[0].isEmpty()) 1 else parts[0].toIntOrNull() ?: 0
                    val dieSides = parts[1].toIntOrNull() ?: 0
                    if (numDice > 0 && dieSides > 0) {
                        diceDataList.add(DiceRollData(numDice, dieSides, type = "dice"))
                        pushStringAnnotation(tag = "damage", annotation = index.toString())
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append(dice)
                        }
                        pop()
                    }
                }
                matchResult.value.startsWith("{@scaledice") -> {
                    val scaledDie = matchResult.groupValues[5].replace("\\s+".toRegex(), "")
                    val levelRange = matchResult.groupValues[6]
                    val (scaledNumDice, scaledDieSides) = scaledDie.split("d").map { it.toIntOrNull() ?: 0 }
                    if (scaledNumDice > 0 && scaledDieSides > 0) {
                        diceDataList.add(DiceRollData(scaledNumDice, scaledDieSides, type = "scaledice", scaledDie = scaledDie, levelRange = levelRange))
                        pushStringAnnotation(tag = "damage", annotation = index.toString())
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append(scaledDie)
                        }
                        pop()
                    }
                }
                matchResult.value.startsWith("{@scaledamage") -> {
                    val scaledDie = matchResult.groupValues[8].replace("\\s+".toRegex(), "")
                    val levelRange = matchResult.groupValues[9]
                    val (scaledNumDice, scaledDieSides) = scaledDie.split("d").map { it.toIntOrNull() ?: 0 }
                    if (scaledNumDice > 0 && scaledDieSides > 0) {
                        diceDataList.add(DiceRollData(scaledNumDice, scaledDieSides, type = "scaledamage", scaledDie = scaledDie, levelRange = levelRange))
                        pushStringAnnotation(tag = "damage", annotation = index.toString())
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append(scaledDie)
                        }
                        pop()
                    }
                }
                matchResult.value.startsWith("{@condition") -> {
                    val condition = matchResult.groupValues[10]
                    val display = when (condition) {
                        "deafened||deaf" -> "deafened"
                        "blinded||blind" -> "blinded"
                        else -> condition
                    }
                    pushStringAnnotation(tag = "condition", annotation = condition)
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append(display)
                    }
                    pop()
                }
            }
            lastIndex = end
        }
        // Append the remaining text after the last match
        append(text.substring(lastIndex))
    }
}

// Función para realizar la tirada de dados
fun rollDice(dice: DiceRollData, slotLevel: Int = 1): Pair<List<Int>, Int> {
    val baseNumDice = dice.numDice
    val scaledNumDice = if (dice.type == "scaledice" || dice.type == "scaledamage") {
        val levelRange = dice.levelRange?.split("-")?.map { it.toInt() } ?: listOf(1, 9)
        val minLevel = levelRange[0]
        val maxLevel = levelRange[1]
        if (slotLevel in minLevel..maxLevel) {
            val levelAdjustment = slotLevel - minLevel + 1
            baseNumDice * levelAdjustment
        } else {
            baseNumDice
        }
    } else {
        baseNumDice
    }
    val dieSides = when (dice.type) {
        "hit" -> 20 // Usar d20 para "to hit"
        else -> dice.dieSides
    }
    val rolls = (1..scaledNumDice).map { Random.nextInt(1, dieSides + 1) }
    val total = rolls.sum() + (dice.bonus ?: 0)
    return rolls to total
}

// Construye el texto del desglose con colores para máximo y mínimo
fun buildDiceRollBreakdown(
    rolls: List<Int>,
    dieSides: Int,
    bonus: Int?,
    defaultColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        rolls.forEachIndexed { index, roll ->
            val color = when {
                roll == dieSides -> Color.Green
                roll == 1 -> Color.Red
                else -> defaultColor
            }
            withStyle(style = SpanStyle(color = color)) {
                append(roll.toString())
            }
            if (index < rolls.size - 1 || bonus != null) {
                append(" + ")
            }
        }
        if (bonus != null) {
            withStyle(style = SpanStyle(color = defaultColor)) {
                append(bonus.toString())
            }
        }
    }
}