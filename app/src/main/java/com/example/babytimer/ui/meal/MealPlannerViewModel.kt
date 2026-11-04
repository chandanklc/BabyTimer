package com.example.babytimer.ui.meal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babytimer.data.DayMealPlan
import com.example.babytimer.data.MealRepositoryImpl
import com.example.babytimer.data.Recipe
import com.example.babytimer.data.RecipeRepositoryImpl
import com.example.babytimer.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealPlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val mealRepository = MealRepositoryImpl(application.dataStore)
    private val recipeRepository = RecipeRepositoryImpl(application.dataStore)

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

    fun updateMeal(dayName: String, slotType: String, newMeal: String) {
        viewModelScope.launch {
            mealRepository.updateMeal(dayName, slotType, newMeal)
        }
    }

    fun updateMealTime(slotType: String, timeStr: String) {
        viewModelScope.launch {
            mealRepository.updateMealTime(slotType, timeStr)
        }
    }

    fun saveRecipe(title: String, content: String) {
        viewModelScope.launch {
            recipeRepository.saveRecipe(title, content)
        }
    }
}
