package com.example.babytimer.ui.home

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.babytimer.MainActivity
import com.example.babytimer.worker.DiaperReminderWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class DiaperTimerState(
    val normalSecondsRemaining: Long = 3 * 3600, // 3 hours
    val normalIsActive: Boolean = false,
    val reusableSecondsRemaining: Long = 1 * 3600, // 1 hour
    val reusableIsActive: Boolean = false
)

class DiaperViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DiaperTimerState())
    val state: StateFlow<DiaperTimerState> = _state.asStateFlow()

    private var normalJob: Job? = null
    private var reusableJob: Job? = null

    companion object {
        private const val NORMAL_NOTIFICATION_ID = 1001
        private const val REUSABLE_NOTIFICATION_ID = 1002
    }

    fun startNormalTimer() {
        if (_state.value.normalIsActive) return
        _state.update { it.copy(normalIsActive = true, normalSecondsRemaining = 3 * 3600) }

        showRunningNotification(
            "Normal Diaper Timer ⏱️",
            "Timer active: 3 hours remaining",
            NORMAL_NOTIFICATION_ID
        )

        scheduleWork("normal_diaper_work", 3, TimeUnit.HOURS, "Normal Diaper Change Time! (3 hours elapsed)")

        normalJob?.cancel()
        normalJob = viewModelScope.launch {
            while (_state.value.normalSecondsRemaining > 0 && _state.value.normalIsActive) {
                delay(1000L)
                _state.update {
                    val next = it.normalSecondsRemaining - 1
                    if (next <= 0) {
                        cancelNotification(NORMAL_NOTIFICATION_ID)
                        it.copy(normalSecondsRemaining = 0, normalIsActive = false)
                    } else {
                        it.copy(normalSecondsRemaining = next)
                    }
                }
            }
        }
    }

    fun resetNormalTimer() {
        normalJob?.cancel()
        cancelNotification(NORMAL_NOTIFICATION_ID)
        WorkManager.getInstance(getApplication()).cancelUniqueWork("normal_diaper_work")
        _state.update { it.copy(normalIsActive = false, normalSecondsRemaining = 3 * 3600) }
    }

    fun startReusableTimer() {
        if (_state.value.reusableIsActive) return
        _state.update { it.copy(reusableIsActive = true, reusableSecondsRemaining = 1 * 3600) }

        showRunningNotification(
            "Reusable Diaper Timer ⏱️",
            "Timer active: 1 hour remaining",
            REUSABLE_NOTIFICATION_ID
        )

        scheduleWork("reusable_diaper_work", 1, TimeUnit.HOURS, "Reusable Diaper Change Time! (1 hour elapsed)")

        reusableJob?.cancel()
        reusableJob = viewModelScope.launch {
            while (_state.value.reusableSecondsRemaining > 0 && _state.value.reusableIsActive) {
                delay(1000L)
                _state.update {
                    val next = it.reusableSecondsRemaining - 1
                    if (next <= 0) {
                        cancelNotification(REUSABLE_NOTIFICATION_ID)
                        it.copy(reusableSecondsRemaining = 0, reusableIsActive = false)
                    } else {
                        it.copy(reusableSecondsRemaining = next)
                    }
                }
            }
        }
    }

    fun resetReusableTimer() {
        reusableJob?.cancel()
        cancelNotification(REUSABLE_NOTIFICATION_ID)
        WorkManager.getInstance(getApplication()).cancelUniqueWork("reusable_diaper_work")
        _state.update { it.copy(reusableIsActive = false, reusableSecondsRemaining = 1 * 3600) }
    }

    private fun showRunningNotification(title: String, message: String, notificationId: Int) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "diaper_timer_running_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Active Diaper Timers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing timers for diaper changes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun cancelNotification(notificationId: Int) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    private fun scheduleWork(uniqueName: String, duration: Long, timeUnit: TimeUnit, message: String) {
        val data = workDataOf("message" to message)
        val workRequest = OneTimeWorkRequestBuilder<DiaperReminderWorker>()
            .setInitialDelay(duration, timeUnit)
            .setInputData(data)
            .setConstraints(Constraints.Builder().setRequiresDeviceIdle(false).build())
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
