package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.model.*
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.utils.conditionDescriptions
import com.javier.mappster.utils.sourceMap
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.floor
import kotlin.random.Random

// Componente reutilizable para las tarjetas de detalle
@Composable
fun MonsterDetailCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterDetailScreen(
    monster: Monster,
    navController: NavHostController, isTwoPaneMode: Boolean = false
    ) {
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
            if (!isTwoPaneMode) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = monster.name ?: "Unknown Monster",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = colorScheme.tertiary,
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = colorScheme.tertiary.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = colorScheme.tertiary
                    ),
                    modifier = Modifier.shadow(elevation = 4.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = if (isTwoPaneMode) 16.dp else 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sección: Fuente (Source) debajo del nombre
                monster.source?.let { sourceCode ->
                    val sourceName = sourceMap[sourceCode] ?: "Unknown Source"
                    Text(
                        text = "Source: $sourceName",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            color = colorScheme.tertiary
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Sección: Tamaño, Tipo, Alineamiento y CR
                MonsterInfoSection(monster)

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: HP, AC e Iniciativa
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Velocidad
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterSpeed(monster)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Ability Scores y Saving Throws
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterStats(monster) { label, modifier ->
                        currentDiceData = DiceRollData(numDice = 1, dieSides = 20, bonus = modifier, type = "hit")
                        val (rolls, total) = rollDice(currentDiceData!!)
                        diceRollDetails = rolls
                        diceRollTotal = total
                        showDiceRollDialog = true
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Habilidades
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Resistencias e Inmunidades
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterResistancesAndImmunities(monster)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Sentidos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterSenses(monster)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Lenguajes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterLanguages(monster)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Traits
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterTraits(monster, onDiceRollClick, onConditionClick)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Spellcasting
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterSpellcasting(monster, onConditionClick)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Actions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterActions(monster, onDiceRollClick, onConditionClick)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Bonus Actions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterBonusActions(monster, onDiceRollClick, onConditionClick)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Reactions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterReactions(monster, onDiceRollClick, onConditionClick)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección: Legendary Actions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    MonsterLegendary(monster, onDiceRollClick, onConditionClick)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
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
                        tint = colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Dice Roll",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = CinzelDecorative,
                            color = colorScheme.tertiary,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Roll formula
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
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Breakdown
                    Text(
                        text = "Breakdown:",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = buildDiceRollBreakdown(
                            rolls = diceRollDetails,
                            dieSides = currentDiceData!!.dieSides,
                            bonus = currentDiceData!!.bonus,
                            defaultColor = MaterialTheme.colorScheme.onSurface
                        ).text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.tertiary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.tertiary.copy(alpha = 0.2f))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Total: $diceRollTotal",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.tertiary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDiceRollDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.tertiary
                    ),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colorScheme.tertiary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Close",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = CinzelDecorative
                        )
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.9f),
            tonalElevation = 8.dp,
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = colorScheme.tertiary.copy(alpha = 0.2f)
                )
                .border(
                    width = 1.dp,
                    color = colorScheme.tertiary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                )
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
                        tint = colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currentCondition.replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = CinzelDecorative,
                            color = colorScheme.tertiary,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = conditionDescriptions[currentCondition.lowercase()]
                                ?: "No description available for this condition.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = colorScheme.onSurfaceVariant,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showConditionDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorScheme.tertiary
                        ),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = colorScheme.tertiary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "Close",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = CinzelDecorative
                            )
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.9f),
            tonalElevation = 8.dp,
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = colorScheme.tertiary.copy(alpha = 0.2f)
                )
                .border(
                    width = 1.dp,
                    color = colorScheme.tertiary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                )
        )
    }
}

