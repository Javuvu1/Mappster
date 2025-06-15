package com.javier.mappster.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.model.*
import com.javier.mappster.ui.screen.spells.SpellListViewModel
import com.javier.mappster.utils.conditionDescriptions
import com.javier.mappster.utils.normalizeSpellName
import com.javier.mappster.utils.sourceMap
import java.net.URLEncoder
import kotlin.random.Random

@Composable
private fun SectionTitle(title: String, icon: ImageVector, schoolColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = schoolColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = schoolColor
        )
    }
}

data class DiceRollData(
    val numDice: Int,
    val dieSides: Int,
    val bonus: Int? = null,
    val type: String = "damage",
    val scaledDie: String? = null,
    val levelRange: String? = null
)

data class ChanceData(
    val percentage: Int,
    val failMessage: String,
    val successMessage: String
)

val quickRefDescriptions = mapOf(
    "half cover" to "A target with half cover has a +2 bonus to AC and Dexterity saving throws. A target has half cover if an obstacle blocks at least half of its body. The obstacle might be a low wall, a large piece of furniture, a narrow tree trunk, or a creature, whether that creature is an enemy or a friend.",
    "three-quarters cover" to "A target with three-quarters cover has a +5 bonus to AC and Dexterity saving throws. A target has three-quarters cover if about three-quarters of it is covered by an obstacle. The obstacle might be a portcullis, an arrow slit, or a thick tree trunk.",
    "difficult terrain" to "Every foot of movement in difficult terrain costs 1 extra foot. This rule is true even if multiple things in a space count as difficult terrain.",
    "heavily obscured" to "You have the Blinded condition while trying to see something in a Heavily Obscured space.",
    "total cover" to "A target with total cover can't be targeted directly by an attack or a spell, although some spells can reach such a target by including it in an area of effect. A target has total cover if it is completely concealed by an obstacle."
)

private val PATTERN = Regex(
    "\\{@damage\\s*(\\d+d\\d+\\s*\\+\\s*\\d+|\\d+d\\d+)\\}|" +
            "\\{@dice\\s*(\\d*d\\d+)\\}|" +
            "\\{@scaledice\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" +
            "\\{@scaledamage\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" +
            "\\{@condition\\s*(charmed|unconscious|frightened|restrained|petrified|blinded|deafened|poisoned|paralyzed|stunned|incapacitated|invisible|prone|grappled|exhaustion|deafened\\|\\|deaf|blinded\\|\\|blind)\\}|" +
            "\\{@spell\\s*([^\\}]+)\\}|" +
            "\\{@chance\\s*(\\d+)\\s*\\|\\|\\|\\s*([^\\|]+)\\|([^\\}]+)\\}|" +
            "\\{@quickref\\s+(Cover\\|\\|3\\|\\|(half cover|three-quarters cover|total cover))\\}|" +
            "\\{@quickref\\s+(Vision and Light\\|PHB\\|2\\|\\|heavily obscured)\\}|" +
            "\\{@quickref\\s+(difficult terrain\\|\\|3)\\}|" +
            "\\{@quickref\\s*([^\\|\\}]+?)(?:\\|\\|(\\d+)?(?:\\|\\|([^\\}]+))?)?\\s*\\}|" +
            "\\{@quickref\\s*[^\\}]+\\|PHB\\|\\d+\\|\\d*\\|([^\\}]+)\\}|" +
            "\\{@skill\\s*([^\\}]+)\\}|" +
            "\\{@d20\\s*(-?\\d+)\\}"
)

