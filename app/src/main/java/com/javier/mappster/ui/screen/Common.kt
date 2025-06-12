package com.javier.mappster.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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

    val colorScheme = MaterialTheme.colorScheme

    // Contenedor principal con bordes redondeados solo arriba
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp)
            .background(
                color = colorScheme.primaryContainer,
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .clip(
                RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
    ) {
        NavigationBar(
            containerColor = Color.Transparent, // Hacemos transparente el fondo del NavigationBar
            contentColor = colorScheme.onPrimaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

            NavigationBarItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Hechizos") },
                label = {
                    Text("Spells", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimaryContainer,
                    selectedTextColor = colorScheme.onPrimaryContainer,
                    unselectedIconColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    unselectedTextColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    indicatorColor = colorScheme.primary.copy(alpha = 0.3f)
                )
            )

            NavigationBarItem(
                icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Listas Hechizos") },
                label = {
                    Text("Spell Lists", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimaryContainer,
                    selectedTextColor = colorScheme.onPrimaryContainer,
                    unselectedIconColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    unselectedTextColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    indicatorColor = colorScheme.primary.copy(alpha = 0.3f)
                )
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.Pets, contentDescription = "Monstruos") },
                label = {
                    Text("Monsters", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimaryContainer,
                    selectedTextColor = colorScheme.onPrimaryContainer,
                    unselectedIconColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    unselectedTextColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    indicatorColor = colorScheme.primary.copy(alpha = 0.3f)
                )
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.Timer, contentDescription = "Initiative Tracker") },
                label = {
                    Text("Initiative", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimaryContainer,
                    selectedTextColor = colorScheme.onPrimaryContainer,
                    unselectedIconColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    unselectedTextColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    indicatorColor = colorScheme.primary.copy(alpha = 0.3f)
                )
            )

            NavigationBarItem(
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log Out") },
                label = {
                    Text("C.Sesión", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                selected = currentRoute == Destinations.LOGIN,
                onClick = { showLogoutDialog = true },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimaryContainer,
                    selectedTextColor = colorScheme.onPrimaryContainer,
                    unselectedIconColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    unselectedTextColor = colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    indicatorColor = colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres cerrar sesión?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
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
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.primary
                    )
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.primary
                    )
                ) {
                    Text("No")
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = colorScheme.surface
        )
    }
}
