package com.javier.mappster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.javier.mappster.navigation.NavGraph
import com.javier.mappster.ui.theme.MappsterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MappsterTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
