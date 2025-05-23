package com.javier.mappster.data

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

class AuthManager private constructor(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("156216937024-np0619iobt2rds49vrti2s99odtcqa4g.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    companion object {
        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("AuthManager", "No authenticated user found")
        } else {
            Log.d("AuthManager", "Current user ID: $userId")
        }
        return userId
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun signInWithGoogle(data: Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).await()
            Log.d("AuthManager", "Google Sign-In successful, user ID: ${auth.currentUser?.uid}")
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In failed: ${e.message}", e)
            false
        }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        Log.d("AuthManager", "User signed out")
    }

    fun userStateFlow(): Flow<String?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid).also {
                Log.d("AuthManager", "Emitting user ID: ${auth.currentUser?.uid}")
            }
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
            Log.d("AuthManager", "AuthStateListener removed")
        }
    }.distinctUntilChanged()
}