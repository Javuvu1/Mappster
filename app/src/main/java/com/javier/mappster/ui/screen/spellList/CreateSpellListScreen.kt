package com.javier.mappster.ui.screen.spellList

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Masks
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.R
import com.javier.mappster.model.SchoolData
import com.javier.mappster.model.Spell
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.spells.SpellListViewModel
import com.javier.mappster.ui.screen.spells.provideSpellListViewModel
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.utils.normalizeSpellName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpellListScreen(
    viewModel: SpellListViewModel = provideSpellListViewModel(LocalContext.current),
    navController: NavHostController,
    listId: String? = null,
    listName: String? = null,
    spellIds: List<String>? = null
) {
    val spellListManagerViewModel = provideSpellListManagerViewModel(LocalContext.current)
    val spells by viewModel.spells.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var listNameState by remember { mutableStateOf(listName ?: "") }
    val selectedSpells = remember {
        mutableStateMapOf<String, Boolean>().apply {
            spellIds?.forEach { spellId -> put(spellId, true) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (listId == null) "Crear Lista" else "Editar Lista",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = CinzelDecorative,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.shadow(elevation = 4.dp)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val name = if (listNameState.isBlank()) "Lista sin nombre" else listNameState
                    if (listId == null) {
                        spellListManagerViewModel.createSpellList(name, selectedSpells.keys.toList())
                    } else {
                        spellListManagerViewModel.updateSpellList(listId, name, selectedSpells.keys.toList())
                    }
                    navController.popBackStack(Destinations.CUSTOM_SPELL_LISTS, inclusive = false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                enabled = selectedSpells.isNotEmpty() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (listId == null) "GUARDAR LISTA" else "ACTUALIZAR LISTA",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = CinzelDecorative,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
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
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = listNameState,
                onValueChange = { listNameState = it },
                label = {
                    Text(
                        "Nombre de la lista",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                com.javier.mappster.ui.screen.spells.SearchBar(
                    query = searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            when {
                isLoading -> LoadingIndicator()
                error != null -> ErrorMessage(error!!, onDismiss = { viewModel.clearError() })
                spells.isEmpty() -> EmptySpellsMessage()
                else -> SpellListContent(
                    spells = spells,
                    selectedSpells = selectedSpells,
                    onSpellSelected = { spell, isSelected ->
                        val spellId = normalizeSpellName(spell.name)
                        if (isSelected) selectedSpells[spellId] = true
                        else selectedSpells.remove(spellId)
                    }
                )
            }
        }
    }
}

@Composable
private fun SpellListContent(
    spells: List<Spell>,
    selectedSpells: MutableMap<String, Boolean>,
    onSpellSelected: (Spell, Boolean) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 8.dp,
            start = 12.dp,
            end = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(spells) { spell ->
            SpellListItem(
                spell = spell,
                isSelected = selectedSpells.containsKey(normalizeSpellName(spell.name)),
                onSelectedChange = { isSelected -> onSpellSelected(spell, isSelected) }
            )
        }
    }
}

@Composable
private fun SpellListItem(
    spell: Spell,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val schoolData = remember(spell.school) {
        when (spell.school.uppercase()) {
            "A" -> SchoolData("Abjuración", Color(0xFF4CAF50), Icons.Default.Shield)
            "C" -> SchoolData("Conjuración", Color(0xFF9C27B0), Icons.Default.CallMerge)
            "D" -> SchoolData("Adivinación", Color(0xFF00ACC1), Icons.Default.Visibility)
            "E" -> SchoolData("Encantamiento", Color(0xFFE91E63), Icons.Default.Favorite)
            "V" -> SchoolData("Evocación", Color(0xFFFF5722), Icons.Default.Whatshot)
            "I" -> SchoolData("Ilusión", Color(0xFF7C4DFF), Icons.Default.Masks)
            "N" -> SchoolData("Nigromancia", Color(0xFF607D8B), Icons.Default.Coronavirus)
            "T" -> SchoolData("Transmutación", Color(0xFFFFC107), Icons.Default.AutoAwesome)
            else -> SchoolData(spell.school, colorScheme.primary, Icons.Default.AutoFixHigh)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = schoolData.color.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = schoolData.color.copy(alpha = 0.1f)
            ),
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
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = spell.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontFamily = CinzelDecorative,
                            fontSize = 18.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = schoolData.color.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = schoolData.icon,
                                contentDescription = schoolData.name,
                                modifier = Modifier.size(16.dp),
                                tint = schoolData.color
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (spell.level == 0) "Truco" else "Nvl. ${spell.level}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = schoolData.color
                            ),
                            modifier = Modifier
                                .background(
                                    color = schoolData.color.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = schoolData.name.uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = schoolData.color.copy(alpha = 0.9f),
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.tertiary,
                        checkmarkColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptySpellsMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No se encontraron hechizos",
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