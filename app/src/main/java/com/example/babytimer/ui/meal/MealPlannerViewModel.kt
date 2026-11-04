package com.example.babytimer.ui.meal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.babytimer.data.DayMealPlan
import com.example.babytimer.data.MealRepositoryImpl
import com.example.babytimer.data.Recipe
import com.example.babytimer.data.RecipeRepositoryImpl
import com.example.babytimer.data.dataStore
import com.example.babytimer.worker.MealReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

    init {
        // Schedule work on init based on saved times
        viewModelScope.launch {
            mealTimes.collect { times ->
                times.forEach { (slot, timeStr) ->
                    scheduleMealWork(slot, timeStr)
                }
            }
        }
    }

    fun updateMeal(dayName: String, slotType: String, newMeal: String) {
        viewModelScope.launch {
            mealRepository.updateMeal(dayName, slotType, newMeal)
            // Reschedule work with updated meal name
            mealTimes.value[slotType]?.let { timeStr ->
                scheduleMealWork(slotType, timeStr)
            }
        }
    }

    fun updateMealTime(slotType: String, timeStr: String) {
        viewModelScope.launch {
            mealRepository.updateMealTime(slotType, timeStr)
            scheduleMealWork(slotType, timeStr)
        }
    }

    fun saveRecipe(title: String, content: String) {
        viewModelScope.launch {
            recipeRepository.saveRecipe(title, content)
        }
    }

    private fun scheduleMealWork(slotType: String, timeStr: String) {
        val delayMillis = calculateInitialDelay(timeStr)
        val title = when (slotType) {
            "am" -> "AM Meal"
            "lunch" -> "Lunch"
            "snack" -> "Snack"
            "dinner" -> "Dinner"
            else -> "Meal Time"
        }

        // Get current day's meal name
        val currentPlan = weeklyPlan.value.firstOrNull()
        val mealName = when (slotType) {
            "am" -> currentPlan?.amMeal ?: "AM Meal"
            "lunch" -> currentPlan?.lunch ?: "Lunch"
            "snack" -> currentPlan?.snack ?: "Snack"
            "dinner" -> currentPlan?.dinner ?: "Dinner"
            else -> "Meal"
        }

        val data = workDataOf(
            "meal_title" to title,
            "meal_name" to mealName
        )

        val workRequest = OneTimeWorkRequestBuilder<MealReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .setConstraints(Constraints.Builder().setRequiresDeviceIdle(false).build())
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniqueWork(
            "meal_work_$slotType",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun calculateInitialDelay(timeStr: String): Long {
        try {
            val parts = timeStr.split(":", " ")
            var hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val amPm = parts.getOrNull(2) ?: "AM"
            if (amPm.equals("PM", ignoreCase = true) && hour < 12) hour += 12
            if (amPm.equals("AM", ignoreCase = true) && hour == 12) hour = 0

            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
            }
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        } catch (e: Exception) {
            return TimeUnit.MINUTES.toMillis(1)
        }
    }
}
