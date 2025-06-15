package com.javier.mappster.ui.screen.initiativeTracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.javier.mappster.model.CustomMonster
import com.javier.mappster.model.InitiativeEntry
import com.javier.mappster.model.InitiativeTrackerUiState
import com.javier.mappster.model.Monster
import com.javier.mappster.model.UnifiedMonster
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.BottomNavigationBar
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.ui.theme.MappsterTheme
import com.javier.mappster.viewmodel.MonsterListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitiativeTrackerScreen(
    navController: NavHostController,
    monsterViewModel: MonsterListViewModel,
    trackerViewModel: InitiativeTrackerViewModel = viewModel(),
    isTwoPaneMode: Boolean = false,
    onItemClick: (UnifiedMonster) -> Unit = {}
) {
    val searchQuery by trackerViewModel.searchQuery.collectAsState()
    val uiState by trackerViewModel.uiState.collectAsState()
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showMonsterSearch by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }

    MappsterTheme {
        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Rastreador de Iniciativa",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { trackerViewModel.rollMonsterInitiatives() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Lanzar Iniciativa",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = CinzelDecorative
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { trackerViewModel.sortEntries() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Ordenar",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = CinzelDecorative
                                )
                            )
                        }
                    }
                }
            },
            bottomBar = { if (!isTwoPaneMode) BottomNavigationBar(navController) }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showMonsterSearch = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Añadir Monstruo",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = CinzelDecorative
                                )
                            )
                        }
                        Button(
                            onClick = { showAddPlayerDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Añadir Jugador",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = CinzelDecorative
                                )
                            )
                        }
                    }

                    when (val state = uiState) {
                        is InitiativeTrackerUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.entries, key = { it.id }) { entry ->
                                    InitiativeEntryItem(
                                        entry = entry,
                                        onInitiativeChange = { newValue ->
                                            trackerViewModel.updateEntryInitiative(entry.id, newValue)
                                        },
                                        onRemove = { trackerViewModel.removeEntry(entry.id) },
                                        onHpClick = { hp, id ->
                                            if (entry is InitiativeEntry.MonsterEntry) {
                                                trackerViewModel.showHpDialog(id, hp)
                                            }
                                        },
                                        navController = navController,
                                        isTwoPaneMode = isTwoPaneMode,
                                        onItemClick = onItemClick
                                    )
                                }
                            }
                        }
                        is InitiativeTrackerUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                        is InitiativeTrackerUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    state.message,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = FontStyle.Italic,
                                        fontFamily = CinzelDecorative
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (showMonsterSearch) {
                MonsterSearchDialog(
                    monsterViewModel = monsterViewModel,
                    onDismiss = { showMonsterSearch = false },
                    onMonsterSelected = { monster ->
                        trackerViewModel.addMonster(monster)
                    }
                )
            }

            if (showAddPlayerDialog) {
                AlertDialog(
                    onDismissRequest = { showAddPlayerDialog = false },
                    title = {
                        Text(
                            "Añadir Jugador",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    text = {
                        Column {
                            TextField(
                                value = playerName,
                                onValueChange = { playerName = it },
                                label = {
                                    Text(
                                        "Nombre del Jugador",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = CinzelDecorative
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.tertiary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = CinzelDecorative
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (playerName.isNotBlank()) {
                                    trackerViewModel.addPlayer(playerName)
                                    playerName = ""
                                    showAddPlayerDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Añadir",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = CinzelDecorative
                                )
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAddPlayerDialog = false },
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

            trackerViewModel.hpDialogState?.let { dialogState ->
                AlertDialog(
                    onDismissRequest = { trackerViewModel.closeHpDialog() },
                    title = {
                        Text(
                            "Modificar HP",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = CinzelDecorative,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    text = {
                        Column {
                            Text(
                                "HP Actual: ${dialogState.currentHp ?: "Desconocido"} / ${dialogState.maxHp ?: "?"}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = CinzelDecorative
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = dialogState.hpChange,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 4 && newValue.matches(Regex("^[+-]?\\d*\$"))) {
                                        trackerViewModel.updateHpChange(newValue)
                                    }
                                },
                                label = {
                                    Text(
                                        "Cambiar HP (ej., +10 para sumar, 5 para restar)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = CinzelDecorative
                                        )
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.tertiary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = CinzelDecorative
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { trackerViewModel.confirmHpChange() },
                            enabled = dialogState.hpChange.matches(Regex("^[+-]?\\d+\$")),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp)
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
                            onClick = { trackerViewModel.closeHpDialog() },
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterSearchDialog(
    monsterViewModel: MonsterListViewModel,
    onDismiss: () -> Unit,
    onMonsterSelected: (UnifiedMonster) -> Unit
) {
    val state by monsterViewModel.state.collectAsState()
    var selectedMonster by remember { mutableStateOf<UnifiedMonster?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Seleccionar Monstruo",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = CinzelDecorative,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        monsterViewModel.onSearchQueryChanged(it)
                    },
                    label = {
                        Text(
                            "Buscar por nombre",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CinzelDecorative
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = CinzelDecorative
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.monsters, key = { it.id }) { monster ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = if (selectedMonster?.id == monster.id)
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                                .clickable { selectedMonster = monster },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
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
                                    Text(
                                        monster.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontFamily = CinzelDecorative
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "CR: ${monster.cr ?: "?"}, ${monster.size ?: "?"} ${monster.type ?: "?"}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = CinzelDecorative
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
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedMonster?.let {
                        onMonsterSelected(it)
                        onDismiss()
                    }
                },
                enabled = selectedMonster != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Añadir",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = CinzelDecorative
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitiativeEntryItem(
    entry: InitiativeEntry,
    onInitiativeChange: (Int?) -> Unit,
    onRemove: () -> Unit,
    onHpClick: (Int?, String) -> Unit,
    navController: NavHostController,
    isTwoPaneMode: Boolean = false,
    onItemClick: (UnifiedMonster) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (entry is InitiativeEntry.MonsterEntry) {
                    if (isTwoPaneMode) {
                        onItemClick(entry.monster)
                    } else {
                        val monster = entry.monster
                        if (monster.isCustom) {
                            navController.navigate("${Destinations.CUSTOM_MONSTER_DETAIL}/${monster.id}")
                        } else {
                            val encodedName = java.net.URLEncoder.encode(monster.name, "UTF-8")
                            val encodedSource = java.net.URLEncoder.encode(monster.source ?: "", "UTF-8")
                            navController.navigate("${Destinations.MONSTER_DETAIL}/$encodedName/$encodedSource")
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.1.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontFamily = CinzelDecorative
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (entry is InitiativeEntry.MonsterEntry) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onHpClick(entry.hp, entry.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HP: ",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = CinzelDecorative
                                    )
                                )
                                Text(
                                    text = "${entry.hp ?: "?"}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CinzelDecorative
                                    )
                                )
                            }
                            Text(
                                "AC: ${entry.ac ?: "?"}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = CinzelDecorative
                                )
                            )
                            Text(
                                "Inic: ${entry.baseInitiative?.let {
                                    if (it >= 0) "+$it" else "$it"
                                } ?: "?"}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = CinzelDecorative
                                )
                            )
                        }
                        entry.monster.let { monster ->
                            val saveText = if (monster.isCustom) {
                                (monster as? CustomMonster)?.saves?.entries
                                    ?.filter { it.value != null }
                                    ?.joinToString(", ") { "${it.key.uppercase()}: ${it.value}" }
                            } else {
                                (monster as? Monster)?.save?.let { save ->
                                    listOfNotNull(
                                        save.str?.let { "STR: $it" },
                                        save.dex?.let { "DEX: $it" },
                                        save.con?.let { "CON: $it" },
                                        save.int?.let { "INT: $it" },
                                        save.wis?.let { "WIS: $it" },
                                        save.cha?.let { "CHA: $it" }
                                    ).joinToString(", ")
                                }
                            }
                            if (!saveText.isNullOrEmpty()) {
                                Text(
                                    "Saves: $saveText",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = CinzelDecorative
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    TextField(
                        value = entry.initiative?.toString() ?: "",
                        onValueChange = { onInitiativeChange(it.toIntOrNull()) },
                        modifier = Modifier.width(72.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontFamily = CinzelDecorative,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        label = {
                            Text(
                                "Inic",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = CinzelDecorative,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}