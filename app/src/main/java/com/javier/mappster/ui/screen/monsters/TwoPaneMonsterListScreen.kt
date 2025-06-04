package com.javier.mappster.ui.screen.monsters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.Monster
import com.javier.mappster.model.UnifiedMonster
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.CustomMonsterDetailScreen
import com.javier.mappster.ui.screen.MonsterDetailScreen
import com.javier.mappster.ui.screen.MonsterItem
import com.javier.mappster.viewmodel.MonsterListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModelFactory

@Composable
fun TwoPaneMonsterListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: MonsterListViewModel = viewModel(
        factory = MonsterListViewModelFactory(LocalDataManager(context), AuthManager.getInstance(context))
    )
    val state = viewModel.state.collectAsState().value
    val searchQuery = viewModel.searchQuery.collectAsState().value
    var selectedMonster by remember { mutableStateOf<UnifiedMonster?>(null) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Panel izquierdo (lista)
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra de búsqueda
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChanged = viewModel::onSearchQueryChanged,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { navController.navigate(Destinations.CREATE_MONSTER) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Crear Monstruo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Lista de monstruos
                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null -> {
                        Text(
                            text = "Error: ${state.error}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    state.monsters.isEmpty() -> {
                        Text(
                            text = if (searchQuery.isEmpty()) "No hay monstruos"
                            else "No se encontraron resultados",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.monsters) { monster ->
                                MonsterItem(
                                    monster = monster,
                                    isSelected = selectedMonster?.id == monster.id,
                                    isTwoPaneMode = true,
                                    onItemClick = { selectedMonster = it },
                                    onDeleteClick = { viewModel.deleteCustomMonster(it) },
                                    navController = null
                                )
                            }
                        }
                    }
                }
            }
        }

        // Panel derecho (detalle)
        Surface(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
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
                    val dataManager = LocalDataManager(LocalContext.current)
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

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        placeholder = { Text("Buscar monstruos...") },
        modifier = modifier,
        singleLine = true
    )
}