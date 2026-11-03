package com.example.babytimer.ui.meal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babytimer.data.DayMealPlan
import com.example.babytimer.data.MealRepositoryImpl
import com.example.babytimer.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealPlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MealRepositoryImpl(application.dataStore)

    val weeklyPlan: StateFlow<List<DayMealPlan>> = repository.weeklyPlan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun updateMeal(dayName: String, slotType: String, newMeal: String) {
        viewModelScope.launch {
            repository.updateMeal(dayName, slotType, newMeal)
        }
    }
}
