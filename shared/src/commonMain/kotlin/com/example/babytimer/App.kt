package com.example.babytimer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.babytimer.data.*
import com.example.babytimer.ui.home.*
import com.example.babytimer.ui.meal.*
import com.example.babytimer.ui.splash.*
import com.example.babytimer.ui.theme.BabyTimerTheme

@Composable
fun App() {
    BabyTimerTheme {
        val dataStore = remember { createDataStore() }
        val repository = remember { BabyPreferencesRepositoryImpl(dataStore) }
        val mealRepository = remember { MealRepositoryImpl(dataStore) }
        val recipeRepository = remember { RecipeRepositoryImpl(dataStore) }

        val splashViewModel = remember { SplashViewModel(repository) }
        val diaperViewModel = remember { DiaperViewModel(repository) }
        val mealPlannerViewModel = remember { MealPlannerViewModel(mealRepository, recipeRepository) }
        val recipeViewModel = remember { RecipeViewModel(mealRepository, recipeRepository) }

        var currentScreen by remember { mutableStateOf("splash") }
        var activeDay by remember { mutableStateOf("") }
        var activeSlot by remember { mutableStateOf("") }
        var activeMealName by remember { mutableStateOf("") }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    "splash" -> SplashScreen(
                        viewModel = splashViewModel,
                        onEnterHome = { currentScreen = "home" }
                    )
                    "home" -> HomeScreen(
                        viewModel = splashViewModel,
                        diaperViewModel = diaperViewModel,
                        onNavigateBackToSplash = { currentScreen = "splash" },
                        onNavigateToMealPlanner = { currentScreen = "meal_planner" }
                    )
                    "meal_planner" -> MealPlannerScreen(
                        viewModel = mealPlannerViewModel,
                        onBack = { currentScreen = "home" },
                        onSelectMealSlot = { day, slot, meal ->
                            activeDay = day
                            activeSlot = slot
                            activeMealName = meal
                            currentScreen = "recipe_editor"
                        }
                    )
                    "recipe_editor" -> RecipeEditorScreen(
                        dayName = activeDay,
                        slotType = activeSlot,
                        initialMealName = activeMealName,
                        viewModel = recipeViewModel,
                        onBack = { currentScreen = "meal_planner" }
                    )
                }
            }
        }
    }
}
