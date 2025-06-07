package com.javier.mappster.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
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
    var showLogoutDialog by remember { mutableStateOf(false) }

    NavigationBar(
        containerColor = Color(0xFF0D47A1), // Azul oscuro
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Hechizos") },
            label = {
                Text(
                    text = "Spells",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == Destinations.SPELL_LIST,
            onClick = {
                navController.navigate(Destinations.SPELL_LIST) {
                    if (currentRoute != Destinations.SPELL_LIST) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Listas Hechizos") },
            label = {
                Text(
                    text = "Spell Lists",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == Destinations.CUSTOM_SPELL_LISTS,
            onClick = {
                navController.navigate(Destinations.CUSTOM_SPELL_LISTS) {
                    if (currentRoute != Destinations.CUSTOM_SPELL_LISTS) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Pets, contentDescription = "Monstruos") },
            label = {
                Text(
                    text = "Monsters",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == Destinations.MONSTER_LIST,
            onClick = {
                navController.navigate(Destinations.MONSTER_LIST) {
                    if (currentRoute != Destinations.MONSTER_LIST) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        // Pendiente de implementar en un futuro, no borrar
        // NavigationBarItem(
        //     icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = "Listas Monstruos") },
        //     label = { Text("Monster Ls.") },
        //     selected = currentRoute == Destinations.CUSTOM_MONSTER_LISTS,
        //     onClick = {
        //         navController.navigate(Destinations.CUSTOM_MONSTER_LISTS) {
        //             if (currentRoute != Destinations.CUSTOM_MONSTER_LISTS) {
        //                 popUpTo(navController.graph.startDestinationId) { inclusive = false }
        //             }
        //             launchSingleTop = true
        //         }
        //     }
        // )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Initiative Tracker"
                )
            },
            label = {
                Text(
                    text = "Initiative",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == Destinations.INITIATIVE_TRACKER,
            onClick = {
                navController.navigate(Destinations.INITIATIVE_TRACKER) {
                    if (currentRoute != Destinations.INITIATIVE_TRACKER) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log Out") },
            label = {
                Text(
                    text = "C.Sesión",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == Destinations.LOGIN,
            onClick = {
                showLogoutDialog = true
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }

    // Diálogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres cerrar sesión?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authManager.signOut()
                        navController.navigate(Destinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF0D47A1))
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF0D47A1))
                ) {
                    Text("No")
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}