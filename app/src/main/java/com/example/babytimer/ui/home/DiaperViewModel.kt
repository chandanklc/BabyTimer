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
import com.example.babytimer.data.BabyPreferencesRepositoryImpl
import com.example.babytimer.data.dataStore
import com.example.babytimer.worker.DiaperReminderWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class DiaperTimerState(
    val normalSelectedDurationSeconds: Long = 20, // Default 20s for testing
    val normalSecondsRemaining: Long = 20,
    val normalIsActive: Boolean = false,
    val reusableSelectedDurationSeconds: Long = 3600, // Default 1 hour
    val reusableSecondsRemaining: Long = 3600,
    val reusableIsActive: Boolean = false
)

class DiaperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BabyPreferencesRepositoryImpl(application.dataStore)

    private val _state = MutableStateFlow(DiaperTimerState())
    val state: StateFlow<DiaperTimerState> = _state.asStateFlow()

    private var normalJob: Job? = null
    private var reusableJob: Job? = null

    companion object {
        private const val NORMAL_NOTIFICATION_ID = 1001
        private const val REUSABLE_NOTIFICATION_ID = 1002
    }

    init {
        restoreTimers()
    }

    private fun restoreTimers() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            // Restore normal timer
            val normalEnd = repository.normalTimerEndTime.first()
            if (normalEnd > now) {
                val remaining = (normalEnd - now) / 1000
                if (remaining > 0) {
                    _state.update { it.copy(normalIsActive = true, normalSecondsRemaining = remaining) }
                    startNormalTicker(remaining)
                    showRunningNotification("Normal Diaper Timer ⏱️", "Timer active: ${formatTime(remaining)} remaining", NORMAL_NOTIFICATION_ID)
                }
            } else {
                repository.updateNormalTimerEndTime(0L)
            }

            // Restore reusable timer
            val reusableEnd = repository.reusableTimerEndTime.first()
            if (reusableEnd > now) {
                val remaining = (reusableEnd - now) / 1000
                if (remaining > 0) {
                    _state.update { it.copy(reusableIsActive = true, reusableSecondsRemaining = remaining) }
                    startReusableTicker(remaining)
                    showRunningNotification("Reusable Diaper Timer ⏱️", "Timer active: ${formatTime(remaining)} remaining", REUSABLE_NOTIFICATION_ID)
                }
            } else {
                repository.updateReusableTimerEndTime(0L)
            }
        }
    }

    fun setNormalDuration(seconds: Long) {
        if (_state.value.normalIsActive) return
        _state.update { it.copy(normalSelectedDurationSeconds = seconds, normalSecondsRemaining = seconds) }
    }

    fun setReusableDuration(seconds: Long) {
        if (_state.value.reusableIsActive) return
        _state.update { it.copy(reusableSelectedDurationSeconds = seconds, reusableSecondsRemaining = seconds) }
    }

    fun startNormalTimer() {
        if (_state.value.normalIsActive) return
        val durationSeconds = _state.value.normalSelectedDurationSeconds
        val endTime = System.currentTimeMillis() + (durationSeconds * 1000L)

        viewModelScope.launch {
            repository.updateNormalTimerEndTime(endTime)
        }

        _state.update { it.copy(normalIsActive = true, normalSecondsRemaining = durationSeconds) }

        showRunningNotification(
            "Normal Diaper Timer ⏱️",
            "Timer active: ${formatTime(durationSeconds)} remaining",
            NORMAL_NOTIFICATION_ID
        )

        scheduleWork("normal_diaper_work", durationSeconds, "Normal Diaper Change Time!")

        startNormalTicker(durationSeconds)
    }

    private fun startNormalTicker(initialSeconds: Long) {
        normalJob?.cancel()
        normalJob = viewModelScope.launch {
            var current = initialSeconds
            while (current > 0 && _state.value.normalIsActive) {
                delay(1000L)
                current--
                _state.update {
                    if (current <= 0) {
                        cancelNotification(NORMAL_NOTIFICATION_ID)
                        viewModelScope.launch { repository.updateNormalTimerEndTime(0L) }
                        it.copy(normalSecondsRemaining = 0, normalIsActive = false)
                    } else {
                        showRunningNotification(
                            "Normal Diaper Timer ⏱️",
                            "Timer active: ${formatTime(current)} remaining",
                            NORMAL_NOTIFICATION_ID
                        )
                        it.copy(normalSecondsRemaining = current)
                    }
                }
            }
        }
    }

    fun resetNormalTimer() {
        normalJob?.cancel()
        cancelNotification(NORMAL_NOTIFICATION_ID)
        WorkManager.getInstance(getApplication()).cancelUniqueWork("normal_diaper_work")
        viewModelScope.launch {
            repository.updateNormalTimerEndTime(0L)
        }
        _state.update { it.copy(normalIsActive = false, normalSecondsRemaining = it.normalSelectedDurationSeconds) }
    }

    fun startReusableTimer() {
        if (_state.value.reusableIsActive) return
        val durationSeconds = _state.value.reusableSelectedDurationSeconds
        val endTime = System.currentTimeMillis() + (durationSeconds * 1000L)

        viewModelScope.launch {
            repository.updateReusableTimerEndTime(endTime)
        }

        _state.update { it.copy(reusableIsActive = true, reusableSecondsRemaining = durationSeconds) }

        showRunningNotification(
            "Reusable Diaper Timer ⏱️",
            "Timer active: ${formatTime(durationSeconds)} remaining",
            REUSABLE_NOTIFICATION_ID
        )

        scheduleWork("reusable_diaper_work", durationSeconds, "Reusable Diaper Change Time!")

        startReusableTicker(durationSeconds)
    }

    private fun startReusableTicker(initialSeconds: Long) {
        reusableJob?.cancel()
        reusableJob = viewModelScope.launch {
            var current = initialSeconds
            while (current > 0 && _state.value.reusableIsActive) {
                delay(1000L)
                current--
                _state.update {
                    if (current <= 0) {
                        cancelNotification(REUSABLE_NOTIFICATION_ID)
                        viewModelScope.launch { repository.updateReusableTimerEndTime(0L) }
                        it.copy(reusableSecondsRemaining = 0, reusableIsActive = false)
                    } else {
                        showRunningNotification(
                            "Reusable Diaper Timer ⏱️",
                            "Timer active: ${formatTime(current)} remaining",
                            REUSABLE_NOTIFICATION_ID
                        )
                        it.copy(reusableSecondsRemaining = current)
                    }
                }
            }
        }
    }

    fun resetReusableTimer() {
        reusableJob?.cancel()
        cancelNotification(REUSABLE_NOTIFICATION_ID)
        WorkManager.getInstance(getApplication()).cancelUniqueWork("reusable_diaper_work")
        viewModelScope.launch {
            repository.updateReusableTimerEndTime(0L)
        }
        _state.update { it.copy(reusableIsActive = false, reusableSecondsRemaining = it.reusableSelectedDurationSeconds) }
    }

    private fun formatTime(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%d:%02d:%02d", hrs, mins, secs)
        } else if (mins > 0) {
            String.format("%02d:%02d", mins, secs)
        } else {
            String.format("00:%02d", secs)
        }
    }

    private fun showRunningNotification(title: String, message: String, notificationId: Int) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun cancelNotification(notificationId: Int) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    private fun scheduleWork(uniqueName: String, durationSeconds: Long, message: String) {
        val data = workDataOf("message" to message)
        val workRequest = OneTimeWorkRequestBuilder<DiaperReminderWorker>()
            .setInitialDelay(durationSeconds, TimeUnit.SECONDS)
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