@Composable
fun MonsterInfoSection(monster: Monster) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Size, Type and Alignment
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sizeText = monster.size?.firstOrNull()?.let { size ->
                        when (size.uppercase()) {
                            "M" -> "MEDIUM"
                            "L" -> "LARGE"
                            "S" -> "SMALL"
                            "T" -> "TINY"
                            "H" -> "HUGE"
                            "G" -> "GARGANTUAN"
                            else -> size.uppercase()
                        }
                    } ?: "UNKNOWN"

                    val typeText = monster.type?.type?.jsonPrimitive?.contentOrNull
                        ?.removeSurrounding("\"")
                        ?.uppercase()
                        ?: "UNKNOWN"

                    val alignmentText = monster.alignment?.joinToString(" ") { align ->
                        when (align.uppercase()) {
                            "L" -> "LAWFUL"
                            "N" -> "NEUTRAL"
                            "C" -> "CHAOTIC"
                            "G" -> "GOOD"
                            "E" -> "EVIL"
                            "A" -> "ANY"
                            else -> align.uppercase()
                        }
                    }?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CinzelDecorative
                                )
                            ) {
                                append("$sizeText ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = CinzelDecorative,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(typeText)
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = CinzelDecorative
                                )
                            ) {
                                append(alignmentText)
                            }
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // Challenge Rating
                monster.cr?.let { cr ->
                    val crText = cr.value ?: "?"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        colorScheme.tertiary.copy(alpha = 0.3f),
                                        colorScheme.tertiary.copy(alpha = 0.1f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = colorScheme.tertiary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "CR: $crText",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = CinzelDecorative,
                                color = colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
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
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.tertiary)) {
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
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.tertiary)) {
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
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.tertiary)) {
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
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.tertiary)) {
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
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.tertiary)) {
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
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.tertiary)) {
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
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.tertiary)) {
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

// Nueva sección para Spellcasting
@Composable
fun MonsterSpellcasting(
    monster: Monster,
    onConditionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (monster.spellcasting.isNullOrEmpty()) {
                    Text(
                        text = "No spellcasting abilities",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                } else {
                    monster.spellcasting.forEachIndexed { index, spellcasting ->
                        if (index > 0) {
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.tertiary.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )
                        }

                        // Spellcasting type name
                        spellcasting.name?.let { name ->
                            Text(
                                text = cleanTraitEntry(name).uppercase(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = CinzelDecorative,
                                    color = colorScheme.tertiary,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Header entries
                        spellcasting.headerEntries?.forEach { entry ->
                            val diceDataList = remember { mutableListOf<DiceRollData>() }
                            val cleanedText = cleanTraitEntry(entry)
                            val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = colorScheme.tertiary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                ClickableText(
                                    text = annotatedText,
                                    onClick = { offset ->
                                        annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                onConditionClick(annotation.item)
                                            }
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                        }

                        // Spells by level
                        spellcasting.spells?.forEach { (level, spellLevel) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colorScheme.tertiary.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "LEVEL $level",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = CinzelDecorative,
                                            color = colorScheme.tertiary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${spellLevel.slots ?: 0} slots)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                            }

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
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(start = 24.dp, bottom = 10.dp)
                                )
                            }
                        }

                        // At will spells
                        spellcasting.will?.let { willSpells ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AT WILL",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = CinzelDecorative,
                                        color = colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

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
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                                )
                            }
                        }

                        // Daily spells
                        spellcasting.daily?.forEach { (frequency, dailySpells) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${frequency.replace("e", "").uppercase()}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = CinzelDecorative,
                                        color = colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

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
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Nueva sección para Legendary Actions
@Composable
fun MonsterLegendary(
    monster: Monster,
    onDiceRollClick: (DiceRollData) -> Unit,
    onConditionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LEGENDARY ACTIONS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (monster.legendary.isNullOrEmpty()) {
                    Text(
                        text = "No legendary actions available",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                } else {
                    monster.legendary.forEach { legendary ->
                        legendary.name?.let { name ->
                            Text(
                                text = cleanTraitEntry(name).uppercase(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = CinzelDecorative,
                                    color = colorScheme.tertiary,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        legendary.entries?.forEach { entry ->
                            val diceDataList = remember { mutableListOf<DiceRollData>() }
                            val cleanedText = cleanTraitEntry(entry.toString())
                            val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = colorScheme.tertiary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                ClickableText(
                                    text = annotatedText,
                                    onClick = { offset ->
                                        annotatedText.getStringAnnotations(tag = "condition", start = offset, end = offset)
                                            .firstOrNull()?.let { annotation ->
                                                onConditionClick(annotation.item)
                                            }
                                        diceDataList.getOrNull(0)?.let { diceData ->
                                            onDiceRollClick(diceData)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 24.sp
                                    )
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
fun MonsterCombatStats(monster: Monster, onModifierClick: (String, Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                Text(
                    text = "COMBAT STATS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Hit Points
                monster.hp?.let { hp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "HIT POINTS: ${hp.average ?: "Unknown"} (${hp.formula ?: "No formula"})",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Initiative
                val initModValue = monster.initiative?.proficiency?.toString()?.toIntOrNull()
                    ?: calculateModifier(monster.dex) ?: 0
                val initMod = formatModifier(initModValue)
                val initAnnotatedText = buildAnnotatedString {
                    append("INITIATIVE: ")
                    pushStringAnnotation(tag = "initModifier", annotation = initModValue.toString())
                    withStyle(
                        style = SpanStyle(
                            fontFamily = CinzelDecorative,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.tertiary,
                            letterSpacing = 0.5.sp
                        )
                    ) {
                        append(initMod)
                    }
                    pop()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    ClickableText(
                        text = initAnnotatedText,
                        onClick = { offset ->
                            initAnnotatedText.getStringAnnotations(tag = "initModifier", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    onModifierClick("Initiative", annotation.item.toInt())
                                }
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = CinzelDecorative
                        )
                    )
                }

                // Armor Class
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ARMOR CLASS: $acText",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACTIONS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (monster.action.isNullOrEmpty()) {
                    Text(
                        text = "No actions available",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                } else {
                    monster.action.forEach { action ->
                        val actionName = cleanTraitEntry(action.name ?: "Unnamed Action")
                        Text(
                            text = actionName.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        action.entries?.forEach { entry ->
                            when (entry) {
                                is JsonPrimitive -> {
                                    val diceDataList = remember { mutableListOf<DiceRollData>() }
                                    val cleanedText = cleanTraitEntry(entry.contentOrNull ?: "Unknown")
                                    val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, bottom = 12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .border(
                                                width = 1.dp,
                                                color = colorScheme.tertiary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 24.sp
                                            )
                                        )
                                    }
                                }
                                is JsonObject -> {
                                    val type = entry["type"]?.jsonPrimitive?.contentOrNull
                                    if (type == "list") {
                                        Text(
                                            text = "LIST:",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = CinzelDecorative,
                                                color = colorScheme.tertiary,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
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

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                                    .padding(8.dp)
                                            ) {
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontStyle = FontStyle.Italic
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Unsupported entry format",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BONUS ACTIONS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (monster.bonus.isNullOrEmpty()) {
                    Text(
                        text = "No bonus actions available",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                } else {
                    monster.bonus.forEach { bonus ->
                        val bonusName = cleanTraitEntry(bonus.name ?: "Unnamed Bonus Action")
                        Text(
                            text = bonusName.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        bonus.entries?.forEach { entry ->
                            when (entry) {
                                is JsonPrimitive -> {
                                    val diceDataList = remember { mutableListOf<DiceRollData>() }
                                    val cleanedText = cleanTraitEntry(entry.contentOrNull ?: "Unknown")
                                    val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, bottom = 12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .border(
                                                width = 1.dp,
                                                color = colorScheme.tertiary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 24.sp
                                            )
                                        )
                                    }
                                }
                                is JsonObject -> {
                                    val type = entry["type"]?.jsonPrimitive?.contentOrNull
                                    if (type == "list") {
                                        Text(
                                            text = "LIST:",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = CinzelDecorative,
                                                color = colorScheme.tertiary,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
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

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                                    .padding(8.dp)
                                            ) {
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontStyle = FontStyle.Italic
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Unsupported entry format",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterSpeed(monster: Monster) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "SPEED",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                var hasSpeedData by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    Column {
                        monster.speed?.let { speed ->
                            speed.walk?.let { walk ->
                                val walkValue = when (walk) {
                                    is JsonPrimitive -> walk.intOrNull?.toString() ?: walk.contentOrNull ?: "0"
                                    is JsonObject -> walk["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                                    else -> "0"
                                }
                                SpeedItem(type = "Walk", value = "$walkValue ft")
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
                                SpeedItem(
                                    type = "Fly",
                                    value = "$flyValue${if (speed.canHover == true) " (hover)" else ""}"
                                )
                                hasSpeedData = true
                            }

                            speed.swim?.let { swim ->
                                val swimValue = when (swim) {
                                    is JsonPrimitive -> swim.intOrNull?.toString() ?: swim.contentOrNull ?: "0"
                                    is JsonObject -> swim["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                                    else -> "0"
                                }
                                SpeedItem(type = "Swim", value = "$swimValue ft")
                                hasSpeedData = true
                            }

                            speed.climb?.let { climb ->
                                val climbValue = when (climb) {
                                    is JsonPrimitive -> climb.intOrNull?.toString() ?: climb.contentOrNull ?: "0"
                                    is JsonObject -> climb["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                                    else -> "0"
                                }
                                SpeedItem(type = "Climb", value = "$climbValue ft")
                                hasSpeedData = true
                            }

                            speed.burrow?.let { burrow ->
                                val burrowValue = when (burrow) {
                                    is JsonPrimitive -> burrow.intOrNull?.toString() ?: burrow.contentOrNull ?: "0"
                                    is JsonObject -> burrow["number"]?.jsonPrimitive?.intOrNull?.toString() ?: "0"
                                    else -> "0"
                                }
                                SpeedItem(type = "Burrow", value = "$burrowValue ft")
                                hasSpeedData = true
                            }

                            speed.alternate?.let { alternate ->
                                alternate.walk?.forEach { walk ->
                                    walk.number?.let { num ->
                                        SpeedItem(
                                            type = "Walk",
                                            value = "$num ft${walk.condition?.let { " ($it)" } ?: ""}"
                                        )
                                        hasSpeedData = true
                                    }
                                }
                                alternate.fly?.forEach { fly ->
                                    fly.number?.let { num ->
                                        SpeedItem(
                                            type = "Fly",
                                            value = "$num ft${fly.condition?.let { " ($it)" } ?: ""}${if (speed.canHover == true) " (hover)" else ""}"
                                        )
                                        hasSpeedData = true
                                    }
                                }
                                alternate.climb?.forEach { climb ->
                                    climb.number?.let { num ->
                                        SpeedItem(
                                            type = "Climb",
                                            value = "$num ft${climb.condition?.let { " ($it)" } ?: ""}"
                                        )
                                        hasSpeedData = true
                                    }
                                }
                            }

                            speed.choose?.let { choose ->
                                choose.from?.forEach { speedType ->
                                    SpeedItem(
                                        type = speedType.replaceFirstChar { it.uppercase() },
                                        value = "${choose.amount ?: 0} ft${choose.note?.let { " ($it)" } ?: ""}"
                                    )
                                    hasSpeedData = true
                                }
                            }
                        }

                        if (!hasSpeedData) {
                            Text(
                                text = "No speed data available",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedItem(type: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(80.dp)) {
            Text(
                text = "$type:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = CinzelDecorative,
                    color = colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun MonsterStats(monster: Monster, onModifierClick: (String, Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ABILITY SCORES & SAVES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(16.dp)
                ) {
                    Column {
                        // First row: STR, DEX, CON
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn(
                                label = "STR",
                                abilityValue = monster.str,
                                saveValue = monster.save?.str?.trim(),
                                saveModifier = monster.save?.str?.trim()?.removePrefix("+")?.toIntOrNull()
                                    ?: calculateModifier(monster.str) ?: 0,
                                isSaveFromBase = monster.save?.str == null,
                                extraModifier = calculateModifier(monster.str) ?: 0,
                                onModifierClick = onModifierClick
                            )
                            StatColumn(
                                label = "DEX",
                                abilityValue = monster.dex,
                                saveValue = monster.save?.dex?.trim(),
                                saveModifier = monster.save?.dex?.trim()?.removePrefix("+")?.toIntOrNull()
                                    ?: calculateModifier(monster.dex) ?: 0,
                                isSaveFromBase = monster.save?.dex == null,
                                extraModifier = calculateModifier(monster.dex) ?: 0,
                                onModifierClick = onModifierClick
                            )
                            StatColumn(
                                label = "CON",
                                abilityValue = monster.con,
                                saveValue = monster.save?.con?.trim(),
                                saveModifier = monster.save?.con?.trim()?.removePrefix("+")?.toIntOrNull()
                                    ?: calculateModifier(monster.con) ?: 0,
                                isSaveFromBase = monster.save?.con == null,
                                extraModifier = calculateModifier(monster.con) ?: 0,
                                onModifierClick = onModifierClick
                            )
                        }

                        // Second row: INT, WIS, CHA
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn(
                                label = "INT",
                                abilityValue = monster.int,
                                saveValue = monster.save?.int?.trim(),
                                saveModifier = monster.save?.int?.trim()?.removePrefix("+")?.toIntOrNull()
                                    ?: calculateModifier(monster.int) ?: 0,
                                isSaveFromBase = monster.save?.int == null,
                                extraModifier = calculateModifier(monster.int) ?: 0,
                                onModifierClick = onModifierClick
                            )
                            StatColumn(
                                label = "WIS",
                                abilityValue = monster.wis,
                                saveValue = monster.save?.wis?.trim(),
                                saveModifier = monster.save?.wis?.trim()?.removePrefix("+")?.toIntOrNull()
                                    ?: calculateModifier(monster.wis) ?: 0,
                                isSaveFromBase = monster.save?.wis == null,
                                extraModifier = calculateModifier(monster.wis) ?: 0,
                                onModifierClick = onModifierClick
                            )
                            StatColumn(
                                label = "CHA",
                                abilityValue = monster.cha,
                                saveValue = monster.save?.cha?.trim(),
                                saveModifier = monster.save?.cha?.trim()?.removePrefix("+")?.toIntOrNull()
                                    ?: calculateModifier(monster.cha) ?: 0,
                                isSaveFromBase = monster.save?.cha == null,
                                extraModifier = calculateModifier(monster.cha) ?: 0,
                                onModifierClick = onModifierClick
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun StatColumn(
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
        modifier = Modifier.width(100.dp) // Aumenté un poco el ancho para acomodar textos más grandes
    ) {
        // Ability Score - Más grande y destacado
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = CinzelDecorative,
                color = colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
        Text(
            text = abilityValue?.toString() ?: "--",
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Save Modifier - Texto más grande
        val saveAnnotatedText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append("SAVE: ")
            }
            pushStringAnnotation(tag = "saveModifier", annotation = saveModifier.toString())
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isSaveFromBase) MaterialTheme.colorScheme.onSurfaceVariant
                    else colorScheme.tertiary
                )
            ) {
                append(formatModifier(saveModifier))
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
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        // Ability Modifier - Texto más grande
        val abilityAnnotatedText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append("MOD: ")
            }
            pushStringAnnotation(tag = "abilityModifier", annotation = extraModifier.toString())
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorScheme.tertiary
                )
            ) {
                append(formatModifier(extraModifier))
            }
            pop()
        }

        ClickableText(
            text = abilityAnnotatedText,
            onClick = { offset ->
                abilityAnnotatedText.getStringAnnotations(tag = "abilityModifier", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        onModifierClick(label, annotation.item.toInt())
                    }
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
fun MonsterSkills(monster: Monster, onSkillClick: (String, Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SKILLS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(16.dp)
                ) {
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
                        Column {
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
                                        val cleanedValue = value.removePrefix("+").trim()
                                        val modifier = cleanedValue.toIntOrNull() ?: 0
                                        val skillAnnotatedText = buildAnnotatedString {
                                            append("$label: ")
                                            pushStringAnnotation(tag = "skillModifier", annotation = modifier.toString())
                                            withStyle(
                                                style = SpanStyle(
                                                    fontFamily = CinzelDecorative,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    letterSpacing = 0.5.sp
                                                )
                                            ) {
                                                append(formatModifier(modifier))
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 18.sp
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
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
                                                val cleanedValue = value.removePrefix("+").trim()
                                                val modifier = cleanedValue.toIntOrNull() ?: 0
                                                val skillAnnotatedText = buildAnnotatedString {
                                                    append("$label: ")
                                                    pushStringAnnotation(tag = "skillModifier", annotation = modifier.toString())
                                                    withStyle(
                                                        style = SpanStyle(
                                                            fontFamily = CinzelDecorative,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 20.sp,
                                                            color = MaterialTheme.colorScheme.tertiary,
                                                            letterSpacing = 0.5.sp
                                                        )
                                                    ) {
                                                        append(formatModifier(modifier))
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 18.sp
                                                    ),
                                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No skills available",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterResistancesAndImmunities(monster: Monster) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RESISTANCES & IMMUNITIES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                var hasResistances = false
                monster.resist?.let { resists ->
                    Text(
                        text = "RESISTS:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = CinzelDecorative,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    resists.forEach { resist ->
                        resist.resist?.let { entries ->
                            entries.joinToString(", ") { it.value?.replaceFirstChar { it.uppercase() } ?: "Unknown" }
                                .let { resistText ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, bottom = 8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "$resistText${resist.note?.let { " ($it)" } ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        hasResistances = true
                                    }
                                }
                        } ?: resist.special?.let { special ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = special.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                hasResistances = true
                            }
                        }
                    }
                }

                var hasImmunities = false
                monster.immune?.let { immunities ->
                    Text(
                        text = "IMMUNE TO:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = CinzelDecorative,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    immunities.forEach { immune ->
                        when (immune) {
                            is JsonPrimitive -> {
                                val immuneText = immune.contentOrNull?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, bottom = 8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = immuneText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    hasImmunities = true
                                }
                            }
                            is JsonObject -> {
                                val immuneList = immune["immune"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                                val note = immune["note"]?.jsonPrimitive?.contentOrNull
                                immuneList?.joinToString(", ") { it.replaceFirstChar { it.uppercase() } }?.let { immuneText ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, bottom = 8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "$immuneText${note?.let { " ($it)" } ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        hasImmunities = true
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }

                if (!hasResistances && !hasImmunities) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "No resistances or immunities available",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterSenses(monster: Monster) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SENSES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                monster.senses?.let { senses ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = senses.joinToString(", ") { it.replaceFirstChar { it.uppercase() } },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "No senses available",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }

                monster.passive?.let { passive ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        val passiveValue = when (passive) {
                            is Passive.Value -> passive.value
                            is Passive.Formula -> passive.formula
                        }
                        Text(
                            text = "Passive Perception: $passiveValue",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterLanguages(monster: Monster) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LANGUAGES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    monster.languages?.let { languages ->
                        Text(
                            text = languages.joinToString(", ").ifEmpty { "No languages available" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    } ?: run {
                        Text(
                            text = "No languages available",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRAITS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (monster.trait.isNullOrEmpty()) {
                    Text(
                        text = "No traits available",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    monster.trait.forEach { trait ->
                        val traitName = cleanTraitEntry(trait.name ?: "Unknown Trait")
                        Text(
                            text = traitName.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        trait.entries?.forEach { entry ->
                            when (entry) {
                                is JsonPrimitive -> {
                                    val diceDataList = remember { mutableListOf<DiceRollData>() }
                                    val cleanedText = cleanTraitEntry(entry.contentOrNull ?: "Unknown")
                                    val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, bottom = 12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 24.sp
                                            )
                                        )
                                    }
                                }
                                is JsonObject -> {
                                    val type = entry["type"]?.jsonPrimitive?.contentOrNull
                                    if (type == "list") {
                                        Text(
                                            text = "LIST:",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = CinzelDecorative,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
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

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                                    .padding(8.dp)
                                            ) {
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Unsupported entry type: $type",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontStyle = FontStyle.Italic
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Unsupported entry format",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REACTIONS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (monster.reaction.isNullOrEmpty()) {
                    Text(
                        text = "No reactions available",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    monster.reaction.forEach { reaction ->
                        val reactionName = cleanTraitEntry(reaction.name ?: "Unnamed Reaction")
                        Text(
                            text = reactionName.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        reaction.entries?.forEach { entry ->
                            when (entry) {
                                is JsonPrimitive -> {
                                    val diceDataList = remember { mutableListOf<DiceRollData>() }
                                    val cleanedText = cleanTraitEntry(entry.contentOrNull ?: "Unknown")
                                    val annotatedText = buildDamageAnnotatedString(cleanedText, diceDataList)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, bottom = 12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 24.sp
                                            )
                                        )
                                    }
                                }
                                is JsonObject -> {
                                    val type = entry["type"]?.jsonPrimitive?.contentOrNull
                                    if (type == "list") {
                                        Text(
                                            text = "LIST:",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = CinzelDecorative,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
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

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                                    .padding(8.dp)
                                            ) {
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontStyle = FontStyle.Italic
                                            ),
                                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Unsupported entry format",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}