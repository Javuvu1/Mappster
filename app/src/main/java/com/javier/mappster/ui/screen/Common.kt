package com.javier.mappster.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.javier.mappster.data.AuthManager
import com.javier.mappster.navigation.Destinations

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Hechizos") },
            label = { Text("Hechizos") },
            selected = currentRoute == Destinations.SPELL_LIST,
            onClick = {
                navController.navigate(Destinations.SPELL_LIST) {
                    if (currentRoute != Destinations.SPELL_LIST) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Listas Hechizos") },
            label = { Text("Listas H.") },
            selected = currentRoute == Destinations.CUSTOM_SPELL_LISTS,
            onClick = {
                navController.navigate(Destinations.CUSTOM_SPELL_LISTS) {
                    if (currentRoute != Destinations.CUSTOM_SPELL_LISTS) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
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
                    if (currentRoute != Destinations.MONSTER_LIST) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = "Listas Monstruos") },
            label = { Text("Listas M.") },
            selected = currentRoute == Destinations.CUSTOM_MONSTER_LISTS,
            onClick = {
                navController.navigate(Destinations.CUSTOM_MONSTER_LISTS) {
                    if (currentRoute != Destinations.CUSTOM_MONSTER_LISTS) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión") },
            label = { Text("C.Sesión") },
            selected = currentRoute == Destinations.LOGIN,
            onClick = {
                authManager.signOut()
                navController.navigate(Destinations.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}