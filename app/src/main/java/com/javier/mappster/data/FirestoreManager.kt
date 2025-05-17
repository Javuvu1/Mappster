package com.javier.mappster.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.javier.mappster.model.Spell
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val spellsCollection = "spells"

    suspend fun getSpells(userId: String? = null): List<Spell> {
        if (userId == null) {
            Log.e("FirestoreManager", "User ID is null, fetching only non-custom spells")
            try {
                val nonCustomQuery = db.collection(spellsCollection)
                    .whereEqualTo("custom", false)
                    .get()
                    .await()
                val spells = nonCustomQuery.documents.mapNotNull {
                    it.toObject(Spell::class.java)?.also { spell ->
                        Log.d("FirestoreManager", "Parsed non-custom spell: ${spell.name}, custom=${spell.custom}, userId=${spell.userId}, public=${spell.public}")
                    }
                }.sortedBy { it.name.lowercase() }
                Log.d("FirestoreManager", "Fetched ${spells.size} non-custom spells: ${spells.map { it.name }}")
                return spells
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Error fetching non-custom spells: ${e.message}", e)
                return emptyList()
            }
        }

        return try {
            // Consulta 1: Hechizos no personalizados
            val nonCustomQuery = db.collection(spellsCollection)
                .whereEqualTo("custom", false)
            // Consulta 2: Hechizos personalizados del usuario
            val userCustomQuery = db.collection(spellsCollection)
                .whereEqualTo("custom", true)
                .whereEqualTo("userId", userId)
            // Consulta 3: Hechizos personalizados públicos
            val publicCustomQuery = db.collection(spellsCollection)
                .whereEqualTo("custom", true)
                .whereEqualTo("public", true)

            // Ejecutar consultas en paralelo
            val results = listOf(
                nonCustomQuery.get().also { Log.d("FirestoreManager", "Executing nonCustomQuery") },
                userCustomQuery.get().also { Log.d("FirestoreManager", "Executing userCustomQuery for userId=$userId") },
                publicCustomQuery.get().also { Log.d("FirestoreManager", "Executing publicCustomQuery") }
            ).map { task ->
                try {
                    task.await().also { snapshot ->
                        Log.d("FirestoreManager", "Query returned ${snapshot.size()} documents: ${snapshot.documents.map { it.id }}")
                        snapshot.documents.forEach { doc ->
                            Log.d("FirestoreManager", "Document data: ${doc.id} -> ${doc.data}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FirestoreManager", "Query failed: ${e.message}", e)
                    throw e
                }
            }

            // Combinar resultados, eliminar duplicados y ordenar alfabéticamente
            val spells = results.flatMap { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Spell::class.java)?.also {
                            Log.d("FirestoreManager", "Parsed spell: ${it.name}, custom=${it.custom}, userId=${it.userId}, public=${it.public}")
                        }
                    } catch (e: Exception) {
                        Log.e("FirestoreManager", "Error parsing spell document ${document.id}: ${e.message}", e)
                        null
                    }
                }
            }.distinctBy { it.name }.sortedBy { it.name.lowercase() }

            Log.d("FirestoreManager", "Fetched ${spells.size} spells from $spellsCollection: ${spells.map { it.name }}")
            spells
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error fetching spells from $spellsCollection: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun createSpell(spell: Spell): Boolean {
        return try {
            db.collection(spellsCollection)
                .document(spell.name)
                .set(spell)
                .await()
            Log.d("FirestoreManager", "Spell created successfully: ${spell.name}, data: ${spell.javaClass.declaredFields.associate { it.apply { isAccessible = true }.name to it.get(spell) }}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error creating spell ${spell.name}: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            // Verificar si el hechizo se creó a pesar del error
            val exists = try {
                db.collection(spellsCollection)
                    .document(spell.name)
                    .get()
                    .await()
                    .exists()
            } catch (e2: Exception) {
                Log.e("FirestoreManager", "Error checking if spell ${spell.name} exists: ${e2.message}")
                false
            }
            Log.d("FirestoreManager", "Spell ${spell.name} exists after error: $exists")
            exists
        }
    }

    suspend fun deleteSpell(spellName: String): Boolean {
        return try {
            db.collection(spellsCollection)
                .document(spellName)
                .delete()
                .await()
            Log.d("FirestoreManager", "Spell deleted: $spellName")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error deleting spell $spellName: ${e.message}", e)
            false
        }
    }

    suspend fun updateSpellVisibility(spellName: String, public: Boolean): Boolean {
        return try {
            db.collection(spellsCollection)
                .document(spellName)
                .update("public", public)
                .await()
            Log.d("FirestoreManager", "Spell $spellName visibility updated to public=$public")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error updating visibility for spell $spellName: ${e.message}", e)
            false
        }
    }
}