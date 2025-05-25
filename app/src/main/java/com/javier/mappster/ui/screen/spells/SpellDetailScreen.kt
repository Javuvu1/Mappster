package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.javier.mappster.model.*

// Mapa de condiciones y sus descripciones
private val conditionDescriptions = mapOf(
    "charmed" to "A charmed creature can't attack the charmer or target the charmer with harmful abilities or magical effects.\nThe charmer has advantage on any ability check to interact socially with the creature",
    "unconscious" to "An unconscious creature is incapacitated, can't move or speak, and is unaware of its surroundings.\nThe creature drops whatever it's holding and falls prone.\nThe creature automatically fails Strength and Dexterity saving throws.\nAttack rolls against the creature have advantage.\nAny attack that hits the creature is a critical hit if the attacker is within 5 feet of the creature.",
    "frightened" to "A frightened creature has disadvantage on ability checks and attack rolls while the source of its fear is within line of sight.\nThe creature can't willingly move closer to the source of its fear.",
    "restrained" to "A restrained creature's speed becomes 0, and it can't benefit from any bonus to its speed.\nAttack rolls against the creature have advantage, and the creature's attack rolls have disadvantage.\nThe creature has disadvantage on Dexterity saving throws.",
    "petrified" to "A petrified creature is transformed, along with any nonmagical object it is wearing or carrying, into a solid inanimate substance (usually stone). Its weight increases by a factor of ten, and it ceases aging.\nThe creature is incapacitated, can't move or speak, and is unaware of its surroundings.\nAttack rolls against the creature have advantage.\nThe creature automatically fails Strength and Dexterity saving throws.\nThe creature has resistance to all damage.\nThe creature is immune to poison and disease, although a poison or disease already in its system is suspended, not neutralized.",
    "blinded" to "A blinded creature can't see and automatically fails any ability check that requires sight.\nAttack rolls against the creature have advantage, and the creature's attack rolls have disadvantage.",
    "deafened" to "A deafened creature can't hear and automatically fails any ability check that requires hearing.",
    "poisoned" to "A poisoned creature has disadvantage on attack rolls and ability checks",
    "paralyzed" to "A paralyzed creature is incapacitated and can't move or speak.\nThe creature automatically fails Strength and Dexterity saving throws.\nAttack rolls against the creature have advantage.\nAny attack that hits the creature is a critical hit if the attacker is within 5 feet of the creature.",
    "stunned" to "A stunned creature is incapacitated, can't move, and can speak only falteringly.\nThe creature automatically fails Strength and Dexterity saving throws.\nAttack rolls against the creature have advantage.",
    "incapacitated" to "An incapacitated creature can't take actions or reactions.",
    "invisible" to "An invisible creature is impossible to see without the aid of magic or a special sense. For the purpose of hiding, the creature is heavily obscured. The creature's location can be detected by any noise it makes or any tracks it leaves.\nAttack rolls against the creature have disadvantage, and the creature's attack rolls have advantage.",
    "prone" to "A prone creature's only movement option is to crawl, unless it stands up and thereby ends the condition.\nThe creature has disadvantage on attack rolls.\nAn attack roll against the creature has advantage if the attacker is within 5 feet of the creature. Otherwise, the attack roll has disadvantage.",
    "grappled" to "A grappled creature's speed becomes 0, and it can't benefit from any bonus to its speed.\nThe condition ends if the grappler is incapacitated.\nThe condition also ends if an effect removes the grappled creature from the reach of the grappler or grappling effect, such as when a creature is hurled away by the thunderwave spell.",
    "exhaustion" to "Some special abilities and environmental hazards, such as starvation and the long-term effects of freezing or scorching temperatures, can lead to a special condition called exhaustion. Exhaustion is measured in six levels. An effect can give a creature one or more levels of exhaustion, as specified in the effect's description.\n1: Disadvantage on ability checks\n2: Speed halved\n3: Disadvantage on attack rolls and saving throws\n4: Hit point maximum halved\n5: Speed reduced to 0\n6: Death\nIf an already exhausted creature suffers another effect that causes exhaustion, its current level of exhaustion increases by the amount specified in the effect's description.\nA creature suffers the effect of its current level of exhaustion as well as all lower levels. For example, a creature suffering level 2 exhaustion has its speed halved and has disadvantage on ability checks.\nAn effect that removes exhaustion reduces its level as specified in the effect's description, with all exhaustion effects ending if a creature's exhaustion level is reduced below 1.\nFinishing a long rest reduces a creature's exhaustion level by 1, provided that the creature has also ingested some food and drink. Also, being raised from the dead reduces a creature's exhaustion level by 1."
)

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
    val type: String = "damage" // "damage", "dice", "scaledice", "scaledamage"
)

