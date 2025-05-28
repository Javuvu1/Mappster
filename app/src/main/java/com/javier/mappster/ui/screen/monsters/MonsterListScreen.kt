package com.javier.mappster.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.javier.mappster.data.LocalDataManager
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

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (monsters.isEmpty()) {
                Text(text = "No monsters available. Check the JSON file.")
            } else {
                LazyColumn {
                    items(monsters) { monster ->
                        Text(
                            text = monster.name ?: "Unnamed Monster",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}