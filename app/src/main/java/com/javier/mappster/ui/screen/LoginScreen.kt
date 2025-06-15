package com.javier.mappster.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.R
import com.javier.mappster.data.AuthManager
import com.javier.mappster.navigation.Destinations
import com.javier.mappster.ui.theme.CinzelDecorative
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
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.dragon),
            contentDescription = "Fondo de dragón",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.7f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Mappster",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = CinzelDecorative,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 48.sp
                )
            )

            Text(
                text = "Para Directores y Jugadores",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = CinzelDecorative,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    fontSize = 20.sp
                ),
            )

            Text(
                text = "Dragones y Mazmorras (5ª Edición)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = CinzelDecorative,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { launcher.launch(authManager.getSignInIntent()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Logo de Google",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar sesión con Google")
                }
            }
        }
    }
}