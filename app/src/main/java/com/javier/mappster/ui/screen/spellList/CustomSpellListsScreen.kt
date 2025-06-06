package com.javier.mappster.ui.screen.spellList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.model.SpellList
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.BottomNavigationBar
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder

@Composable
private fun EmptySpellListsMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No tienes listas de hechizos",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error", style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", style = MaterialTheme.typography.labelLarge)
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
                title = { Text("Mis Listas de Hechizos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Destinations.CREATE_SPELL_LIST) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear lista")
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(error!!, onDismiss = { viewModel.clearError() })
            spellLists.isEmpty() -> EmptySpellListsMessage()
            else -> SpellListsContent(
                spellLists = spellLists,
                paddingValues = paddingValues,
                onListClick = { navController.navigate("${Destinations.SPELL_LIST_VIEW}/$it") },
                onDeleteClick = viewModel::deleteSpellList,
                onEditClick = { spellList ->
                    val spellIdsJson = Json.encodeToString(spellList.spellIds)
                    val encodedName = URLEncoder.encode(spellList.name, "UTF-8")
                    val encodedSpellIds = URLEncoder.encode(spellIdsJson, "UTF-8")
                    navController.navigate("${Destinations.EDIT_SPELL_LIST.replace("{id}", spellList.id).replace("{name}", encodedName).replace("{spellIds}", encodedSpellIds)}")
                }
            )
        }
    }
}

@Composable
private fun SpellListsContent(
    spellLists: List<SpellList>,
    paddingValues: PaddingValues,
    onListClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onEditClick: (SpellList) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 8.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
            start = 12.dp,
            end = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(spellLists, key = { it.id }) { spellList ->
            SpellListItem(
                spellList = spellList,
                onClick = { onListClick(spellList.id) },
                onDeleteClick = { onDeleteClick(spellList.id) },
                onEditClick = { onEditClick(spellList) }
            )
        }
    }
}

@Composable
private fun SpellListItem(
    spellList: SpellList,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar lista") },
            text = { Text("¿Seguro que deseas eliminar \"${spellList.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = spellList.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}