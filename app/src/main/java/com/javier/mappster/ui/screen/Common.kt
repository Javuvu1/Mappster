package com.javier.mappster.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.javier.mappster.navigation.Destinations

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Hechizos") },
            label = { Text("Hechizos") },
            selected = currentRoute == Destinations.SPELL_LIST,
            onClick = {
                navController.navigate(Destinations.SPELL_LIST) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PlaylistAdd, contentDescription = "Listas Hechizos") },
            label = { Text("Listas H.") },
            selected = currentRoute == Destinations.CUSTOM_SPELL_LISTS,
            onClick = {
                navController.navigate(Destinations.CUSTOM_SPELL_LISTS) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Pets, contentDescription = "Monstruos") },
            label = { Text("Monstruos") },
            selected = currentRoute == Destinations.MONSTER_LIST,
            onClick = {
                navController.navigate(Destinations.MONSTER_LIST) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PlaylistPlay, contentDescription = "Listas Monstruos") },
            label = { Text("Listas M.") },
            selected = currentRoute == Destinations.CUSTOM_MONSTER_LISTS,
            onClick = {
                navController.navigate(Destinations.CUSTOM_MONSTER_LISTS) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }
}