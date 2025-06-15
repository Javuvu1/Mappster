package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.R
import com.javier.mappster.model.Spell
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.SpellDetailScreen
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
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .padding(paddingValues)
            ) {
                // Panel izquierdo (lista)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Barra de búsqueda
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Transparent
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
                                    onClick = { navController.navigate(Destinations.CREATE_SPELL) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Crear Hechizo",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        // Lista de hechizos
                        when {
                            isLoading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            error != null -> {
                                Text(
                                    text = "Error: ${error!!}",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            spells.isEmpty() -> {
                                Text(
                                    text = if (searchQuery.isEmpty()) "No hay hechizos"
                                    else "No se encontraron resultados",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    items(spells) { spell ->
                                        SpellListItem(
                                            spell = spell,
                                            isSelected = selectedSpell?.name == spell.name,
                                            onClick = { selectedSpell = spell },
                                            onDeleteClick = { viewModel.deleteSpell(it) },
                                            onToggleVisibilityClick = { isPublic ->
                                                viewModel.updateSpellVisibility(spell, isPublic)
                                            },
                                            onEditClick = {
                                                val encodedName = java.net.URLEncoder.encode(spell.name, "UTF-8")
                                                navController.navigate("${Destinations.EDIT_SPELL.replace("{spellName}", encodedName)}")
                                            },
                                            isTwoPaneMode = true,
                                            navController = navController
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                ) {
                    selectedSpell?.let { spell ->
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Título del hechizo
                            Text(
                                text = spell.name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )

                            SpellDetailScreen(
                                spell = spell,
                                isTwoPaneMode = true,
                                navController = navController,
                                viewModel = viewModel,
                                onSpellSelected = { newSpell ->
                                    selectedSpell = newSpell
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    } ?: Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        Text(
                            "Selecciona un hechizo para ver los detalles",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    )
}