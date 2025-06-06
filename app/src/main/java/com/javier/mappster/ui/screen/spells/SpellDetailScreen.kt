package com.javier.mappster.ui.screen.spells

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
import androidx.navigation.NavHostController
import com.javier.mappster.model.*
import com.javier.mappster.utils.conditionDescriptions
import com.javier.mappster.utils.sourceMap
import java.net.URLEncoder
import kotlin.random.Random

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

// Datos de la tirada de dados
data class DiceRollData(
    val numDice: Int,
    val dieSides: Int,
    val bonus: Int? = null,
    val type: String = "damage", // "damage", "dice", "scaledice", "scaledamage"
    val scaledDie: String? = null, // Último valor de los dados escalados (ej. "1d6")
    val levelRange: String? = null // Rango de niveles para escalar (ej. "3-9")
)

// Datos para {@chance}
data class ChanceData(
    val percentage: Int,
    val failMessage: String,
    val successMessage: String
)

// Construye un AnnotatedString con partes clicables para los daños, dados, condiciones, hechizos y chances
fun buildDamageAnnotatedString(
    text: String,
    diceDataList: MutableList<DiceRollData>,
    chanceDataList: MutableList<ChanceData>,
    schoolColor: Color
): AnnotatedString {
    // Patrones para detectar {@damage}, {@dice}, {@scaledice}, {@scaledamage}, {@condition}, {@spell}, y {@chance}
    val pattern = Regex(
        "\\{@damage\\s*(\\d+d\\d+\\s*\\+\\s*\\d+|\\d+d\\d+)\\}|" + // Para {@damage 1d4+1} o {@damage 8d8}
                "\\{@dice\\s*(\\d*d\\d+)\\}|" + // Para {@dice 2d6} o {@dice d20}
                "\\{@scaledice\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" + // Para {@scaledice 8d6|3-9|1d6}
                "\\{@scaledamage\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" + // Para {@scaledamage 8d6|3-9|1d6}
                "\\{@condition\\s*(charmed|unconscious|frightened|restrained|petrified|blinded|deafened|poisoned|paralyzed|stunned|incapacitated|invisible|prone|grappled|exhaustion|deafened\\|\\|deaf|blinded\\|\\|blind)\\}|" + // Para {@condition <estado>}
                "\\{@spell\\s*([^\\}]+)\\}|" + // Para {@spell magic missile}
                "\\{@chance\\s*(\\d+)\\s*\\|\\|\\|\\s*([^\\|]+)\\|([^\\}]+)\\}" // Para {@chance 5|||Message lost!|Message arrives}
    )
    val matches = pattern.findAll(text).toList()
    diceDataList.clear()
    chanceDataList.clear()

    return buildAnnotatedString {
        var lastIndex = 0
        matches.forEachIndexed { index, match ->
            val start = match.range.first
            val end = match.range.last + 1

            // Determinar el texto a mostrar
            val displayText = when {
                match.value.startsWith("{@damage") -> {
                    val damage = match.groupValues[1].replace("\\s+".toRegex(), "")
                    val parts = damage.split("+")
                    if (parts.size == 1) {
                        val (numDice, dieSides) = parts[0].split("d").map { it.toInt() }
                        diceDataList.add(DiceRollData(numDice, dieSides, type = "damage"))
                    } else {
                        val (dicePart, bonusPart) = parts
                        val (numDice, dieSides) = dicePart.split("d").map { it.toInt() }
                        val bonus = bonusPart.toInt()
                        diceDataList.add(DiceRollData(numDice, dieSides, bonus, "damage"))
                    }
                    damage
                }
                match.value.startsWith("{@dice") -> {
                    val dice = match.groupValues[2].replace("\\s+".toRegex(), "")
                    val parts = dice.split("d")
                    val numDice = if (parts[0].isEmpty()) 1 else parts[0].toInt()
                    val dieSides = parts[1].toInt()
                    diceDataList.add(DiceRollData(numDice, dieSides, type = "dice"))
                    dice
                }
                match.value.startsWith("{@scaledice") -> {
                    val initialDice = match.groupValues[3].replace("\\s+".toRegex(), "")
                    val levelRange = match.groupValues[4]
                    val scaledDie = match.groupValues[5].replace("\\s+".toRegex(), "")
                    val (scaledNumDice, scaledDieSides) = scaledDie.split("d").map { it.toInt() }
                    diceDataList.add(DiceRollData(scaledNumDice, scaledDieSides, type = "scaledice", scaledDie = scaledDie, levelRange = levelRange))
                    scaledDie
                }
                match.value.startsWith("{@scaledamage") -> {
                    val initialDice = match.groupValues[6].replace("\\s+".toRegex(), "")
                    val levelRange = match.groupValues[7]
                    val scaledDie = match.groupValues[8].replace("\\s+".toRegex(), "")
                    val (numDice, dieSides) = scaledDie.split("d").map { it.toInt() }
                    diceDataList.add(DiceRollData(numDice, dieSides, type = "scaledamage", scaledDie = scaledDie, levelRange = levelRange))
                    scaledDie
                }
                match.value.startsWith("{@condition") -> {
                    val condition = match.groupValues[9]
                    when (condition) {
                        "deafened||deaf" -> "deafened"
                        "blinded||blind" -> "blinded"
                        else -> condition
                    }
                }
                match.value.startsWith("{@spell") -> {
                    val spellName = match.groupValues[10].trim()
                    Log.d("SpellDetailScreen", "Parsed spell: '$spellName' from ${match.value}")
                    spellName
                }
                match.value.startsWith("{@chance") -> {
                    val percentage = match.groupValues[11].toInt()
                    val failMessage = match.groupValues[12].trim()
                    val successMessage = match.groupValues[13].trim()
                    chanceDataList.add(ChanceData(percentage, failMessage, successMessage))
                    Log.d("SpellDetailScreen", "Parsed chance: $percentage%, fail='$failMessage', success='$successMessage'")
                    "$percentage%"
                }
                else -> ""
            }

            append(text.substring(lastIndex, start))
            when {
                match.value.startsWith("{@condition") -> {
                    pushStringAnnotation(tag = "condition", annotation = match.groupValues[9])
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@damage") || match.value.startsWith("{@dice") ||
                        match.value.startsWith("{@scaledice") || match.value.startsWith("{@scaledamage") -> {
                    pushStringAnnotation(tag = "damage", annotation = index.toString())
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@spell") -> {
                    pushStringAnnotation(tag = "spell", annotation = displayText)
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
                match.value.startsWith("{@chance") -> {
                    pushStringAnnotation(tag = "chance", annotation = index.toString())
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = schoolColor)) {
                        append(displayText)
                    }
                    pop()
                }
            }
            lastIndex = end
        }
        append(text.substring(lastIndex))
    }
}

// Función para realizar la tirada de dados y devolver un desglose
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

// Función para realizar una tirada d100 para {@chance}
fun rollChance(percentage: Int): Pair<Int, Boolean> {
    val roll = Random.nextInt(1, 101) // Tirada de 1 a 100
    val isSuccess = roll <= percentage
    Log.d("SpellDetailScreen", "Chance roll: $roll, percentage: $percentage, success: $isSuccess")
    return roll to isSuccess
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
                roll == dieSides -> Color.Green // Máximo
                roll == 1 -> Color.Red // Mínimo
                else -> defaultColor // Normal
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
        else -> SchoolData(spell.school, Color.Gray, Icons.Default.AutoFixHigh)
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

    Scaffold(
        topBar = {
            if (!isTwoPaneMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = spell.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = schoolData.color,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                            if (spell.components.m != null) add("M (un componente de material)")
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
                            val diceDataList = remember { mutableListOf<DiceRollData>() }
                            val chanceDataList = remember { mutableListOf<ChanceData>() }
                            val annotatedText = remember(entry) {
                                buildDamageAnnotatedString(entry, diceDataList, chanceDataList, schoolData.color)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Spacer(modifier = Modifier.width(12.dp))
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
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
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
                        val classList = spell.classes?.fromClassList?.joinToString { it.name } ?: "Ninguna"
                        val subclassList = spell.classes?.fromSubclass?.joinToString { "${it.classEntry.name}: ${it.subclass.name}" } ?: "Ninguna"
                        Text(
                            text = "Clases: $classList, $subclassList",
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
                                    val diceDataList = remember { mutableListOf<DiceRollData>() }
                                    val chanceDataList = remember { mutableListOf<ChanceData>() }
                                    val annotatedText = remember(entry) {
                                        buildDamageAnnotatedString(entry, diceDataList, chanceDataList, schoolData.color)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Spacer(modifier = Modifier.width(12.dp))
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
                                            },
                                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
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

        // Diálogo para mostrar el resultado de la tirada de dados
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
                            color = MaterialTheme.colorScheme.onSurface
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
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                schoolData.color,
                                MaterialTheme.colorScheme.background
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
                            .padding(10.dp)
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
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                schoolData.color,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(10.dp)
            )
        }

        // Diálogo para mostrar el resultado de la tirada de {@chance}
        if (showChanceDialog && currentChanceData != null && chanceRollResult != null && chanceIsSuccess != null) {
            val chanceData = currentChanceData!!
            val isSuccess = chanceIsSuccess!!
            val rollResult = chanceIsSuccess!!

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
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Probabilidad: ${chanceData.percentage}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Roll (d100): $rollResult",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSuccess) Color.Green else Color.Red
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Resultado: ${if (isSuccess) chanceData.successMessage else chanceData.failMessage}",
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
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                schoolData.color,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(10.dp)
            )
        }
    }
}

data class SchoolData(
    val name: String,
    val color: Color,
    val icon: ImageVector
)