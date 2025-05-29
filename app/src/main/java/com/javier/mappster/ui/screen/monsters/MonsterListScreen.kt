package com.javier.mappster.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.Monster
import com.javier.mappster.utils.sourceMap
import com.javier.mappster.viewmodel.MonsterListViewModel

@Composable
fun MonsterListScreen(
    navController: NavHostController,
    dataManager: LocalDataManager
) {
    val viewModel: MonsterListViewModel = viewModel {
        MonsterListViewModel(dataManager)
    }
    val monsters = viewModel.monsters.collectAsState().value
    val searchQuery = viewModel.searchQuery.collectAsState().value

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (monsters.isEmpty()) {
                Text(
                    text = if (searchQuery.isEmpty()) "No monsters available. Check the JSON file." else "No monsters found for \"$searchQuery\"",
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
                        top = 8.dp, // Reducimos el padding superior para acercar el primer item
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(monsters) { monster ->
                        MonsterItem(monster = monster)
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
                contentDescription = "Search",
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = { Text("Search monsters...") },
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
fun MonsterItem(monster: Monster) {

    val defaultColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { /* TODO: Añadir navegación a detalle si se desea */ }
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = monster.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp
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
                            color = defaultColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Monster Icon",
                        modifier = Modifier.size(16.dp),
                        tint = defaultColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Monster",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = defaultColor.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = sourceMap[monster.source?.uppercase()] ?: monster.source ?: "Unknown",
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