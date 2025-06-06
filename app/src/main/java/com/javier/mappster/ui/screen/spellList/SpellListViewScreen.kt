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
import androidx.compose.ui.draw.shadow
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
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.Spell
import com.javier.mappster.model.SpellList
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.screen.spells.SpellListViewModel
import com.javier.mappster.utils.normalizeSpellName
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
        Text("Esta lista está vacía")
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
            TopAppBar(
                title = { Text(spellList?.name ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            com.javier.mappster.ui.screen.BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
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

@Composable
private fun SpellListContent(
    spells: List<Spell>,
    paddingValues: PaddingValues,
    onSpellClick: (Spell) -> Unit
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
            .clickable { onClick() }
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

private data class SchoolData(
    val name: String,
    val color: Color,
    val icon: ImageVector
)