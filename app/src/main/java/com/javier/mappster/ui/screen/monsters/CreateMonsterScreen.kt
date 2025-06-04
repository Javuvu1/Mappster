package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.CustomMonster
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMonsterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var alignment by remember { mutableStateOf("") }
    var cr by remember { mutableStateOf("") }
    var hp by remember { mutableStateOf("") }
    var ac by remember { mutableStateOf("") }
    var str by remember { mutableStateOf("") }
    var dex by remember { mutableStateOf("") }
    var con by remember { mutableStateOf("") }
    var int by remember { mutableStateOf("") }
    var wis by remember { mutableStateOf("") }
    var cha by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Monstruo Personalizado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                coroutineScope.launch {
                                    isSaving = true
                                    try {
                                        val userId = authManager.getCurrentUserId()
                                        Log.d("CreateMonsterScreen", "User ID: $userId")
                                        if (userId != null) {
                                            val customMonster = CustomMonster(
                                                userId = userId,
                                                name = name,
                                                size = size.takeIf { it.isNotBlank() },
                                                type = type.split(",").map { it.trim() }.takeIf { it.isNotEmpty() },
                                                alignment = alignment.takeIf { it.isNotBlank() },
                                                cr = cr.takeIf { it.isNotBlank() },
                                                hp = hp.toIntOrNull(),
                                                ac = ac.takeIf { it.isNotBlank() },
                                                str = str.toIntOrNull(),
                                                dex = dex.toIntOrNull(),
                                                con = con.toIntOrNull(),
                                                int = int.toIntOrNull(),
                                                wis = wis.toIntOrNull(),
                                                cha = cha.toIntOrNull(),
                                                source = "Custom",
                                                public = false
                                            )
                                            Log.d("CreateMonsterScreen", "Saving CustomMonster: $customMonster")
                                            firestoreManager.saveCustomMonster(customMonster)
                                            navController.popBackStack()
                                        } else {
                                            errorMessage = "Debes iniciar sesión para guardar."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error al guardar: ${e.message}"
                                        Log.e("CreateMonsterScreen", "Error saving monster: ${e.message}", e)
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            } else {
                                errorMessage = "El nombre es obligatorio."
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar",
                            tint = if (isSaving) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onPrimary
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = size,
                onValueChange = { size = it },
                label = { Text("Tamaño") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Tipo (separar con comas)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = alignment,
                onValueChange = { alignment = it },
                label = { Text("Alineamiento") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cr,
                onValueChange = { cr = it },
                label = { Text("CR") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = hp,
                onValueChange = { hp = it },
                label = { Text("HP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ac,
                onValueChange = { ac = it },
                label = { Text("CA") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = str,
                onValueChange = { str = it },
                label = { Text("Fuerza") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dex,
                onValueChange = { dex = it },
                label = { Text("Destreza") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = con,
                onValueChange = { con = it },
                label = { Text("Constitución") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = int,
                onValueChange = { int = it },
                label = { Text("Inteligencia") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = wis,
                onValueChange = { wis = it },
                label = { Text("Sabiduría") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cha,
                onValueChange = { cha = it },
                label = { Text("Carisma") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (isSaving) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}