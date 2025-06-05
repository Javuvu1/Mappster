package com.javier.mappster.ui.screen.initiativeTracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.javier.mappster.model.CustomMonster
import com.javier.mappster.model.InitiativeEntry
import com.javier.mappster.model.InitiativeTrackerUiState
import com.javier.mappster.model.Monster
import com.javier.mappster.model.UnifiedMonster
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
                    IconButton(onClick = { trackerViewModel.rollInitiatives() }) {
                        Icon(Icons.Default.Casino, contentDescription = "Roll Initiatives")
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
                                onRemove = { trackerViewModel.removeEntry(entry.id) }
                            )
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
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.name,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }

            if (entry is InitiativeEntry.MonsterEntry) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("HP: ${entry.hp ?: "?"}")
                    Text("AC: ${entry.ac ?: "?"}")
                    Text("Init Mod: ${entry.baseInitiative?.let { if (it >= 0) "+$it" else "$it" } ?: "?"}")
                }
                entry.monster.let { monster ->
                    if (monster.isCustom) {
                        (monster as? CustomMonster)?.saves?.let { saves ->
                            val saveText = saves.entries
                                .filter { it.value != null }
                                .joinToString(", ") { "${it.key.uppercase()}: ${it.value}" }
                            if (saveText.isNotEmpty()) {
                                Text("Saves: $saveText", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        (monster as? Monster)?.save?.let { save ->
                            val saveText = listOfNotNull(
                                save.str?.let { "STR: $it" },
                                save.dex?.let { "DEX: $it" },
                                save.con?.let { "CON: $it" },
                                save.int?.let { "INT: $it" },
                                save.wis?.let { "WIS: $it" },
                                save.cha?.let { "CHA: $it" }
                            ).joinToString(", ")
                            if (saveText.isNotEmpty()) {
                                Text("Saves: $saveText", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Initiative:", modifier = Modifier.padding(end = 8.dp))
                TextField(
                    value = entry.initiative?.toString() ?: "",
                    onValueChange = { onInitiativeChange(it.toIntOrNull()) },
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}