package com.javier.mappster.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.javier.mappster.ui.screen.spellList.CreateSpellListScreen
import com.javier.mappster.ui.screen.spellList.CustomSpellListsScreen
import com.javier.mappster.ui.screen.spells.CreateSpellScreen
import com.javier.mappster.ui.screen.spells.EditSpellScreen
import com.javier.mappster.ui.screen.spells.SpellDetailScreen
import com.javier.mappster.ui.screen.spells.SpellListScreen
import com.javier.mappster.ui.screen.spells.SpellListViewModel
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
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            val spell = spellListViewModel.spells.value.find { it.name == spellName }
            spell?.let {
                SpellDetailScreen(spell = it)
            } ?: navController.popBackStack(Destinations.SPELL_LIST, inclusive = false)
        }
        composable(Destinations.CREATE_SPELL) {
            CreateSpellScreen(navController = navController)
        }
        composable(
            route = Destinations.EDIT_SPELL,
            arguments = listOf(navArgument("spellName") { type = NavType.StringType })
        ) { backStackEntry ->
            val spellName = backStackEntry.arguments?.getString("spellName")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            val spell = spellListViewModel.spells.value.find { it.name == spellName }
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
            val monsterViewModel: MonsterListViewModel = viewModel(
                factory = MonsterListViewModelFactory(dataManager, authManager)
            )
            MonsterListScreen(
                navController = navController,
                viewModel = monsterViewModel
            )
        }
        composable(Destinations.CUSTOM_MONSTER_LISTS) {
            CustomMonsterListsScreen(navController = navController)
        }
        composable(Destinations.CREATE_MONSTER) {
            CreateMonsterScreen(navController = navController)
        }
        composable("create_spell_list") {
            CreateSpellListScreen(
                viewModel = spellListViewModel,
                navController = navController
            )
        }
        composable(
            route = "create_spell_list/{listId}/{listName}/{spellIds}",
            arguments = listOf(
                navArgument("listId") { type = NavType.StringType; nullable = true },
                navArgument("listName") { type = NavType.StringType; nullable = true },
                navArgument("spellIds") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")
            val listName = backStackEntry.arguments?.getString("listName")
            val spellIdsJson = backStackEntry.arguments?.getString("spellIds")
            val spellIds = spellIdsJson?.let { Json.decodeFromString<List<String>>(it) }
            CreateSpellListScreen(
                viewModel = spellListViewModel,
                navController = navController,
                listId = listId,
                listName = listName,
                spellIds = spellIds
            )
        }
        composable(
            route = "spell_list_view/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            SpellListViewScreen(
                listId = backStackEntry.arguments?.getString("listId") ?: "",
                viewModel = spellListViewModel,
                navController = navController
            )
        }
        composable(
            route = "${Destinations.MONSTER_DETAIL}/{name}/{source}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("source") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: ""
            val source = backStackEntry.arguments?.getString("source")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: ""
            var monster by remember { mutableStateOf<Monster?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(name, source) {
                try {
                    val loadedMonster = dataManager.getMonsterByNameAndSource(name, source)
                    if (loadedMonster != null) {
                        monster = loadedMonster
                    } else {
                        error = "Monstruo no encontrado."
                    }
                } catch (e: Exception) {
                    Log.e("NavGraph", "Error loading monster: ${e.message}", e)
                    error = "Error al cargar el monstruo: ${e.message}"
                } finally {
                    isLoading = false
                }
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text(
                        text = error ?: "Error desconocido.",
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    LaunchedEffect(Unit) {
                        delay(2000)
                        navController.popBackStack(Destinations.MONSTER_LIST, inclusive = false)
                    }
                }
                monster != null -> {
                    MonsterDetailScreen(monster = monster!!)
                }
                else -> {
                    Text(
                        text = "Monstruo no encontrado. Volviendo atrás...",
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    LaunchedEffect(Unit) {
                        delay(2000)
                        navController.popBackStack(Destinations.MONSTER_LIST, inclusive = false)
                    }
                }
            }
        }
        composable(
            route = "${Destinations.CUSTOM_MONSTER_DETAIL}/{monsterId}",
            arguments = listOf(navArgument("monsterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val monsterId = backStackEntry.arguments?.getString("monsterId") ?: ""
            CustomMonsterDetailScreen(
                navController = navController,
                monsterId = monsterId
            )
        }
    }
}