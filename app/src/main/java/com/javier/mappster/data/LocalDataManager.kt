package com.javier.mappster.data

import android.content.Context
import android.util.Log
import com.javier.mappster.model.Monster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

class LocalDataManager(private val context: Context) {

    fun loadMonsters(): List<Monster> {
        return runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    val jsonString = context.assets.open("bestiary-completo.json").bufferedReader().use { it.readText() }
                    val json = Json { ignoreUnknownKeys = true }
                    val monsters = json.decodeFromString<List<Monster>>(jsonString)
                    Log.d("LocalDataManager", "Successfully loaded ${monsters.size} monsters")
                    monsters // Quitamos .take(50) para cargar todos los monstruos
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("LocalDataManager", "Error reading JSON file: ${e.message}")
                    emptyList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LocalDataManager", "Error deserializing JSON: ${e.message}")
                    emptyList()
                }
            }
        }
    }
}