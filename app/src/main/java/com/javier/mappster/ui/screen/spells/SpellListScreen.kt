package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.javier.mappster.data.AuthManager
import com.javier.mappster.model.Spell
import com.javier.mappster.model.SchoolData
import com.javier.mappster.ui.screen.BottomNavigationBar

@Composable
private fun EmptySpellsMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No se encontraron hechizos")
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
fun SpellListScreen(
    viewModel: SpellListViewModel = provideSpellListViewModel(LocalContext.current),
    onSpellClick: (Spell) -> Unit,
    onCreateSpellClick: () -> Unit,
    onEditSpellClick: (Spell) -> Unit,
    navController: NavHostController
) {
    val spells by viewModel.spells.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChanged = viewModel::onSearchQueryChanged,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCreateSpellClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear hechizo"
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(error!!, onDismiss = { viewModel.clearError() })
            spells.isEmpty() -> EmptySpellsMessage()
            else -> SpellListContent(
                spells = spells,
                paddingValues = paddingValues,
                onSpellClick = onSpellClick,
                onDeleteSpellClick = { spell -> viewModel.deleteSpell(spell) },
                onToggleVisibilityClick = { spell, isPublic ->
                    viewModel.updateSpellVisibility(spell, isPublic)
                },
                onEditSpellClick = onEditSpellClick
            )
        }
    }
}

@Composable
private fun SpellListContent(
    spells: List<Spell>,
    paddingValues: PaddingValues,
    onSpellClick: (Spell) -> Unit,
    onDeleteSpellClick: (Spell) -> Unit,
    onToggleVisibilityClick: (Spell, Boolean) -> Unit,
    onEditSpellClick: (Spell) -> Unit
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
        items(spells) { spell ->
            SpellListItem(
                spell = spell,
                onClick = { onSpellClick(spell) },
                onDeleteClick = { onDeleteSpellClick(spell) },
                onToggleVisibilityClick = { isPublic -> onToggleVisibilityClick(spell, isPublic) },
                onEditClick = { onEditSpellClick(spell) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun SearchBar(
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
        placeholder = { Text("Buscar hechizos...") },
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
private fun SpellListItem(
    spell: Spell,
    onClick: (Spell) -> Unit,
    onDeleteClick: (Spell) -> Unit,
    onToggleVisibilityClick: (Boolean) -> Unit,
    onEditClick: (Spell) -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val currentUserId = authManager.getCurrentUserId()
    val canModify = spell.custom && spell.userId == currentUserId
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var pendingVisibility by remember { mutableStateOf(spell.public) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres borrar el hechizo \"${spell.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick(spell)
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

    if (showVisibilityDialog) {
        AlertDialog(
            onDismissRequest = { showVisibilityDialog = false },
            title = { Text("Confirmar cambio de visibilidad") },
            text = {
                Text(
                    "¿Estás seguro de que quieres hacer el hechizo \"${spell.name}\" " +
                            "${if (pendingVisibility) "público" else "privado"}?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onToggleVisibilityClick(pendingVisibility)
                    showVisibilityDialog = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVisibilityDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    val medievalSharpFontFamily = FontFamily(
        Font(
            googleFont = GoogleFont("MedievalSharp"),
            fontProvider = provider
        )
    )

    val defaultColor = MaterialTheme.colorScheme.primary

    val sourceMap = mapOf(
        "AAG" to "Astral Adventurer's Guide",
        "AI" to "Aquisitions Incorporated",
        "AITFR-AVT" to "A Verdant Tomb",
        "BMT" to "The Book of Many Things",
        "DODK" to "Dungeons of Drakkenheim",
        "EGW" to "Explorer's Guide to Wildemount",
        "FTD" to "Fizban's Treasury of Dragons",
        "GGR" to "Guildmasters' Guide to Ravnica",
        "GHLOE" to "Grim Hollow",
        "IDROTF" to "Icewind Dale: Rime of the Frostmaiden",
        "LLK" to "Lost Laboratory of Kwalish",
        "PHB" to "Player's Handbook",
        "SATO" to "Sigil and the Outlands",
        "SCC" to "Strixhaven: Curriculum of Chaos",
        "SCAG" to "Sword Coast Adventurer's Guide",
        "TCE" to "Tasha's Cauldron of Everything",
        "TDCSR" to "Tal'Dorei Campaign Setting",
        "XGE" to "Xanathar's Guide to Everything"
    )

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
            else -> SchoolData(spell.school, defaultColor, Icons.Default.AutoFixHigh)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { onClick(spell) }
            .border(
                width = 2.dp,
                color = schoolData.color.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = schoolData.color.copy(alpha = 0.1f)
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = spell.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp,
                        fontFamily = medievalSharpFontFamily
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (spell.custom && !canModify) {
                    Icon(
                        imageVector = if (spell.public) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = if (spell.public) "Público" else "Privado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (canModify) {
                    IconButton(
                        onClick = {
                            pendingVisibility = !spell.public
                            showVisibilityDialog = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (spell.public) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = if (spell.public) "Hacer privado" else "Hacer público",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onEditClick(spell) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar hechizo",
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
                            contentDescription = "Borrar hechizo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (spell.level == 0) "Truco" else "Nvl. ${spell.level}",
                    style = MaterialTheme.typography.labelMedium.copy(
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

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = schoolData.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = schoolData.color.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = sourceMap[spell.source.uppercase()] ?: spell.source,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}