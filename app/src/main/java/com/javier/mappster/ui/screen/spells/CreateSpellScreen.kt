package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.*
import com.javier.mappster.utils.normalizeSpellName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpellScreen(
    onSpellCreatedWithRefresh: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("0") }
    var school by remember { mutableStateOf("A") }
    var source by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var verbal by remember { mutableStateOf(false) }
    var somatic by remember { mutableStateOf(false) }
    var material by remember { mutableStateOf(false) }
    var materialText by remember { mutableStateOf("") }
    var timeNumber by remember { mutableStateOf("1") }
    var timeUnit by remember { mutableStateOf("action") }
    var durationType by remember { mutableStateOf("instantaneous") }
    var durationAmount by remember { mutableStateOf("") }
    var rangeType by remember { mutableStateOf("self") }
    var rangeAmount by remember { mutableStateOf("") }

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
    val levelOptions = (0..9).map { if (it == 0) "Truco" else "Nivel $it" }
    val timeUnitOptions = listOf("action", "bonus action", "reaction", "minute", "hour")
    val durationTypeOptions = listOf("instantaneous", "timed")
    val rangeTypeOptions = listOf("self", "touch", "ranged")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Hechizo") },
                navigationIcon = {
                    IconButton(onClick = { onSpellCreatedWithRefresh() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            var levelExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = levelExpanded,
                onExpandedChange = { levelExpanded = !levelExpanded }
            ) {
                OutlinedTextField(
                    value = levelOptions[level.toInt()],
                    onValueChange = {},
                    label = { Text("Nivel") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = levelExpanded,
                    onDismissRequest = { levelExpanded = false }
                ) {
                    levelOptions.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                level = index.toString()
                                levelExpanded = false
                            }
                        )
                    }
                }
            }

            var schoolExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = schoolExpanded,
                onExpandedChange = { schoolExpanded = !schoolExpanded }
            ) {
                OutlinedTextField(
                    value = schoolOptions.find { it.first == school }?.second ?: "",
                    onValueChange = {},
                    label = { Text("Escuela") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = schoolExpanded,
                    onDismissRequest = { schoolExpanded = false }
                ) {
                    schoolOptions.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                school = code
                                schoolExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Fuente (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = verbal,
                    onCheckedChange = { verbal = it },
                    modifier = Modifier.weight(1f)
                )
                Text("Verbal", modifier = Modifier.weight(2f))
                Checkbox(
                    checked = somatic,
                    onCheckedChange = { somatic = it },
                    modifier = Modifier.weight(1f)
                )
                Text("Somático", modifier = Modifier.weight(2f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = material,
                    onCheckedChange = { material = it },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = materialText,
                    onValueChange = { materialText = it },
                    label = { Text("Material") },
                    modifier = Modifier.weight(4f),
                    enabled = material
                )
            }

            var timeUnitExpanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = timeNumber,
                    onValueChange = { timeNumber = it.filter { it.isDigit() } },
                    label = { Text("Tiempo") },
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuBox(
                    expanded = timeUnitExpanded,
                    onExpandedChange = { timeUnitExpanded = !timeUnitExpanded }
                ) {
                    OutlinedTextField(
                        value = timeUnit,
                        onValueChange = {},
                        label = { Text("Unidad") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(2f)
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = timeUnitExpanded,
                        onDismissRequest = { timeUnitExpanded = false }
                    ) {
                        timeUnitOptions.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    timeUnit = unit
                                    timeUnitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            var durationExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = durationExpanded,
                onExpandedChange = { durationExpanded = !durationExpanded }
            ) {
                OutlinedTextField(
                    value = durationType,
                    onValueChange = {},
                    label = { Text("Duración") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = durationExpanded,
                    onDismissRequest = { durationExpanded = false }
                ) {
                    durationTypeOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                durationType = type
                                durationExpanded = false
                            }
                        )
                    }
                }
            }

            if (durationType == "timed") {
                OutlinedTextField(
                    value = durationAmount,
                    onValueChange = { durationAmount = it.filter { it.isDigit() } },
                    label = { Text("Cantidad (minutos)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            var rangeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = rangeExpanded,
                onExpandedChange = { rangeExpanded = !rangeExpanded }
            ) {
                OutlinedTextField(
                    value = rangeType,
                    onValueChange = {},
                    label = { Text("Rango") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = rangeExpanded,
                    onDismissRequest = { rangeExpanded = false }
                ) {
                    rangeTypeOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                rangeType = type
                                rangeExpanded = false
                            }
                        )
                    }
                }
            }

            if (rangeType == "ranged") {
                OutlinedTextField(
                    value = rangeAmount,
                    onValueChange = { rangeAmount = it.filter { it.isDigit() } },
                    label = { Text("Distancia (pies)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank()) {
                        errorMessage = "El nombre y la descripción son obligatorios"
                        return@Button
                    }

                    val userId = authManager.getCurrentUserId()
                    if (userId == null) {
                        errorMessage = "Usuario no autenticado. Por favor, inicia sesión."
                        return@Button
                    }

                    val normalizedId = normalizeSpellName(name)
                    if (normalizedId.isBlank()) {
                        errorMessage = "El nombre del hechizo no es válido"
                        return@Button
                    }

                    val spell = Spell(
                        name = name,
                        level = level.toInt(),
                        school = school,
                        source = source,
                        page = 0,
                        basicRules = false,
                        components = Components(
                            v = verbal,
                            s = somatic,
                            m = if (material) materialText else null
                        ),
                        time = listOf(Time(
                            number = timeNumber.toIntOrNull() ?: 1,
                            unit = timeUnit
                        )),
                        duration = listOf(Duration(
                            type = durationType,
                            duration = if (durationType == "timed") DurationX(
                                type = "minute",
                                amount = durationAmount.toIntOrNull() ?: 1
                            ) else null
                        )),
                        range = Range(
                            type = rangeType,
                            distance = Distance(
                                type = rangeType,
                                amount = rangeAmount.toIntOrNull()
                            )
                        ),
                        areaTags = emptyList(),
                        meta = Meta(),
                        miscTags = emptyList(),
                        hasFluff = false,
                        hasFluffImages = false,
                        entries = listOf(description),
                        entriesHigherLevel = emptyList(),
                        savingThrow = emptyList(),
                        spellAttack = emptyList(),
                        abilityCheck = emptyList(),
                        damageInflict = emptyList(),
                        damageResist = emptyList(),
                        damageVulnerable = emptyList(),
                        damageImmune = emptyList(),
                        conditionInflict = emptyList(),
                        conditionImmune = emptyList(),
                        affectsCreatureType = emptyList(),
                        subschools = emptyList(),
                        reprintedAs = emptyList(),
                        additionalSources = emptyList(),
                        otherSources = emptyList(),
                        userId = userId,
                        _custom = true,
                        _public = false
                    )

                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val success = firestoreManager.createSpell(normalizedId, spell)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    onSpellCreatedWithRefresh()
                                } else {
                                    errorMessage = "Error al crear el hechizo, pero puede haberse guardado. Verifica en la lista."
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Hechizo")
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}