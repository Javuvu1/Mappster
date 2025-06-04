package com.javier.mappster.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.CustomMonster
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMonsterDetailScreen(navController: NavHostController, monsterId: String) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    var customMonster by remember { mutableStateOf<CustomMonster?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(monsterId) {
        coroutineScope.launch {
            try {
                val userId = authManager.getCurrentUserId()
                if (userId != null) {
                    val monsters = firestoreManager.getCustomMonsters(userId)
                    customMonster = monsters.find { it.id == monsterId }
                    isLoading = false
                    if (customMonster == null) {
                        errorMessage = "Monstruo no encontrado o no tienes acceso."
                    }
                } else {
                    errorMessage = "Debes iniciar sesión para ver este monstruo."
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar el monstruo: ${e.message}"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Monstruo Personalizado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else if (customMonster != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = customMonster!!.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    item {
                        customMonster!!.size?.let { size ->
                            Text("Tamaño: $size", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        customMonster!!.type?.joinToString(", ")?.let { type ->
                            Text("Tipo: $type", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        customMonster!!.alignment?.let { alignment ->
                            Text("Alineamiento: $alignment", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        customMonster!!.cr?.let { cr ->
                            Text("CR: $cr", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        customMonster!!.hp?.let { hp ->
                            Text("HP: $hp", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        customMonster!!.ac?.let { ac ->
                            Text("CA: $ac", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    // Añade más campos según necesites (str, dex, con, etc.)
                }
            } else {
                Text(
                    text = "Monstruo no encontrado",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}