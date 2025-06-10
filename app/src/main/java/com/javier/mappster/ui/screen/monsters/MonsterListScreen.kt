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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.model.UnifiedMonster
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.utils.sourceMap
import com.javier.mappster.viewmodel.MonsterListViewModel

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
            Log.e("MonsterListScreen", "User not authenticated, navigating to login")
            navController.navigate(Destinations.LOGIN) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (isCheckingAuth) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
                                navController.navigate("${Destinations.CREATE_MONSTER}?monsterId=null")
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Crear Monstruo",
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
                        CircularProgressIndicator()
                    }
                } else if (state.error != null) {
                    Text(
                        text = "Error al cargar monstruos: ${state.error}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (state.monsters.isEmpty()) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No hay monstruos disponibles." else "No se encontraron monstruos para \"$searchQuery\"",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
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
                                authManager = authManager
                            )
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
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = { Text("Buscar monstruos...") },
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
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
    authManager: AuthManager, // Añadir este parámetro
    modifier: Modifier = Modifier
) {
    Log.d("MonsterItem", "Rendering monster: ${monster.name}, id=${monster.id}, isCustom=${monster.isCustom}")
    val defaultColor = MaterialTheme.colorScheme.primary
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres borrar el monstruo \"${monster.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick(monster)
                    showDeleteDialog = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable {
                if (isTwoPaneMode) {
                    onItemClick(monster)
                } else {
                    navController?.let {
                        if (monster.isCustom) {
                            it.navigate("${Destinations.CUSTOM_MONSTER_DETAIL}/${monster.id}")
                        } else {
                            val encodedName = java.net.URLEncoder.encode(monster.name, "UTF-8")
                            val encodedSource = java.net.URLEncoder.encode(monster.source ?: "", "UTF-8")
                            it.navigate("${Destinations.MONSTER_DETAIL}/$encodedName/$encodedSource")
                        }
                    }
                }
            }
            .border(
                width = 2.dp,
                color = defaultColor.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = defaultColor.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
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
                    modifier = Modifier.weight(1f)
                )
                monster.cr?.let { cr ->
                    Text(
                        text = "CR: $cr",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = defaultColor.copy(alpha = 0.9f),
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
                            "M" -> "Mediano"
                            "L" -> "Grande"
                            "S" -> "Pequeño"
                            "T" -> "Diminuto"
                            "H" -> "Enorme"
                            "G" -> "Gigantesco"
                            else -> size
                        }
                    } ?: "Desconocido"

                    val typeText = monster.type?.replaceFirstChar { it.uppercase() } ?: "Desconocido"

                    val alignmentText = monster.alignment?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""

                    Text(
                        text = "$sizeText $typeText$alignmentText",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sourceMap[monster.source?.uppercase()] ?: monster.source ?: "Desconocido",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = if (monster.isCustom) 4.dp else 0.dp)
                    )

                    if (monster.isCustom && monster.id != null) {
                        val userId = authManager.getCurrentUserId()
                        IconButton(
                            onClick = {
                                if (userId == null) {
                                    Log.e("MonsterItem", "User not authenticated, redirecting to login")
                                    navController?.navigate(Destinations.LOGIN) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else if (!isTwoPaneMode) {
                                    val encodedMonsterId = java.net.URLEncoder.encode(monster.id, "UTF-8")
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
                                contentDescription = "Editar monstruo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar monstruo",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}