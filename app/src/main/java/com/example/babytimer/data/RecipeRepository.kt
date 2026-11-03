package com.example.babytimer.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String
)

interface RecipeRepository {
    val savedRecipes: Flow<List<Recipe>>
    suspend fun saveRecipe(title: String, content: String): Recipe
    suspend fun deleteRecipe(id: String)
}

class RecipeRepositoryImpl(private val dataStore: androidx.datastore.core.DataStore<Preferences>) : RecipeRepository {
    private val RECIPES_KEY = stringPreferencesKey("saved_recipes_json_key")

    override val savedRecipes: Flow<List<Recipe>> = dataStore.data.map { prefs ->
        val jsonStr = prefs[RECIPES_KEY] ?: "[]"
        val list = mutableListOf<Recipe>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Recipe(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        content = obj.optString("content", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    override suspend fun saveRecipe(title: String, content: String): Recipe {
        val newRecipe = Recipe(title = title, content = content)
        dataStore.edit { prefs ->
            val jsonStr = prefs[RECIPES_KEY] ?: "[]"
            val arr = JSONArray(jsonStr)
            val obj = JSONObject().apply {
                put("id", newRecipe.id)
                put("title", newRecipe.title)
                put("content", newRecipe.content)
            }
            arr.put(obj)
            prefs[RECIPES_KEY] = arr.toString()
        }
        return newRecipe
    }

    override suspend fun deleteRecipe(id: String) {
        dataStore.edit { prefs ->
            val jsonStr = prefs[RECIPES_KEY] ?: "[]"
            val arr = JSONArray(jsonStr)
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("id") != id) {
                    newArr.put(obj)
                }
            }
            prefs[RECIPES_KEY] = newArr.toString()
        }
    }
}
