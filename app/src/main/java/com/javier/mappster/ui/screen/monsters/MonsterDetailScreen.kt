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
import com.javier.mappster.ui.screen.monsters.MonsterActions
import com.javier.mappster.ui.screen.monsters.MonsterBonusActions
import com.javier.mappster.ui.screen.monsters.MonsterCombatStats
import com.javier.mappster.ui.screen.monsters.MonsterInfoSection
import com.javier.mappster.ui.screen.monsters.MonsterLanguages
import com.javier.mappster.ui.screen.monsters.MonsterReactions
import com.javier.mappster.ui.screen.monsters.MonsterResistancesAndImmunities
import com.javier.mappster.ui.screen.monsters.MonsterSenses
import com.javier.mappster.ui.screen.monsters.MonsterSkills
import com.javier.mappster.ui.screen.monsters.MonsterSpeed
import com.javier.mappster.ui.screen.monsters.MonsterStats
import com.javier.mappster.ui.screen.monsters.MonsterTraits
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

            Spacer(modifier = Modifier.height(12.dp))

            // Nueva sección: Spellcasting
            MonsterSpellcasting(monster, onConditionClick)
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

// Nueva sección para Spellcasting
@Composable
fun MonsterSpellcasting(
    monster: Monster,
    onConditionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Spellcasting:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monster.spellcasting.isNullOrEmpty()) {
                Text(
                    text = "No spellcasting available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            } else {
                monster.spellcasting.forEach { spellcasting ->
                    // Nombre del tipo de spellcasting (por ejemplo, "Innate Spellcasting")
                    spellcasting.name?.let { name ->
                        Text(
                            text = cleanTraitEntry(name),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }

                    // Entradas de cabecera (headerEntries)
                    spellcasting.headerEntries?.forEach { entry ->
                        val diceDataList = remember { mutableListOf<DiceRollData>() }
                        val cleanedText = cleanTraitEntry(entry)
                        val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)
                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        onConditionClick(annotation.item)
                                    }
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    // Hechizos por nivel (spells)
                    spellcasting.spells?.forEach { (level, spellLevel) ->
                        Text(
                            text = "Level $level (${spellLevel.slots ?: 0} slots):",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                        spellLevel.spells?.joinToString(", ") { cleanTraitEntry(it) }?.let { spellsText ->
                            val diceDataList = remember { mutableListOf<DiceRollData>() }
                            val annotatedText = buildDamageAnnotatedString(spellsText, diceDataList)
                            ClickableText(
                                text = annotatedText,
                                onClick = { offset ->
                                    annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            onConditionClick(annotation.item)
                                        }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                            )
                        }
                    }

                    // Hechizos "a voluntad" (will)
                    spellcasting.will?.let { willSpells ->
                        Text(
                            text = "At Will:",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                        willSpells.joinToString(", ") { it.entry ?: "Unknown" }.let { willText ->
                            val diceDataList = remember { mutableListOf<DiceRollData>() }
                            val cleanedText = cleanTraitEntry(willText)
                            val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)
                            ClickableText(
                                text = annotatedText,
                                onClick = { offset ->
                                    annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            onConditionClick(annotation.item)
                                        }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                            )
                        }
                    }

                    // Hechizos diarios (daily)
                    spellcasting.daily?.forEach { (frequency, dailySpells) ->
                        Text(
                            text = "${frequency.replace("e", "")}:",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                        dailySpells.joinToString(", ") { it.entry ?: "Unknown" }.let { dailyText ->
                            val diceDataList = remember { mutableListOf<DiceRollData>() }
                            val cleanedText = cleanTraitEntry(dailyText)
                            val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)
                            ClickableText(
                                text = annotatedText,
                                onClick = { offset ->
                                    annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            onConditionClick(annotation.item)
                                        }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Función para limpiar las etiquetas de las entradas de los rasgos
fun cleanTraitEntry(entry: String): String {
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

fun calculateModifier(stat: Int?): Int? {
    return stat?.let {
        floor((it - 10) / 2.0).toInt()
    }
}

fun formatModifier(modifier: Int): String {
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