package com.example.babytimer.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.babytimer.data.DayMealPlan
import com.example.babytimer.data.MealRepository
import com.example.babytimer.data.Recipe
import com.example.babytimer.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealPlannerViewModel(
    private val mealRepository: MealRepository,
    private val recipeRepository: RecipeRepository,
    private val onScheduleMealWork: (String, String, String) -> Unit = { _, _, _ -> }
) : ViewModel() {

    val weeklyPlan: StateFlow<List<DayMealPlan>> = mealRepository.weeklyPlan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val mealTimes: StateFlow<Map<String, String>> = mealRepository.mealTimes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = mapOf("am" to "08:30 AM", "lunch" to "01:00 PM", "snack" to "03:30 PM", "dinner" to "07:30 PM")
        )

    val savedRecipes: StateFlow<List<Recipe>> = recipeRepository.savedRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            mealTimes.collect { times ->
                times.forEach { (slot, timeStr) ->
                    val currentPlan = weeklyPlan.value.firstOrNull()
                    val mealName = when (slot) {
                        "am" -> currentPlan?.amMeal ?: "AM Meal"
                        "lunch" -> currentPlan?.lunch ?: "Lunch"
                        "snack" -> currentPlan?.snack ?: "Snack"
                        "dinner" -> currentPlan?.dinner ?: "Dinner"
                        else -> "Meal"
                    }
                    onScheduleMealWork(slot, timeStr, mealName)
                }
            }
        }
    }

    fun updateMeal(dayName: String, slotType: String, newMeal: String) {
        viewModelScope.launch {
            mealRepository.updateMeal(dayName, slotType, newMeal)
            mealTimes.value[slotType]?.let { timeStr ->
                onScheduleMealWork(slotType, timeStr, newMeal)
            }
        }
    }

    fun updateMealTime(slotType: String, timeStr: String) {
        viewModelScope.launch {
            mealRepository.updateMealTime(slotType, timeStr)
            val currentPlan = weeklyPlan.value.firstOrNull()
            val mealName = when (slotType) {
                "am" -> currentPlan?.amMeal ?: "AM Meal"
                "lunch" -> currentPlan?.lunch ?: "Lunch"
                "snack" -> currentPlan?.snack ?: "Snack"
                "dinner" -> currentPlan?.dinner ?: "Dinner"
                else -> "Meal"
            }
            onScheduleMealWork(slotType, timeStr, mealName)
        }
    }

    fun saveRecipe(title: String, content: String) {
        viewModelScope.launch {
            recipeRepository.saveRecipe(title, content)
        }
    }
}
