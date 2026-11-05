package com.example.babytimer.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.babytimer.data.MealRepository
import com.example.babytimer.data.Recipe
import com.example.babytimer.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val mealRepository: MealRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

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

    fun saveRecipe(title: String, content: String) {
        viewModelScope.launch {
            recipeRepository.saveRecipe(title, content)
        }
    }

    fun deleteRecipe(id: String) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(id)
        }
    }
}
