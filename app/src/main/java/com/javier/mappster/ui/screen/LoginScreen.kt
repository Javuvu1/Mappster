package com.javier.mappster.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.navigation.Destinations
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                val success = authManager.signInWithGoogle(result.data)
                if (success && authManager.isUserSignedIn()) {
                    navController.navigate(Destinations.SPELL_LIST) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LaunchedEffect(authManager.isUserSignedIn()) {
        if (authManager.isUserSignedIn()) {
            navController.navigate(Destinations.SPELL_LIST) {
                popUpTo(Destinations.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { launcher.launch(authManager.getSignInIntent()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Iniciar sesión con Google")
        }
    }
}