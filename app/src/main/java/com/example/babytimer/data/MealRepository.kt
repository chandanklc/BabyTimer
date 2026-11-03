package com.example.babytimer.data

import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DayMealPlan(
    val dayName: String,
    val amMeal: String,
    val lunch: String,
    val dinner: String,
    val snack: String,
    val cardColor: Color
)

interface MealRepository {
    val weeklyPlan: Flow<List<DayMealPlan>>
    suspend fun updateMeal(dayName: String, slotType: String, newMeal: String)
}

class MealRepositoryImpl(private val dataStore: androidx.datastore.core.DataStore<Preferences>) : MealRepository {

    private val defaultPlans = listOf(
        Triple("Monday", listOf("Oats", "Veggie", "Rice", "Fruit"), Color(0xFFD3F5E4)),
        Triple("Tuesday", listOf("Idli", "Lentil", "Pasta", "Yogurt"), Color(0xFFE8E0FF)),
        Triple("Wednesday", listOf("Toast", "Khichdi", "Soup", "Banana"), Color(0xFFFFE0CC)),
        Triple("Thursday", listOf("Pancake", "Rice", "Dal", "Berries"), Color(0xFFFFD6E0)),
        Triple("Friday", listOf("Porridge", "Potato", "Fish", "Apple"), Color(0xFFD3F5E4)),
        Triple("Saturday", listOf("Puff", "Noodles", "Stew", "Cookie"), Color(0xFFE8E0FF)),
        Triple("Sunday", listOf("Waffle", "Paneer", "Khichdi", "Mango"), Color(0xFFFFE0CC))
    )

    override val weeklyPlan: Flow<List<DayMealPlan>> = dataStore.data.map { prefs ->
        defaultPlans.map { (day, defaults, color) ->
            val am = prefs[stringPreferencesKey("meal_${day}_am")] ?: defaults[0]
            val lunch = prefs[stringPreferencesKey("meal_${day}_lunch")] ?: defaults[1]
            val dinner = prefs[stringPreferencesKey("meal_${day}_dinner")] ?: defaults[2]
            val snack = prefs[stringPreferencesKey("meal_${day}_snack")] ?: defaults[3]
            DayMealPlan(day, am, lunch, dinner, snack, color)
        }
    }

    override suspend fun updateMeal(dayName: String, slotType: String, newMeal: String) {
        val key = stringPreferencesKey("meal_${dayName}_${slotType}")
        dataStore.edit { prefs ->
            prefs[key] = newMeal
        }
    }
}
