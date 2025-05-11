package com.javier.mappster.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.javier.mappster.R
import com.javier.mappster.model.Spell
import com.javier.mappster.viewmodel.SpellListViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellListScreen(
    viewModel: SpellListViewModel = viewModel(),
    onSpellClick: (Spell) -> Unit
) {
    val spells by viewModel.spells.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged
                )
            }
        }
    ) { paddingValues ->
        when {
            spells.isEmpty() -> EmptySpellsMessage()
            else -> SpellListContent(spells = spells, paddingValues = paddingValues, onSpellClick = onSpellClick)
        }
    }
}

@Composable
private fun SpellListContent(
    spells: List<Spell>,
    paddingValues: PaddingValues,
    onSpellClick: (Spell) -> Unit  //Añadido
) {

    LazyColumn(
        contentPadding = paddingValues,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        items(spells) { spell ->
            SpellListItem(spell = spell, onClick = { onSpellClick(spell) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
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
                modifier = Modifier.size(20.dp))
        },
        placeholder = { Text("Buscar hechizos...") },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        singleLine = true,
        shape = RoundedCornerShape(12.dp))
}

@Composable
private fun SpellListItem(spell: Spell,
                          onClick: (Spell) -> Unit ) {

    // Configura el proveedor de Google Fonts
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    // Define la familia de fuentes que quieres usar
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
            .clickable { onClick(spell) } //Añadido
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
            Text(
                text = spell.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp,
                    fontFamily = medievalSharpFontFamily
                )
            )

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

                spell.source?.let { sourceCode ->
                    val fullSource = sourceMap[sourceCode.uppercase()] ?: sourceCode
                    Text(
                        text = fullSource,
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
}

private data class SchoolData(
    val name: String,
    val color: Color,
    val icon: ImageVector
)