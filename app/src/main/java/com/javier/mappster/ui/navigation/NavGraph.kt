package com.javier.mappster.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.javier.mappster.ui.SpellListScreen
import com.javier.mappster.ui.screen.SpellDetailScreen

fun NavGraphBuilder.appNavGraph(navController: NavHostController) {
    composable<SpellListDestination> {
        SpellListScreen(
            onSpellClick = { spell ->
                navController.navigate(
                    SpellDetailDestination(
                        spellName = spell.name,
                        spellSchool = spell.school,
                        spellLevel = spell.level,
                        spellSource = spell.source
                    )
                )
            }
        )
    }

    composable<SpellDetailDestination> {
        val args = it.toRoute<SpellDetailDestination>()
        SpellDetailScreen(
            spellDetails = args,
            onBackClick = { navController.popBackStack() }
        )
    }
}
