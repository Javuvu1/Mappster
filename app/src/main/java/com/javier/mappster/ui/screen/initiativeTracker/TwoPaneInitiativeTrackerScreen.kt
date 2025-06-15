package com.javier.mappster.ui.screen.initiativeTracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.Monster
import com.javier.mappster.model.UnifiedMonster
import com.javier.mappster.ui.screen.BottomNavigationBar
import com.javier.mappster.ui.screen.CustomMonsterDetailScreen
import com.javier.mappster.ui.screen.MonsterDetailScreen
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.viewmodel.MonsterListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPaneInitiativeTrackerScreen(
    navController: NavHostController,
    monsterViewModel: MonsterListViewModel
) {
    val context = LocalContext.current
    var selectedMonster by remember { mutableStateOf<UnifiedMonster?>(null) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        content = { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                // Left Pane: Initiative Tracker
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    InitiativeTrackerScreen(
                        navController = navController,
                        monsterViewModel = monsterViewModel,
                        isTwoPaneMode = true,
                        onItemClick = { monster ->
                            selectedMonster = monster
                        }
                    )
                }

                // Right Pane: Monster Details
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Título del monstruo seleccionado
                    selectedMonster?.let { monster ->
                        Text(
                            text = monster.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    // Contenido del detalle del monstruo
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        selectedMonster?.let { monster ->
                            if (monster.isCustom) {
                                CustomMonsterDetailScreen(
                                    navController = navController,
                                    monsterId = monster.id ?: "",
                                    isTwoPaneMode = true
                                )
                            } else {
                                val dataManager = LocalDataManager(context)
                                var loadedMonster by remember { mutableStateOf<Monster?>(null) }
                                var isLoading by remember { mutableStateOf(true) }
                                var error by remember { mutableStateOf<String?>(null) }

                                LaunchedEffect(monster) {
                                    try {
                                        val result = dataManager.getMonsterByNameAndSource(
                                            monster.name,
                                            monster.source ?: ""
                                        )
                                        loadedMonster = result
                                    } catch (e: Exception) {
                                        error = "Error al cargar el monstruo: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    when {
                                        isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                                        error != null -> Text(
                                            text = error ?: "Error desconocido",
                                            modifier = Modifier.align(Alignment.Center),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        loadedMonster != null -> MonsterDetailScreen(
                                            monster = loadedMonster!!,
                                            navController = navController,
                                            isTwoPaneMode = true
                                        )
                                        else -> Text(
                                            text = "Monstruo no encontrado",
                                            modifier = Modifier.align(Alignment.Center),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } ?: Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                "Selecciona un monstruo para ver los detalles",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    )
}