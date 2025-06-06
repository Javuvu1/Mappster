package com.javier.mappster.navigation

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import android.content.Context
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
import com.javier.mappster.data.FirestoreManager
import com.javier.mappster.data.LocalDataManager
import com.javier.mappster.model.Monster
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
import com.javier.mappster.ui.screen.spells.CreateSpellScreen
import com.javier.mappster.ui.screen.spells.EditSpellScreen
import com.javier.mappster.ui.screen.spells.SpellDetailScreen
import com.javier.mappster.ui.screen.spells.SpellListScreen
import com.javier.mappster.ui.screen.spells.provideSpellListViewModel
import com.javier.mappster.ui.screen.spellList.SpellListViewScreen
import com.javier.mappster.viewmodel.MonsterListViewModel
import com.javier.mappster.viewmodel.MonsterListViewModelFactory
import kotlinx.coroutines.delay
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

    NavHost(navController = navController, startDestination = Destinations.LOGIN) {
        composable(Destinations.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(Destinations.SPELL_LIST) {
            SpellListScreen(
                viewModel = spellListViewModel,
                onSpellClick = { spell ->
                    val encodedName = java.net.URLEncoder.encode(spell.name, "UTF-8")
                    navController.navigate("${Destinations.SPELL_DETAIL}/$encodedName")
                },
                onCreateSpellClick = {
                    navController.navigate(Destinations.CREATE_SPELL)
                },
                onEditSpellClick = { spell ->
                    val encodedName = java.net.URLEncoder.encode(spell.name, "UTF-8")
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
                Log.d("NavGraph", "Spell '$spellName' not found, popping back to SPELL_LIST")
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
            val spell = spellName?.let { spellListViewModel.getSpellByName(it) }
            spell?.let {
                EditSpellScreen(
                    spell = it,
                    viewModel = spellListViewModel,
                    onSpellUpdated = {
                        navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
                    }
                )
            } ?: navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
        }
        composable(Destinations.CUSTOM_SPELL_LISTS) {
            CustomSpellListsScreen(navController = navController)
        }
        composable(Destinations.MONSTER_LIST) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
    }
}