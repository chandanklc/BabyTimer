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
    val mealTimes: Flow<Map<String, String>>
    suspend fun updateMeal(dayName: String, slotType: String, newMeal: String)
    suspend fun updateMealTime(slotType: String, timeStr: String)
}

class MealRepositoryImpl(private val dataStore: androidx.datastore.core.DataStore<Preferences>) : MealRepository {

    private val defaultPlans = listOf(
        Triple("Monday", listOf("Oats & Banana Mash", "Steamed Apple & Pear Puree", "Moong Dal Khichdi", "Mashed Sweet Potato & Carrot"), Color(0xFFD3F5E4)),
        Triple("Tuesday", listOf("Soft Idli & Veggie Mash", "Avocado & Yogurt Cream", "Rice & Dal Soup", "Oats & Banana Mash"), Color(0xFFE8E0FF)),
        Triple("Wednesday", listOf("Steamed Apple & Pear Puree", "Moong Dal Khichdi", "Mashed Sweet Potato & Carrot", "Soft Idli & Veggie Mash"), Color(0xFFFFE0CC)),
        Triple("Thursday", listOf("Avocado & Yogurt Cream", "Rice & Dal Soup", "Oats & Banana Mash", "Steamed Apple & Pear Puree"), Color(0xFFFFD6E0)),
        Triple("Friday", listOf("Moong Dal Khichdi", "Mashed Sweet Potato & Carrot", "Soft Idli & Veggie Mash", "Avocado & Yogurt Cream"), Color(0xFFD3F5E4)),
        Triple("Saturday", listOf("Rice & Dal Soup", "Oats & Banana Mash", "Steamed Apple & Pear Puree", "Moong Dal Khichdi"), Color(0xFFE8E0FF)),
        Triple("Sunday", listOf("Mashed Sweet Potato & Carrot", "Soft Idli & Veggie Mash", "Avocado & Yogurt Cream", "Rice & Dal Soup"), Color(0xFFFFE0CC))
    )

    private val defaultTimes = mapOf(
        "am" to "08:30 AM",
        "lunch" to "01:00 PM",
        "snack" to "03:30 PM",
        "dinner" to "07:30 PM"
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

    override val mealTimes: Flow<Map<String, String>> = dataStore.data.map { prefs ->
        mapOf(
            "am" to (prefs[stringPreferencesKey("time_am")] ?: defaultTimes["am"]!!),
            "lunch" to (prefs[stringPreferencesKey("time_lunch")] ?: defaultTimes["lunch"]!!),
            "snack" to (prefs[stringPreferencesKey("time_snack")] ?: defaultTimes["snack"]!!),
            "dinner" to (prefs[stringPreferencesKey("time_dinner")] ?: defaultTimes["dinner"]!!)
        )
    }

    override suspend fun updateMeal(dayName: String, slotType: String, newMeal: String) {
        val key = stringPreferencesKey("meal_${dayName}_${slotType}")
        dataStore.edit { prefs ->
            prefs[key] = newMeal
        }
    }

    override suspend fun updateMealTime(slotType: String, timeStr: String) {
        val key = stringPreferencesKey("time_${slotType}")
        dataStore.edit { prefs ->
            prefs[key] = timeStr
        }
    }
}
