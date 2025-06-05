package com.javier.mappster.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.javier.mappster.model.CustomMonster
import com.javier.mappster.model.Spell
import com.javier.mappster.model.SpellList
import com.javier.mappster.utils.normalizeSpellName
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val spellsCollection = db.collection("spells")
    private val spellListsCollection = db.collection("spellLists")
    private val monstersCollection = db.collection("custom_monsters")

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
            val nonCustomQuery = spellsCollection
                .whereEqualTo("custom", false)
            val userCustomQuery = spellsCollection
                .whereEqualTo("custom", true)
                .whereEqualTo("userId", userId)
            val publicCustomQuery = spellsCollection
                .whereEqualTo("custom", true)
                .whereEqualTo("public", true)

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
        return normalizeSpellName(spell.name)
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

    suspend fun getSpellLists(userId: String): List<SpellList> {
        return try {
            val query = spellListsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val spellLists = query.documents.mapNotNull {
                it.toObject(SpellList::class.java)?.also { list ->
                    Log.d("FirestoreManager", "Parsed spell list: ${list.name}, id=${list.id}, userId=${list.userId}, spellIds=${list.spellIds}")
                }
            }.sortedBy { it.name.trim().lowercase() }
            Log.d("FirestoreManager", "Fetched ${spellLists.size} spell lists: ${spellLists.map { it.name }}")
            spellLists
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error fetching spell lists: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getSpellListById(listId: String): SpellList? {
        return try {
            val snapshot = spellListsCollection
                .document(listId)
                .get()
                .await()
            val spellList = snapshot.toObject(SpellList::class.java)
            spellList?.also {
                Log.d("FirestoreManager", "Fetched spell list: ${it.name}, id=${it.id}, userId=${it.userId}, spellIds=${it.spellIds}")
            } ?: run {
                Log.d("FirestoreManager", "Spell list with id $listId not found")
            }
            spellList
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error fetching spell list $listId: ${e.message}", e)
            null
        }
    }

    suspend fun createSpellList(spellList: SpellList): Boolean {
        return try {
            val document = spellListsCollection.document()
            val spellListWithId = spellList.copy(id = document.id)
            document.set(spellListWithId).await()
            Log.d("FirestoreManager", "Spell list created successfully: ${spellList.name}, id: ${document.id}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error creating spell list ${spellList.name}: ${e.message}", e)
            false
        }
    }

    suspend fun updateSpellList(spellList: SpellList): Boolean {
        return try {
            spellListsCollection.document(spellList.id).set(spellList).await()
            Log.d("FirestoreManager", "Spell list updated successfully: ${spellList.name}, id: ${spellList.id}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error updating spell list ${spellList.name}: ${e.message}", e)
            false
        }
    }

    suspend fun deleteSpellList(listId: String): Boolean {
        return try {
            spellListsCollection.document(listId).delete().await()
            Log.d("FirestoreManager", "Spell list deleted successfully: $listId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error deleting spell list $listId: ${e.message}", e)
            false
        }
    }

    // Guardar un monstruo personalizado
    suspend fun saveCustomMonster(customMonster: CustomMonster) {
        val monsterId = customMonster.id ?: monstersCollection.document().id
        monstersCollection
            .document(monsterId)
            .set(customMonster.copy(id = monsterId))
            .await()
        Log.d("FirestoreManager", "CustomMonster saved successfully: ${customMonster.name}, id: $monsterId")
    }

    // Obtener monstruos personalizados
    suspend fun getCustomMonsters(userId: String?): List<CustomMonster> {
        return try {
            if (userId == null) {
                Log.d("FirestoreManager", "User ID is null, not fetching custom monsters")
                return emptyList()
            }

            val userCustomQuery = monstersCollection
                .whereEqualTo("userId", userId)  // Elimina el filtro de source

            val publicCustomQuery = monstersCollection
                .whereEqualTo("public", true)

            val results = listOf(
                userCustomQuery.get(),
                publicCustomQuery.get()
            ).map { it.await() }

            val monsters = results.flatMap { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        Log.d("FirestoreManager", "Processing document: ${document.id}")
                        Log.d("FirestoreManager", "Raw document data: ${document.data}")
                        val customMonster = document.toObject(CustomMonster::class.java)
                        if (customMonster != null) {
                            val monsterWithId = customMonster.copy(id = document.id)
                            Log.d("FirestoreManager", "Parsed CustomMonster: ${monsterWithId.name}, id=${monsterWithId.id}, userId=${monsterWithId.userId}")
                            monsterWithId
                        } else {
                            Log.e("FirestoreManager", "Failed to parse document ${document.id} to CustomMonster")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("FirestoreManager", "Error parsing CustomMonster document ${document.id}: ${e.message}", e)
                        Log.d("FirestoreManager", "Stack trace: ${e.stackTraceToString()}")
                        null
                    }
                }
            }.distinctBy { it.id }.sortedBy { it.name.trim().lowercase() }

            Log.d("FirestoreManager", "Fetched ${monsters.size} custom monsters: ${monsters.map { it.name }}")
            monsters
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error fetching custom monsters: ${e.message}", e)
            Log.d("FirestoreManager", "Stack trace: ${e.stackTraceToString()}")
            emptyList()
        }
    }

    suspend fun deleteCustomMonster(monsterId: String) {
        monstersCollection.document(monsterId).delete().await()
        Log.d("FirestoreManager", "CustomMonster deleted successfully: id=$monsterId")
    }
}