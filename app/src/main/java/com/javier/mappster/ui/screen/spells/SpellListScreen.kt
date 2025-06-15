package com.javier.mappster.ui.screen.spells

import android.content.res.Configuration
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
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
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.BottomNavigationBar
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.ui.theme.magicColors
import com.javier.mappster.utils.sourceMap

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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        TwoPaneSpellListScreen(navController = navController, viewModel = viewModel)
    } else {
        SinglePaneSpellListScreen(
            viewModel = viewModel,
            onSpellClick = onSpellClick,
            onCreateSpellClick = onCreateSpellClick,
            onEditSpellClick = onEditSpellClick,
            navController = navController
        )
    }
}

@Composable
private fun SpellListContent(
    spells: List<Spell>,
    paddingValues: PaddingValues,
    onSpellClick: (Spell) -> Unit,
    onDeleteSpellClick: (Spell) -> Unit,
    onToggleVisibilityClick: (Spell, Boolean) -> Unit,
    onEditSpellClick: (Spell) -> Unit,
    isTwoPaneMode: Boolean = false,
    navController: NavHostController? = null
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
                isSelected = false, // O manejar selección si es necesario
                onClick = onSpellClick,
                onDeleteClick = onDeleteSpellClick,
                onToggleVisibilityClick = { isPublic -> onToggleVisibilityClick(spell, isPublic) },
                onEditClick = onEditSpellClick,
                isTwoPaneMode = isTwoPaneMode,
                navController = navController
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
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        placeholder = {
            Text(
                "Buscar hechizos...",
                color = MaterialTheme.colorScheme.tertiary
            )
        },
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
fun SpellListItem(
    spell: Spell,
    isSelected: Boolean = false,
    onClick: (Spell) -> Unit,
    onDeleteClick: (Spell) -> Unit,
    onToggleVisibilityClick: (Boolean) -> Unit,
    onEditClick: (Spell) -> Unit,
    isTwoPaneMode: Boolean = false,
    navController: NavHostController? = null
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
    val magicColors = MaterialTheme.magicColors // Access composable property here

    // Define school mapping
    data class SchoolInfo(val name: String, val colorKey: String, val icon: ImageVector)

    val schoolMap = mapOf(
        "A" to SchoolInfo("Abjuración", "Abjuration", Icons.Default.Shield),
        "C" to SchoolInfo("Conjuración", "Conjuration", Icons.Default.CallMerge),
        "D" to SchoolInfo("Adivinación", "Divination", Icons.Default.Visibility),
        "E" to SchoolInfo("Encantamiento", "Enchantment", Icons.Default.Favorite),
        "V" to SchoolInfo("Evocación", "Evocation", Icons.Default.Whatshot),
        "I" to SchoolInfo("Ilusión", "Ilussion", Icons.Default.Masks),
        "N" to SchoolInfo("Nigromancia", "Necromancy", Icons.Default.Coronavirus),
        "T" to SchoolInfo("Transmutación", "Transmutation", Icons.Default.AutoAwesome)
    )

    val schoolData = remember(spell.school) {
        val schoolInfo = schoolMap[spell.school.uppercase()]
        SchoolData(
            name = schoolInfo?.name ?: spell.school,
            color = schoolInfo?.let { magicColors[it.colorKey] } ?: defaultColor,
            icon = schoolInfo?.icon ?: Icons.Default.AutoFixHigh
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable {
                if (isTwoPaneMode) {
                    onClick(spell)
                } else {
                    navController?.let {
                        val encodedName = java.net.URLEncoder.encode(spell.name, "UTF-8")
                        it.navigate("${Destinations.SPELL_DETAIL}/$encodedName")
                    }
                }
            }
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
                            fontFamily = CinzelDecorative,
                            color = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (spell.custom && !canModify) {
                        Icon(
                            imageVector = if (spell.public) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = if (spell.public) "Público" else "Privado",
                            tint = MaterialTheme.colorScheme.tertiary,
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
                                tint = MaterialTheme.colorScheme.tertiary,
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
                                tint = MaterialTheme.colorScheme.tertiary,
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
                                tint = MaterialTheme.colorScheme.tertiary,
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
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = sourceMap[spell.source] ?: spell.source,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SinglePaneSpellListScreen(
    viewModel: SpellListViewModel,
    onSpellClick: (Spell) -> Unit,
    onCreateSpellClick: () -> Unit,
    onEditSpellClick: (Spell) -> Unit,
    navController: NavHostController
) {
    val spells by viewModel.spells.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshSpellsPublic()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Lista de Hechizos",
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
            // Search Bar and Create Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCreateSpellClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear hechizo",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            when {
                isLoading -> LoadingIndicator()
                error != null -> ErrorMessage(error!!, onDismiss = { viewModel.clearError() })
                spells.isEmpty() -> EmptySpellsMessage()
                else -> SpellListContent(
                    spells = spells,
                    paddingValues = PaddingValues(0.dp), // Reset padding to avoid double padding
                    onSpellClick = onSpellClick,
                    onDeleteSpellClick = { spell -> viewModel.deleteSpell(spell) },
                    onToggleVisibilityClick = { spell, isPublic ->
                        viewModel.updateSpellVisibility(spell, isPublic)
                    },
                    onEditSpellClick = onEditSpellClick,
                    isTwoPaneMode = false,
                    navController = navController
                )
            }
        }
    }
}