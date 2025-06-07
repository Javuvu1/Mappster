package com.javier.mappster.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.CustomMonster
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
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

    Scaffold(
        topBar = {
            if (!isTwoPaneMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = customMonster?.name ?: "Unknown Monster",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
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
                            Text(
                                text = "Source: $source",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    item {
                        MonsterInfoSection(customMonster!!)
                    }
                    item {
                        MonsterCombatStats(customMonster!!)
                    }
                    item {
                        MonsterStats(customMonster!!)
                    }
                    item {
                        MonsterSkillsSection(customMonster!!)
                    }
                }
            } else {
                Text(
                    text = "Monstruo no encontrado",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun MonsterInfoSection(monster: CustomMonster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeText = monster.size ?: "Medium"
                val typeText = monster.type?.joinToString(", ")?.replaceFirstChar { it.uppercase() }
                    ?: "Unknown"
                val alignmentText = monster.alignment?.let { ", $it" } ?: ""

                Text(
                    text = "$sizeText $typeText$alignmentText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                monster.cr?.let { cr ->
                    Text(
                        text = "CR: $cr",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MonsterCombatStats(monster: CustomMonster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            monster.hp?.let { hp ->
                Text(
                    text = "Hit Points: $hp",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            monster.initiative?.let { init ->
                val initMod = if (init >= 0) "+$init" else "$init"
                Text(
                    text = "Initiative: $initMod",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            monster.ac?.let { ac ->
                Text(
                    text = "Armor Class: $ac",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Ability Scores & Saves:",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
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
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Text(
            text = "$label: ${abilityValue?.toString() ?: "–"}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = fontWeight,
                fontSize = textSize
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Save: $saveDisplayText",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = modifierTextSize,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Mod: $modifierText",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = modifierTextSize,
                color = MaterialTheme.colorScheme.primary
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Skills:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${skill.key.replace("_", " ").replaceFirstChar { it.uppercase() }}: ",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = skill.value,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showRollDialog && rollResult != null) {
        AlertDialog(
            onDismissRequest = { showRollDialog = false },
            title = { Text("Skill Check: ${rollResult!!.first}") },
            text = { Text("Result: ${rollResult!!.second}") },
            confirmButton = {
                TextButton(onClick = { showRollDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}