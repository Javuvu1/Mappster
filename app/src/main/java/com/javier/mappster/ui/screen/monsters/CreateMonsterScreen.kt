package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.model.CustomMonster
import androidx.compose.ui.platform.LocalContext
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
            color = Color(0xFF3469FA)
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF3469FA))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMonsterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var size by remember { mutableStateOf("Medium") }
    var type1 by remember { mutableStateOf("humanoid") }
    var type2 by remember { mutableStateOf("") }
    var type2Error by remember { mutableStateOf<String?>(null) }
    var alignment by remember { mutableStateOf("Neutral") }
    var cr by remember { mutableStateOf("0") }
    var hp by remember { mutableStateOf("") }
    var hpError by remember { mutableStateOf<String?>(null) }
    var ac by remember { mutableStateOf("") }
    var acError by remember { mutableStateOf<String?>(null) }
    var str by remember { mutableStateOf("") }
    var dex by remember { mutableStateOf("") }
    var con by remember { mutableStateOf("") }
    var int by remember { mutableStateOf("") }
    var wis by remember { mutableStateOf("") }
    var cha by remember { mutableStateOf("") }

    val sizeOptions = listOf("Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan")
    val type1Options = listOf("Aberration", "Beast", "Celestial", "Construct", "Dragon", "Elemental", "Fey", "Fiend", "Giant", "Humanoid", "Monstrosity", "Ooze", "Plant", "Undead")
    val alignmentOptions = listOf("Lawful Good", "Neutral Good", "Chaotic Good", "Lawful Neutral", "Neutral", "Chaotic Neutral", "Lawful Evil", "Neutral Evil", "Chaotic Evil", "Unaligned")
    val crOptions = listOf("0", "1/8", "1/4", "1/2", "1") + (2..30).map { it.toString() }

    fun validateFields() {
        nameError = when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length > 35 -> "Máximo 35 caracteres"
            else -> null
        }
        type2Error = when {
            type2.length > 20 -> "Máximo 20 caracteres"
            else -> null
        }
        hpError = when {
            hp.isNotBlank() && !hp.matches(Regex("\\d{1,6}")) -> "Solo números, máximo 6 dígitos"
            else -> null
        }
        acError = when {
            ac.isNotBlank() && !ac.matches(Regex("\\d{1,2}")) -> "Solo números, máximo 2 dígitos"
            else -> null
        }
    }

    val isFormValid by remember(nameError, type2Error, hpError, acError) {
        derivedStateOf {
            nameError == null && type2Error == null && hpError == null && acError == null
        }
    }

    LaunchedEffect(name, type2, hp, ac) {
        validateFields()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Monstruo Personalizado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1)
                )
            )
        },
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    Color(0xFF0D47A1).copy(alpha = 0.5f)
                )
            )
        ),
        bottomBar = {
            Button(
                onClick = {
                    validateFields()
                    if (!isFormValid) return@Button

                    val userId = authManager.getCurrentUserId()
                    if (userId == null) {
                        errorMessage = "Debes iniciar sesión para guardar."
                        return@Button
                    }

                    val customMonster = CustomMonster(
                        userId = userId,
                        name = name,
                        size = size,
                        type = listOfNotNull(type1, type2.takeIf { it.isNotBlank() }),
                        alignment = alignment,
                        cr = cr,
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
                    isSaving = true
                    coroutineScope.launch {
                        try {
                            firestoreManager.saveCustomMonster(customMonster)
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMessage = "Error al guardar: ${e.message}"
                            Log.e("CreateMonsterScreen", "Error saving monster: ${e.message}", e)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !isSaving && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Guardar Monstruo")
            }
        }
    ) { paddingValues ->
        if (isSaving) {
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
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

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
                        nameError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                        var sizeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sizeExpanded,
                            onExpandedChange = { sizeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = size,
                                onValueChange = {},
                                label = { Text("Tamaño") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = sizeExpanded,
                                onDismissRequest = { sizeExpanded = false }
                            ) {
                                sizeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            size = option
                                            sizeExpanded = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        var type1Expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = type1Expanded,
                            onExpandedChange = { type1Expanded = it }
                        ) {
                            OutlinedTextField(
                                value = type1,
                                onValueChange = {},
                                label = { Text("Tipo Principal") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = type1Expanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = type1Expanded,
                                onDismissRequest = { type1Expanded = false }
                            ) {
                                type1Options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            type1 = option
                                            type1Expanded = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = type2,
                            onValueChange = { if (it.length <= 20) type2 = it },
                            label = { Text("Subtipo (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = type2Error != null,
                            trailingIcon = {
                                Text(
                                    text = "${type2.length}/20",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        type2Error?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                        var alignmentExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = alignmentExpanded,
                            onExpandedChange = { alignmentExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = alignment,
                                onValueChange = {},
                                label = { Text("Alineamiento") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alignmentExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = alignmentExpanded,
                                onDismissRequest = { alignmentExpanded = false }
                            ) {
                                alignmentOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            alignment = option
                                            alignmentExpanded = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        var crExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = crExpanded,
                            onExpandedChange = { crExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = cr,
                                onValueChange = {},
                                label = { Text("CR") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = crExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = crExpanded,
                                onDismissRequest = { crExpanded = false }
                            ) {
                                crOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            cr = option
                                            crExpanded = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Estadísticas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Estadísticas", Icons.Default.Settings)

                        OutlinedTextField(
                            value = hp,
                            onValueChange = { if (it.length <= 6) hp = it.filter { it.isDigit() } },
                            label = { Text("HP") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = hpError != null,
                            trailingIcon = {
                                Text(
                                    text = "${hp.length}/6",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        hpError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                        OutlinedTextField(
                            value = ac,
                            onValueChange = { if (it.length <= 2) ac = it.filter { it.isDigit() } },
                            label = { Text("CA") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = acError != null,
                            trailingIcon = {
                                Text(
                                    text = "${ac.length}/2",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        acError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = str,
                                onValueChange = { str = it.filter { it.isDigit() } },
                                label = { Text("Fuerza") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = dex,
                                onValueChange = { dex = it.filter { it.isDigit() } },
                                label = { Text("Destreza") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = con,
                                onValueChange = { con = it.filter { it.isDigit() } },
                                label = { Text("Constitución") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = int,
                                onValueChange = { int = it.filter { it.isDigit() } },
                                label = { Text("Inteligencia") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = wis,
                                onValueChange = { wis = it.filter { it.isDigit() } },
                                label = { Text("Sabiduría") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = cha,
                                onValueChange = { cha = it.filter { it.isDigit() } },
                                label = { Text("Carisma") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }
        }
    }
}