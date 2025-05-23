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
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.*
import com.javier.mappster.navigation.Destinations
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
            color = Color(0xFF3469FA) // Azul oscuro
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF3469FA)) // Azul oscuro
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpellScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf("0") }
    var school by remember { mutableStateOf("A") }
    var source by remember { mutableStateOf("") }
    var sourceError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var verbal by remember { mutableStateOf(false) }
    var somatic by remember { mutableStateOf(false) }
    var material by remember { mutableStateOf(false) }
    var materialText by remember { mutableStateOf("") }
    var materialError by remember { mutableStateOf<String?>(null) }
    var timeNumber by remember { mutableStateOf("1") }
    var timeUnit by remember { mutableStateOf("action") }
    var durationType by remember { mutableStateOf("instantaneous") }
    var durationAmount by remember { mutableStateOf("") }
    var rangeType by remember { mutableStateOf("self") }
    var rangeAmount by remember { mutableStateOf("") }
    var rangeAmountError by remember { mutableStateOf<String?>(null) }
    var rangeAreaType by remember { mutableStateOf("") }
    var rangeAreaTypeError by remember { mutableStateOf<String?>(null) }
    var classList by remember { mutableStateOf("") }
    var classListError by remember { mutableStateOf<String?>(null) }
    var subclassList by remember { mutableStateOf("") }
    var subclassListError by remember { mutableStateOf<String?>(null) }
    var feats by remember { mutableStateOf("") }
    var featsError by remember { mutableStateOf<String?>(null) }
    var backgrounds by remember { mutableStateOf("") }
    var backgroundsError by remember { mutableStateOf<String?>(null) }
    var races by remember { mutableStateOf("") }
    var racesError by remember { mutableStateOf<String?>(null) }
    var optionalFeatures by remember { mutableStateOf("") }
    var optionalFeaturesError by remember { mutableStateOf<String?>(null) }

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
        classListError = when {
            classList.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
        subclassListError = when {
            subclassList.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
        featsError = when {
            feats.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
        backgroundsError = when {
            backgrounds.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
        racesError = when {
            races.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
        optionalFeaturesError = when {
            optionalFeatures.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
    }

    // Estado del botón con remember
    val isFormValid by remember(
        nameError, sourceError, descriptionError, materialError,
        rangeAmountError, rangeAreaTypeError, classListError,
        subclassListError, featsError, backgroundsError,
        racesError, optionalFeaturesError
    ) {
        derivedStateOf {
            nameError == null &&
                    sourceError == null &&
                    descriptionError == null &&
                    materialError == null &&
                    rangeAmountError == null &&
                    rangeAreaTypeError == null &&
                    classListError == null &&
                    subclassListError == null &&
                    featsError == null &&
                    backgroundsError == null &&
                    racesError == null &&
                    optionalFeaturesError == null
        }
    }

    LaunchedEffect(name, source, description, materialText, rangeAmount, rangeAreaType, classList, subclassList, feats, backgrounds, races, optionalFeatures) {
        validateFields()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Hechizo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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

                    val userId = authManager.getCurrentUserId()
                    if (userId == null) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Usuario no autenticado. Por favor, inicia sesión.")
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

                    // Parse classList
                    val classEntries = classList.split(",").map { it.trim() }.filter { it.isNotBlank() }.map {
                        ClassEntry(name = it, source = "Custom")
                    }

                    // Parse subclassList
                    val subclassEntries = subclassList.split(",").map { it.trim() }.filter { it.isNotBlank() }.mapNotNull {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            SubclassEntry(
                                classEntry = ClassEntry(name = parts[0], source = "Custom"),
                                subclass = SubclassDetail(name = parts[1], source = "Custom")
                            )
                        } else null
                    }

                    // Parse feats, backgrounds, races, optionalFeatures
                    val featList = feats.split(",").map { it.trim() }.filter { it.isNotBlank() }.map {
                        Feat(name = it, source = "Custom")
                    }
                    val backgroundList = backgrounds.split(",").map { it.trim() }.filter { it.isNotBlank() }.map {
                        Background(name = it, source = "Custom")
                    }
                    val raceList = races.split(",").map { it.trim() }.filter { it.isNotBlank() }.map {
                        Race(name = it, source = "Custom")
                    }
                    val optionalFeatureList = optionalFeatures.split(",").map { it.trim() }.filter { it.isNotBlank() }.map {
                        OptionalFeature(name = it, source = "Custom", featureType = listOf("Custom"))
                    }

                    // Parse rangeAreaType
                    val areaTags = if (rangeType == "ranged" && rangeAreaType.isNotBlank()) {
                        listOf(rangeAreaType.trim())
                    } else {
                        emptyList()
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
                        areaTags = areaTags,
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
                        classes = Classes(fromClassList = classEntries, fromSubclass = subclassEntries),
                        feats = featList,
                        backgrounds = backgroundList,
                        races = raceList,
                        optionalFeatures = optionalFeatureList,
                        userId = userId,
                        _custom = true,
                        _public = false
                    )

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val success = firestoreManager.createSpell(normalizedId, spell)
                            if (success) {
                                snackbarHostState.showSnackbar("Hechizo creado exitosamente")
                                navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
                            } else {
                                snackbarHostState.showSnackbar("Error: El hechizo ya existe")
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
                Text("Crear Hechizo")
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
                            onValueChange = { if (it.length <= 35) name = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
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
                                value = levelOptions[level.toInt()],
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
                                value = schoolOptions.find { it.first == school }?.second ?: "",
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
                            value = classList,
                            onValueChange = { if (it.length <= 50) classList = it },
                            label = { Text("Clases (opcional, separadas por comas)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Sorcerer,Wizard") },
                            isError = classListError != null,
                            trailingIcon = {
                                Text(
                                    text = "${classList.length}/50",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        classListError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedTextField(
                            value = subclassList,
                            onValueChange = { if (it.length <= 50) subclassList = it },
                            label = { Text("Subclases (opcional, formato Clase:Subclase)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Cleric:Arcana Domain,Fighter:Eldritch Knight") },
                            isError = subclassListError != null,
                            trailingIcon = {
                                Text(
                                    text = "${subclassList.length}/50",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        subclassListError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedTextField(
                            value = feats,
                            onValueChange = { if (it.length <= 50) feats = it },
                            label = { Text("Hazañas (opcional, separadas por comas)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Magic Initiate,Artificer Initiate") },
                            isError = featsError != null,
                            trailingIcon = {
                                Text(
                                    text = "${feats.length}/50",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        featsError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedTextField(
                            value = backgrounds,
                            onValueChange = { if (it.length <= 50) backgrounds = it },
                            label = { Text("Trasfondos (opcional, separadas por comas)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Simic Scientist,Sage") },
                            isError = backgroundsError != null,
                            trailingIcon = {
                                Text(
                                    text = "${backgrounds.length}/50",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        backgroundsError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedTextField(
                            value = races,
                            onValueChange = { if (it.length <= 50) races = it },
                            label = { Text("Razas (opcional, separadas por comas)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Elf (High),Genasi (Water)") },
                            isError = racesError != null,
                            trailingIcon = {
                                Text(
                                    text = "${races.length}/50",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        racesError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedTextField(
                            value = optionalFeatures,
                            onValueChange = { if (it.length <= 50) optionalFeatures = it },
                            label = { Text("Características Opcionales (opcional, separadas por comas)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Pact of the Tome") },
                            isError = optionalFeaturesError != null,
                            trailingIcon = {
                                Text(
                                    text = "${optionalFeatures.length}/50",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        optionalFeaturesError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}