fun buildDamageAnnotatedString(
    text: String,
    diceDataList: MutableList<DiceRollData>,
    chanceDataList: MutableList<ChanceData>,
    schoolColor: Color,
    highlightColor: Color = Color.LightGray.copy(alpha = 0.2f)
): AnnotatedString {
    Log.d("SpellDetailScreen", "Parsing text: '$text'")

    val matches = PATTERN.findAll(text).toList()
    diceDataList.clear()
    chanceDataList.clear()

    return buildAnnotatedString {
        var lastIndex = 0
        matches.forEachIndexed { index, match ->
            val startTime = System.nanoTime()
            Log.d("SpellDetailScreen", "Match: ${match.value}, groups: ${match.groupValues}")
            val start = match.range.first
            val end = match.range.last + 1

            val result = when {
                match.value.startsWith("{@damage") -> {
                    val damage = match.groupValues.getOrNull(1)?.replace("\\s+".toRegex(), "") ?: ""
                    val parts = damage.split("+")
                    if (parts.size == 1 && damage.isNotEmpty()) {
                        val (numDice, dieSides) = parts[0].split("d").map { it.toIntOrNull() ?: 0 }
                        diceDataList.add(DiceRollData(numDice, dieSides, type = "damage"))
                    } else if (parts.size == 2) {
                        val (dicePart, bonusPart) = parts
                        val (numDice, dieSides) = dicePart.split("d").map { it.toIntOrNull() ?: 0 }
                        val bonus = bonusPart.toIntOrNull() ?: 0
                        diceDataList.add(DiceRollData(numDice, dieSides, bonus, type = "damage"))
                    }
                    Pair(damage, true)
                }
                match.value.startsWith("{@dice") -> {
                    val dice = match.groupValues.getOrNull(2)?.replace("\\s+".toRegex(), "") ?: ""
                    val parts = dice.split("d")
                    val numDice = if (parts[0].isEmpty()) 1 else parts[0].toIntOrNull() ?: 1
                    val dieSides = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    diceDataList.add(DiceRollData(numDice, dieSides, type = "dice"))
                    Pair(dice, true)
                }
                match.value.startsWith("{@scaledice") -> {
                    val initialDice = match.groupValues.getOrNull(3)?.replace("\\s+".toRegex(), "") ?: ""
                    val levelRange = match.groupValues.getOrNull(4) ?: ""
                    val scaledDie = match.groupValues.getOrNull(5)?.replace("\\s+".toRegex(), "") ?: ""
                    val (scaledNumDice, scaledDieSides) = scaledDie.split("d").map { it.toIntOrNull() ?: 0 }
                    diceDataList.add(DiceRollData(scaledNumDice, scaledDieSides, type = "scaledice", scaledDie = scaledDie, levelRange = levelRange))
                    Pair(scaledDie, true)
                }
                match.value.startsWith("{@scaledamage") -> {
                    val initialDice = match.groupValues.getOrNull(6)?.replace("\\s+".toRegex(), "") ?: ""
                    val levelRange = match.groupValues.getOrNull(7) ?: ""
                    val scaledDie = match.groupValues.getOrNull(8)?.replace("\\s+".toRegex(), "") ?: ""
                    val (numDice, dieSides) = scaledDie.split("d").map { it.toIntOrNull() ?: 0 }
                    diceDataList.add(DiceRollData(numDice, dieSides, type = "scaledamage", scaledDie = scaledDie, levelRange = levelRange))
                    Pair(scaledDie, true)
                }
                match.value.startsWith("{@condition") -> {
                    val condition = match.groupValues.getOrNull(9) ?: ""
                    val displayCondition = when {
                        condition == "deafened||deaf" -> "deafened"
                        condition == "blinded||blind" -> "blinded"
                        else -> condition
                    }
                    Pair(displayCondition, true)
                }
                match.value.startsWith("{@spell") -> {
                    val spellName = match.groupValues.getOrNull(10)?.trim() ?: ""
                    Log.d("SpellDetailScreen", "Parsed spell: '$spellName' from ${match.value}")
                    Pair(spellName, true)
                }
                match.value.startsWith("{@chance") -> {
                    val percentage = match.groupValues.getOrNull(11)?.toIntOrNull() ?: 0
                    val failMessage = match.groupValues.getOrNull(12)?.trim() ?: ""
                    val successMessage = match.groupValues.getOrNull(13)?.trim() ?: ""
                    chanceDataList.add(ChanceData(percentage, failMessage, successMessage))
                    Log.d("SpellDetailScreen", "Parsed chance: $percentage%, fail='$failMessage', success='$successMessage'")
                    Pair("$percentage%", true)
                }
                match.value.contains("Cover||3||half cover") -> Pair("half cover", true)
                match.value.contains("Cover||3||three-quarters cover") -> Pair("three-quarters cover", true)
                match.value.contains("Cover||3||total cover") -> Pair("total cover", true)
                match.value.contains("Vision and Light|PHB|2||heavily obscured") -> Pair("heavily obscured", true)
                match.value.contains("difficult terrain||3") -> Pair("difficult terrain", true)
                match.value.startsWith("{@skill") -> {
                    val skillName = match.groupValues.getOrNull(23)?.trim() ?: ""
                    Log.d("SpellDetailScreen", "Parsed skill: '$skillName' from ${match.value}")
                    Pair("($skillName)", false)
                }
                match.value.startsWith("{@d20") -> {
                    val modifier = match.groupValues.last { it.isNotEmpty() && it != match.value }.toInt()
                    diceDataList.add(DiceRollData(numDice = 1, dieSides = 20, bonus = modifier, type = "d20"))
                    Log.d("SpellDetailScreen", "Parsed d20: modifier=$modifier from ${match.value}")
                    val display = if (modifier >= 0) "+$modifier" else "$modifier"
                    Pair(display, true)
                }
                match.value.startsWith("{@quickref") -> {
                    val displayText = when {
                        match.groupValues.getOrNull(18)?.isNotEmpty() == true -> match.groupValues[18].trim()
                        match.groupValues.getOrNull(19)?.isNotEmpty() == true -> match.groupValues[19].trim()
                        match.groupValues.getOrNull(16)?.isNotEmpty() == true -> match.groupValues[16].trim()
                        else -> ""
                    }
                    val isClickable = displayText.isNotEmpty() &&
                            quickRefDescriptions.keys.any { it.equals(displayText, ignoreCase = true) }
                    Log.d("SpellDetailScreen", "Quickref parsed: '${match.value}' -> '$displayText' (clickable: $isClickable)")
                    Pair(displayText, isClickable)
                }
                else -> Pair("", false)
            }
            val displayText = result.first
            val isClickable = result.second
            val endTime = System.nanoTime()
            Log.d("SpellDetailScreen", "Processed match ${match.value} in ${(endTime - startTime) / 1_000_000}ms")

            Log.d("SpellDetailScreen", "Appending from $lastIndex to $start: '${text.substring(lastIndex, start)}'")
            append(text.substring(lastIndex, start))

            when {
                match.value.startsWith("{@condition") && isClickable -> {
                    pushStringAnnotation(tag = "condition", annotation = match.groupValues.getOrNull(9) ?: "")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                (match.value.startsWith("{@damage") || match.value.startsWith("{@dice") ||
                        match.value.startsWith("{@scaledice") || match.value.startsWith("{@scaledamage")) && isClickable -> {
                    pushStringAnnotation(tag = "damage", annotation = index.toString())
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@spell") && isClickable -> {
                    pushStringAnnotation(tag = "spell", annotation = displayText)
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@chance") && isClickable -> {
                    pushStringAnnotation(tag = "chance", annotation = index.toString())
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@quickref") && isClickable -> {
                    pushStringAnnotation(tag = "quickref", annotation = displayText)
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@d20") && isClickable -> {
                    pushStringAnnotation(tag = "d20", annotation = index.toString())
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@skill") -> {
                    append(displayText)
                }
                else -> append(displayText)
            }
            lastIndex = end
        }
        Log.d("SpellDetailScreen", "Appending remaining text: '${text.substring(lastIndex)}'")
        append(text.substring(lastIndex))
    }
}

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
    val dieSides = dice.dieSides
    val rolls = (1..scaledNumDice).map { Random.nextInt(1, dieSides + 1) }
    val total = rolls.sum() + (dice.bonus ?: 0)
    return rolls to total
}

