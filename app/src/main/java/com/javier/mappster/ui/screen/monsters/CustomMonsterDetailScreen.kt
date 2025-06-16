package com.javier.mappster.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.CustomMonster
import androidx.compose.ui.platform.LocalContext
import com.javier.mappster.ui.theme.CinzelDecorative

import com.javier.mappster.ui.theme.MappsterTheme

import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMonsterDetailScreen(navController: NavHostController, monsterId: String, isTwoPaneMode: Boolean = false) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    var customMonster by remember { mutableStateOf<CustomMonster?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(monsterId) {
        coroutineScope.launch {
            try {
                val userId = authManager.getCurrentUserId()
                if (userId != null) {
                    val monsters = firestoreManager.getCustomMonsters(userId)
                    customMonster = monsters.find { it.id == monsterId }
                    isLoading = false
                    if (customMonster == null) {
                        errorMessage = "Monstruo no encontrado o no tienes acceso."
                    }
                } else {
                    errorMessage = "Debes iniciar sesión para ver este monstruo."
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar el monstruo: ${e.message}"
                isLoading = false
            }
        }
    }

    MappsterTheme {
        val colorScheme = MaterialTheme.colorScheme
        Scaffold(
            topBar = {
                if (!isTwoPaneMode) {
                    TopAppBar(
                        title = {
                            Text(
                                text = customMonster?.name ?: "Unknown Monster",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    fontFamily = CinzelDecorative,
                                    letterSpacing = 0.5.sp
                                ),
                                color = colorScheme.onSurface
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            titleContentColor = colorScheme.onSurface,
                            navigationIconContentColor = colorScheme.onSurface
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                colorScheme.surface.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.errorContainer.copy(alpha = 0.8f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                        )
                    }
                } else if (customMonster != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = if (isTwoPaneMode) 16.dp else 16.dp)
                            .padding(top = if (!isTwoPaneMode) 0.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            customMonster?.source?.let { source ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Source: $source",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            fontSize = 16.sp
                                        ),
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        item { MonsterInfoSection(customMonster!!) }
                        item { MonsterCombatStats(customMonster!!) }
                        item { MonsterSpeedSection(customMonster!!) }
                        item { MonsterStats(customMonster!!) }
                        item { MonsterSkillsSection(customMonster!!) }
                        item { MonsterSensesSection(customMonster!!) }
                        item { MonsterLanguagesSection(customMonster!!) }
                        item { MonsterResistancesImmunitiesSection(customMonster!!) }
                        item { MonsterTraitsSection(customMonster!!) }
                        if (customMonster?.spellcasting?.firstOrNull() != null) {
                            item { SpellcastingDetailSection(customMonster!!) }
                        }
                        item { MonsterActionsSection(customMonster!!) }
                        item { MonsterBonusActionsSection(customMonster!!) }
                        item { MonsterReactionsSection(customMonster!!) }
                        item { MonsterLegendaryActionsSection(customMonster!!) }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.errorContainer.copy(alpha = 0.8f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Monstruo no encontrado",
                            color = colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterCard(content: @Composable ColumnScope.() -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
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
                            colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun MonsterInfoSection(monster: CustomMonster) {
    MonsterCard {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeText = monster.size ?: "Medium"
                val typeText = monster.type?.joinToString(", ")?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                val alignmentText = monster.alignment?.let { ", $it" } ?: ""

                Text(
                    text = "$sizeText $typeText$alignmentText",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                monster.cr?.let { cr ->
                    Text(
                        text = "CR: $cr",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonsterCombatStats(monster: CustomMonster) {
    MonsterCard {
        monster.hp?.let { hp ->
            Text(
                text = "Hit Points: $hp",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        monster.initiative?.let { init ->
            val initMod = if (init >= 0) "+$init" else "$init"
            Text(
                text = "Initiative: $initMod",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        monster.ac?.let { ac ->
            Text(
                text = "Armor Class: $ac",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MonsterSpeedSection(monster: CustomMonster) {
    val speed = monster.speed ?: emptyMap()
    if (speed.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "Speed:",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            speed.entries.sortedBy { it.key }.forEach { entry ->
                Text(
                    text = "${entry.key.replaceFirstChar { it.uppercase() }}: ${entry.value} ft.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MonsterStats(monster: CustomMonster) {
    fun calculateModifier(score: Int?): Int {
        return score?.let { (it - 10) / 2 } ?: 0
    }

    MonsterCard {
        Text(
            text = "ABILITY SCORES & SAVES",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.tertiary,
                fontFamily = CinzelDecorative,
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
                        saveValue = monster.saves?.get("str"),
                        modifier = calculateModifier(monster.str)
                    )
                    StatColumn(
                        label = "DEX",
                        abilityValue = monster.dex,
                        saveValue = monster.saves?.get("dex"),
                        modifier = calculateModifier(monster.dex)
                    )
                    StatColumn(
                        label = "CON",
                        abilityValue = monster.con,
                        saveValue = monster.saves?.get("con"),
                        modifier = calculateModifier(monster.con)
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
                        saveValue = monster.saves?.get("int"),
                        modifier = calculateModifier(monster.int)
                    )
                    StatColumn(
                        label = "WIS",
                        abilityValue = monster.wis,
                        saveValue = monster.saves?.get("wis"),
                        modifier = calculateModifier(monster.wis)
                    )
                    StatColumn(
                        label = "CHA",
                        abilityValue = monster.cha,
                        saveValue = monster.saves?.get("cha"),
                        modifier = calculateModifier(monster.cha)
                    )
                }
            }
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    abilityValue: Int?,
    saveValue: String?,
    modifier: Int
) {
    val textSize = 20.sp
    val modifierTextSize = 22.sp
    val fontWeight = FontWeight.SemiBold

    val saveDisplayText = saveValue ?: if (modifier >= 0) "+$modifier" else "$modifier"
    val modifierText = if (modifier >= 0) "+$modifier" else "$modifier"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = textSize,
                color = MaterialTheme.colorScheme.tertiary,
                fontFamily = CinzelDecorative
            )
        )
        Text(
            text = abilityValue?.toString() ?: "–",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = "SAVE: $saveDisplayText",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = modifierTextSize,
                color = MaterialTheme.colorScheme.tertiary
            )
        )
        Text(
            text = "MOD: $modifierText",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = modifierTextSize,
                color = MaterialTheme.colorScheme.tertiary
            )
        )
    }
}

@Composable
fun MonsterSkillsSection(monster: CustomMonster) {
    val skills = monster.skills ?: emptyMap()
    var showRollDialog by remember { mutableStateOf(false) }
    var rollResult by remember { mutableStateOf<Pair<String, Int>?>(null) }

    if (skills.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "SKILLS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(12.dp)
            ) {
                skills.entries.sortedBy { it.key }.forEach { skill ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val bonus = skill.value.replace("[^0-9-]".toRegex(), "").toIntOrNull() ?: 0
                                val roll = Random.nextInt(1, 21)
                                val total = roll + bonus
                                rollResult = Pair(skill.key.replace("_", " ").replaceFirstChar { it.uppercase() }, total)
                                showRollDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "${skill.key.replace("_", " ").replaceFirstChar { it.uppercase() }}: ",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = skill.value,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }
        }
    }

    if (showRollDialog && rollResult != null) {
        AlertDialog(
            onDismissRequest = { showRollDialog = false },
            title = {
                Text(
                    "Skill Check: ${rollResult!!.first}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colorScheme.onSurface,
                        fontFamily = CinzelDecorative
                    )
                )
            },
            text = {
                Text(
                    "Result: ${rollResult!!.second}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.tertiary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showRollDialog = false }) {
                    Text(
                        "OK",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.9f),
            titleContentColor = colorScheme.onSurface,
            textContentColor = colorScheme.onSurface
        )
    }
}

@Composable
fun MonsterResistancesImmunitiesSection(monster: CustomMonster) {
    val resistances = monster.resist ?: emptyList()
    val immunities = monster.immune ?: emptyList()

    if (resistances.isNotEmpty() || immunities.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "RESISTANCES & IMMUNITIES",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
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
                Column {
                    if (resistances.isNotEmpty()) {
                        Text(
                            text = "Resistances:",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = resistances.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } },
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    if (immunities.isNotEmpty()) {
                        Text(
                            text = "Immunities:",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = immunities.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } },
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterSensesSection(monster: CustomMonster) {
    val senses = monster.senses ?: emptyList()
    val wisdomScore = monster.wis ?: 10
    val wisdomModifier = (wisdomScore - 10) / 2
    val proficiencyBonus = monster.proficiencyBonus ?: 0
    val perceptionSkill = monster.skills?.get("perception")
    val perceptionBonus = perceptionSkill?.replace("[^0-9-]".toRegex(), "")?.toIntOrNull() ?: wisdomModifier
    val passivePerception = 10 + wisdomModifier + if (perceptionBonus > wisdomModifier) proficiencyBonus else 0

    if (senses.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "SENSES",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
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
                Column {
                    Text(
                        text = senses.joinToString(", ") { it },
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Passive Perception:",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    )
                    Text(
                        text = "$passivePerception",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonsterLanguagesSection(monster: CustomMonster) {
    val languages = monster.languages ?: emptyList()

    if (languages.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "LANGUAGES",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
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
                Text(
                    text = languages.joinToString(", ") { it },
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


@Composable
fun MonsterTraitsSection(monster: CustomMonster) {
    val traits = monster.traits ?: emptyList()

    if (traits.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "TRAITS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                traits.forEach { trait ->
                    Column {
                        Text(
                            text = trait.name.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontFamily = CinzelDecorative
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        trait.entries.forEach { entry ->
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterActionsSection(monster: CustomMonster) {
    val actions = monster.actions ?: emptyList()

    if (actions.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "ACTIONS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                actions.forEach { action ->
                    Column {
                        Text(
                            text = action.name.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontFamily = CinzelDecorative
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        action.entries.forEach { entry ->
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterBonusActionsSection(monster: CustomMonster) {
    val bonusActions = monster.bonus ?: emptyList()

    if (bonusActions.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "BONUS ACTIONS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bonusActions.forEach { bonusAction ->
                    Column {
                        Text(
                            text = bonusAction.name.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontFamily = CinzelDecorative
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        bonusAction.entries.forEach { entry ->
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterReactionsSection(monster: CustomMonster) {
    val reactions = monster.reactions ?: emptyList()

    if (reactions.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "REACTIONS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                reactions.forEach { reaction ->
                    Column {
                        Text(
                            text = reaction.name.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontFamily = CinzelDecorative
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        reaction.entries.forEach { entry ->
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterLegendaryActionsSection(monster: CustomMonster) {
    val legendaryActions = monster.legendary ?: emptyList()

    if (legendaryActions.isNotEmpty()) {
        MonsterCard {
            Text(
                text = "LEGENDARY ACTIONS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = CinzelDecorative,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${monster.name} can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. ${monster.name} regains spent legendary actions at the start of its turn.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                legendaryActions.forEach { legendaryAction ->
                    Column {
                        Text(
                            text = legendaryAction.name.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontFamily = CinzelDecorative
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        legendaryAction.entries.forEach { entry ->
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun calculateSpellStats(monster: CustomMonster): Pair<Int, Int> {
    val abilityModifier = when (monster.spellcasting?.firstOrNull()?.ability) {
        "cha" -> floor((monster.cha?.minus(10) ?: 0) / 2.0).toInt()
        "wis" -> floor((monster.wis?.minus(10) ?: 0) / 2.0).toInt()
        "int" -> floor((monster.int?.minus(10) ?: 0) / 2.0).toInt()
        else -> 0
    }
    val pb = monster.proficiencyBonus
    val spellSaveDC = 8 + pb + abilityModifier
    val spellAttackBonus = pb + abilityModifier
    return Pair(spellSaveDC, spellAttackBonus)
}

@Composable
fun SpellcastingDetailSection(monster: CustomMonster) {
    val spellcasting = monster.spellcasting?.firstOrNull() ?: return
    val (spellSaveDC, spellAttackBonus) = calculateSpellStats(monster)
    val abilityName = when (spellcasting.ability) {
        "cha" -> "Charisma"
        "wis" -> "Wisdom"
        "int" -> "Intelligence"
        else -> ""
    }

    MonsterCard {
        Text(
            text = "SPELLCASTING",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.tertiary,
                fontFamily = CinzelDecorative,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Spellcasting Ability: $abilityName",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spell Save DC: $spellSaveDC",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Spell Attack: +$spellAttackBonus",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${monster.name} has the following spells:",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )

                spellcasting.spells.entries.sortedBy { it.key.toIntOrNull() ?: 0 }.forEach { (level, spellLevel) ->
                    val slotText = when (level.toIntOrNull() ?: 0) {
                        0 -> "CANTRIPS (at will)"
                        1 -> "1ST LEVEL (${spellLevel.slots} slots)"
                        2 -> "2ND LEVEL (${spellLevel.slots} slots)"
                        3 -> "3RD LEVEL (${spellLevel.slots} slots)"
                        4 -> "4TH LEVEL (${spellLevel.slots} slots)"
                        5 -> "5TH LEVEL (${spellLevel.slots} slots)"
                        6 -> "6TH LEVEL (${spellLevel.slots} slots)"
                        7 -> "7TH LEVEL (${spellLevel.slots} slots)"
                        8 -> "8TH LEVEL (${spellLevel.slots} slots)"
                        9 -> "9TH LEVEL (${spellLevel.slots} slots)"
                        else -> "$level LEVEL (${spellLevel.slots} slots)"
                    }
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = slotText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        val sortedSpells = spellLevel.spells.sorted()
                        if (sortedSpells.isNotEmpty()) {
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                sortedSpells.forEach { spellName ->
                                    Text(
                                        text = "• $spellName",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
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