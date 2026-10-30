package com.example.babytimer.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
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

    fun startNormalTimer() {
        if (_state.value.normalIsActive) return
        _state.update { it.copy(normalIsActive = true, normalSecondsRemaining = 3 * 3600) }

        scheduleWork("normal_diaper_work", 3, TimeUnit.HOURS, "Normal Diaper Change Time! (3 hours elapsed)")

        normalJob?.cancel()
        normalJob = viewModelScope.launch {
            while (_state.value.normalSecondsRemaining > 0 && _state.value.normalIsActive) {
                delay(1000L)
                _state.update {
                    val next = it.normalSecondsRemaining - 1
                    if (next <= 0) {
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
        WorkManager.getInstance(getApplication()).cancelUniqueWork("normal_diaper_work")
        _state.update { it.copy(normalIsActive = false, normalSecondsRemaining = 3 * 3600) }
    }

    fun startReusableTimer() {
        if (_state.value.reusableIsActive) return
        _state.update { it.copy(reusableIsActive = true, reusableSecondsRemaining = 1 * 3600) }

        scheduleWork("reusable_diaper_work", 1, TimeUnit.HOURS, "Reusable Diaper Change Time! (1 hour elapsed)")

        reusableJob?.cancel()
        reusableJob = viewModelScope.launch {
            while (_state.value.reusableSecondsRemaining > 0 && _state.value.reusableIsActive) {
                delay(1000L)
                _state.update {
                    val next = it.reusableSecondsRemaining - 1
                    if (next <= 0) {
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
        WorkManager.getInstance(getApplication()).cancelUniqueWork("reusable_diaper_work")
        _state.update { it.copy(reusableIsActive = false, reusableSecondsRemaining = 1 * 3600) }
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
