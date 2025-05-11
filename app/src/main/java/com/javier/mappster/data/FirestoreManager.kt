package com.javier.mappster.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.javier.mappster.model.Spell
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getSpells(): List<Spell> {
        return try {
            val snapshot = db.collection("spells").get().await()
            val spells = snapshot.documents.mapNotNull { it.toObject(Spell::class.java) }
            Log.d("FirestoreManager", "Fetched ${spells.size} spells")
            spells
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error fetching spells", e)
            emptyList()
        }
    }

}
