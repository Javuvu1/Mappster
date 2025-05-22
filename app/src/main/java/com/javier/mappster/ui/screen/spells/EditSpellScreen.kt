package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.Spell
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSpellScreen(
    spell: Spell,
    viewModel: SpellListViewModel = provideSpellListViewModel(LocalContext.current),
    onSpellUpdated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf(spell.name) }
    var level by remember { mutableStateOf(spell.level.toString()) }
    var school by remember { mutableStateOf(spell.school) }
    var entries by remember { mutableStateOf(spell.entries.joinToString("\n") { it.toString() }) }
    var isPublic by remember { mutableStateOf(spell.public) }
    var source by remember { mutableStateOf(spell.source) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val firestoreManager = remember { FirestoreManager() }

    val schoolOptions = listOf(
        "A" to "Abjuración",
        "C" to "Conjuración",
        "D" to "Adivinación",
        "E" to "Encantamiento",
        "V" to "Evocación",
        "I" to "Ilusión",
        "N" to "Nigromancia",
        "T" to "Transmutación"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Hechizo") },
                navigationIcon = {
                    IconButton(onClick = onSpellUpdated) {
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Nombre no editable para mantener ID del documento
            )

            OutlinedTextField(
                value = level,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                        level = newValue
                    }
                },
                label = { Text("Nivel (0-9)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = schoolOptions.find { it.first == school }?.second ?: school,
                onValueChange = { newValue ->
                    school = schoolOptions.find { it.second == newValue }?.first ?: school
                },
                label = { Text("Escuela") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = entries,
                onValueChange = { entries = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Fuente") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
                Text("Público")
            }

            Button(
                onClick = {
                    if (level.isBlank() || school.isBlank() || entries.isBlank()) {
                        errorMessage = "Por favor, completa todos los campos obligatorios"
                        return@Button
                    }
                    val levelInt = level.toIntOrNull()
                    if (levelInt == null || levelInt !in 0..9) {
                        errorMessage = "El nivel debe ser un número entre 0 y 9"
                        return@Button
                    }
                    if (!schoolOptions.any { it.first == school }) {
                        errorMessage = "Selecciona una escuela válida"
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        val updatedSpell = Spell(
                            name = spell.name,
                            level = levelInt,
                            school = school,
                            source = source,
                            userId = spell.userId,
                            entries = entries.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                            _public = isPublic,
                            _custom = true
                        )
                        val success = firestoreManager.updateSpell(updatedSpell)
                        isLoading = false
                        if (success) {
                            viewModel.refreshSpells()
                            onSpellUpdated()
                        } else {
                            errorMessage = "Error al actualizar el hechizo"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Guardar Cambios")
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}