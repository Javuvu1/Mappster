package com.javier.mappster.ui.screen.spells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.*
import com.javier.mappster.utils.normalizeSpellName
import kotlinx.coroutines.launch

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF0D47A1) // Azul oscuro
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF0D47A1)) // Azul oscuro
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSpellScreen(
    spell: Spell,
    viewModel: SpellListViewModel = provideSpellListViewModel(LocalContext.current),
    onSpellUpdated: () -> Unit
) {
    val context = LocalContext.current
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(spell.name) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf(spell.level.toString()) }
    var school by remember { mutableStateOf(spell.school) }
    var source by remember { mutableStateOf(spell.source) }
    var sourceError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf(spell.entries.joinToString("\n") { it.toString() }) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var verbal by remember { mutableStateOf(spell.components.v ?: false) }
    var somatic by remember { mutableStateOf(spell.components.s ?: false) }
    var material by remember { mutableStateOf(spell.components.m != null) }
    var materialText by remember { mutableStateOf(spell.components.m ?: "") }
    var materialError by remember { mutableStateOf<String?>(null) }
    var timeNumber by remember { mutableStateOf(spell.time.firstOrNull()?.number?.toString() ?: "1") }
    var timeUnit by remember { mutableStateOf(spell.time.firstOrNull()?.unit ?: "action") }
    var durationType by remember { mutableStateOf(spell.duration.firstOrNull()?.type ?: "instantaneous") }
    var durationAmount by remember { mutableStateOf(spell.duration.firstOrNull()?.duration?.amount?.toString() ?: "") }
    var rangeType by remember { mutableStateOf(spell.range.type) }
    var rangeAmount by remember { mutableStateOf(spell.range.distance.amount?.toString() ?: "") }
    var rangeAmountError by remember { mutableStateOf<String?>(null) }
    var rangeAreaType by remember { mutableStateOf(spell.areaTags.firstOrNull() ?: "") }
    var rangeAreaTypeError by remember { mutableStateOf<String?>(null) }
    var customAccess by remember { mutableStateOf(spell.customAccess) }
    var customAccessError by remember { mutableStateOf<String?>(null) }
    var customHigherLevel by remember { mutableStateOf(spell.customHigherLevel) }
    var customHigherLevelError by remember { mutableStateOf<String?>(null) }
    var isPublic by remember { mutableStateOf(spell.public ?: false) }

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
    var durationTypeOptions = listOf("instantaneous", "timed")
    var rangeTypeOptions = listOf("self", "touch", "ranged")

    // Validaciones
    fun validateFields() {
        nameError = when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length > 35 -> "Máximo 35 caracteres"
            else -> null
        }
        sourceError = when {
            source.length > 30 -> "Máximo 30 caracteres"
            else -> null
        }
        descriptionError = when {
            description.isBlank() -> "La descripción es obligatoria"
            description.length > 600 -> "Máximo 600 caracteres"
            else -> null
        }
        materialError = when {
            material && materialText.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
        rangeAmountError = when {
            rangeType == "ranged" && rangeAmount.isNotBlank() && !rangeAmount.matches(Regex("\\d{1,6}")) -> "Solo números, máximo 6 dígitos"
            else -> null
        }
        rangeAreaTypeError = when {
            rangeType == "ranged" && rangeAreaType.length > 15 -> "Máximo 15 caracteres"
            else -> null
        }
        customAccessError = when {
            customAccess.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
        customHigherLevelError = when {
            customHigherLevel.length > 300 -> "Máximo 300 caracteres"
            else -> null
        }
    }

    // Estado del botón con remember
    val isFormValid by remember(
        nameError, sourceError, descriptionError, materialError,
        rangeAmountError, rangeAreaTypeError, customAccessError,
        customHigherLevelError
    ) {
        derivedStateOf {
            nameError == null &&
                    sourceError == null &&
                    descriptionError == null &&
                    materialError == null &&
                    rangeAmountError == null &&
                    rangeAreaTypeError == null &&
                    customAccessError == null &&
                    customHigherLevelError == null
        }
    }

    LaunchedEffect(
        name, source, description, materialText, rangeAmount, rangeAreaType,
        customAccess, customHigherLevel
    ) {
        validateFields()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Hechizo") },
                navigationIcon = {
                    IconButton(onClick = onSpellUpdated) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1) // Azul oscuro
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    Color(0xFF0D47A1).copy(alpha = 0.5f) // Azul oscuro
                )
            )
        ),
        bottomBar = {
            Button(
                onClick = {
                    validateFields()
                    if (!isFormValid) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Corrige los errores en el formulario")
                        }
                        return@Button
                    }

                    val normalizedId = normalizeSpellName(name)
                    if (normalizedId.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("El nombre del hechizo no es válido")
                        }
                        return@Button
                    }

                    // Parse rangeAreaType
                    val areaTags = if (rangeType == "ranged" && rangeAreaType.isNotBlank()) {
                        listOf(rangeAreaType.trim())
                    } else {
                        emptyList()
                    }

                    val updatedSpell = Spell(
                        name = spell.name, // Mantener nombre original
                        level = level.toIntOrNull() ?: spell.level,
                        school = school,
                        source = source,
                        page = spell.page,
                        basicRules = spell.basicRules,
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
                        areaTags = areaTags,
                        meta = spell.meta,
                        miscTags = spell.miscTags,
                        hasFluff = spell.hasFluff,
                        hasFluffImages = spell.hasFluffImages,
                        entries = description.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                        entriesHigherLevel = spell.entriesHigherLevel,
                        customAccess = customAccess,
                        customHigherLevel = customHigherLevel,
                        savingThrow = spell.savingThrow,
                        spellAttack = spell.spellAttack,
                        abilityCheck = spell.abilityCheck,
                        damageInflict = spell.damageInflict,
                        damageResist = spell.damageResist,
                        damageVulnerable = spell.damageVulnerable,
                        damageImmune = spell.damageImmune,
                        conditionInflict = spell.conditionInflict,
                        conditionImmune = spell.conditionImmune,
                        affectsCreatureType = spell.affectsCreatureType,
                        subschools = spell.subschools,
                        reprintedAs = spell.reprintedAs,
                        additionalSources = spell.additionalSources,
                        otherSources = spell.otherSources,
                        classes = Classes(fromClassList = emptyList(), fromSubclass = emptyList()),
                        feats = emptyList(),
                        backgrounds = emptyList(),
                        races = emptyList(),
                        optionalFeatures = emptyList(),
                        userId = spell.userId,
                        _custom = spell.custom,
                        _public = isPublic
                    )

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val success = firestoreManager.updateSpell(updatedSpell)
                            if (success) {
                                viewModel.refreshSpellsPublic()
                                snackbarHostState.showSnackbar("Hechizo actualizado exitosamente")
                                onSpellUpdated()
                            } else {
                                snackbarHostState.showSnackbar("Error al actualizar el hechizo")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !isLoading && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1), // Azul oscuro
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Guardar Cambios")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información General
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Información General", Icons.Default.Info)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { }, // No editable
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            isError = nameError != null,
                            trailingIcon = {
                                Text(
                                    text = "${name.length}/35",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        nameError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        var levelExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = levelExpanded,
                            onExpandedChange = { levelExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = levelOptions[level.toIntOrNull() ?: 0],
                                onValueChange = {},
                                label = { Text("Nivel") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) }
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
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        var schoolExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = schoolExpanded,
                            onExpandedChange = { schoolExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = schoolOptions.find { it.first == school }?.second ?: school,
                                onValueChange = {},
                                label = { Text("Escuela") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded) }
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
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = source,
                            onValueChange = { if (it.length <= 30) source = it },
                            label = { Text("Fuente (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = sourceError != null,
                            trailingIcon = {
                                Text(
                                    text = "${source.length}/30",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        sourceError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Descripción
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Descripción", Icons.Default.Description)

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 600) description = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 5,
                            isError = descriptionError != null,
                            trailingIcon = {
                                Text(
                                    text = "${description.length}/600",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        descriptionError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Componentes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Componentes", Icons.Default.Build)

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
                                onValueChange = { if (it.length <= 50) materialText = it },
                                label = { Text("Material") },
                                modifier = Modifier.weight(4f),
                                enabled = material,
                                isError = materialError != null,
                                trailingIcon = {
                                    Text(
                                        text = "${materialText.length}/50",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                        materialError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Mecánicas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Mecánicas", Icons.Default.Settings)

                        var timeUnitExpanded by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = timeNumber,
                                onValueChange = { timeNumber = it.filter { it.isDigit() } },
                                label = { Text("Tiempo") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            ExposedDropdownMenuBox(
                                expanded = timeUnitExpanded,
                                onExpandedChange = { timeUnitExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = timeUnit,
                                    onValueChange = {},
                                    label = { Text("Unidad") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .weight(2f)
                                        .menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeUnitExpanded) }
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
                                            },
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        var durationExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = durationExpanded,
                            onExpandedChange = { durationExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = durationType,
                                onValueChange = {},
                                label = { Text("Duración") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded) }
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
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (durationType == "timed") {
                            OutlinedTextField(
                                value = durationAmount,
                                onValueChange = { durationAmount = it.filter { it.isDigit() } },
                                label = { Text("Cantidad (minutos)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        var rangeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = rangeExpanded,
                            onExpandedChange = { rangeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = rangeType,
                                onValueChange = {},
                                label = { Text("Rango") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rangeExpanded) }
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
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (rangeType == "ranged") {
                            OutlinedTextField(
                                value = rangeAmount,
                                onValueChange = { if (it.length <= 6) rangeAmount = it.filter { it.isDigit() } },
                                label = { Text("Distancia (pies)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = rangeAmountError != null,
                                trailingIcon = {
                                    Text(
                                        text = "${rangeAmount.length}/6",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            rangeAmountError?.let {
                                Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }

                            OutlinedTextField(
                                value = rangeAreaType,
                                onValueChange = { if (it.length <= 15) rangeAreaType = it },
                                label = { Text("Tipo de Área (opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = rangeAreaTypeError != null,
                                placeholder = { Text("Ej: cono, línea, radio, etc.") },
                                trailingIcon = {
                                    Text(
                                        text = "${rangeAreaType.length}/15",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            rangeAreaTypeError?.let {
                                Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Acceso
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Acceso", Icons.Default.Group)

                        OutlinedTextField(
                            value = customAccess,
                            onValueChange = { if (it.length <= 100) customAccess = it },
                            label = { Text("Acceso (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Sorcerer, Wizard, Cleric:Arcana Domain, Magic Initiate") },
                            isError = customAccessError != null,
                            trailingIcon = {
                                Text(
                                    text = "${customAccess.length}/100",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        customAccessError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "Especifica clases, subclases, hazañas, trasfondos, razas o características que permiten usar este hechizo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
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
                    }
                }

                // A nivel superior
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("A nivel superior", Icons.Default.Upgrade)

                        OutlinedTextField(
                            value = customHigherLevel,
                            onValueChange = { if (it.length <= 300) customHigherLevel = it },
                            label = { Text("A nivel superior (opcional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 4,
                            isError = customHigherLevelError != null,
                            placeholder = { Text("Ej: El daño aumenta en {@damage 1d6} por cada nivel superior.") },
                            trailingIcon = {
                                Text(
                                    text = "${customHigherLevel.length}/300",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        customHigherLevelError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "Usa {@damage NdM} para dados interactivos, {@condition nombre} para condiciones, etc.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}