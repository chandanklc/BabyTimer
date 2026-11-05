package com.example.babytimer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.babytimer.data.BabyPreferencesRepository
import com.example.babytimer.data.BabyPreferencesRepositoryImpl
import com.example.babytimer.data.createDataStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiaperTimerState(
    val normalSelectedDurationSeconds: Long = 20, // Default 20s for testing
    val normalSecondsRemaining: Long = 20,
    val normalIsActive: Boolean = false,
    val reusableSelectedDurationSeconds: Long = 3600, // Default 1 hour
    val reusableSecondsRemaining: Long = 3600,
    val reusableIsActive: Boolean = false
)

class DiaperViewModel(
    private val repository: BabyPreferencesRepository = BabyPreferencesRepositoryImpl(createDataStore()),
    private val onStartWork: (String, Long, String) -> Unit = { _, _, _ -> },
    private val onCancelWork: (String) -> Unit = {},
    private val onShowNotification: (String, String, Int) -> Unit = { _, _, _ -> },
    private val onCancelNotification: (Int) -> Unit = {}
) : ViewModel() {

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
            val now = com.example.babytimer.data.currentTimeMillis()

            // Restore normal timer
            val normalEnd = repository.normalTimerEndTime.first()
            if (normalEnd > now) {
                val remaining = (normalEnd - now) / 1000
                if (remaining > 0) {
                    _state.update { it.copy(normalIsActive = true, normalSecondsRemaining = remaining) }
                    startNormalTicker(remaining)
                    onShowNotification("Normal Diaper Timer ⏱️", "Timer active: ${formatTime(remaining)} remaining", NORMAL_NOTIFICATION_ID)
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
                    onShowNotification("Reusable Diaper Timer ⏱️", "Timer active: ${formatTime(remaining)} remaining", REUSABLE_NOTIFICATION_ID)
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
        val endTime = com.example.babytimer.data.currentTimeMillis() + (durationSeconds * 1000L)

        viewModelScope.launch {
            repository.updateNormalTimerEndTime(endTime)
        }

        _state.update { it.copy(normalIsActive = true, normalSecondsRemaining = durationSeconds) }

        onShowNotification(
            "Normal Diaper Timer ⏱️",
            "Timer active: ${formatTime(durationSeconds)} remaining",
            NORMAL_NOTIFICATION_ID
        )

        onStartWork("normal_diaper_work", durationSeconds, "Normal Diaper Change Time!")

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
                        onCancelNotification(NORMAL_NOTIFICATION_ID)
                        viewModelScope.launch { repository.updateNormalTimerEndTime(0L) }
                        it.copy(normalSecondsRemaining = 0, normalIsActive = false)
                    } else {
                        onShowNotification(
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
        onCancelNotification(NORMAL_NOTIFICATION_ID)
        onCancelWork("normal_diaper_work")
        viewModelScope.launch {
            repository.updateNormalTimerEndTime(0L)
        }
        _state.update { it.copy(normalIsActive = false, normalSecondsRemaining = it.normalSelectedDurationSeconds) }
    }

    fun startReusableTimer() {
        if (_state.value.reusableIsActive) return
        val durationSeconds = _state.value.reusableSelectedDurationSeconds
        val endTime = com.example.babytimer.data.currentTimeMillis() + (durationSeconds * 1000L)

        viewModelScope.launch {
            repository.updateReusableTimerEndTime(endTime)
        }

        _state.update { it.copy(reusableIsActive = true, reusableSecondsRemaining = durationSeconds) }

        onShowNotification(
            "Reusable Diaper Timer ⏱️",
            "Timer active: ${formatTime(durationSeconds)} remaining",
            REUSABLE_NOTIFICATION_ID
        )

        onStartWork("reusable_diaper_work", durationSeconds, "Reusable Diaper Change Time!")

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
                        onCancelNotification(REUSABLE_NOTIFICATION_ID)
                        viewModelScope.launch { repository.updateReusableTimerEndTime(0L) }
                        it.copy(reusableSecondsRemaining = 0, reusableIsActive = false)
                    } else {
                        onShowNotification(
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
        onCancelNotification(REUSABLE_NOTIFICATION_ID)
        onCancelWork("reusable_diaper_work")
        viewModelScope.launch {
            repository.updateReusableTimerEndTime(0L)
        }
        _state.update { it.copy(reusableIsActive = false, reusableSecondsRemaining = it.reusableSelectedDurationSeconds) }
    }

    private fun formatTime(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        val hStr = if (hrs < 10) "0$hrs" else "$hrs"
        val mStr = if (mins < 10) "0$mins" else "$mins"
        val sStr = if (secs < 10) "0$secs" else "$secs"
        return if (hrs > 0) {
            "$hStr:$mStr:$sStr"
        } else if (mins > 0) {
            "$mStr:$sStr"
        } else {
            "00:$sStr"
        }
    }
}
