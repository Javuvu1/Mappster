package com.javier.mappster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.model.SpellList
import com.javier.mappster.viewmodel.SpellListManagerViewModel
import com.javier.mappster.viewmodel.provideSpellListManagerViewModel

@Composable
private fun EmptySpellListsMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No tienes listas de hechizos")
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
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSpellListsScreen(
    viewModel: SpellListManagerViewModel = provideSpellListManagerViewModel(LocalContext.current),
    navController: NavHostController
) {
    val spellLists by viewModel.spellLists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listas de Hechizos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_spell_list") },
                content = { Icon(Icons.Default.Add, contentDescription = "Crear lista") }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(error!!, onDismiss = { viewModel.clearError() })
            spellLists.isEmpty() -> EmptySpellListsMessage()
            else -> SpellListsContent(
                spellLists = spellLists,
                paddingValues = paddingValues,
                onListClick = { listId ->
                    navController.navigate("spell_list_view/$listId")
                },
                onDeleteClick = { listId -> viewModel.deleteSpellList(listId) }
            )
        }
    }
}

@Composable
private fun SpellListsContent(
    spellLists: List<SpellList>,
    paddingValues: PaddingValues,
    onListClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding() + 16.dp
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        items(spellLists) { spellList ->
            SpellListItem(
                spellList = spellList,
                onClick = { onListClick(spellList.id) },
                onDeleteClick = { onDeleteClick(spellList.id) }
            )
        }
    }
}

@Composable
private fun SpellListItem(
    spellList: SpellList,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres borrar la lista \"${spellList.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
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
            .clickable { onClick() }
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = spellList.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar lista",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}