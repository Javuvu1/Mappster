package com.javier.mappster.navigation

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.javier.mappster.data.AuthManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.ui.CustomMonsterListsScreen
import com.javier.mappster.ui.LoginScreen
import com.javier.mappster.ui.screen.CreateMonsterScreen
import com.javier.mappster.ui.screen.CustomMonsterDetailScreen
import com.javier.mappster.ui.screen.MonsterDetailScreen
import com.javier.mappster.ui.screen.MonsterListScreen
import com.javier.mappster.ui.screen.initiativeTracker.InitiativeTrackerScreen
import com.javier.mappster.ui.screen.initiativeTracker.TwoPaneInitiativeTrackerScreen
import com.javier.mappster.ui.screen.monsters.TwoPaneMonsterListScreen
import com.javier.mappster.ui.screen.spellList.CreateSpellListScreen
import com.javier.mappster.ui.screen.spellList.CustomSpellListsScreen
import com.javier.mappster.ui.screen.spellList.SpellListViewScreen
import com.javier.mappster.ui.screen.spells.CreateSpellScreen
import com.javier.mappster.ui.screen.spells.EditSpellScreen
import com.javier.mappster.ui.screen.spells.SpellDetailScreen
import com.javier.mappster.ui.screen.spells.SpellListScreen
import com.javier.mappster.ui.screen.spells.provideSpellListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModelFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLDecoder

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val spellListViewModel = provideSpellListViewModel(context)
    val dataManager = remember { LocalDataManager(context) }
    val authManager = remember { AuthManager.getInstance(context) }

    LaunchedEffect(navController) {
        Log.d("NavGraph", "Available routes: ${navController.graph.map { it.route ?: "null" }.joinToString(", ")}")
    }

    NavHost(navController = navController, startDestination = Destinations.LOGIN) {
        composable(Destinations.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(Destinations.SPELL_LIST) {
            SpellListScreen(
                viewModel = spellListViewModel,
                onSpellClick = { spell ->
                    val encodedName = java.net.URLEncoder.encode(spell.name, "UTF-8")
                    Log.d("NavGraph", "Navigating to spell_detail/$encodedName from spell_list")
                    navController.navigate("${Destinations.SPELL_DETAIL}/$encodedName")
                },
                onCreateSpellClick = {
                    Log.d("NavGraph", "Navigating to create_spell")
                    navController.navigate(Destinations.CREATE_SPELL)
                },
                onEditSpellClick = { spell ->
                    val encodedName = java.net.URLEncoder.encode(spell.name, "UTF-8")
                    Log.d("NavGraph", "Navigating to edit_spell/$encodedName")
                    navController.navigate("${Destinations.EDIT_SPELL.replace("{spellName}", encodedName)}")
                },
                navController = navController
            )
        }
        composable(
            route = "${Destinations.SPELL_DETAIL}/{spellName}",
            arguments = listOf(navArgument("spellName") { type = NavType.StringType })
        ) { backStackEntry ->
            val spellName = backStackEntry.arguments?.getString("spellName")?.let {
                URLDecoder.decode(it, "UTF-8")
            }
            Log.d("NavGraph", "Navigating to spell_detail, spellName: '$spellName'")
            val spell = spellName?.let { spellListViewModel.getSpellByName(it) }
            Log.d("NavGraph", "Found spell: $spell")
            spell?.let {
                SpellDetailScreen(
                    spell = it,
                    isTwoPaneMode = false,
                    navController = navController,
                    viewModel = spellListViewModel
                )
            } ?: run {
                Log.e("NavGraph", "Spell '$spellName' not found, popping back to SPELL_LIST")
                navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
            }
        }
        composable(Destinations.CREATE_SPELL) {
            CreateSpellScreen(navController = navController)
        }
        composable(
            route = Destinations.EDIT_SPELL,
            arguments = listOf(navArgument("spellName") { type = NavType.StringType })
        ) { backStackEntry ->
            val spellName = backStackEntry.arguments?.getString("spellName")?.let {
                URLDecoder.decode(it, "UTF-8")
            }
            Log.d("NavGraph", "Navigating to edit_spell, spellName: '$spellName'")
            val spell = spellName?.let { spellListViewModel.getSpellByName(it) }
            spell?.let {
                EditSpellScreen(
                    spell = it,
                    viewModel = spellListViewModel,
                    onSpellUpdated = {
                        Log.d("NavGraph", "Spell updated, popping back to SPELL_LIST")
                        navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
                    }
                )
            } ?: run {
                Log.e("NavGraph", "Spell '$spellName' not found, popping back to SPELL_LIST")
                navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
            }
        }
        composable(Destinations.CUSTOM_SPELL_LISTS) {
            Log.d("NavGraph", "Navigating to custom_spell_lists")
            CustomSpellListsScreen(navController = navController)
        }
        composable(
            route = "${Destinations.SPELL_LIST_VIEW}/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")
            Log.d("NavGraph", "Navigating to spell_list_view, listId: '$listId'")
            if (listId != null) {
                SpellListViewScreen(
                    listId = listId,
                    viewModel = spellListViewModel,
                    navController = navController
                )
            } else {
                Log.e("NavGraph", "Invalid spell_list_view arguments, popping back to CUSTOM_SPELL_LISTS")
                navController.popBackStack(Destinations.CUSTOM_SPELL_LISTS, inclusive = false)
            }
        }
        composable(Destinations.CREATE_SPELL_LIST) {
            Log.d("NavGraph", "Navigating to create_spell_list")
            CreateSpellListScreen(
                navController = navController
            )
        }
        composable(
            route = "${Destinations.EDIT_SPELL_LIST}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("spellIds") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val name = backStackEntry.arguments?.getString("name")?.let {
                URLDecoder.decode(it, "UTF-8")
            }
            val spellIdsJson = backStackEntry.arguments?.getString("spellIds")?.let {
                URLDecoder.decode(it, "UTF-8")
            }
            Log.d("NavGraph", "Navigating to edit_spell_list, id: '$id', name: '$name', spellIdsJson: '$spellIdsJson'")
            if (id != null && name != null && spellIdsJson != null) {
                val spellIds = try {
                    Json.decodeFromString<List<String>>(spellIdsJson)
                } catch (e: Exception) {
                    Log.e("NavGraph", "Error decoding spellIds: ${e.message}")
                    emptyList<String>()
                }
                CreateSpellListScreen(
                    navController = navController,
                    listId = id,
                    listName = name,
                    spellIds = spellIds
                )
            } else {
                Log.e("NavGraph", "Invalid edit_spell_list arguments, popping back to CUSTOM_SPELL_LISTS")
                navController.popBackStack(Destinations.CUSTOM_SPELL_LISTS, inclusive = false)
            }
        }
        composable(Destinations.MONSTER_LIST) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            Log.d("NavGraph", "Navigating to monster_list, isLandscape: $isLandscape")
            if (isLandscape) {
                TwoPaneMonsterListScreen(navController = navController)
            } else {
                val monsterViewModel: MonsterListViewModel = viewModel(
                    factory = MonsterListViewModelFactory(dataManager, authManager)
                )
                MonsterListScreen(
                    navController = navController,
                    viewModel = monsterViewModel
                )
            }
        }
        composable(Destinations.CUSTOM_MONSTER_LISTS) {
            Log.d("NavGraph", "Navigating to custom_monster_lists")
            CustomMonsterListsScreen(navController = navController)
        }
        composable(
            route = "${Destinations.MONSTER_DETAIL}/{name}/{source}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("source") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")?.let {
                URLDecoder.decode(it, "UTF-8")
            }
            val source = backStackEntry.arguments?.getString("source")?.let {
                URLDecoder.decode(it, "UTF-8")
            }
            Log.d("NavGraph", "Navigating to monster_detail, name: '$name', source: '$source'")
            if (name != null && source != null) {
                var monster by remember { mutableStateOf<com.javier.mappster.model.Monster?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(name, source) {
                    try {
                        val result = dataManager.getMonsterByNameAndSource(name, source)
                        monster = result
                    } catch (e: Exception) {
                        error = "Error al cargar el monstruo: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> CircularProgressIndicator()
                        error != null -> Text(
                            text = error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        monster != null -> MonsterDetailScreen(
                            monster = monster!!,
                            isTwoPaneMode = false
                        )
                        else -> Text(
                            text = "Monstruo no encontrado",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Log.e("NavGraph", "Invalid monster_detail arguments, popping back to MONSTER_LIST")
                navController.popBackStack(Destinations.MONSTER_LIST, inclusive = false)
            }
        }
        composable(Destinations.CREATE_MONSTER) {
            Log.d("NavGraph", "Navigating to create_monster")
            CreateMonsterScreen(navController = navController)
        }
        composable(
            route = "${Destinations.CUSTOM_MONSTER_DETAIL}/{monsterId}",
            arguments = listOf(navArgument("monsterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val monsterId = backStackEntry.arguments?.getString("monsterId")
            Log.d("NavGraph", "Navigating to custom_monster_detail, monsterId: '$monsterId'")
            if (monsterId != null) {
                CustomMonsterDetailScreen(
                    monsterId = monsterId,
                    navController = navController,
                    isTwoPaneMode = false
                )
            } else {
                Log.e("NavGraph", "Invalid custom_monster_detail arguments, popping back to CUSTOM_MONSTER_LISTS")
                navController.popBackStack(Destinations.CUSTOM_MONSTER_LISTS, inclusive = false)
            }
        }
        composable(Destinations.INITIATIVE_TRACKER) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val monsterViewModel: MonsterListViewModel = viewModel(
                factory = MonsterListViewModelFactory(dataManager, authManager)
            )
            Log.d("NavGraph", "Navigating to initiative_tracker, isLandscape: $isLandscape")
            if (isLandscape) {
                TwoPaneInitiativeTrackerScreen(
                    navController = navController,
                    monsterViewModel = monsterViewModel
                )
            } else {
                InitiativeTrackerScreen(
                    navController = navController,
                    monsterViewModel = monsterViewModel,
                    isTwoPaneMode = false
                )
            }
        }
    }
}