package com.javier.mappster.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.CustomMonster
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
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
                                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeText = monster.size ?: "Medium"
                val typeText = monster.type?.joinToString(", ")?.replaceFirstChar { it.uppercase() } ?: "Unknown"
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
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
    // Función para calcular el modificador
    fun calculateModifier(score: Int?): Int {
        return score?.let { (it - 10) / 2 } ?: 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
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
                // STR
                StatColumn(
                    label = "STR",
                    abilityValue = monster.str,
                    saveValue = monster.saves?.get("str"),
                    modifier = calculateModifier(monster.str)
                )

                // DEX
                StatColumn(
                    label = "DEX",
                    abilityValue = monster.dex,
                    saveValue = monster.saves?.get("dex"),
                    modifier = calculateModifier(monster.dex)
                )

                // CON
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
                // INT
                StatColumn(
                    label = "INT",
                    abilityValue = monster.int,
                    saveValue = monster.saves?.get("int"),
                    modifier = calculateModifier(monster.int)
                )

                // WIS
                StatColumn(
                    label = "WIS",
                    abilityValue = monster.wis,
                    saveValue = monster.saves?.get("wis"),
                    modifier = calculateModifier(monster.wis)
                )

                // CHA
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

    // Mostrar el saveValue si existe, de lo contrario mostrar el modificador
    val saveDisplayText = saveValue ?: if (modifier >= 0) "+$modifier" else "$modifier"
    val modifierText = if (modifier >= 0) "+$modifier" else "$modifier"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        // Puntuación de habilidad
        Text(
            text = "$label: ${abilityValue?.toString() ?: "–"}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = fontWeight,
                fontSize = textSize
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Tirada de salvación (usa el valor guardado o el modificador)
        Text(
            text = "Save: $saveDisplayText",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = modifierTextSize,
                color = MaterialTheme.colorScheme.primary
            )
        )

        // Modificador (siempre mostrado)
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