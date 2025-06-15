package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.R
import com.javier.mappster.data.AuthManager
import com.javier.mappster.model.UnifiedMonster
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.ui.theme.MappsterTheme
import com.javier.mappster.utils.sourceMap
import com.javier.mappster.viewmodel.MonsterListViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterListScreen(
    navController: NavHostController,
    viewModel: MonsterListViewModel,
    authManager: AuthManager
) {
    val state = viewModel.state.collectAsState().value
    val searchQuery = viewModel.searchQuery.collectAsState().value
    var isCheckingAuth by remember { mutableStateOf(true) }
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userId = authManager.getCurrentUserId()
        isCheckingAuth = false
        if (userId == null) {
            navController.navigate(Destinations.LOGIN) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            viewModel.refreshCustomMonsters()
        }
    }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            if (backStackEntry.destination.route == Destinations.MONSTER_LIST) {
                Log.d("MonsterList", "Returned to list, refreshing...")
                viewModel.refreshCustomMonsters()
            }
        }
    }

    MappsterTheme {
        if (isCheckingAuth) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.tertiary,
                    strokeWidth = 3.dp
                )
            }
        } else if (userId != null) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Lista de Monstruos",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = CinzelDecorative,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.shadow(elevation = 4.dp)
                    )
                },
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
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
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SearchBar(
                                    query = searchQuery,
                                    onQueryChanged = viewModel::onSearchQueryChanged,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        Log.d("MonsterListScreen", "Navigating to create_monster for new monster")
                                        navController.navigate(Destinations.CREATE_MONSTER)
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Crear Monstruo",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        if (state.isLoading) {
                            LoadingIndicator()
                        } else if (state.error != null) {
                            ErrorMessage(
                                message = "Error loading monsters: ${state.error}",
                                onDismiss = { }
                            )
                        } else if (state.monsters.isEmpty()) {
                            EmptyMonstersMessage(searchQuery)
                        } else {
                            MonsterListContent(
                                monsters = state.monsters,
                                navController = navController,
                                onDeleteClick = { viewModel.deleteCustomMonster(it) },
                                onToggleVisibilityClick = { monster, isPublic ->
                                    viewModel.updateMonsterVisibility(monster, isPublic)
                                },
                                authManager = authManager
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMonstersMessage(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (searchQuery.isEmpty()) "No hay monstruos disponibles"
            else "No se encontraron monstruos para \"$searchQuery\"",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                fontFamily = CinzelDecorative
            )
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            color = MaterialTheme.colorScheme.tertiary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Error",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = CinzelDecorative
                )
            )
        },
        text = {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(
                    "OK",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = CinzelDecorative
                    )
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = {
            Text(
                "Buscar monstruos...",
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
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
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

@Composable
private fun MonsterListContent(
    monsters: List<UnifiedMonster>,
    navController: NavHostController,
    onDeleteClick: (UnifiedMonster) -> Unit,
    onToggleVisibilityClick: (UnifiedMonster, Boolean) -> Unit,
    authManager: AuthManager
) {
    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(monsters) { monster ->
            MonsterItem(
                monster = monster,
                navController = navController,
                onDeleteClick = onDeleteClick,
                onToggleVisibilityClick = onToggleVisibilityClick,
                authManager = authManager
            )
        }
    }
}

@Composable
fun MonsterItem(
    monster: UnifiedMonster,
    isSelected: Boolean = false,
    navController: NavHostController?,
    isTwoPaneMode: Boolean = false,
    onItemClick: (UnifiedMonster) -> Unit = {},
    onDeleteClick: (UnifiedMonster) -> Unit,
    onToggleVisibilityClick: (UnifiedMonster, Boolean) -> Unit,
    authManager: AuthManager,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var pendingVisibility by remember { mutableStateOf(monster.public) }

    val currentUserId = authManager.getCurrentUserId()
    val isOwner = currentUserId != null && monster.userId == currentUserId

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Confirmar eliminación",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres borrar el monstruo \"${monster.name}\"?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(monster)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        "Confirmar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = CinzelDecorative
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = CinzelDecorative
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showVisibilityDialog) {
        AlertDialog(
            onDismissRequest = { showVisibilityDialog = false },
            title = {
                Text(
                    "Confirmar cambio de visibilidad",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = CinzelDecorative,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres hacer el monstruo \"${monster.name}\" " +
                            "${if (pendingVisibility) "público" else "privado"}?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleVisibilityClick(monster, pendingVisibility)
                        showVisibilityDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        "Confirmar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = CinzelDecorative
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showVisibilityDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = CinzelDecorative
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            .clickable {
                if (isTwoPaneMode) {
                    onItemClick(monster)
                } else {
                    navController?.let {
                        if (monster.isCustom) {
                            it.navigate("${Destinations.CUSTOM_MONSTER_DETAIL}/${monster.id}")
                        } else {
                            val encodedName = URLEncoder.encode(monster.name, "UTF-8")
                            val encodedSource = URLEncoder.encode(monster.source ?: "", "UTF-8")
                            it.navigate("${Destinations.MONSTER_DETAIL}/$encodedName/$encodedSource")
                        }
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monster.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.1.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontFamily = CinzelDecorative
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (monster.isCustom && monster.id != null) {
                            // Botón de visibilidad sin fondo
                            IconButton(
                                onClick = {
                                    pendingVisibility = !monster.public
                                    showVisibilityDialog = true
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (monster.public) Icons.Default.Public else Icons.Default.Lock,
                                    contentDescription = if (monster.public) "Make private" else "Make public",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Botón de edición sin fondo
                            if (isOwner) {
                                IconButton(
                                    onClick = {
                                        if (currentUserId == null) {
                                            navController?.navigate(Destinations.LOGIN) {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else if (!isTwoPaneMode) {
                                            val encodedMonsterId = URLEncoder.encode(monster.id, "UTF-8")
                                            val route = "create_monster?monsterId=$encodedMonsterId"
                                            navController?.navigate(route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit monster",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Botón de eliminación sin fondo
                            if (isOwner) {
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete monster",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        monster.cr?.let { cr ->
                            Text(
                                text = "CR: $cr",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sizeText = monster.size?.let { size ->
                            when (size.uppercase()) {
                                "M" -> "Medium"
                                "L" -> "Large"
                                "S" -> "Small"
                                "T" -> "Tiny"
                                "H" -> "Huge"
                                "G" -> "Gargantuan"
                                else -> size
                            }
                        } ?: "Unknown"

                        val typeText = monster.type
                            ?.removeSurrounding("\"")
                            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            ?: "Unknown"

                        Text(
                            text = "$sizeText $typeText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sourceMap[monster.source] ?: monster.source ?: "Unknown",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.tertiary,
                                fontStyle = FontStyle.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}