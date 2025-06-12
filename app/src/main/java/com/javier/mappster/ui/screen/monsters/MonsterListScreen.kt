package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.javier.mappster.ui.theme.MappsterTheme
import com.javier.mappster.utils.sourceMap
import com.javier.mappster.viewmodel.MonsterListViewModel
import java.net.URLEncoder

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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (userId != null) {
            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SearchBar(
                                query = searchQuery,
                                onQueryChanged = viewModel::onSearchQueryChanged,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    Log.d("MonsterListScreen", "Navigating to create_monster for new monster")
                                    navController.navigate(Destinations.CREATE_MONSTER)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Monster",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (state.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (state.error != null) {
                        Text(
                            text = "Error loading monsters: ${state.error}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (state.monsters.isEmpty()) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No monsters available." else "No monsters found for \"$searchQuery\"",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                top = 8.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.monsters) { monster ->
                                MonsterItem(
                                    monster = monster,
                                    navController = navController,
                                    onDeleteClick = { viewModel.deleteCustomMonster(monster) },
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = {
            Text(
                "Buscar monstruos...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
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
                    "Confirm deletion",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete the monster \"${monster.name}\"?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(monster)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showVisibilityDialog) {
        AlertDialog(
            onDismissRequest = { showVisibilityDialog = false },
            title = {
                Text(
                    "Confirm visibility change",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Are you sure you want to make the monster \"${monster.name}\" " +
                            "${if (pendingVisibility) "public" else "private"}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleVisibilityClick(monster, pendingVisibility)
                        showVisibilityDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showVisibilityDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
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
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monster.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    monster.cr?.let { cr ->
                        Text(
                            text = "CR: $cr",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
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

                        val typeText = monster.type?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                        val alignmentText = monster.alignment?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""

                        Text(
                            text = "$sizeText $typeText$alignmentText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (monster.isCustom && monster.id != null) {
                            Log.d("MonsterItem", "Custom monster block entered: id=${monster.id}, isCustom=${monster.isCustom}")
                            // Visibility toggle for all users
                            IconButton(
                                onClick = {
                                    pendingVisibility = !monster.public
                                    showVisibilityDialog = true
                                    Log.d("MonsterItem", "Visibility toggle clicked for ${monster.name}, new pendingVisibility=$pendingVisibility")
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (monster.public) Icons.Default.Public else Icons.Default.Lock,
                                    contentDescription = if (monster.public) "Make private" else "Make public",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // Edit button only for owner
                            if (isOwner) {
                                IconButton(
                                    onClick = {
                                        if (currentUserId == null) {
                                            Log.e("MonsterItem", "User not authenticated, redirecting to login")
                                            navController?.navigate(Destinations.LOGIN) {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else if (!isTwoPaneMode) {
                                            val encodedMonsterId = URLEncoder.encode(monster.id, "UTF-8")
                                            val route = "create_monster?monsterId=$encodedMonsterId"
                                            Log.d("MonsterItem", "Attempting navigation to: $route, navController available: ${navController != null}, current destination: ${navController?.currentDestination?.route}")
                                            try {
                                                navController?.navigate(route) {
                                                    launchSingleTop = true
                                                } ?: Log.e("MonsterItem", "NavController is null")
                                            } catch (e: IllegalArgumentException) {
                                                Log.e("MonsterItem", "Navigation failed for route $route: ${e.message}, current graph routes: ${navController?.graph?.mapNotNull { it.route }?.joinToString(", ")}", e)
                                            }
                                        } else {
                                            Log.d("MonsterItem", "Edit button clicked in two-pane mode, no navigation")
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit monster",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            // Delete button only for owner
                            if (isOwner) {
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete monster",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = sourceMap[monster.source?.uppercase()] ?: monster.source ?: "Unknown",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontStyle = FontStyle.Italic
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = if (monster.isCustom) 4.dp else 0.dp)
                        )
                    }
                }
            }
        }
    }
}