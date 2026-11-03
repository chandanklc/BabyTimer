package com.example.babytimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.babytimer.data.BabyPreferencesRepositoryImpl
import com.example.babytimer.data.dataStore
import com.example.babytimer.ui.home.HomeScreen
import com.example.babytimer.ui.meal.MealPlannerScreen
import com.example.babytimer.ui.meal.RecipeEditorScreen
import com.example.babytimer.ui.splash.SplashScreen
import com.example.babytimer.ui.splash.SplashViewModel
import com.example.babytimer.ui.theme.BabyTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = BabyPreferencesRepositoryImpl(applicationContext.dataStore)
        val splashViewModel = SplashViewModel(repository)

        setContent {
            BabyTimerTheme {
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
                                onNavigateBackToSplash = { currentScreen = "splash" },
                                onNavigateToMealPlanner = { currentScreen = "meal_planner" }
                            )
                            "meal_planner" -> MealPlannerScreen(
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
                                onBack = { currentScreen = "meal_planner" }
                            )
                        }
                    }
                }
            }
        }
    }
}
