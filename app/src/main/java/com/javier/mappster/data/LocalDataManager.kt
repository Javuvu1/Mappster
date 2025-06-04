package com.javier.mappster.data

import android.content.Context
import android.util.Log
import com.javier.mappster.model.Monster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

class LocalDataManager(private val context: Context) {

    companion object {
        private var cachedMonsters: List<Monster>? = null
    }

    suspend fun loadMonsters(): Result<List<Monster>> = withContext(Dispatchers.IO) {
        try {
            if (cachedMonsters != null) {
                Log.d("LocalDataManager", "Returning cached ${cachedMonsters!!.size} monsters")
                return@withContext Result.success(cachedMonsters!!)
            }

            val jsonString = context.assets.open("bestiary-completo.json").bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            val monsters = json.decodeFromString<List<Monster>>(jsonString)
            cachedMonsters = monsters // Cachear los monstruos
            Log.d("LocalDataManager", "Successfully loaded ${monsters.size} monsters")
            Result.success(monsters)
        } catch (e: IOException) {
            Log.e("LocalDataManager", "Error reading JSON file: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("LocalDataManager", "Error deserializing JSON: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getMonsterByNameAndSource(name: String, source: String): Monster? = withContext(Dispatchers.IO) {
        try {
            if (cachedMonsters == null) {
                loadMonsters() // Asegurarnos de que los monstruos estén cargados
            }
            val monster = cachedMonsters?.find {
                it.name == name && (it.source == source || (it.source.isNullOrEmpty() && source.isEmpty()))
            }
            if (monster != null) {
                Log.d("LocalDataManager", "Found monster: ${monster.name}, source: ${monster.source}")
            } else {
                Log.w("LocalDataManager", "Monster not found for name: $name, source: $source")
            }
            monster
        } catch (e: Exception) {
            Log.e("LocalDataManager", "Error finding monster by name and source: ${e.message}", e)
            null
        }
    }
}