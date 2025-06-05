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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.CustomMonster
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.viewmodel.MonsterListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

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
    val dataManager = remember { LocalDataManager(context) }
    val viewModel: MonsterListViewModel = viewModel(
        factory = MonsterListViewModelFactory(dataManager, authManager)
    )
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var size by remember { mutableStateOf("Medium") }
    var type1 by remember { mutableStateOf("Humanoid") }
    var type2 by remember { mutableStateOf("") }
    var type2Error by remember { mutableStateOf<String?>(null) }
    var alignment by remember { mutableStateOf("Neutral") }
    var cr by remember { mutableStateOf("0") }
    var hp by remember { mutableStateOf("") }
    var hpError by remember { mutableStateOf<String?>(null) }
    var ac by remember { mutableStateOf("") }
    var acError by remember { mutableStateOf<String?>(null) }
    var str by remember { mutableStateOf("") }
    var strError by remember { mutableStateOf<String?>(null) }
    var dex by remember { mutableStateOf("") }
    var dexError by remember { mutableStateOf<String?>(null) }
    var con by remember { mutableStateOf("") }
    var conError by remember { mutableStateOf<String?>(null) }
    var int by remember { mutableStateOf("") }
    var intError by remember { mutableStateOf<String?>(null) }
    var wis by remember { mutableStateOf("") }
    var wisError by remember { mutableStateOf<String?>(null) }
    var cha by remember { mutableStateOf("") }
    var chaError by remember { mutableStateOf<String?>(null) }
    var proficiencyBonus by remember { mutableStateOf("2") }
    var proficiencyBonusError by remember { mutableStateOf<String?>(null) }
    var source by remember { mutableStateOf("Custom") }
    var sourceError by remember { mutableStateOf<String?>(null) }
    var initiative by remember { mutableStateOf("") }
    var initiativeError by remember { mutableStateOf<String?>(null) }
    var saveStr by remember { mutableStateOf(false) }
    var saveDex by remember { mutableStateOf(false) }
    var saveCon by remember { mutableStateOf(false) }
    var saveInt by remember { mutableStateOf(false) }
    var saveWis by remember { mutableStateOf(false) }
    var saveCha by remember { mutableStateOf(false) }

    val sizeOptions = listOf("Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan")
    val type1Options = listOf(
        "Aberration", "Beast", "Celestial", "Construct", "Dragon", "Elemental",
        "Fey", "Fiend", "Giant", "Humanoid", "Monstrosity", "Ooze", "Plant", "Undead"
    )
    val alignmentOptions = listOf(
        "Lawful Good", "Neutral Good", "Chaotic Good", "Lawful Neutral", "Neutral",
        "Chaotic Neutral", "Lawful Evil", "Neutral Evil", "Chaotic Evil", "Unaligned"
    )
    val crOptions = listOf("0", "1/8", "1/4", "1/2", "1") + (2..30).map { it.toString() }

    fun calculateModifier(score: String, proficiencyBonus: Int): String? {
        return score.toIntOrNull()?.let {
            if (it in 1..30) {
                val modifier = floor((it - 10.0) / 2).toInt() + proficiencyBonus
                "+$modifier"
            } else null
        }
    }

    fun validateFields() {
        nameError = when {
            name.isBlank() -> "Name is required"
            name.length > 35 -> "Maximum 35 characters"
            else -> null
        }
        type2Error = when {
            type2.length > 20 -> "Maximum 20 characters"
            else -> null
        }
        hpError = when {
            hp.isNotBlank() && !hp.matches(Regex("\\d{1,5}")) -> "Numbers only, max 5 digits"
            else -> null
        }
        acError = when {
            ac.isNotBlank() && ac.length > 25 -> "Maximum 25 characters"
            else -> null
        }
        strError = when {
            str.isBlank() -> "Strength is required"
            !str.matches(Regex("\\d+")) -> "Numbers only"
            str.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        dexError = when {
            dex.isBlank() -> "Dexterity is required"
            !dex.matches(Regex("\\d+")) -> "Numbers only"
            dex.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        conError = when {
            con.isBlank() -> "Constitution is required"
            !con.matches(Regex("\\d+")) -> "Numbers only"
            con.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        intError = when {
            int.isBlank() -> "Intelligence is required"
            !int.matches(Regex("\\d+")) -> "Numbers only"
            int.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        wisError = when {
            wis.isBlank() -> "Wisdom is required"
            !wis.matches(Regex("\\d+")) -> "Numbers only"
            wis.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        chaError = when {
            cha.isBlank() -> "Charisma is required"
            !cha.matches(Regex("\\d+")) -> "Numbers only"
            cha.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        proficiencyBonusError = when {
            proficiencyBonus.isBlank() -> "Proficiency bonus is required"
            !proficiencyBonus.matches(Regex("\\d+")) -> "Numbers only"
            proficiencyBonus.toInt() !in 2..9 -> "Must be between 2 and 9"
            else -> null
        }
        sourceError = when {
            source.length > 30 -> "Maximum 30 characters"
            else -> null
        }
        initiativeError = when {
            initiative.isNotBlank() && !initiative.matches(Regex("-?\\d+")) -> "Integers only"
            else -> null
        }
    }

    val isFormValid by remember(
        nameError, type2Error, hpError, acError, strError, dexError, conError, intError,
        wisError, chaError, proficiencyBonusError, sourceError, initiativeError
    ) {
        derivedStateOf {
            nameError == null && type2Error == null && hpError == null && acError == null &&
                    strError == null && dexError == null && conError == null && intError == null &&
                    wisError == null && chaError == null && proficiencyBonusError == null &&
                    sourceError == null && initiativeError == null
        }
    }

    LaunchedEffect(
        name, type2, hp, ac, str, dex, con, int, wis, cha, proficiencyBonus, source, initiative
    ) {
        validateFields()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Custom Monster") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        errorMessage = "You must be logged in to save."
                        return@Button
                    }

                    val savesMap = mutableMapOf<String, String?>()
                    val pb = proficiencyBonus.toInt()
                    if (saveStr) savesMap["str"] = calculateModifier(str, pb)
                    if (saveDex) savesMap["dex"] = calculateModifier(dex, pb)
                    if (saveCon) savesMap["con"] = calculateModifier(con, pb)
                    if (saveInt) savesMap["int"] = calculateModifier(int, pb)
                    if (saveWis) savesMap["wis"] = calculateModifier(wis, pb)
                    if (saveCha) savesMap["cha"] = calculateModifier(cha, pb)

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
                        proficiencyBonus = pb,
                        saves = savesMap.takeIf { it.isNotEmpty() },
                        source = source,
                        initiative = initiative.toIntOrNull(),
                        public = false
                    )
                    isSaving = true
                    coroutineScope.launch {
                        try {
                            firestoreManager.saveCustomMonster(customMonster)
                            viewModel.refreshCustomMonsters()
                            delay(500)
                            navController.popBackStack(route = Destinations.MONSTER_LIST, inclusive = false)
                        } catch (e: Exception) {
                            errorMessage = "Error saving: ${e.message}"
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
                Text("Save Monster")
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

                // General Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("General Information", Icons.Default.Info)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (it.length <= 35) name = it },
                            label = { Text("Name") },
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
                                label = { Text("Size") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
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
                                label = { Text("Primary Type") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
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
                            label = { Text("Subtype (optional)") },
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
                                label = { Text("Alignment") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
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

                        OutlinedTextField(
                            value = source,
                            onValueChange = { if (it.length <= 30) source = it },
                            label = { Text("Source") },
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
                        sourceError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                        OutlinedTextField(
                            value = initiative,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("-?\\d*"))) {
                                    initiative = it
                                }
                            },
                            label = { Text("Initiative") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = initiativeError != null
                        )
                        initiativeError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                    }
                }

                // Statistics
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Statistics", Icons.Default.Settings)

                        OutlinedTextField(
                            value = proficiencyBonus,
                            onValueChange = { if (it.length <= 1) proficiencyBonus = it.filter { it.isDigit() } },
                            label = { Text("Proficiency Bonus") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = proficiencyBonusError != null
                        )
                        proficiencyBonusError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                        OutlinedTextField(
                            value = hp,
                            onValueChange = { if (it.length <= 5) hp = it.filter { it.isDigit() } },
                            label = { Text("HP") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = hpError != null,
                            trailingIcon = {
                                Text(
                                    text = "${hp.length}/5",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        hpError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                        OutlinedTextField(
                            value = ac,
                            onValueChange = { if (it.length <= 25) ac = it },
                            label = { Text("AC") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = acError != null,
                            trailingIcon = {
                                Text(
                                    text = "${ac.length}/25",
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
                                onValueChange = { if (it.length <= 2) str = it.filter { it.isDigit() } },
                                label = { Text("Strength") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = strError != null
                            )
                            OutlinedTextField(
                                value = dex,
                                onValueChange = { if (it.length <= 2) dex = it.filter { it.isDigit() } },
                                label = { Text("Dexterity") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = dexError != null
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            strError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                            dexError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = con,
                                onValueChange = { if (it.length <= 2) con = it.filter { it.isDigit() } },
                                label = { Text("Constitution") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = conError != null
                            )
                            OutlinedTextField(
                                value = int,
                                onValueChange = { if (it.length <= 2) int = it.filter { it.isDigit() } },
                                label = { Text("Intelligence") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = intError != null
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            conError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                            intError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = wis,
                                onValueChange = { if (it.length <= 2) wis = it.filter { it.isDigit() } },
                                label = { Text("Wisdom") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = wisError != null
                            )
                            OutlinedTextField(
                                value = cha,
                                onValueChange = { if (it.length <= 2) cha = it.filter { it.isDigit() } },
                                label = { Text("Charisma") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = chaError != null
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            wisError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                            chaError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }

                // Saving Throws
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("Saving Throws", Icons.Default.Security)
                        val pb = proficiencyBonus.toIntOrNull() ?: 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = saveStr,
                                    onCheckedChange = { saveStr = it }
                                )
                                Text("Strength ${if (saveStr) calculateModifier(str, pb) ?: "" else ""}")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = saveDex,
                                    onCheckedChange = { saveDex = it }
                                )
                                Text("Dexterity ${if (saveDex) calculateModifier(dex, pb) ?: "" else ""}")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = saveCon,
                                    onCheckedChange = { saveCon = it }
                                )
                                Text("Constitution ${if (saveCon) calculateModifier(con, pb) ?: "" else ""}")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = saveInt,
                                    onCheckedChange = { saveInt = it }
                                )
                                Text("Intelligence ${if (saveInt) calculateModifier(int, pb) ?: "" else ""}")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = saveWis,
                                    onCheckedChange = { saveWis = it }
                                )
                                Text("Wisdom ${if (saveWis) calculateModifier(wis, pb) ?: "" else ""}")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = saveCha,
                                    onCheckedChange = { saveCha = it }
                                )
                                Text("Charisma ${if (saveCha) calculateModifier(cha, pb) ?: "" else ""}")
                            }
                        }
                    }
                }
            }
        }
    }
}