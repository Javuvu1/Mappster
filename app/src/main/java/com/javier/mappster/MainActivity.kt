package com.javier.mappster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.javier.mappster.ui.SpellListScreen
import com.javier.mappster.ui.SpellListScreen
import com.javier.mappster.ui.navigation.SpellListDestination
import com.javier.mappster.ui.navigation.appNavGraph

import com.javier.mappster.ui.theme.MappsterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MappsterTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = SpellListDestination
                ) {
                    appNavGraph(navController)
                }
            }
        }
    }
}

