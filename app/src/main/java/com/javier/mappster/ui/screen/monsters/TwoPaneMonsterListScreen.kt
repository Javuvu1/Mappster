package com.javier.mappster.ui.screen.monsters

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.Monster
import com.javier.mappster.model.UnifiedMonster
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.BottomNavigationBar
import com.javier.mappster.ui.screen.CustomMonsterDetailScreen
import com.javier.mappster.ui.screen.MonsterDetailScreen
import com.javier.mappster.ui.screen.MonsterItem
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.viewmodel.MonsterListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPaneMonsterListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: MonsterListViewModel = viewModel(
        factory = MonsterListViewModelFactory(LocalDataManager(context), AuthManager.getInstance(context))
    )
    val state = viewModel.state.collectAsState().value
    val searchQuery = viewModel.searchQuery.collectAsState().value
    var selectedMonster by remember { mutableStateOf<UnifiedMonster?>(null) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        content = { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
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
                                    onClick = { navController.navigate(Destinations.CREATE_MONSTER) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Create Monster",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        // Lista de monstruos
                        when {
                            state.isLoading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.tertiary,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                            state.error != null -> {
                                Text(
                                    text = "Error: ${state.error}",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = CinzelDecorative,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            }
                            state.monsters.isEmpty() -> {
                                Text(
                                    text = if (searchQuery.isEmpty()) "No monsters" else "No results found",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = CinzelDecorative,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentPadding = PaddingValues(
                                        bottom = if (isLandscape) 32.dp else 16.dp,
                                        top = if (isLandscape) 16.dp else 8.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(if (isLandscape) 10.dp else 8.dp)
                                ) {
                                    items(state.monsters) { monster ->
                                        MonsterItem(
                                            monster = monster,
                                            isSelected = selectedMonster?.id == monster.id,
                                            isTwoPaneMode = true,
                                            onItemClick = { selectedMonster = it },
                                            onDeleteClick = { viewModel.deleteCustomMonster(it) },
                                            onToggleVisibilityClick = { monster, isPublic -> viewModel.updateMonsterVisibility(monster, isPublic) },
                                            navController = null,
                                            authManager = AuthManager.getInstance(context)
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
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Nombre del monstruo en la parte superior
                            Text(
                                text = monster.name ?: "Unknown Monster",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = CinzelDecorative,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            )

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
                                        error = "Error loading monster: ${e.message}"
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
                                        isLoading -> CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                            color = MaterialTheme.colorScheme.tertiary,
                                            strokeWidth = 3.dp
                                        )
                                        error != null -> Text(
                                            text = error ?: "Unknown error",
                                            modifier = Modifier.align(Alignment.Center),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = CinzelDecorative,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                        loadedMonster != null -> MonsterDetailScreen(
                                            monster = loadedMonster!!,
                                            navController = navController,
                                            isTwoPaneMode = true
                                        )
                                        else -> Text(
                                            text = "Monster not found",
                                            modifier = Modifier.align(Alignment.Center),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = CinzelDecorative,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                    }
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
                            text = "Select a monster to see details",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = {
            Text(
                "Search monsters...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.tertiary
                )
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.tertiary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}