// Construye un AnnotatedString con partes clicables para los daños, dados y condiciones
fun buildDamageAnnotatedString(
    text: String,
    diceDataList: MutableList<DiceRollData>
): AnnotatedString {
    // Patrones para detectar {@damage}, {@dice}, {@scaledice}, {@scaledamage}, y {@condition}
    val pattern = Regex(
        "\\{@damage\\s*(\\d+d\\d+\\s*\\+\\s*\\d+|\\d+d\\d+)\\}|" + // Para {@damage 1d4+1} o {@damage 8d8}
                "\\{@dice\\s*(\\d*d\\d+)\\}|" + // Para {@dice 2d6} o {@dice d20}
                "\\{@scaledice\\s*(\\d+d\\d+)(?:\\|.*?)*\\}|" + // Para {@scaledice 1d8|1-9|1d8}
                "\\{@scaledamage\\s*(\\d+d\\d+)(?:\\|.*?)*\\}|" + // Para {@scaledamage 3d6|1-9|1d6}
                "\\{@condition\\s*(charmed|unconscious|frightened|restrained|petrified|blinded|deafened|poisoned|paralyzed|stunned|incapacitated|invisible|prone|grappled|exhaustion|deafened\\|\\|deaf|blinded\\|\\|blind)\\}" // Para {@condition <estado>}
    )
    val matches = pattern.findAll(text).toList()
    diceDataList.clear()

    matches.forEach { match ->
        if (match.value.startsWith("{@damage")) {
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
        } else if (match.value.startsWith("{@dice")) {
            val dice = match.groupValues[2].replace("\\s+.toRegex()", "")
            val parts = dice.split("d")
            val numDice = if (parts[0].isEmpty()) 1 else parts[0].toInt()
            val dieSides = parts[1].toInt()
            diceDataList.add(DiceRollData(numDice, dieSides, type = "dice"))
        } else if (match.value.startsWith("{@scaledice")) {
            val dice = match.groupValues[3].replace("\\s+.toRegex()", "")
            val (numDice, dieSides) = dice.split("d").map { it.toInt() }
            diceDataList.add(DiceRollData(numDice, dieSides, type = "scaledice"))
        } else if (match.value.startsWith("{@scaledamage")) {
            val dice = match.groupValues[4].replace("\\s+.toRegex()", "")
            val (numDice, dieSides) = dice.split("d").map { it.toInt() }
            diceDataList.add(DiceRollData(numDice, dieSides, type = "scaledamage"))
        } else if (match.value.startsWith("{@condition")) {
            // No añadimos a diceDataList, solo procesamos el texto de la condición
        }
    }

    return buildAnnotatedString {
        var lastIndex = 0
        matches.forEachIndexed { index, matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            // Determinar el texto a mostrar
            val displayText = when {
                matchResult.value.startsWith("{@damage") -> matchResult.groupValues[1].replace("\\s+.toRegex()", "")
                matchResult.value.startsWith("{@dice") -> matchResult.groupValues[2].replace("\\s+.toRegex()", "")
                matchResult.value.startsWith("{@scaledice") -> matchResult.groupValues[3].replace("\\s+.toRegex()", "")
                matchResult.value.startsWith("{@scaledamage") -> matchResult.groupValues[4].replace("\\s+.toRegex()", "")
                matchResult.value.startsWith("{@condition") -> {
                    val condition = matchResult.groupValues[5]
                    when (condition) {
                        "deafened||deaf" -> "deafened"
                        "blinded||blind" -> "blinded"
                        else -> condition
                    }
                }
                else -> ""
            }

            append(text.substring(lastIndex, start))
            if (matchResult.value.startsWith("{@condition")) {
                pushStringAnnotation(tag = "condition", annotation = matchResult.groupValues[5]) // Almacena el estado original
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(displayText)
                }
                pop()
            } else {
                pushStringAnnotation(tag = "damage", annotation = index.toString())
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(displayText)
                }
                pop()
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
        // Escala el número de dados según el nivel del slot (nivel del slot - 1)
        val levelAdjustment = maxOf(0, slotLevel - 1)
        baseNumDice * levelAdjustment
    } else {
        baseNumDice
    }
    val rolls = (1..scaledNumDice).map { (1..dice.dieSides).random() }
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
fun SpellDetailScreen(spell: Spell) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = spell.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = schoolData.color,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    schoolData.color.copy(alpha = 0.3f)
                )
            )
        )
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp)) // Separación adicional desde la TopAppBar
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
                            if (spell.components.m != null) add("M (un poco de esponja)")
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
                            val annotatedText = remember(entry) {
                                buildDamageAnnotatedString(entry, diceDataList)
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
                        val classList = spell.classes.fromClassList.joinToString { it.name }
                        val subclassList = spell.classes.fromSubclass.joinToString { "${it.classEntry.name}: ${it.subclass.name}" }
                        Text(
                            text = "Clases: ${if (classList.isNotEmpty()) classList else "Ninguna"}, ${if (subclassList.isNotEmpty()) subclassList else "Ninguna"}",
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
                                    val annotatedText = remember(entry) {
                                        buildDamageAnnotatedString(entry, diceDataList)
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

        // Diálogo para mostrar el resultado de la tirada con desglose
        if (showDiceRollDialog && currentDiceData != null) {
            AlertDialog(
                onDismissRequest = { showDiceRollDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Casino, contentDescription = null, tint = schoolData.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resultado de la tirada", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tirada: ${currentDiceData!!.numDice}d${currentDiceData!!.dieSides}" +
                                    (currentDiceData!!.bonus?.let { " + $it" } ?: ""),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Total: $diceRollTotal",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
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
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            )
        }

        // Diálogo para mostrar la descripción de la condición con desplazamiento
        if (showConditionDialog) {
            AlertDialog(
                onDismissRequest = { showConditionDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = schoolData.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentCondition.replace("||", " or ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        item {
                            val description = buildDamageAnnotatedString(
                                conditionDescriptions[currentCondition] ?: "No description available",
                                mutableListOf()
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            )
        }
    }
}

data class SchoolData(
    val name: String,
    val color: Color,
    val icon: ImageVector
)