package com.javier.mappster.ui.screen.spells

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.model.Spell
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPaneSpellListScreen(
    navController: NavHostController,
    viewModel: SpellListViewModel = provideSpellListViewModel(LocalContext.current)
) {
    val spells by viewModel.spells.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedSpell by remember { mutableStateOf<Spell?>(null) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        content = { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues) // Usar paddingValues aquí
            ) {
                // Panel izquierdo (lista)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Barra de búsqueda
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface
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
                                    onClick = { navController.navigate(Destinations.CREATE_SPELL) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Crear Hechizo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Lista de hechizos
                        when {
                            isLoading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            error != null -> {
                                Text(
                                    text = "Error: ${error!!}",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            spells.isEmpty() -> {
                                Text(
                                    text = if (searchQuery.isEmpty()) "No hay hechizos"
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
                                    items(spells) { spell ->
                                        SpellListItem(
                                            spell = spell,
                                            isSelected = selectedSpell?.name == spell.name,
                                            onClick = {
                                                Log.d("TwoPaneSpellListScreen", "Spell clicked in list: ${spell.name}")
                                                selectedSpell = spell
                                            },
                                            onDeleteClick = { viewModel.deleteSpell(it) },
                                            onToggleVisibilityClick = { isPublic ->
                                                viewModel.updateSpellVisibility(spell, isPublic)
                                            },
                                            onEditClick = {
                                                val encodedName = java.net.URLEncoder.encode(spell.name, "UTF-8")
                                                navController.navigate("${Destinations.EDIT_SPELL.replace("{spellName}", encodedName)}")
                                            },
                                            isTwoPaneMode = true
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
                    selectedSpell?.let { spell ->
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Título del hechizo
                            Text(
                                text = spell.name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            SpellDetailScreen(
                                spell = spell,
                                isTwoPaneMode = true,
                                navController = navController,
                                viewModel = viewModel,
                                modifier = Modifier.padding(8.dp),
                                onSpellSelected = { newSpell ->
                                    Log.d("TwoPaneSpellListScreen", "Spell selected via callback: ${newSpell.name}")
                                    selectedSpell = newSpell
                                }
                            )
                        }
                    } ?: Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            "Selecciona un hechizo para ver los detalles",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    )
}