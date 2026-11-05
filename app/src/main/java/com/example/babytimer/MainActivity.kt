package com.example.babytimer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.babytimer.data.BabyPreferencesRepositoryImpl
import com.example.babytimer.data.MealRepositoryImpl
import com.example.babytimer.data.RecipeRepositoryImpl
import com.example.babytimer.data.appContext
import com.example.babytimer.data.createDataStore
import com.example.babytimer.ui.home.DiaperViewModel
import com.example.babytimer.ui.home.HomeScreen
import com.example.babytimer.ui.meal.MealPlannerScreen
import com.example.babytimer.ui.meal.MealPlannerViewModel
import com.example.babytimer.ui.meal.RecipeEditorScreen
import com.example.babytimer.ui.meal.RecipeViewModel
import com.example.babytimer.ui.splash.SplashScreen
import com.example.babytimer.ui.splash.SplashViewModel
import com.example.babytimer.ui.theme.BabyTimerTheme
import com.example.babytimer.worker.DiaperReminderWorker
import com.example.babytimer.worker.MealReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private var hasNotificationPermission = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContext = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasNotificationPermission = true
        }

        val dataStore = createDataStore()
        val babyPreferencesRepository = BabyPreferencesRepositoryImpl(dataStore)
        val mealRepository = MealRepositoryImpl(dataStore)
        val recipeRepository = RecipeRepositoryImpl(dataStore)

        val splashViewModel = SplashViewModel(babyPreferencesRepository)

        val diaperViewModel = DiaperViewModel(
            repository = babyPreferencesRepository,
            onStartWork = { uniqueName, durationSeconds, message ->
                scheduleDiaperWork(uniqueName, durationSeconds, message)
            },
            onCancelWork = { uniqueName ->
                WorkManager.getInstance(applicationContext).cancelUniqueWork(uniqueName)
            },
            onShowNotification = { title, message, id ->
                showRunningNotification(title, message, id)
            },
            onCancelNotification = { id ->
                cancelNotification(id)
            }
        )

        val mealPlannerViewModel = MealPlannerViewModel(
            mealRepository = mealRepository,
            recipeRepository = recipeRepository,
            onScheduleMealWork = { slotType, timeStr, mealName ->
                scheduleMealWork(slotType, timeStr, mealName)
            }
        )

        val recipeViewModel = RecipeViewModel(
            mealRepository = mealRepository,
            recipeRepository = recipeRepository
        )

        val imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                splashViewModel.updatePhotoUri(it.toString())
            }
        }

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
                                onEnterHome = { currentScreen = "home" },
                                onPickImage = { imagePickerLauncher.launch("image/*") }
                            )
                            "home" -> HomeScreen(
                                viewModel = splashViewModel,
                                diaperViewModel = diaperViewModel,
                                onNavigateBackToSplash = { currentScreen = "splash" },
                                onNavigateToMealPlanner = { currentScreen = "meal_planner" },
                                onRequestNotificationPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            )
                            "meal_planner" -> MealPlannerScreen(
                                viewModel = mealPlannerViewModel,
                                onBack = { currentScreen = "home" },
                                onSelectMealSlot = { day, slot, meal ->
                                    activeDay = day
                                    activeSlot = slot
                                    activeMealName = meal
                                    currentScreen = "recipe_editor"
                                },
                                onOpenTimePicker = { slotType, currentTimeStr ->
                                    openTimePicker(slotType, currentTimeStr, mealPlannerViewModel)
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
    }

    private fun openTimePicker(slotType: String, currentTimeStr: String, viewModel: MealPlannerViewModel) {
        val parts = currentTimeStr.split(":", " ")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 30
        val isPm = currentTimeStr.contains("PM", ignoreCase = true)
        if (isPm && hour < 12) hour += 12
        if (!isPm && hour == 12) hour = 0

        android.app.TimePickerDialog(
            this,
            { _, h, m ->
                val formattedTime = String.format(
                    "%02d:%02d %s",
                    if (h % 12 == 0) 12 else h % 12,
                    m,
                    if (h >= 12) "PM" else "AM"
                )
                viewModel.updateMealTime(slotType, formattedTime)
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun scheduleDiaperWork(uniqueName: String, durationSeconds: Long, message: String) {
        val data = workDataOf("message" to message)
        val workRequest = OneTimeWorkRequestBuilder<DiaperReminderWorker>()
            .setInitialDelay(durationSeconds, TimeUnit.SECONDS)
            .setInputData(data)
            .setConstraints(Constraints.Builder().setRequiresDeviceIdle(false).build())
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun scheduleMealWork(slotType: String, timeStr: String, mealName: String) {
        val delayMillis = calculateInitialDelay(timeStr)
        val title = when (slotType) {
            "am" -> "AM Meal"
            "lunch" -> "Lunch"
            "snack" -> "Snack"
            "dinner" -> "Dinner"
            else -> "Meal Time"
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

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
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

    private fun showRunningNotification(title: String, message: String, notificationId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "diaper_timer_running_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Active Diaper Timers",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows ongoing timers for diaper changes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            s.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun cancelNotification(notificationId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
