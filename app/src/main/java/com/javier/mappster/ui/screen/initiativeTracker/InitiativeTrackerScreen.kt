package com.javier.mappster.ui.screen.initiativeTracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.javier.mappster.viewmodel.MonsterListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitiativeTrackerScreen(
    navController: NavHostController,
    monsterViewModel: MonsterListViewModel,
    trackerViewModel: InitiativeTrackerViewModel = viewModel()
) {
    val searchQuery by trackerViewModel.searchQuery.collectAsState()
    val uiState by trackerViewModel.uiState.collectAsState()
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showMonsterSearch by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Initiative Tracker") },
                actions = {
                    TextButton(onClick = { trackerViewModel.rollMonsterInitiatives() }) {
                        Text("Roll Initiative")
                    }
                    TextButton(onClick = { trackerViewModel.sortEntries() }) {
                        Text("Sort")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMonsterSearch = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Monster")
                }
                Button(
                    onClick = { showAddPlayerDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Player")
                }
            }

            when (val state = uiState) {
                is InitiativeTrackerUiState.Success -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
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
                                navController = navController // Añadido para navegación
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                        }
                    }
                }
                is InitiativeTrackerUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is InitiativeTrackerUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message)
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
                title = { Text("Add Player") },
                text = {
                    Column {
                        TextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Player Name") }
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
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPlayerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Diálogo para modificar HP
        trackerViewModel.hpDialogState?.let { dialogState ->
            AlertDialog(
                onDismissRequest = { trackerViewModel.closeHpDialog() },
                title = { Text("Modify HP") },
                text = {
                    Column {
                        Text("Current HP: ${dialogState.currentHp ?: "Unknown"} / ${dialogState.maxHp ?: "?"}")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = dialogState.hpChange,
                            onValueChange = { newValue ->
                                // Limitar a 4 caracteres y permitir solo números, + y -
                                if (newValue.length <= 4 && newValue.matches(Regex("^[+-]?\\d*\$"))) {
                                    trackerViewModel.updateHpChange(newValue)
                                }
                            },
                            label = { Text("Change HP (e.g., +10 to add, 5 to subtract)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { trackerViewModel.confirmHpChange() },
                        enabled = dialogState.hpChange.matches(Regex("^[+-]?\\d+\$"))
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { trackerViewModel.closeHpDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

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
        title = { Text("Select Monster") },
        text = {
            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        monsterViewModel.onSearchQueryChanged(it)
                    },
                    label = { Text("Search by name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(state.monsters, key = { it.id }) { monster ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMonster = monster }
                                .background(
                                    if (selectedMonster?.id == monster.id) Color.Gray.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .padding(16.dp)
                        ) {
                            Text(monster.name)
                            Text(
                                "CR: ${monster.cr ?: "?"}, ${monster.size ?: "?"} ${monster.type ?: "?"}",
                                style = MaterialTheme.typography.bodySmall
                            )
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
                enabled = selectedMonster != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InitiativeEntryItem(
    entry: InitiativeEntry,
    onInitiativeChange: (Int?) -> Unit,
    onRemove: () -> Unit,
    onHpClick: (Int?, String) -> Unit,
    navController: NavHostController // Añadido para navegación
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clickable {
                // Navegación solo para monstruos
                if (entry is InitiativeEntry.MonsterEntry) {
                    val monster = entry.monster
                    if (monster.isCustom) {
                        navController.navigate("${Destinations.CUSTOM_MONSTER_DETAIL}/${monster.id}")
                    } else {
                        val encodedName = java.net.URLEncoder.encode(monster.name, "UTF-8")
                        val encodedSource = java.net.URLEncoder.encode(monster.source ?: "", "UTF-8")
                        navController.navigate("${Destinations.MONSTER_DETAIL}/$encodedName/$encodedSource")
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (entry is InitiativeEntry.MonsterEntry) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.clickable(
                                // Priorizar el diálogo de HP sobre la navegación
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onHpClick(entry.hp, entry.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HP: ",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${entry.hp ?: "?"}",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text("AC: ${entry.ac ?: "?"}", fontSize = 14.sp)
                        Text(
                            "Init: ${entry.baseInitiative?.let {
                                if (it >= 0) "+$it" else "$it"
                            } ?: "?"}",
                            fontSize = 14.sp
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
                                fontSize = 12.sp,
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
                    modifier = Modifier.width(56.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Init", fontSize = 12.sp) }
                )
            }
        }
    }
}