package com.javier.mappster.ui.screen.spellList

import android.util.Log
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.R
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.Spell
import com.javier.mappster.model.SpellList
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.BottomNavigationBar
import com.javier.mappster.ui.screen.spells.SpellListViewModel
import com.javier.mappster.ui.theme.CinzelDecorative
import com.javier.mappster.ui.theme.magicColors
import com.javier.mappster.utils.normalizeSpellName
import com.javier.mappster.utils.sourceMap
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
private fun EmptySpellsMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Esta lista está vacía",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellListViewScreen(
    listId: String,
    viewModel: SpellListViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    var spellList by remember { mutableStateOf<SpellList?>(null) }
    val spells by viewModel.spells.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listId) {
        coroutineScope.launch {
            try {
                val userId = authManager.getCurrentUserId() ?: run {
                    error = "Usuario no autenticado"
                    isLoading = false
                    return@launch
                }
                val list = firestoreManager.getSpellListById(listId)
                spellList = list
                if (list == null || list.userId != userId) {
                    error = "Lista no encontrada o no autorizada"
                    isLoading = false
                    return@launch
                }
            } catch (e: Exception) {
                error = "Error al cargar la lista: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        spellList?.name ?: "Cargando...",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = CinzelDecorative,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
            BottomNavigationBar(navController = navController)
        }
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
            when {
                isLoading -> LoadingIndicator()
                error != null -> ErrorMessage(error!!, onDismiss = { error = null })
                spellList == null -> EmptySpellsMessage()
                spellList!!.spellIds.isEmpty() -> EmptySpellsMessage()
                else -> {
                    val listSpells = spells.filter { spell ->
                        spellList!!.spellIds.contains(normalizeSpellName(spell.name))
                    }.sortedBy { it.level }
                    Log.d("SpellListViewScreen", "Sorted spells: ${listSpells.map { "${it.name} (Level ${it.level})" }}")
                    if (listSpells.isEmpty()) {
                        EmptySpellsMessage()
                    } else {
                        SpellListContent(
                            spells = listSpells,
                            paddingValues = paddingValues,
                            onSpellClick = { spell ->
                                val encodedName = URLEncoder.encode(spell.name, "UTF-8")
                                navController.navigate("${Destinations.SPELL_DETAIL}/$encodedName")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellListContent(
    spells: List<Spell>,
    paddingValues: PaddingValues,
    onSpellClick: (Spell) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 8.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
            start = 12.dp,
            end = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(spells) { spell ->
            SpellListItem(
                spell = spell,
                onClick = { onSpellClick(spell) }
            )
        }
    }
}

@Composable
private fun SpellListItem(
    spell: Spell,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val magicColors = MaterialTheme.magicColors

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
            color = schoolInfo?.let { magicColors[it.colorKey] } ?: colorScheme.primary,
            icon = schoolInfo?.icon ?: Icons.Default.AutoFixHigh
        )
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
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = schoolData.color.copy(alpha = 0.1f)
            )
            .clickable { onClick() },
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
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = spell.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontFamily = CinzelDecorative,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = schoolData.color.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = schoolData.icon,
                            contentDescription = schoolData.name,
                            modifier = Modifier.size(20.dp),
                            tint = schoolData.color
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (spell.level == 0) "Truco" else "Nvl. ${spell.level}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = schoolData.color,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .background(
                                color = schoolData.color.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = schoolData.name.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = schoolData.color.copy(alpha = 0.9f),
                            letterSpacing = 1.sp,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = sourceMap[spell.source.uppercase()] ?: spell.source,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp
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

private data class SchoolData(
    val name: String,
    val color: Color,
    val icon: ImageVector
)