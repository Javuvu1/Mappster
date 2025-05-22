package com.javier.mappster.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.javier.mappster.model.Spell
import com.javier.mappster.utils.normalizeSpellName
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val spellsCollection = db.collection("spells")

    suspend fun getSpells(userId: String? = null): List<Spell> {
        if (userId == null) {
            Log.e("FirestoreManager", "User ID is null, fetching only non-custom spells")
            try {
                val nonCustomQuery = spellsCollection
                    .whereEqualTo("custom", false)
                    .get()
                    .await()
                val spells = nonCustomQuery.documents.mapNotNull {
                    it.toObject(Spell::class.java)?.also { spell ->
                        Log.d("FirestoreManager", "Parsed non-custom spell: ${spell.name}, custom=${spell.custom}, userId=${spell.userId}, public=${spell.public}")
                    }
                }.sortedBy { it.name.trim().lowercase() }
                Log.d("FirestoreManager", "Fetched ${spells.size} non-custom spells: ${spells.map { it.name }}")
                return spells
            } catch (e: Exception) {
                Log.e("FirestoreManager", "Error fetching non-custom spells: ${e.message}", e)
                return emptyList()
            }
        }

        return try {
            // Consulta 1: Hechizos no personalizados
            val nonCustomQuery = spellsCollection
                .whereEqualTo("custom", false)
            // Consulta 2: Hechizos personalizados del usuario
            val userCustomQuery = spellsCollection
                .whereEqualTo("custom", true)
                .whereEqualTo("userId", userId)
            // Consulta 3: Hechizos personalizados públicos
            val publicCustomQuery = spellsCollection
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
            }.distinctBy { it.name }.sortedBy { it.name.trim().lowercase() }

            Log.d("FirestoreManager", "Fetched ${spells.size} spells from spells collection: ${spells.map { it.name }}")
            spells
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error fetching spells from spells collection: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun createSpell(id: String, spell: Spell): Boolean {
        return try {
            val document = spellsCollection.document(id)
            document.get().await().let { doc ->
                if (doc.exists()) {
                    Log.d("FirestoreManager", "Spell with ID $id already exists")
                    return false
                }
            }
            document.set(spell).await()
            Log.d("FirestoreManager", "Spell created successfully: ${spell.name}, ID: $id, data: $spell")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error creating spell ${spell.name} with ID $id: ${e.message}", e)
            spellsCollection.document(id).get().await().let { doc ->
                Log.d("FirestoreManager", "Spell with ID $id exists after error: ${doc.exists()}")
                return doc.exists()
            }
        }
    }

    private fun getSpellId(spell: Spell): String {
        return if (spell.custom) normalizeSpellName(spell.name) else spell.name
    }

    suspend fun updateSpell(spell: Spell): Boolean {
        return try {
            val id = getSpellId(spell)
            spellsCollection.document(id).set(spell).await()
            Log.d("FirestoreManager", "Spell updated successfully: ${spell.name}, ID: $id, data: $spell")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error updating spell ${spell.name}: ${e.message}", e)
            false
        }
    }

    suspend fun deleteSpell(spellName: String): Boolean {
        return try {
            spellsCollection.document(spellName).delete().await()
            Log.d("FirestoreManager", "Spell deleted successfully: $spellName")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error deleting spell $spellName: ${e.message}", e)
            false
        }
    }

    suspend fun updateSpellVisibility(spellName: String, public: Boolean): Boolean {
        return try {
            spellsCollection.document(spellName)
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