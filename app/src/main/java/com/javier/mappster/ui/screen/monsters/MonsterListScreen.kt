package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.model.Monster
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.utils.sourceMap
import com.javier.mappster.viewmodel.MonsterListViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun MonsterListScreen(
    navController: NavHostController,
    viewModel: MonsterListViewModel
) {
    val state = viewModel.state.collectAsState().value
    val searchQuery = viewModel.searchQuery.collectAsState().value

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 8.dp, top = 32.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChanged = viewModel::onSearchQueryChanged,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { navController.navigate(Destinations.CREATE_MONSTER) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear Monstruo",
                            tint = Color.White
                        )
                    }
                }
            }
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
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text(
                    text = "Error al cargar monstruos: ${state.error}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (state.monsters.isEmpty()) {
                Text(
                    text = if (searchQuery.isEmpty()) "No hay monstruos disponibles." else "No se encontraron monstruos para \"$searchQuery\"",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.monsters) { monster ->
                        MonsterItem(
                            monster = monster,
                            onClick = {
                                Log.d("MonsterListScreen", "Serializando monstruo: $monster")
                                val monsterJson = try {
                                    Json.encodeToString(monster)
                                } catch (e: Exception) {
                                    Log.e("MonsterListScreen", "Error de serialización: ${e.message}", e)
                                    return@MonsterItem
                                }
                                val encodedJson = java.net.URLEncoder.encode(monsterJson, "UTF-8")
                                navController.navigate("${Destinations.MONSTER_DETAIL}/$encodedJson")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
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
        placeholder = { Text("Buscar monstruos...") },
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
fun MonsterItem(monster: Monster, onClick: () -> Unit) {
    val defaultColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = defaultColor.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = defaultColor.copy(alpha = 0.1f)
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monster.name ?: "Desconocido",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                monster.cr?.let { cr ->
                    val crText = cr.value ?: "?"
                    Text(
                        text = "CR: $crText",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = defaultColor.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sizeText = monster.size?.firstOrNull()?.let { size ->
                        when (size.uppercase()) {
                            "M" -> "Mediano"
                            "L" -> "Grande"
                            "S" -> "Pequeño"
                            "T" -> "Diminuto"
                            "H" -> "Enorme"
                            "G" -> "Gigantesco"
                            else -> size
                        }
                    } ?: "Desconocido"

                    val typeText = monster.type?.type?.jsonPrimitive?.contentOrNull?.removeSurrounding("\"")?.replaceFirstChar { it.uppercase() } ?: "Desconocido"

                    val alignmentText = monster.alignment?.joinToString(" ") { align ->
                        when (align.uppercase()) {
                            "L" -> "Legal"
                            "N" -> "Neutral"
                            "C" -> "Caótico"
                            "G" -> "Bueno"
                            "E" -> "Maligno"
                            "A" -> "Cualquier alineamiento"
                            else -> align
                        }
                    }?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""

                    Text(
                        text = "$sizeText $typeText$alignmentText",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = sourceMap[monster.source?.uppercase()] ?: monster.source ?: "Desconocido",
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