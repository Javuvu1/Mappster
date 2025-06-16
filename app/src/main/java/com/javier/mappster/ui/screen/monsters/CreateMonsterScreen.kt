package com.javier.mappster.ui.screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.CustomMonster
import com.javier.mappster.viewmodel.MonsterListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.javier.mappster.model.ActionEntry
import com.javier.mappster.model.CustomSpellLevel
import com.javier.mappster.model.Spell
import com.javier.mappster.model.SpellcastingEntry
import com.javier.mappster.model.TraitEntry
import com.javier.mappster.ui.theme.CinzelDecorative
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
            color = MaterialTheme.colorScheme.tertiary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun CreateMonsterCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (icon != null) {
                    SectionTitle(title, icon)
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = CinzelDecorative,
                            color = MaterialTheme.colorScheme.tertiary,
                            letterSpacing = 0.1.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                content()
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = CinzelDecorative,
                color = MaterialTheme.colorScheme.tertiary,
                letterSpacing = 0.1.sp
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMonsterScreen(navController: NavHostController,
                        monsterId: String? = null) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val firestoreManager = remember { FirestoreManager() }
    val dataManager = remember { LocalDataManager(context) }
    val viewModel: MonsterListViewModel = viewModel(
        factory = MonsterListViewModelFactory(dataManager, authManager)
    )
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(monsterId != null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados generales
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

    // Estados para saving throws
    var saveStr by remember { mutableStateOf(false) }
    var saveDex by remember { mutableStateOf(false) }
    var saveCon by remember { mutableStateOf(false) }
    var saveInt by remember { mutableStateOf(false) }
    var saveWis by remember { mutableStateOf(false) }
    var saveCha by remember { mutableStateOf(false) }

    // Estados para habilidades
    var skillAthletics by remember { mutableStateOf(false) } // STR
    var skillAcrobatics by remember { mutableStateOf(false) } // DEX
    var skillSleightOfHand by remember { mutableStateOf(false) } // DEX
    var skillStealth by remember { mutableStateOf(false) } // DEX
    var skillArcana by remember { mutableStateOf(false) } // INT
    var skillHistory by remember { mutableStateOf(false) } // INT
    var skillInvestigation by remember { mutableStateOf(false) } // INT
    var skillNature by remember { mutableStateOf(false) } // INT
    var skillReligion by remember { mutableStateOf(false) } // INT
    var skillAnimalHandling by remember { mutableStateOf(false) } // WIS
    var skillInsight by remember { mutableStateOf(false) } // WIS
    var skillMedicine by remember { mutableStateOf(false) } // WIS
    var skillPerception by remember { mutableStateOf(false) } // WIS
    var skillSurvival by remember { mutableStateOf(false) } // WIS
    var skillDeception by remember { mutableStateOf(false) } // CHA
    var skillIntimidation by remember { mutableStateOf(false) } // CHA
    var skillPerformance by remember { mutableStateOf(false) } // CHA
    var skillPersuasion by remember { mutableStateOf(false) } // CHA

    var walkSpeed by remember { mutableStateOf("") }
    var walkSpeedError by remember { mutableStateOf<String?>(null) }
    var flySpeed by remember { mutableStateOf("") }
    var flySpeedError by remember { mutableStateOf<String?>(null) }
    var swimSpeed by remember { mutableStateOf("") }
    var swimSpeedError by remember { mutableStateOf<String?>(null) }
    var climbSpeed by remember { mutableStateOf("") }
    var climbSpeedError by remember { mutableStateOf<String?>(null) }
    var burrowSpeed by remember { mutableStateOf("") }
    var burrowSpeedError by remember { mutableStateOf<String?>(null) }

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

    // Resistances and Immunities
    val damageTypes = listOf(
        "Acid", "Bludgeoning", "Cold", "Fire", "Force", "Lightning",
        "Necrotic", "Piercing", "Poison", "Psychic", "Radiant",
        "Slashing", "Thunder"
    )
    var selectedResistances by remember { mutableStateOf(emptySet<String>()) }
    var customResistance by remember { mutableStateOf("") }
    var customResistanceError by remember { mutableStateOf<String?>(null) }
    var selectedImmunities by remember { mutableStateOf(emptySet<String>()) }
    var customImmunity by remember { mutableStateOf("") }
    var customImmunityError by remember { mutableStateOf<String?>(null) }

    val sensesTypes = listOf(
        "blindsight 60 ft.",
        "darkvision 60 ft.",
        "darkvision 120 ft.",
        "tremorsense 60 ft.",
        "truesight 120 ft."
    )

    // Añade nuevos estados para sentidos
    val selectedSenses = remember { mutableStateListOf<String>() }
    var customSense by remember { mutableStateOf("") }
    var customSenseError by remember { mutableStateOf<String?>(null) }

    val languageTypes = listOf(
        "Common", "Dwarvish", "Elvish", "Giant", "Gnomish", "Goblin", "Halfling", "Orc",
        "Abyssal", "Celestial", "Draconic", "Deep Speech", "Infernal", "Primordial", "Sylvan", "Undercommon",
        "Auran", "Aarakocra", "Ignan", "Terran", "Aquan"
    )

    // Estados para idiomas
    val selectedLanguages = remember { mutableStateListOf<String>() }
    var customLanguage by remember { mutableStateOf("") }
    var customLanguageError by remember { mutableStateOf<String?>(null) }

    val traits = remember { mutableStateListOf<Pair<String, String>>() }
    val traitNameErrors = remember { mutableStateListOf<String?>() }
    val traitEntryErrors = remember { mutableStateListOf<String?>() }

    //Acciones de varios tipos
    val actions = remember { mutableStateListOf<Pair<String, String>>() }
    val actionNameErrors = remember { mutableStateListOf<String?>() }
    val actionEntryErrors = remember { mutableStateListOf<String?>() }

    val bonusActions = remember { mutableStateListOf<Pair<String, String>>() }
    val bonusActionNameErrors = remember { mutableStateListOf<String?>() }
    val bonusActionEntryErrors = remember { mutableStateListOf<String?>() }

    val reactions = remember { mutableStateListOf<Pair<String, String>>() }
    val reactionNameErrors = remember { mutableStateListOf<String?>() }
    val reactionEntryErrors = remember { mutableStateListOf<String?>() }

    val legendaryActions = remember { mutableStateListOf<Pair<String, String>>() }
    val legendaryActionNameErrors = remember { mutableStateListOf<String?>() }
    val legendaryActionEntryErrors = remember { mutableStateListOf<String?>() }

    var showSpellModal by remember { mutableStateOf(false) }
    var spellSearchQuery by remember { mutableStateOf("") }
    val selectedSpells = remember { mutableStateListOf<Spell>() }
    val spellcastingEntries = remember { mutableStateListOf<SpellcastingEntry>() }
    var spellcastingAbility by remember { mutableStateOf("cha") }

    LaunchedEffect(monsterId) {
        if (monsterId != null) {
            try {
                val userId = authManager.getCurrentUserId() ?: throw Exception("Usuario no autenticado")
                Log.d("CreateMonsterScreen", "Loading monster with id=$monsterId for user=$userId")
                val monster = firestoreManager.getCustomMonsterById(userId, monsterId)
                Log.d("CreateMonsterScreen", "Monster fetched: $monster")
                monster?.let {
                    Log.d("CreateMonsterScreen", "Monster loaded: ${monster.name}, id=${monster.id}")
                    name = it.name
                    size = it.size ?: "Medium"
                    type1 = it.type?.getOrNull(0) ?: "Humanoid"
                    type2 = it.type?.getOrNull(1) ?: ""
                    alignment = it.alignment ?: "Neutral"
                    cr = it.cr ?: "0"
                    hp = it.hp?.toString() ?: ""
                    ac = it.ac ?: ""
                    str = it.str?.toString() ?: ""
                    dex = it.dex?.toString() ?: ""
                    con = it.con?.toString() ?: ""
                    int = it.int?.toString() ?: ""
                    wis = it.wis?.toString() ?: ""
                    cha = it.cha?.toString() ?: ""
                    proficiencyBonus = it.proficiencyBonus?.toString() ?: "2"
                    source = it.source ?: "Custom"
                    initiative = it.initiative?.toString() ?: ""

                    // Saving throws
                    it.saves?.let { saves ->
                        saveStr = saves.containsKey("str")
                        saveDex = saves.containsKey("dex")
                        saveCon = saves.containsKey("con")
                        saveInt = saves.containsKey("int")
                        saveWis = saves.containsKey("wis")
                        saveCha = saves.containsKey("cha")
                    }

                    // Skills
                    it.skills?.let { skills ->
                        skillAthletics = skills.containsKey("athletics")
                        skillAcrobatics = skills.containsKey("acrobatics")
                        skillSleightOfHand = skills.containsKey("sleight_of_hand")
                        skillStealth = skills.containsKey("stealth")
                        skillArcana = skills.containsKey("arcana")
                        skillHistory = skills.containsKey("history")
                        skillInvestigation = skills.containsKey("investigation")
                        skillNature = skills.containsKey("nature")
                        skillReligion = skills.containsKey("religion")
                        skillAnimalHandling = skills.containsKey("animal_handling")
                        skillInsight = skills.containsKey("insight")
                        skillMedicine = skills.containsKey("medicine")
                        skillPerception = skills.containsKey("perception")
                        skillSurvival = skills.containsKey("survival")
                        skillDeception = skills.containsKey("deception")
                        skillIntimidation = skills.containsKey("intimidation")
                        skillPerformance = skills.containsKey("performance")
                        skillPersuasion = skills.containsKey("persuasion")
                    }

                    // Speeds
                    it.speed?.let { speeds ->
                        walkSpeed = speeds["walk"]?.toString() ?: ""
                        flySpeed = speeds["fly"]?.toString() ?: ""
                        swimSpeed = speeds["swim"]?.toString() ?: ""
                        climbSpeed = speeds["climb"]?.toString() ?: ""
                        burrowSpeed = speeds["burrow"]?.toString() ?: ""
                    }

                    // Resistencias e inmunidades
                    it.resist?.forEach { resist ->
                        if (damageTypes.contains(resist.capitalize())) {
                            selectedResistances = selectedResistances + resist.capitalize()
                        } else {
                            customResistance = resist
                        }
                    }
                    it.immune?.forEach { immune ->
                        if (damageTypes.contains(immune.capitalize())) {
                            selectedImmunities = selectedImmunities + immune.capitalize()
                        } else {
                            customImmunity = immune
                        }
                    }

                    // Sentidos
                    it.senses?.forEach { sense ->
                        if (sensesTypes.contains(sense)) {
                            selectedSenses.add(sense)
                        } else {
                            customSense = sense
                        }
                    }

                    // Idiomas
                    it.languages?.forEach { language ->
                        if (languageTypes.contains(language)) {
                            selectedLanguages.add(language)
                        } else {
                            customLanguage = language
                        }
                    }

                    // Rasgos
                    it.traits?.forEach { trait ->
                        trait.entries?.firstOrNull()?.let { entry ->
                            traits.add(trait.name to entry)
                            traitNameErrors.add(null)
                            traitEntryErrors.add(null)
                        }
                    }

                    // Acciones
                    it.actions?.forEach { action ->
                        action.entries?.firstOrNull()?.let { entry ->
                            actions.add(action.name to entry)
                            actionNameErrors.add(null)
                            actionEntryErrors.add(null)
                        }
                    }

                    // Acciones bonus
                    it.bonus?.forEach { bonus ->
                        bonus.entries?.firstOrNull()?.let { entry ->
                            bonusActions.add(bonus.name to entry)
                            bonusActionNameErrors.add(null)
                            bonusActionEntryErrors.add(null)
                        }
                    }

                    // Reacciones
                    it.reactions?.forEach { reaction ->
                        reaction.entries?.firstOrNull()?.let { entry ->
                            reactions.add(reaction.name to entry)
                            reactionNameErrors.add(null)
                            reactionEntryErrors.add(null)
                        }
                    }

                    // Acciones legendarias
                    it.legendary?.forEach { legendary ->
                        legendary.entries?.firstOrNull()?.let { entry ->
                            legendaryActions.add(legendary.name to entry)
                            legendaryActionNameErrors.add(null)
                            legendaryActionEntryErrors.add(null)
                        }
                    }

                    // Hechizos
                    it.spellcasting?.firstOrNull()?.let { spellcasting ->
                        spellcastingAbility = spellcasting.ability ?: "cha"
                        selectedSpells.clear()
                        spellcasting.spells.forEach { (level, spellLevel) ->
                            selectedSpells.addAll(spellLevel.spells.map { Spell(name = it, level = level.toIntOrNull() ?: 0) })
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateMonsterScreen", "Error loading monster: ${e.message}", e)
                errorMessage = "Error al cargar el monstruo: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun buildSpellcastingList(): List<SpellcastingEntry>? {
        if (selectedSpells.isEmpty()) return null

        // Agrupar hechizos por nivel
        val spellsByLevel = selectedSpells.groupBy { it.level }

        // Convertir el nivel de Int a String para el mapa
        val spellsMap = spellsByLevel.mapKeys { (level, _) -> level.toString() }
            .mapValues { (_, spells) ->
                CustomSpellLevel(
                    slots = when (spells.first().level) {
                        1 -> 3 // Ejemplo: 3 slots de nivel 1
                        2 -> 2 // Ejemplo: 2 slots de nivel 2
                        else -> 1 // Ejemplo: 1 slot para otros niveles
                    },
                    spells = spells.map { it.name }
                )
            }

        return listOf(SpellcastingEntry(
            name = "Spellcasting",
            spells = spellsMap,
            ability = spellcastingAbility, // Usamos la característica seleccionada
            headerEntries = listOf(
                "The monster is a ${when (spellcastingAbility) {
                    "cha" -> "charisma"
                    "wis" -> "wisdom"
                    else -> "intelligence"
                }}-based spellcaster. Its spellcasting ability is $spellcastingAbility."
            )
        ))
    }

    fun calculateModifier(score: String, proficiencyBonus: Int): String? {
        return score.toIntOrNull()?.let {
            if (it in 1..30) {
                val modifier = floor((it - 10.0) / 2).toInt() + proficiencyBonus
                "+$modifier"
            } else null
        }
    }

    fun validateFields() {
        Log.d("CreateMonsterScreen", "Validating fields...")
        nameError = when {
            name.isBlank() -> "El nombre es obligatorio"
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
            str.isBlank() -> "La Fuerza es Obligatoria"
            !str.matches(Regex("\\d+")) -> "Numbers only"
            str.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        dexError = when {
            dex.isBlank() -> "La Destreza es Obligatoria"
            !dex.matches(Regex("\\d+")) -> "Numbers only"
            dex.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        conError = when {
            con.isBlank() -> "La Constitución es Obligatoria"
            !con.matches(Regex("\\d+")) -> "Numbers only"
            con.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        intError = when {
            int.isBlank() -> "La Inteligencia es Obligatoria"
            !int.matches(Regex("\\d+")) -> "Numbers only"
            int.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        wisError = when {
            wis.isBlank() -> "La Sabiduría es Obligatoria"
            !wis.matches(Regex("\\d+")) -> "Numbers only"
            wis.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        chaError = when {
            cha.isBlank() -> "La Carisma es Obligatoria"
            !cha.matches(Regex("\\d+")) -> "Numbers only"
            cha.toInt() !in 1..30 -> "Must be between 1 and 30"
            else -> null
        }
        proficiencyBonusError = when {
            proficiencyBonus.isBlank() -> "El Bono de Competencia es Obligatorio"
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

        walkSpeedError = when {
            walkSpeed.isNotBlank() && !walkSpeed.matches(Regex("\\d+")) -> "Numbers only"
            walkSpeed.isNotBlank() && walkSpeed.toInt() > 999 -> "Max 999"
            else -> null
        }
        flySpeedError = when {
            flySpeed.isNotBlank() && !flySpeed.matches(Regex("\\d+")) -> "Numbers only"
            flySpeed.isNotBlank() && flySpeed.toInt() > 999 -> "Max 999"
            else -> null
        }
        swimSpeedError = when {
            swimSpeed.isNotBlank() && !swimSpeed.matches(Regex("\\d+")) -> "Numbers only"
            swimSpeed.isNotBlank() && swimSpeed.toInt() > 999 -> "Max 999"
            else -> null
        }
        climbSpeedError = when {
            climbSpeed.isNotBlank() && !climbSpeed.matches(Regex("\\d+")) -> "Numbers only"
            climbSpeed.isNotBlank() && climbSpeed.toInt() > 999 -> "Max 999"
            else -> null
        }
        burrowSpeedError = when {
            burrowSpeed.isNotBlank() && !burrowSpeed.matches(Regex("\\d+")) -> "Numbers only"
            burrowSpeed.isNotBlank() && burrowSpeed.toInt() > 999 -> "Max 999"
            else -> null
        }

        customResistanceError = when {
            customResistance.isNotBlank() && customResistance.length > 50 -> "Máximo 50 caracteres"
            customResistance.isNotBlank() && selectedImmunities.contains(customResistance) -> "No puede ser inmune y resistente al mismo tipo"
            customResistance.isNotBlank() && customResistance.equals(customImmunity, ignoreCase = true) -> "No puede ser inmune y resistente al mismo tipo"
            else -> null
        }
        customImmunityError = when {
            customImmunity.isNotBlank() && customImmunity.length > 50 -> "Máximo 50 caracteres"
            customImmunity.isNotBlank() && selectedResistances.contains(customImmunity) -> "No puede ser inmune y resistente al mismo tipo"
            customImmunity.isNotBlank() && customImmunity.equals(customResistance, ignoreCase = true) -> "No puede ser inmune y resistente al mismo tipo"
            else -> null
        }

        customSenseError = when {
            customSense.isNotBlank() && customSense.length > 30 -> "Máximo 30 caracteres"
            customSense.isNotBlank() && selectedSenses.contains(customSense) -> "Sentido ya seleccionado"
            else -> null
        }

        customLanguageError = when {
            customLanguage.isNotBlank() && customLanguage.length > 30 -> "Máximo 30 caracteres"
            customLanguage.isNotBlank() && selectedLanguages.contains(customLanguage) -> "Idioma ya seleccionado"
            else -> null
        }

        traits.forEachIndexed { index, (name, entry) ->
            if (traitNameErrors.size <= index) {
                traitNameErrors.add(null)
            }
            if (traitEntryErrors.size <= index) {
                traitEntryErrors.add(null)
            }

            traitNameErrors[index] = when {
                name.isBlank() -> "El nombre no puede estar vacío"
                name.length > 50 -> "Máximo 50 caracteres"
                else -> null
            }
            traitEntryErrors[index] = when {
                entry.isBlank() -> "La descripción no puede estar vacía"
                entry.length > 500 -> "Máximo 500 caracteres"
                else -> null
            }
        }

        traits.forEachIndexed { index, (name, entry) ->
            traitNameErrors[index] = when {
                name.isBlank() -> "El nombre no puede estar vacío"
                name.length > 50 -> "Máximo 50 caracteres"
                else -> null
            }
            traitEntryErrors[index] = when {
                entry.isBlank() -> "La descripción no puede estar vacía"
                entry.length > 500 -> "Máximo 500 caracteres"
                else -> null
            }
        }

        actionNameErrors.clear()
        actionEntryErrors.clear()
        actions.forEach { (name, entry) ->
            actionNameErrors.add(when {
                name.isBlank() -> "El nombre no puede estar vacío"
                name.length > 50 -> "Máximo 50 caracteres"
                else -> null
            })
            actionEntryErrors.add(when {
                entry.isBlank() -> "La descripción no puede estar vacía"
                entry.length > 500 -> "Máximo 500 caracteres"
                else -> null
            })
        }

        bonusActionNameErrors.clear()
        bonusActionEntryErrors.clear()
        bonusActions.forEach { (name, entry) ->
            bonusActionNameErrors.add(when {
                name.isBlank() -> "El nombre no puede estar vacío"
                name.length > 50 -> "Máximo 50 caracteres"
                else -> null
            })
            bonusActionEntryErrors.add(when {
                entry.isBlank() -> "La descripción no puede estar vacía"
                entry.length > 500 -> "Máximo 500 caracteres"
                else -> null
            })
        }

        reactionNameErrors.clear()
        reactionEntryErrors.clear()
        reactions.forEach { (name, entry) ->
            reactionNameErrors.add(when {
                name.isBlank() -> "El nombre no puede estar vacío"
                name.length > 50 -> "Máximo 50 caracteres"
                else -> null
            })
            reactionEntryErrors.add(when {
                entry.isBlank() -> "La descripción no puede estar vacía"
                entry.length > 500 -> "Máximo 500 caracteres"
                else -> null
            })
        }

        legendaryActionNameErrors.clear()
        legendaryActionEntryErrors.clear()
        legendaryActions.forEach { (name, entry) ->
            legendaryActionNameErrors.add(when {
                name.isBlank() -> "El nombre no puede estar vacío"
                name.length > 50 -> "Máximo 50 caracteres"
                else -> null
            })
            legendaryActionEntryErrors.add(when {
                entry.isBlank() -> "La descripción no puede estar vacía"
                entry.length > 500 -> "Máximo 500 caracteres"
                else -> null
            })
        }
    }

    val isFormValid by remember(
        nameError, type2Error, hpError, acError, strError, dexError, conError, intError,
        wisError, chaError, proficiencyBonusError, sourceError, initiativeError,
        walkSpeedError, flySpeedError, swimSpeedError, climbSpeedError, burrowSpeedError,
        customResistanceError, customImmunityError, customSenseError, customLanguageError,
        traits, traitNameErrors, traitEntryErrors,
        actions, actionNameErrors, actionEntryErrors,
        bonusActions, bonusActionNameErrors, bonusActionEntryErrors,
        reactions, reactionNameErrors, reactionEntryErrors,
        legendaryActions, legendaryActionNameErrors, legendaryActionEntryErrors
    ) {
        derivedStateOf {
            nameError == null && type2Error == null && hpError == null && acError == null &&
                    strError == null && dexError == null && conError == null && intError == null &&
                    wisError == null && chaError == null && proficiencyBonusError == null &&
                    sourceError == null && initiativeError == null &&
                    walkSpeedError == null && flySpeedError == null && swimSpeedError == null &&
                    climbSpeedError == null && burrowSpeedError == null &&
                    customResistanceError == null && customImmunityError == null &&
                    customSenseError == null && customLanguageError == null &&
                    traitNameErrors.all { it == null } && traitEntryErrors.all { it == null } &&
                    actionNameErrors.all { it == null } && actionEntryErrors.all { it == null } &&
                    bonusActionNameErrors.all { it == null } && bonusActionEntryErrors.all { it == null } &&
                    reactionNameErrors.all { it == null } && reactionEntryErrors.all { it == null } &&
                    legendaryActionNameErrors.all { it == null } && legendaryActionEntryErrors.all { it == null }
        }
    }

    fun buildSpeedMap(): Map<String, Int>? {
        val speeds = mutableMapOf<String, Int>()

        walkSpeed.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { speeds["walk"] = it }
        flySpeed.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { speeds["fly"] = it }
        swimSpeed.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { speeds["swim"] = it }
        climbSpeed.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { speeds["climb"] = it }
        burrowSpeed.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { speeds["burrow"] = it }

        return speeds.takeIf { it.isNotEmpty() }
    }

    fun buildResistList(): List<String>? {
        val resists = selectedResistances.toMutableList()
        customResistance.takeIf { it.isNotBlank() && !selectedImmunities.contains(it) && it != customImmunity }?.let { resists.add(it) }
        return resists.takeIf { it.isNotEmpty() }?.map { it.lowercase() }
    }

    fun buildImmuneList(): List<String>? {
        val immunes = selectedImmunities.toMutableList()
        customImmunity.takeIf { it.isNotBlank() && !selectedResistances.contains(it) && it != customResistance }?.let { immunes.add(it) }
        return immunes.takeIf { it.isNotEmpty() }?.map { it.lowercase() }
    }

    fun buildSensesList(): List<String>? {
        val senses = selectedSenses.toMutableList()
        customSense.takeIf { it.isNotBlank() && !selectedSenses.contains(it) }?.let { senses.add(it) }
        return senses.takeIf { it.isNotEmpty() }
    }

    fun buildLanguagesList(): List<String>? {
        val languages = selectedLanguages.toMutableList()
        customLanguage.takeIf { it.isNotBlank() && !selectedLanguages.contains(it) }?.let { languages.add(it) }
        return languages.takeIf { it.isNotEmpty() }
    }

    fun buildTraitsList(): List<TraitEntry>? {
        return traits.mapNotNull { (name, entry) ->
            if (name.isNotBlank() && entry.isNotBlank()) {
                TraitEntry(name = name, entries = listOf(entry))
            } else null
        }.takeIf { it.isNotEmpty() }
    }

    fun buildActionsList(): List<ActionEntry>? {
        return actions.mapNotNull { (name, entry) ->
            if (name.isNotBlank() && entry.isNotBlank()) {
                ActionEntry(name = name, entries = listOf(entry))
            } else null
        }.takeIf { it.isNotEmpty() }
    }

    fun buildBonusActionsList(): List<ActionEntry>? {
        return bonusActions.mapNotNull { (name, entry) ->
            if (name.isNotBlank() && entry.isNotBlank()) {
                ActionEntry(name = name, entries = listOf(entry))
            } else null
        }.takeIf { it.isNotEmpty() }
    }

    fun buildReactionsList(): List<ActionEntry>? {
        return reactions.mapNotNull { (name, entry) ->
            if (name.isNotBlank() && entry.isNotBlank()) {
                ActionEntry(name = name, entries = listOf(entry))
            } else null
        }.takeIf { it.isNotEmpty() }
    }

    fun buildLegendaryActionsList(): List<ActionEntry>? {
        return legendaryActions.mapNotNull { (name, entry) ->
            if (name.isNotBlank() && entry.isNotBlank()) {
                ActionEntry(name = name, entries = listOf(entry))
            } else null
        }.takeIf { it.isNotEmpty() }
    }

    LaunchedEffect(
        name, type2, hp, ac, str, dex, con, int, wis, cha, proficiencyBonus, source, initiative,
        walkSpeed, flySpeed, swimSpeed, climbSpeed, burrowSpeed,
        customResistance, customImmunity, customSense, customLanguage,
        traits, actions, bonusActions, reactions, legendaryActions
    ) {
        validateFields()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (monsterId == null) "Crear Monstruo Personalizado" else "Editar Monstruo Personalizado",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = MaterialTheme.colorScheme.tertiary,
                    actionIconContentColor = MaterialTheme.colorScheme.tertiary
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

                    val skillsMap = mutableMapOf<String, String>()
                    if (skillAthletics) calculateModifier(str, pb)?.let { skillsMap["athletics"] = it }
                    if (skillAcrobatics) calculateModifier(dex, pb)?.let { skillsMap["acrobatics"] = it }
                    if (skillSleightOfHand) calculateModifier(dex, pb)?.let { skillsMap["sleight_of_hand"] = it }
                    if (skillStealth) calculateModifier(dex, pb)?.let { skillsMap["stealth"] = it }
                    if (skillArcana) calculateModifier(int, pb)?.let { skillsMap["arcana"] = it }
                    if (skillHistory) calculateModifier(int, pb)?.let { skillsMap["history"] = it }
                    if (skillInvestigation) calculateModifier(int, pb)?.let { skillsMap["investigation"] = it }
                    if (skillNature) calculateModifier(int, pb)?.let { skillsMap["nature"] = it }
                    if (skillReligion) calculateModifier(int, pb)?.let { skillsMap["religion"] = it }
                    if (skillAnimalHandling) calculateModifier(wis, pb)?.let { skillsMap["animal_handling"] = it }
                    if (skillInsight) calculateModifier(wis, pb)?.let { skillsMap["insight"] = it }
                    if (skillMedicine) calculateModifier(wis, pb)?.let { skillsMap["medicine"] = it }
                    if (skillPerception) calculateModifier(wis, pb)?.let { skillsMap["perception"] = it }
                    if (skillSurvival) calculateModifier(wis, pb)?.let { skillsMap["survival"] = it }
                    if (skillDeception) calculateModifier(cha, pb)?.let { skillsMap["deception"] = it }
                    if (skillIntimidation) calculateModifier(cha, pb)?.let { skillsMap["intimidation"] = it }
                    if (skillPerformance) calculateModifier(cha, pb)?.let { skillsMap["performance"] = it }
                    if (skillPersuasion) calculateModifier(cha, pb)?.let { skillsMap["persuasion"] = it }

                    val traitsList = buildTraitsList()
                    val actionsList = buildActionsList()
                    val bonusActionsList = buildBonusActionsList()
                    val reactionsList = buildReactionsList()
                    val legendaryActionsList = buildLegendaryActionsList()

                    val customMonster = CustomMonster(
                        id = monsterId,
                        userId = userId,
                        name = name,
                        size = size,
                        type = listOfNotNull(type1, type2.takeIf { it.isNotBlank() }),
                        alignment = alignment,
                        cr = cr,
                        hp = hp.toIntOrNull(),
                        ac = ac.takeIf { it.isNotBlank() },
                        speed = buildSpeedMap(),
                        str = str.toIntOrNull(),
                        dex = dex.toIntOrNull(),
                        con = con.toIntOrNull(),
                        int = int.toIntOrNull(),
                        wis = wis.toIntOrNull(),
                        cha = cha.toIntOrNull(),
                        proficiencyBonus = pb,
                        saves = savesMap.takeIf { it.isNotEmpty() },
                        skills = skillsMap.takeIf { it.isNotEmpty() },
                        resist = buildResistList(),
                        immune = buildImmuneList(),
                        source = source,
                        initiative = initiative.toIntOrNull(),
                        senses = buildSensesList(),
                        languages = buildLanguagesList(),
                        traits = traitsList,
                        actions = actionsList,
                        bonus = bonusActionsList,
                        reactions = reactionsList,
                        legendary = legendaryActionsList,
                        spellcasting = buildSpellcastingList(),
                        public = false
                    )

                    isSaving = true
                    coroutineScope.launch {
                        try {
                            if (monsterId == null) {
                                firestoreManager.saveCustomMonster(customMonster)
                            } else {
                                firestoreManager.updateCustomMonster(customMonster)
                            }

                            // Cambia esta parte:
                            navController.popBackStack() // Simplemente volvemos atrás
                        } catch (e: Exception) {
                            errorMessage = "Error saving: ${e.message}"
                            Log.e("CreateMonsterScreen", "Error saving monster", e)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !isSaving && !isLoading && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (monsterId == null) "Guardar Monstruo" else "Actualizar Monstruo")
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

                CreateMonsterCard(
                    title = "Información General",
                    icon = Icons.Default.Info
                ) {
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
                            label = { Text("Tipo Primario") },
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
                        label = { Text("Subtipo (Opcional)") },
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
                            label = { Text("Nivel de Desafío") },
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
                        label = { Text("Fuente") },
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
                        label = { Text("Iniciativa") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = initiativeError != null
                    )
                    initiativeError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                }

                // Statistics
                CreateMonsterCard(
                    title = "Estadísticas",
                    icon = Icons.Default.Settings
                ) {
                    OutlinedTextField(
                        value = proficiencyBonus,
                        onValueChange = { if (it.length <= 1) proficiencyBonus = it.filter { it.isDigit() } },
                        label = { Text("Bono de Competencia") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = proficiencyBonusError != null
                    )
                    proficiencyBonusError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                    OutlinedTextField(
                        value = hp,
                        onValueChange = { if (it.length <= 5) hp = it.filter { it.isDigit() } },
                        label = { Text("Puntos de Golpe") },
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
                        label = { Text("Clase de Armadura") },
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
                            label = { Text("Fuerza") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = strError != null
                        )
                        OutlinedTextField(
                            value = dex,
                            onValueChange = { if (it.length <= 2) dex = it.filter { it.isDigit() } },
                            label = { Text("Destreza") },
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
                            label = { Text("Constitución") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = conError != null
                        )
                        OutlinedTextField(
                            value = int,
                            onValueChange = { if (it.length <= 2) int = it.filter { it.isDigit() } },
                            label = { Text("Inteligencia") },
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
                            label = { Text("Sabiduría") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = wisError != null
                        )
                        OutlinedTextField(
                            value = cha,
                            onValueChange = { if (it.length <= 2) cha = it.filter { it.isDigit() } },
                            label = { Text("Carisma") },
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

                CreateMonsterCard(
                    title = "Velocidad",
                    icon = Icons.Default.DirectionsWalk
                ) {
                    Text(
                        text = "Pon el movimiento en pies (feet), no pongas nada si no aplica",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = walkSpeed,
                        onValueChange = { if (it.length <= 3) walkSpeed = it.filter { it.isDigit() } },
                        label = { Text("Velocidad a Pie") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = walkSpeedError != null,
                        trailingIcon = {
                            Text(
                                text = if (walkSpeed.isNotBlank()) "ft" else "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                    walkSpeedError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = flySpeed,
                            onValueChange = { if (it.length <= 3) flySpeed = it.filter { it.isDigit() } },
                            label = { Text("Velocidad de Vuelo") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = flySpeedError != null,
                            trailingIcon = {
                                Text(
                                    text = if (flySpeed.isNotBlank()) "ft" else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                        OutlinedTextField(
                            value = swimSpeed,
                            onValueChange = { if (it.length <= 3) swimSpeed = it.filter { it.isDigit() } },
                            label = { Text("Velocidad de Nado") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = swimSpeedError != null,
                            trailingIcon = {
                                Text(
                                    text = if (swimSpeed.isNotBlank()) "ft" else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        flySpeedError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                        swimSpeedError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = climbSpeed,
                            onValueChange = { if (it.length <= 3) climbSpeed = it.filter { it.isDigit() } },
                            label = { Text("Velocidad de Escalada") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = climbSpeedError != null,
                            trailingIcon = {
                                Text(
                                    text = if (climbSpeed.isNotBlank()) "ft" else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                        OutlinedTextField(
                            value = burrowSpeed,
                            onValueChange = { if (it.length <= 3) burrowSpeed = it.filter { it.isDigit() } },
                            label = { Text("Velocidad de Excavación") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = burrowSpeedError != null,
                            trailingIcon = {
                                Text(
                                    text = if (burrowSpeed.isNotBlank()) "ft" else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        climbSpeedError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                        burrowSpeedError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                    }
                }

                // Saving Throws
                CreateMonsterCard(
                    title = "Tiradas de Salvación",
                    icon = Icons.Default.Security
                ) {
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
                            Text("Fuerza ${if (saveStr) calculateModifier(str, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = saveDex,
                                onCheckedChange = { saveDex = it }
                            )
                            Text("Destreza ${if (saveDex) calculateModifier(dex, pb) ?: "" else ""}")
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
                            Text("Constitución ${if (saveCon) calculateModifier(con, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = saveInt,
                                onCheckedChange = { saveInt = it }
                            )
                            Text("Inteligencia ${if (saveInt) calculateModifier(int, pb) ?: "" else ""}")
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
                            Text("Sabiduría ${if (saveWis) calculateModifier(wis, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = saveCha,
                                onCheckedChange = { saveCha = it }
                            )
                            Text("Carisma ${if (saveCha) calculateModifier(cha, pb) ?: "" else ""}")
                        }
                    }
                }

                // Skills Section
                CreateMonsterCard(
                    title = "Habilidades",
                    icon = Icons.Default.Star
                ) {
                    val pb = proficiencyBonus.toIntOrNull() ?: 2

                    // Fuerza
                    Text(
                        text = "Habilidades de Fuerza",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = skillAthletics,
                            onCheckedChange = { skillAthletics = it }
                        )
                        Text("Atletismo ${if (skillAthletics) calculateModifier(str, pb) ?: "" else ""}")
                    }

                    // Destreza
                    Text(
                        text = "Habilidades de Destreza",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillAcrobatics,
                                onCheckedChange = { skillAcrobatics = it }
                            )
                            Text("Acrobacias ${if (skillAcrobatics) calculateModifier(dex, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillSleightOfHand,
                                onCheckedChange = { skillSleightOfHand = it }
                            )
                            Text("Juego de Manos ${if (skillSleightOfHand) calculateModifier(dex, pb) ?: "" else ""}")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = skillStealth,
                            onCheckedChange = { skillStealth = it }
                        )
                        Text("Sigilo ${if (skillStealth) calculateModifier(dex, pb) ?: "" else ""}")
                    }

                    // Inteligencia
                    Text(
                        text = "Habilidades de Inteligencia",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillArcana,
                                onCheckedChange = { skillArcana = it }
                            )
                            Text("Arcana ${if (skillArcana) calculateModifier(int, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillHistory,
                                onCheckedChange = { skillHistory = it }
                            )
                            Text("Historia ${if (skillHistory) calculateModifier(int, pb) ?: "" else ""}")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillInvestigation,
                                onCheckedChange = { skillInvestigation = it }
                            )
                            Text("Investigación ${if (skillInvestigation) calculateModifier(int, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillNature,
                                onCheckedChange = { skillNature = it }
                            )
                            Text("Naturaleza ${if (skillNature) calculateModifier(int, pb) ?: "" else ""}")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = skillReligion,
                            onCheckedChange = { skillReligion = it }
                        )
                        Text("Religión ${if (skillReligion) calculateModifier(int, pb) ?: "" else ""}")
                    }

                    // Sabiduría
                    Text(
                        text = "Habilidades de Sabiduría",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillAnimalHandling,
                                onCheckedChange = { skillAnimalHandling = it }
                            )
                            Text("Trato con Animales ${if (skillAnimalHandling) calculateModifier(wis, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillInsight,
                                onCheckedChange = { skillInsight = it }
                            )
                            Text("Perspicacia ${if (skillInsight) calculateModifier(wis, pb) ?: "" else ""}")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillMedicine,
                                onCheckedChange = { skillMedicine = it }
                            )
                            Text("Medicina ${if (skillMedicine) calculateModifier(wis, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillPerception,
                                onCheckedChange = { skillPerception = it }
                            )
                            Text("Percepción ${if (skillPerception) calculateModifier(wis, pb) ?: "" else ""}")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = skillSurvival,
                            onCheckedChange = { skillSurvival = it }
                        )
                        Text("Supervivencia ${if (skillSurvival) calculateModifier(wis, pb) ?: "" else ""}")
                    }

                    // Carisma
                    Text(
                        text = "Habilidades de Carisma",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillDeception,
                                onCheckedChange = { skillDeception = it }
                            )
                            Text("Engaño ${if (skillDeception) calculateModifier(cha, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillIntimidation,
                                onCheckedChange = { skillIntimidation = it }
                            )
                            Text("Intimidación ${if (skillIntimidation) calculateModifier(cha, pb) ?: "" else ""}")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillPerformance,
                                onCheckedChange = { skillPerformance = it }
                            )
                            Text("Interpretación ${if (skillPerformance) calculateModifier(cha, pb) ?: "" else ""}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skillPersuasion,
                                onCheckedChange = { skillPersuasion = it }
                            )
                            Text("Persuasión ${if (skillPersuasion) calculateModifier(cha, pb) ?: "" else ""}")
                        }
                    }
                }

                // Resistances and Immunities Section
                CreateMonsterCard(
                    title = "Resistencias e Inmunidades",
                    icon = Icons.Default.Security
                ) {
                    // Resistances
                    Text(
                        text = "Resistencias al daño",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        damageTypes.forEach { type ->
                            FilterChip(
                                selected = selectedResistances.contains(type),
                                onClick = {
                                    if (selectedResistances.contains(type)) {
                                        selectedResistances -= type
                                    } else {
                                        selectedResistances += type
                                        selectedImmunities -= type // Elimina de inmunidades si está seleccionado
                                        if (customImmunity.equals(type, ignoreCase = true)) {
                                            customImmunity = "" // Limpia customImmunity si coincide
                                        }
                                    }
                                },
                                label = { Text(type) },
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customResistance,
                            onValueChange = { if (it.length <= 20) customResistance = it },
                            label = { Text("Resistencia Personalizada") },
                            modifier = Modifier.weight(1f),
                            isError = customResistanceError != null,
                            trailingIcon = {
                                Text(
                                    text = "${customResistance.length}/20",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                    customResistanceError?.let {
                        Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Immunities
                    Text(
                        text = "Inmunidades al daño",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        damageTypes.forEach { type ->
                            FilterChip(
                                selected = selectedImmunities.contains(type),
                                onClick = {
                                    if (selectedImmunities.contains(type)) {
                                        selectedImmunities -= type
                                    } else {
                                        selectedImmunities += type
                                        selectedResistances -= type
                                        if (customResistance.equals(type, ignoreCase = true)) {
                                            customResistance = ""
                                        }
                                    }
                                },
                                label = { Text(type) },
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customImmunity,
                            onValueChange = { if (it.length <= 20) customImmunity = it },
                            label = { Text("Inmunidad Personalizada") },
                            modifier = Modifier.weight(1f),
                            isError = customImmunityError != null,
                            trailingIcon = {
                                Text(
                                    text = "${customImmunity.length}/20",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                    customImmunityError?.let {
                        Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }

                CreateMonsterCard(title = "Sentidos") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sensesTypes.forEach { sense ->
                            FilterChip(
                                selected = selectedSenses.contains(sense),
                                onClick = {
                                    if (selectedSenses.contains(sense)) {
                                        selectedSenses.remove(sense)
                                    } else {
                                        selectedSenses.add(sense)
                                        if (customSense.equals(sense, ignoreCase = true)) {
                                            customSense = ""
                                        }
                                    }
                                },
                                label = { Text(sense) },
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customSense,
                            onValueChange = { if (it.length <= 30) customSense = it },
                            label = { Text("Sentido Personalizado") },
                            modifier = Modifier.weight(1f),
                            isError = customSenseError != null,
                            trailingIcon = {
                                Text(
                                    text = "${customSense.length}/30",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                    customSenseError?.let {
                        Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }

                CreateMonsterCard(title = "Idiomas") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        languageTypes.forEach { language ->
                            FilterChip(
                                selected = selectedLanguages.contains(language),
                                onClick = {
                                    if (selectedLanguages.contains(language)) {
                                        selectedLanguages.remove(language)
                                    } else {
                                        selectedLanguages.add(language)
                                        if (customLanguage.equals(language, ignoreCase = true)) {
                                            customLanguage = ""
                                        }
                                    }
                                },
                                label = { Text(language) },
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customLanguage,
                            onValueChange = { if (it.length <= 30) customLanguage = it },
                            label = { Text("Idioma Personalizado") },
                            modifier = Modifier.weight(1f),
                            isError = customLanguageError != null,
                            trailingIcon = {
                                Text(
                                    text = "${customLanguage.length}/30",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                    customLanguageError?.let {
                        Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }

                CreateMonsterCard(title = "Rasgos") {
                    traits.forEachIndexed { index, (name, entry) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        if (it.length <= 50) {
                                            traits[index] = it to entry
                                            traitNameErrors[index] = when {
                                                it.isBlank() -> "El nombre no puede estar vacío"
                                                it.length > 50 -> "Máximo 50 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Nombre de Rasgo") },
                                    isError = traitNameErrors.getOrNull(index) != null,
                                    supportingText = { traitNameErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        Text(
                                            text = "${name.length}/50",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = entry,
                                    onValueChange = {
                                        if (it.length <= 500) {
                                            traits[index] = name to it
                                            traitEntryErrors[index] = when {
                                                it.isBlank() -> "La descripción no puede estar vacía"
                                                it.length > 500 -> "Máximo 500 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Descripción de Rasgo") },
                                    isError = traitEntryErrors.getOrNull(index) != null,
                                    supportingText = { traitEntryErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 5,
                                    trailingIcon = {
                                        Text(
                                            text = "${entry.length}/500",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                            }
                            IconButton(onClick = {
                                traits.removeAt(index)
                                traitNameErrors.removeAt(index)
                                traitEntryErrors.removeAt(index)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar rasgo")
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Button(
                        onClick = {
                            traits.add("" to "")
                            traitNameErrors.add("El nombre no puede estar vacío")
                            traitEntryErrors.add("La descripción no puede estar vacía")
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir rasgo", color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                CreateMonsterCard(title = "Acciones") {
                    actions.forEachIndexed { index, (name, entry) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        if (it.length <= 50) {
                                            actions[index] = it to entry
                                            actionNameErrors[index] = when {
                                                it.isBlank() -> "El nombre no puede estar vacío"
                                                it.length > 50 -> "Máximo 50 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Nombre de Acción") },
                                    isError = actionNameErrors.getOrNull(index) != null,
                                    supportingText = { actionNameErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        Text(
                                            text = "${name.length}/50",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = entry,
                                    onValueChange = {
                                        if (it.length <= 500) {
                                            actions[index] = name to it
                                            actionEntryErrors[index] = when {
                                                it.isBlank() -> "La descripción no puede estar vacía"
                                                it.length > 500 -> "Máximo 500 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Descripción de Acción") },
                                    isError = actionEntryErrors.getOrNull(index) != null,
                                    supportingText = { actionEntryErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 5,
                                    trailingIcon = {
                                        Text(
                                            text = "${entry.length}/500",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                            }
                            IconButton(onClick = {
                                actions.removeAt(index)
                                actionNameErrors.removeAt(index)
                                actionEntryErrors.removeAt(index)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar acción")
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Button(
                        onClick = {
                            actions.add("" to "")
                            actionNameErrors.add("El nombre no puede estar vacío")
                            actionEntryErrors.add("La descripción no puede estar vacía")
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir Accción", color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                CreateMonsterCard(title = "Acciones Bonus") {
                    bonusActions.forEachIndexed { index, (name, entry) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        if (it.length <= 50) {
                                            bonusActions[index] = it to entry
                                            bonusActionNameErrors[index] = when {
                                                it.isBlank() -> "El nombre no puede estar vacío"
                                                it.length > 50 -> "Máximo 50 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Nombre de Acción Bonus") },
                                    isError = bonusActionNameErrors.getOrNull(index) != null,
                                    supportingText = { bonusActionNameErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        Text(
                                            text = "${name.length}/50",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = entry,
                                    onValueChange = {
                                        if (it.length <= 500) {
                                            bonusActions[index] = name to it
                                            bonusActionEntryErrors[index] = when {
                                                it.isBlank() -> "La descripción no puede estar vacía"
                                                it.length > 500 -> "Máximo 500 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Descripción de Acción Bonus") },
                                    isError = bonusActionEntryErrors.getOrNull(index) != null,
                                    supportingText = { bonusActionEntryErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 5,
                                    trailingIcon = {
                                        Text(
                                            text = "${entry.length}/500",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                            }
                            IconButton(onClick = {
                                bonusActions.removeAt(index)
                                bonusActionNameErrors.removeAt(index)
                                bonusActionEntryErrors.removeAt(index)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar acción bonus")
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Button(
                        onClick = {
                            bonusActions.add("" to "")
                            bonusActionNameErrors.add("El nombre no puede estar vacío")
                            bonusActionEntryErrors.add("La descripción no puede estar vacía")
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir Acción Bonus", color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                CreateMonsterCard(title = "Reacciones") {
                    reactions.forEachIndexed { index, (name, entry) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        if (it.length <= 50) {
                                            reactions[index] = it to entry
                                            reactionNameErrors[index] = when {
                                                it.isBlank() -> "El nombre no puede estar vacío"
                                                it.length > 50 -> "Máximo 50 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Nombre de reacción") },
                                    isError = reactionNameErrors.getOrNull(index) != null,
                                    supportingText = { reactionNameErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        Text(
                                            text = "${name.length}/50",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = entry,
                                    onValueChange = {
                                        if (it.length <= 500) {
                                            reactions[index] = name to it
                                            reactionEntryErrors[index] = when {
                                                it.isBlank() -> "La descripción no puede estar vacía"
                                                it.length > 500 -> "Máximo 500 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Descripción de Reacción") },
                                    isError = reactionEntryErrors.getOrNull(index) != null,
                                    supportingText = { reactionEntryErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 5,
                                    trailingIcon = {
                                        Text(
                                            text = "${entry.length}/500",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                            }
                            IconButton(onClick = {
                                reactions.removeAt(index)
                                reactionNameErrors.removeAt(index)
                                reactionEntryErrors.removeAt(index)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar reacción")
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Button(
                        onClick = {
                            reactions.add("" to "")
                            reactionNameErrors.add("El nombre no puede estar vacío")
                            reactionEntryErrors.add("La descripción no puede estar vacía")
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir Reacción", color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                CreateMonsterCard(title = "Acciones Legendarias") {
                    legendaryActions.forEachIndexed { index, (name, entry) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        if (it.length <= 50) {
                                            legendaryActions[index] = it to entry
                                            legendaryActionNameErrors[index] = when {
                                                it.isBlank() -> "El nombre no puede estar vacío"
                                                it.length > 50 -> "Máximo 50 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Nombre de Acción Legendaria") },
                                    isError = legendaryActionNameErrors.getOrNull(index) != null,
                                    supportingText = { legendaryActionNameErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        Text(
                                            text = "${name.length}/50",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = entry,
                                    onValueChange = {
                                        if (it.length <= 500) {
                                            legendaryActions[index] = name to it
                                            legendaryActionEntryErrors[index] = when {
                                                it.isBlank() -> "La descripción no puede estar vacía"
                                                it.length > 500 -> "Máximo 500 caracteres"
                                                else -> null
                                            }
                                        }
                                    },
                                    label = { Text("Descripción de Acción Legendaria") },
                                    isError = legendaryActionEntryErrors.getOrNull(index) != null,
                                    supportingText = { legendaryActionEntryErrors.getOrNull(index)?.let { Text(it, color = Color.Red) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 5,
                                    trailingIcon = {
                                        Text(
                                            text = "${entry.length}/500",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                            }
                            IconButton(onClick = {
                                legendaryActions.removeAt(index)
                                legendaryActionNameErrors.removeAt(index)
                                legendaryActionEntryErrors.removeAt(index)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar acción legendaria")
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Button(
                        onClick = {
                            legendaryActions.add("" to "")
                            legendaryActionNameErrors.add("El nombre no puede estar vacío")
                            legendaryActionEntryErrors.add("La descripción no puede estar vacía")
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir Acción Legendaria", color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                // Spellcasting
                CreateMonsterCard(
                    title = "Hechizos",
                    icon = Icons.Default.Star
                ) {
                    // Selector de característica de lanzamiento
                    var expanded by remember { mutableStateOf(false) }
                    val abilities = listOf("Carisma (CHA)", "Sabiduria (WIS)", "Inteligencia (INT)")
                    val abilityMap = mapOf(
                        "Carisma (CHA)" to "cha",
                        "Sabiduria (WIS)" to "wis",
                        "Inteligencia (INT)" to "int"
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = abilities.firstOrNull {
                                abilityMap[it] == spellcastingAbility
                            } ?: "Select Ability",
                            onValueChange = {},
                            label = { Text("Habilidad de Conjuro") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            abilities.forEach { ability ->
                                DropdownMenuItem(
                                    text = { Text(ability) },
                                    onClick = {
                                        spellcastingAbility = abilityMap[ability] ?: "cha"
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedSpells.isNotEmpty()) {
                        Text("Hechizos Seleccionados:", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))

                        selectedSpells.groupBy { it.level }.toSortedMap().forEach { (level, spells) ->
                            Text(
                                "Nivel $level:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            spells.forEach { spell ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "• ${spell.name} (${spell.school})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(start = 16.dp)
                                            .weight(1f)
                                    )
                                    IconButton(
                                        onClick = { selectedSpells.remove(spell) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Borrar hechizo",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Button(
                        onClick = { showSpellModal = true },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (selectedSpells.isEmpty()) "Añadir Hechizos" else "Gestionar Hechizos",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Modal de selección de hechizos
                if (showSpellModal) {
                    SpellSelectionModal(
                        onDismiss = { showSpellModal = false },
                        onConfirm = { spells ->
                            selectedSpells.clear()
                            selectedSpells.addAll(spells)
                            showSpellModal = false
                        },
                        firestoreManager = firestoreManager,
                        authManager = authManager,
                        selectedSpells = selectedSpells // Pasa los hechizos ya seleccionados
                    )
                }

            }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellSelectionModal(
    onDismiss: () -> Unit,
    onConfirm: (List<Spell>) -> Unit,
    firestoreManager: FirestoreManager,
    authManager: AuthManager,
    selectedSpells: List<Spell> // Añadimos los hechizos ya seleccionados
) {
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var spells by remember { mutableStateOf<List<Spell>>(emptyList()) }
    val currentSelectedSpells = remember { selectedSpells.toMutableStateList() }
    val coroutineScope = rememberCoroutineScope()
    val userId = authManager.getCurrentUserId()

    LaunchedEffect(Unit) {
        isLoading = true
        coroutineScope.launch {
            spells = firestoreManager.getSpells(userId ?: "")
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        title = {
            Text(
                text = "Seleccionar Hechizos",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = CinzelDecorative,
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = 0.5.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar hechizos", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = CinzelDecorative)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(48.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        strokeWidth = 3.dp
                    )
                } else {
                    val filteredSpells = spells.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }.sortedWith(
                        compareBy<Spell> { it.level }.thenBy { it.name }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .padding(8.dp)
                    ) {
                        filteredSpells.groupBy { it.level }.forEach { (level, spellsForLevel) ->
                            item {
                                Text(
                                    text = "Hechizos de Nivel $level",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = CinzelDecorative,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        letterSpacing = 0.1.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }

                            items(spellsForLevel) { spell ->
                                val isSelected = currentSelectedSpells.any { it.name == spell.name }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (isSelected) {
                                                currentSelectedSpells.removeIf { it.name == spell.name }
                                            } else {
                                                currentSelectedSpells.add(spell)
                                            }
                                        }
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.tertiary,
                                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Nv. $level ${spell.school} - ${spell.name}",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontFamily = CinzelDecorative,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                            Text(
                                                text = "Componentes: ${spell.components.v?.let { "V" } ?: ""}${spell.components.s?.let { "S" } ?: ""}${spell.components.m?.let { " M(${it})" } ?: ""}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = CinzelDecorative,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentSelectedSpells.toList()) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(
                    text = "Confirmar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = CinzelDecorative
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = CinzelDecorative
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}