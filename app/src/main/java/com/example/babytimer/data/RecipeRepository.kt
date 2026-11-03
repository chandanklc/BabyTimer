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

    private val defaultRecipes = listOf(
        Recipe(
            title = "Oats & Banana Mash",
            content = "6+ months\nIngredients: Rolled oats, breast milk/formula/water, ripe banana.\nInstructions: Cook oats until soft. Mash ripe banana and stir into warm oats for natural sweetness."
        ),
        Recipe(
            title = "Steamed Apple & Pear Puree",
            content = "6+ months\nIngredients: 1 apple, 1 pear.\nInstructions: Peel, core, and chop fruit. Steam until tender (8-10 mins). Blend until smooth."
        ),
        Recipe(
            title = "Moong Dal Khichdi",
            content = "8+ months\nIngredients: 2 tbsp yellow moong dal, 2 tbsp rice, pinch of turmeric, water.\nInstructions: Wash dal and rice. Pressure cook with extra water and turmeric for 4-5 whistles until very soft. Mash well."
        ),
        Recipe(
            title = "Mashed Sweet Potato & Carrot",
            content = "7+ months\nIngredients: 1 small sweet potato, 1 carrot.\nInstructions: Peel and chop. Steam or boil until fork-tender. Mash with a spoon or fork until smooth."
        ),
        Recipe(
            title = "Soft Idli & Veggie Mash",
            content = "9+ months\nIngredients: 2 soft idlis, warm vegetable broth or milk.\nInstructions: Crumble idlis into a bowl, pour warm broth over them, and mash into a soft, digestible porridge."
        ),
        Recipe(
            title = "Avocado & Yogurt Cream",
            content = "6+ months\nIngredients: 1/2 ripe avocado, 2 tbsp plain whole milk yogurt.\nInstructions: Scoop avocado and mash thoroughly with yogurt until creamy and smooth."
        ),
        Recipe(
            title = "Rice & Dal Soup",
            content = "8+ months\nIngredients: 2 tbsp well-cooked rice, thin yellow dal soup.\nInstructions: Mix soft cooked rice into warm, watery lentil soup until it reaches a comforting, easy-to-sip consistency."
        )
    )

    override val savedRecipes: Flow<List<Recipe>> = dataStore.data.map { prefs ->
        val jsonStr = prefs[RECIPES_KEY]
        val list = mutableListOf<Recipe>()
        try {
            if (jsonStr.isNullOrBlank()) {
                list.addAll(defaultRecipes)
            } else {
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.addAll(defaultRecipes)
        }
        list
    }

    override suspend fun saveRecipe(title: String, content: String): Recipe {
        val newRecipe = Recipe(title = title, content = content)
        dataStore.edit { prefs ->
            val jsonStr = prefs[RECIPES_KEY] ?: run {
                // If first save, serialize defaults + new recipe
                val initialArr = JSONArray()
                defaultRecipes.forEach { r ->
                    initialArr.put(JSONObject().apply {
                        put("id", r.id)
                        put("title", r.title)
                        put("content", r.content)
                    })
                }
                initialArr.toString()
            }
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
