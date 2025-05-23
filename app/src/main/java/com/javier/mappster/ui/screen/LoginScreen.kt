package com.javier.mappster.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.javier.mappster.data.AuthManager
import com.javier.mappster.navigation.Destinations
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Verificar si el usuario ya está autenticado
    LaunchedEffect(Unit) {
        if (authManager.isUserSignedIn()) {
            navController.navigate(Destinations.SPELL_LIST) {
                popUpTo(Destinations.LOGIN) { inclusive = true }
            }
        }
    }

    // Manejar el botón de retroceso para cerrar la app
    BackHandler {
        (context as? Activity)?.finish()
    }

    // Animación para la entrada de elementos
    val transition = updateTransition(targetState = true, label = "LoginAnimation")
    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "alpha"
    ) { if (it) 1f else 0f }
    val offset by transition.animateDp(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "offset"
    ) { if (it) 0.dp else 50.dp }

    // Lanzador para Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                isLoading = true
                val success = authManager.signInWithGoogle(result.data)
                isLoading = false
                if (success) {
                    navController.navigate(Destinations.SPELL_LIST) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                } else {
                    errorMessage = "Error al iniciar sesión con Google."
                }
            }
        } else {
            isLoading = false
            errorMessage = "Inicio de sesión cancelado."
        }
    }

    // Fondo degradado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Azul oscuro
                        Color(0xFF4A148C)  // Morado
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título estilizado
            Text(
                text = "Mappster",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .offset(y = offset)
                    .graphicsLayer { this.alpha = alpha }
            )

            // Botón de Google Sign-In
            Button(
                onClick = {
                    isLoading = true
                    launcher.launch(authManager.getSignInIntent())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .offset(y = offset)
                    .graphicsLayer { this.alpha = alpha },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Placeholder para el icono de Google
                    Text(
                        text = "G",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4), // Azul de Google
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (isLoading) "Iniciando..." else "Iniciar Sesión con Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Mensaje de error
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .offset(y = offset)
                        .graphicsLayer { this.alpha = alpha },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Indicador de carga
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(y = offset)
                        .graphicsLayer { this.alpha = alpha }
                )
            }
        }
    }
}