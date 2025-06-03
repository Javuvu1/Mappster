package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.*
import com.javier.mappster.viewmodel.MonsterListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMonsterScreen(
    navController: NavHostController,
    authManager: AuthManager = AuthManager.getInstance(LocalContext.current),
    firestoreManager: FirestoreManager = FirestoreManager(),
    viewModel: MonsterListViewModel = viewModel(
        factory = MonsterListViewModelFactory(
            dataManager = LocalDataManager(LocalContext.current),
            authManager = AuthManager.getInstance(LocalContext.current)
        )
    )
) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("M") }
    var type by remember { mutableStateOf("") }
    var alignment by remember { mutableStateOf("N") }
    var cr by remember { mutableStateOf("") }
    var hp by remember { mutableStateOf("") }
    var ac by remember { mutableStateOf("") }
    var str by remember { mutableStateOf("") }
    var dex by remember { mutableStateOf("") }
    var con by remember { mutableStateOf("") }
    var int by remember { mutableStateOf("") }
    var wis by remember { mutableStateOf("") }
    var cha by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Monstruo", style = MaterialTheme.typography.headlineSmall) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tamaño
            OutlinedTextField(
                value = size,
                onValueChange = { size = it },
                label = { Text("Tamaño (T, S, M, L, H, G)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tipo
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Tipo (ej. Bestia, Dragón)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Alineamiento
            OutlinedTextField(
                value = alignment,
                onValueChange = { alignment = it },
                label = { Text("Alineamiento (L, N, C, G, E, A)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // CR
            OutlinedTextField(
                value = cr,
                onValueChange = { cr = it },
                label = { Text("CR (ej. 1, 1/2, 1/4)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // HP
            OutlinedTextField(
                value = hp,
                onValueChange = { hp = it },
                label = { Text("Puntos de Golpe (promedio)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // AC
            OutlinedTextField(
                value = ac,
                onValueChange = { ac = it },
                label = { Text("Clase de Armadura") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Puntuaciones de Habilidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = str,
                    onValueChange = { str = it },
                    label = { Text("FUE") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = dex,
                    onValueChange = { dex = it },
                    label = { Text("DES") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = con,
                    onValueChange = { con = it },
                    label = { Text("CON") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = int,
                    onValueChange = { int = it },
                    label = { Text("INT") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = wis,
                    onValueChange = { wis = it },
                    label = { Text("SAB") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = cha,
                    onValueChange = { cha = it },
                    label = { Text("CAR") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Mensaje de error
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botón de guardar
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "El nombre es obligatorio"
                        return@Button
                    }
                    if (hp.isBlank() || ac.isBlank() || cr.isBlank()) {
                        errorMessage = "HP, AC y CR son obligatorios"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    val monster = Monster(
                        name = name,
                        userId = authManager.getCurrentUserId(),
                        size = listOf(size.uppercase()),
                        type = MonsterType(type = JsonPrimitive(type.lowercase())),
                        alignment = listOf(alignment.uppercase()),
                        cr = ChallengeRating(value = cr),
                        hp = Hp(average = hp.toIntOrNull()),
                        ac = listOf(Ac(ac = ac.toIntOrNull())),
                        str = str.toIntOrNull(),
                        dex = dex.toIntOrNull(),
                        con = con.toIntOrNull(),
                        int = int.toIntOrNull(),
                        wis = wis.toIntOrNull(),
                        cha = cha.toIntOrNull(),
                        source = "Custom",
                        custom = true,
                        isNpc = false,
                        isNamedCreature = false
                    )

                    coroutineScope.launch {
                        try {
                            val userId = authManager.getCurrentUserId()
                            if (userId == null) {
                                errorMessage = "Debes estar autenticado para crear un monstruo"
                                isLoading = false
                                return@launch
                            }

                            val success = firestoreManager.createMonster(userId, monster)
                            isLoading = false
                            if (success) {
                                viewModel.refreshMonsters()
                                navController.popBackStack()
                            } else {
                                errorMessage = "Error al crear el monstruo. Puede que el nombre ya exista."
                            }
                        } catch (e: Exception) {
                            Log.e("CreateMonsterScreen", "Error al crear monstruo: ${e.message}", e)
                            errorMessage = "Error al crear el monstruo: ${e.message}"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Crear", fontSize = 16.sp)
                }
            }
        }
    }
}