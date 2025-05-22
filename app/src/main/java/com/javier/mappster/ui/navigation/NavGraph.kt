package com.javier.mappster.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.javier.mappster.ui.CreateSpellListScreen
import com.javier.mappster.ui.CreateSpellScreen
import com.javier.mappster.ui.CustomMonsterListsScreen
import com.javier.mappster.ui.CustomSpellListsScreen
import com.javier.mappster.ui.EditSpellScreen
import com.javier.mappster.ui.LoginScreen
import com.javier.mappster.ui.MonsterListScreen
import com.javier.mappster.ui.SpellListScreen
import com.javier.mappster.ui.SpellListViewScreen
import com.javier.mappster.ui.screens.SpellDetailScreen
import com.javier.mappster.viewmodel.provideSpellListViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel = provideSpellListViewModel(LocalContext.current)

    NavHost(navController = navController, startDestination = Destinations.LOGIN) {
        composable(Destinations.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(Destinations.SPELL_LIST) {
            SpellListScreen(
                viewModel = viewModel,
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
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            val spell = viewModel.spells.value.find { it.name == spellName }
            spell?.let {
                SpellDetailScreen(spell = it)
            } ?: navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
        }
        composable(Destinations.CREATE_SPELL) {
            CreateSpellScreen(
                onSpellCreatedWithRefresh = {
                    viewModel.refreshSpells()
                    navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
                }
            )
        }
        composable(
            route = Destinations.EDIT_SPELL,
            arguments = listOf(navArgument("spellName") { type = NavType.StringType })
        ) { backStackEntry ->
            val spellName = backStackEntry.arguments?.getString("spellName")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            val spell = viewModel.spells.value.find { it.name == spellName }
            spell?.let {
                EditSpellScreen(
                    spell = it,
                    viewModel = viewModel,
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
            MonsterListScreen(navController = navController)
        }
        composable(Destinations.CUSTOM_MONSTER_LISTS) {
            CustomMonsterListsScreen(navController = navController)
        }
        composable("create_spell_list") {
            CreateSpellListScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = "spell_list_view/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            SpellListViewScreen(
                listId = backStackEntry.arguments?.getString("listId") ?: "",
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}