fun rollChance(percentage: Int): Pair<Int, Boolean> {
    val roll = Random.nextInt(1, 101)
    val isSuccess = roll <= percentage
    Log.d("SpellDetailScreen", "Chance roll: $roll, percentage: $percentage, success: $isSuccess")
    return roll to isSuccess
}

@Composable
fun buildDiceRollBreakdown(
    rolls: List<Int>,
    dieSides: Int,
    bonus: Int?,
    defaultColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        rolls.forEachIndexed { index, roll ->
            val color = when {
                roll == dieSides -> MaterialTheme.colorScheme.tertiary
                roll == 1 -> MaterialTheme.colorScheme.tertiary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellDetailScreen(
    spell: Spell,
    isTwoPaneMode: Boolean = false,
    navController: NavHostController,
    viewModel: SpellListViewModel,
    modifier: Modifier = Modifier,
    onSpellSelected: (Spell) -> Unit = {}
) {
    val context = LocalContext.current
    val schoolData = when (spell.school.uppercase()) {
        "A" -> SchoolData("Abjuración", Color(0xFF4CAF50), Icons.Default.Shield)
        "C" -> SchoolData("Conjuración", Color(0xFF9C27B0), Icons.Default.CallMerge)
        "D" -> SchoolData("Adivinación", Color(0xFF00ACC1), Icons.Default.Visibility)
        "E" -> SchoolData("Encantamiento", Color(0xFFE91E63), Icons.Default.Favorite)
        "V" -> SchoolData("Evocación", Color(0xFFFF5722), Icons.Default.Whatshot)
        "I" -> SchoolData("Ilusión", Color(0xFF7C4DFF), Icons.Default.Masks)
        "N" -> SchoolData("Nigromancia", Color(0xFF607D8B), Icons.Default.Coronavirus)
        "T" -> SchoolData("Transmutación", Color(0xFFFFC107), Icons.Default.AutoAwesome)
        else -> SchoolData(spell.school, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.AutoFixHigh)
    }

    var showDiceRollDialog by remember { mutableStateOf(false) }
    var diceRollDetails by remember { mutableStateOf<List<Int>>(emptyList()) }
    var diceRollTotal by remember { mutableStateOf(0) }
    var currentDiceData by remember { mutableStateOf<DiceRollData?>(null) }
    var showConditionDialog by remember { mutableStateOf(false) }
    var currentCondition by remember { mutableStateOf("") }
    var showChanceDialog by remember { mutableStateOf(false) }
    var currentChanceData by remember { mutableStateOf<ChanceData?>(null) }
    var chanceRollResult by remember { mutableStateOf<Int?>(null) }
    var chanceIsSuccess by remember { mutableStateOf<Boolean?>(null) }
    var showQuickRefDialog by remember { mutableStateOf(false) }
    var currentQuickRef by remember { mutableStateOf("") }

    // Log para depurar el objeto range
    LaunchedEffect(spell) {
        Log.d("SpellDetailScreen", "Spell range: type=${spell.range?.type}, distance=${spell.range?.distance?.amount} ${spell.range?.distance?.type}, areaTags=${spell.areaTags}")
    }

    Scaffold(
        topBar = {
            if (!isTwoPaneMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = spell.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = schoolData.color
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = schoolData.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = schoolData.color
                    )
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionTitle("Detalles", Icons.Default.Info, schoolData.color)
                        Text(
                            text = if (spell.level == 0) "Truco" else "Nivel ${spell.level}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Escuela: ${schoolData.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fuente: ${sourceMap[spell.source] ?: spell.source}${if (spell.page != 0) ", ${spell.page}" else ""}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tiempo de lanzamiento: ${spell.time.joinToString { "${it.number} ${it.unit}" }}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rango: ${spell.range?.type ?: "Unknown"}${spell.range?.distance?.amount?.let { " ($it feet${spell.areaTags.firstOrNull()?.let { area -> ", $area" } ?: ""})" } ?: ""}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val componentsList = buildList {
                            if (spell.components?.v == true) add("V")
                            if (spell.components?.s == true) add("S")
                            if (spell.components?.m != null) add("M (${spell.components.m})")
                            if (spell.components?.r == true) add("R")
                        }
                        Text(
                            text = "Componentes: ${componentsList.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Duración: ${spell.duration.firstOrNull()?.type ?: "Instantánea"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (spell.meta?.ritual == true) {
                            Text(
                                text = "Ritual: Sí",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionTitle("Descripción", Icons.Default.Description, schoolData.color)
                        spell.entries.forEach { entry ->
                            val diceDataList = remember { mutableListOf<DiceRollData>() }
                            val chanceDataList = remember { mutableListOf<ChanceData>() }
                            val annotatedText = remember(entry) {
                                buildDamageAnnotatedString(entry, diceDataList, chanceDataList, schoolData.color)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Spacer(modifier = Modifier.width(8.dp))
                                ClickableText(
                                    text = annotatedText,
                                    onClick = { offset ->
                                        annotatedText.getStringAnnotations(tag = "damage", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                val index = annotation.item.toInt()
                                                val diceData = diceDataList[index]
                                                val (rolls, total) = rollDice(diceData, spell.level)
                                                currentDiceData = diceData
                                                diceRollDetails = rolls
                                                diceRollTotal = total
                                                showDiceRollDialog = true
                                            }
                                        annotatedText.getStringAnnotations(tag = "d20", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                val index = annotation.item.toInt()
                                                val diceData = diceDataList[index]
                                                val (rolls, total) = rollDice(diceData, spell.level)
                                                currentDiceData = diceData
                                                diceRollDetails = rolls
                                                diceRollTotal = total
                                                showDiceRollDialog = true
                                            }
                                        annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                currentCondition = annotation.item
                                                showConditionDialog = true
                                            }
                                        annotatedText.getStringAnnotations(tag = "spell", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                val spellName = annotation.item.trim()
                                                Log.d("SpellDetailScreen", "Clicked spell: '$spellName', isTwoPaneMode: $isTwoPaneMode")
                                                val targetSpell = viewModel.getSpellByName(spellName)
                                                Log.d("SpellDetailScreen", "Target spell: ${targetSpell?.name ?: "null"}")
                                                if (targetSpell != null) {
                                                    if (isTwoPaneMode) {
                                                        Log.d("SpellDetailScreen", "Selecting spell in two-pane mode: ${targetSpell.name}")
                                                        onSpellSelected(targetSpell)
                                                        Log.d("SpellDetailScreen", "onSpellSelected invoked for: ${targetSpell.name}, spell object: $targetSpell")
                                                    } else {
                                                        val normalizedSpellName = normalizeSpellName(targetSpell.name)
                                                        val encodedName = URLEncoder.encode(normalizedSpellName, "UTF-8").replace("+", "%20")
                                                        Log.d("SpellDetailScreen", "Navigating to: spell_detail/$encodedName")
                                                        try {
                                                            navController.navigate("spell_detail/$encodedName")
                                                        } catch (e: IllegalArgumentException) {
                                                            Log.e("SpellDetailScreen", "Navigation failed for spell_detail/$encodedName: ${e.message}", e)
                                                            Toast.makeText(context, "Error al navegar a '${targetSpell.name}'.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                } else {
                                                    Log.w("SpellDetailScreen", "Spell '$spellName' not found")
                                                    Toast.makeText(context, "Hechizo '$spellName' no encontrado.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        annotatedText.getStringAnnotations(tag = "chance", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                val index = annotation.item.toInt()
                                                val chanceData = chanceDataList[index]
                                                val (roll, isSuccess) = rollChance(chanceData.percentage)
                                                currentChanceData = chanceData
                                                chanceRollResult = roll
                                                chanceIsSuccess = isSuccess
                                                showChanceDialog = true
                                            }
                                        annotatedText.getStringAnnotations(tag = "quickref", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                Log.d("SpellDetailScreen", "Quickref clicked: '${annotation.item}'")
                                                currentQuickRef = annotation.item
                                                showQuickRefDialog = true
                                            }
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }
                }
            }

            if (spell.customAccess.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            SectionTitle("Acceso", Icons.Default.Group, schoolData.color)
                            Text(
                                text = spell.customAccess,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (spell.entriesHigherLevel.isNotEmpty() || spell.customHigherLevel.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            SectionTitle("A nivel superior", Icons.Default.Upgrade, schoolData.color)
                            if (spell.entriesHigherLevel.isNotEmpty()) {
                                spell.entriesHigherLevel.forEach { entryHigherLevel ->
                                    entryHigherLevel.entries.forEach { entry ->
                                        val diceDataList = remember { mutableListOf<DiceRollData>() }
                                        val chanceDataList = remember { mutableListOf<ChanceData>() }
                                        val annotatedText = remember(entry) {
                                            buildDamageAnnotatedString(entry, diceDataList, chanceDataList, schoolData.color)
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            ClickableText(
                                                text = annotatedText,
                                                onClick = { offset ->
                                                    annotatedText.getStringAnnotations(tag = "damage", start = offset, end = offset)
                                                        .firstOrNull()?.let { annotation ->
                                                            val index = annotation.item.toInt()
                                                            val diceData = diceDataList[index]
                                                            val (rolls, total) = rollDice(diceData, spell.level)
                                                            currentDiceData = diceData
                                                            diceRollDetails = rolls
                                                            diceRollTotal = total
                                                            showDiceRollDialog = true
                                                        }
                                                    annotatedText.getStringAnnotations(tag = "d20", start = offset, end = offset)
                                                        .firstOrNull()?.let { annotation ->
                                                            val index = annotation.item.toInt()
                                                            val diceData = diceDataList[index]
                                                            val (rolls, total) = rollDice(diceData, spell.level)
                                                            currentDiceData = diceData
                                                            diceRollDetails = rolls
                                                            diceRollTotal = total
                                                            showDiceRollDialog = true
                                                        }
                                                    annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                                        .firstOrNull()?.let { annotation ->
                                                            currentCondition = annotation.item
                                                            showConditionDialog = true
                                                        }
                                                    annotatedText.getStringAnnotations(tag = "spell", start = offset, end = offset)
                                                        .firstOrNull()?.let { annotation ->
                                                            val spellName = annotation.item.trim()
                                                            Log.d("SpellDetailScreen", "Clicked spell (higher level): '$spellName', isTwoPaneMode: $isTwoPaneMode")
                                                            val targetSpell = viewModel.getSpellByName(spellName)
                                                            Log.d("SpellDetailScreen", "Target spell: $targetSpell")
                                                            if (targetSpell != null) {
                                                                if (isTwoPaneMode) {
                                                                    Log.d("SpellDetailScreen", "Selecting spell in two-pane mode: ${targetSpell.name}")
                                                                    onSpellSelected(targetSpell)
                                                                } else {
                                                                    val encodedName = URLEncoder.encode(targetSpell.name, "UTF-8")
                                                                    Log.d("SpellDetailScreen", "Navigating to: spell_detail/$encodedName")
                                                                    navController.navigate("spell_detail/$encodedName")
                                                                }
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Hechizo '$spellName' no encontrado.",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        }
                                                    annotatedText.getStringAnnotations(tag = "chance", start = offset, end = offset)
                                                        .firstOrNull()?.let { annotation ->
                                                            val index = annotation.item.toInt()
                                                            val chanceData = chanceDataList[index]
                                                            val (roll, isSuccess) = rollChance(chanceData.percentage)
                                                            currentChanceData = chanceData
                                                            chanceRollResult = roll
                                                            chanceIsSuccess = isSuccess
                                                            showChanceDialog = true
                                                        }
                                                    annotatedText.getStringAnnotations(tag = "quickref", start = offset, end = offset)
                                                        .firstOrNull()?.let { annotation ->
                                                            Log.d("SpellDetailScreen", "Quickref clicked: '${annotation.item}'")
                                                            currentQuickRef = annotation.item
                                                            showQuickRefDialog = true
                                                        }
                                                },
                                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            } else if (spell.customHigherLevel.isNotBlank()) {
                                val diceDataList = remember { mutableListOf<DiceRollData>() }
                                val chanceDataList = remember { mutableListOf<ChanceData>() }
                                val annotatedText = remember(spell.customHigherLevel) {
                                    buildDamageAnnotatedString(spell.customHigherLevel, diceDataList, chanceDataList, schoolData.color)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ClickableText(
                                        text = annotatedText,
                                        onClick = { offset ->
                                            annotatedText.getStringAnnotations(tag = "damage", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    val index = annotation.item.toInt()
                                                    val diceData = diceDataList[index]
                                                    val (rolls, total) = rollDice(diceData, spell.level)
                                                    currentDiceData = diceData
                                                    diceRollDetails = rolls
                                                    diceRollTotal = total
                                                    showDiceRollDialog = true
                                                }
                                            annotatedText.getStringAnnotations(tag = "d20", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    val index = annotation.item.toInt()
                                                    val diceData = diceDataList[index]
                                                    val (rolls, total) = rollDice(diceData, spell.level)
                                                    currentDiceData = diceData
                                                    diceRollDetails = rolls
                                                    diceRollTotal = total
                                                    showDiceRollDialog = true
                                                }
                                            annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    currentCondition = annotation.item
                                                    showConditionDialog = true
                                                }
                                            annotatedText.getStringAnnotations(tag = "spell", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    val spellName = annotation.item.trim()
                                                    Log.d("SpellDetailScreen", "Clicked spell (higher level): '$spellName', isTwoPaneMode: $isTwoPaneMode")
                                                    val targetSpell = viewModel.getSpellByName(spellName)
                                                    Log.d("SpellDetailScreen", "Target spell: $targetSpell")
                                                    if (targetSpell != null) {
                                                        if (isTwoPaneMode) {
                                                            Log.d("SpellDetailScreen", "Selecting spell in two-pane mode: ${targetSpell.name}")
                                                            onSpellSelected(targetSpell)
                                                        } else {
                                                            val encodedName = URLEncoder.encode(targetSpell.name, "UTF-8")
                                                            Log.d("SpellDetailScreen", "Navigating to: spell_detail/$encodedName")
                                                            navController.navigate("spell_detail/$encodedName")
                                                        }
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Hechizo '$spellName' no encontrado.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            annotatedText.getStringAnnotations(tag = "chance", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    val index = annotation.item.toInt()
                                                    val chanceData = chanceDataList[index]
                                                    val (roll, isSuccess) = rollChance(chanceData.percentage)
                                                    currentChanceData = chanceData
                                                    chanceRollResult = roll
                                                    chanceIsSuccess = isSuccess
                                                    showChanceDialog = true
                                                }
                                            annotatedText.getStringAnnotations(tag = "quickref", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    Log.d("SpellDetailScreen", "Quickref clicked: '${annotation.item}'")
                                                    currentQuickRef = annotation.item
                                                    showQuickRefDialog = true
                                                }
                                        },
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDiceRollDialog && currentDiceData != null) {
            AlertDialog(
                onDismissRequest = { showDiceRollDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Casino, contentDescription = null, tint = schoolData.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resultado de la tirada",
                            style = MaterialTheme.typography.headlineSmall,
                            color = schoolData.color
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tirada: ${currentDiceData!!.numDice}d${currentDiceData!!.dieSides}" +
                                    (currentDiceData!!.bonus?.let { " + $it" } ?: ""),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Desglose: ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = buildDiceRollBreakdown(
                                rolls = diceRollDetails,
                                dieSides = currentDiceData!!.dieSides,
                                bonus = currentDiceData!!.bonus,
                                defaultColor = MaterialTheme.colorScheme.onSurface
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total: $diceRollTotal",
                            style = MaterialTheme.typography.headlineSmall,
                            color = schoolData.color
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDiceRollDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = schoolData.color)
                    ) {
                        Text("Cerrar")
                    }
                },
                containerColor = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            )
        }

        if (showConditionDialog) {
            AlertDialog(
                onDismissRequest = { showConditionDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = schoolData.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentCondition.replace("||", "").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineMedium,
                            color = schoolData.color
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        val description = buildDamageAnnotatedString(
                            conditionDescriptions[currentCondition] ?: "",
                            mutableListOf(),
                            mutableListOf(),
                            schoolData.color
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showConditionDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = schoolData.color)
                    ) {
                        Text("Cerrar")
                    }
                },
                containerColor = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }

        if (showQuickRefDialog) {
            Log.d("SpellDetailScreen", "Showing quickref dialog for: '$currentQuickRef'")
            AlertDialog(
                onDismissRequest = { showQuickRefDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = schoolData.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentQuickRef.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineMedium,
                            color = schoolData.color
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        val descriptionText = quickRefDescriptions[currentQuickRef] ?: "No description available."
                        Log.d("SpellDetailScreen", "Quickref description: '$descriptionText'")
                        val description = buildDamageAnnotatedString(
                            descriptionText,
                            mutableListOf(),
                            mutableListOf(),
                            schoolData.color
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showQuickRefDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = schoolData.color)
                    ) {
                        Text("Cerrar")
                    }
                },
                containerColor = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }

        if (showChanceDialog && currentChanceData != null && chanceRollResult != null && chanceIsSuccess != null) {
            AlertDialog(
                onDismissRequest = { showChanceDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Percent, contentDescription = null, tint = schoolData.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resultado de probabilidad",
                            style = MaterialTheme.typography.headlineMedium,
                            color = schoolData.color
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Probabilidad: ${currentChanceData!!.percentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Roll (d100): $chanceRollResult",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (chanceIsSuccess!!) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Resultado: ${if (chanceIsSuccess!!) currentChanceData!!.successMessage else currentChanceData!!.failMessage}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showChanceDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = schoolData.color)
                    ) {
                        Text("Cerrar")
                    }
                },
                containerColor = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }
    }
}

data class SchoolData(
    val name: String,
    val color: Color,
    val icon: ImageVector
)