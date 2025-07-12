package com.example.kelvinma.activitytracker.ui.timer

import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kelvinma.activitytracker.R
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivitySession
import com.example.kelvinma.activitytracker.data.ActivitySessionDao
import com.example.kelvinma.activitytracker.data.CompletionType
import com.example.kelvinma.activitytracker.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class TimerViewModel(
    private val activity: Activity,
    private val activitySessionDao: ActivitySessionDao,
    private val context: Context
) : ViewModel() {

    private val _timerValue = MutableStateFlow(0L)
    val timerValue: StateFlow<Long> = _timerValue

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _currentIntervalIndex = MutableStateFlow(0)
    val currentIntervalIndex: StateFlow<Int> = _currentIntervalIndex

    private val _isActivityComplete = MutableStateFlow(false)
    val isActivityComplete: StateFlow<Boolean> = _isActivityComplete

    private val _progressPercentage = MutableStateFlow(0f)
    val progressPercentage: StateFlow<Float> = _progressPercentage

    private var timer: CountDownTimer? = null
    private var hadPauses = false
    private var startTime = 0L
    private var currentSession: ActivitySession? = null

    init {
        Logger.i(Logger.TAG_TIMER, "Initializing timer for activity: ${activity.name}")
        startTimer()
    }

    private fun startTimer() {
        try {
            if (startTime == 0L) {
                startTime = System.currentTimeMillis()
                createInitialSession()
                Logger.logTimerEvent("Session started", "activity: ${activity.name}")
            }
            
            val intervalIndex = _currentIntervalIndex.value
            if (intervalIndex >= activity.intervals.size) {
                Logger.e(Logger.TAG_TIMER, "Invalid interval index: $intervalIndex, max: ${activity.intervals.size - 1}")
                return
            }
            
            val interval = activity.intervals[intervalIndex]
            Logger.logTimerEvent("Starting interval", "index: $intervalIndex, name: ${interval.name}")
            
            playSound(R.raw.interval_start)
            
            // Convert duration to milliseconds based on unit
            val durationMillis = convertToMilliseconds(interval.duration, interval.duration_unit)
            
            timer?.cancel() // Cancel any existing timer
            timer = object : CountDownTimer(durationMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timerValue.value = millisUntilFinished
                    if (millisUntilFinished / 1000 <= 3) {
                        playSound(R.raw.progress_beep)
                    }
                }

                override fun onFinish() {
                    Logger.logTimerEvent("Interval completed", "index: $intervalIndex")
                    playSound(R.raw.interval_end)
                    nextInterval()
                }
            }.start()
            
        } catch (e: Exception) {
            Logger.e(Logger.TAG_TIMER, "Error starting timer", e)
            // Attempt to continue with next interval if possible
            if (_currentIntervalIndex.value < activity.intervals.size - 1) {
                _currentIntervalIndex.value++
                startTimer()
            }
        }
    }
    
    /**
     * Converts duration to milliseconds based on the unit.
     */
    private fun convertToMilliseconds(duration: Int, unit: String): Long {
        return when (unit.lowercase()) {
            "seconds" -> duration * 1000L
            "minutes" -> duration * 60 * 1000L
            "hours" -> duration * 60 * 60 * 1000L
            else -> {
                Logger.w(Logger.TAG_TIMER, "Unknown duration unit: $unit, defaulting to seconds")
                duration * 1000L
            }
        }
    }

    private fun createInitialSession() {
        currentSession = ActivitySession(
            activity_name = activity.name,
            start_timestamp = startTime,
            end_timestamp = startTime, // Will be updated when session ends
            total_intervals_in_activity = activity.intervals.size,
            intervals_completed = 0,
            overall_progress_percentage = 0f,
            had_pauses = false
        )
    }

    fun pauseTimer() {
        try {
            timer?.cancel()
            _isPaused.value = true
            hadPauses = true
            Logger.logTimerEvent("Timer paused", "interval: ${_currentIntervalIndex.value}")
        } catch (e: Exception) {
            Logger.e(Logger.TAG_TIMER, "Error pausing timer", e)
        }
    }

    fun resumeTimer() {
        try {
            _isPaused.value = false
            val remainingTime = _timerValue.value
            Logger.logTimerEvent("Timer resumed", "remaining: ${remainingTime}ms")
            
            timer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timerValue.value = millisUntilFinished
                    if (millisUntilFinished / 1000 <= 3) {
                        playSound(R.raw.progress_beep)
                    }
                }

                override fun onFinish() {
                    Logger.logTimerEvent("Interval completed after resume", "index: ${_currentIntervalIndex.value}")
                    playSound(R.raw.interval_end)
                    nextInterval()
                }
            }.start()
        } catch (e: Exception) {
            Logger.e(Logger.TAG_TIMER, "Error resuming timer", e)
            // Try to restart from current interval
            startTimer()
        }
    }

    fun skipInterval() {
        timer?.cancel()
        nextInterval()
    }

    private fun nextInterval() {
        _currentIntervalIndex.value++
        updateSessionProgress()
        
        if (_currentIntervalIndex.value < activity.intervals.size) {
            startTimer()
        } else {
            // Activity finished
            _isActivityComplete.value = true
            playSound(R.raw.activity_complete)
            finishActivity()
        }
    }

    private fun updateSessionProgress() {
        currentSession?.let { session ->
            val intervalsCompleted = _currentIntervalIndex.value
            val progressPercentage = (intervalsCompleted.toFloat() / activity.intervals.size) * 100f
            
            _progressPercentage.value = progressPercentage
            
            currentSession = session.copy(
                intervals_completed = intervalsCompleted,
                overall_progress_percentage = progressPercentage,
                had_pauses = hadPauses,
                end_timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun finishActivity() {
        updateSessionProgress()
        // Mark as natural completion when all intervals are finished
        currentSession = currentSession?.copy(completion_type = CompletionType.NATURAL)
        saveCurrentSession()
    }

    private fun saveCurrentSession() {
        currentSession?.let { session ->
            viewModelScope.launch {
                try {
                    Logger.d(Logger.TAG_DATABASE, "Saving session for activity: ${session.activity_name}")
                    activitySessionDao.insert(session)
                    Logger.logDatabaseOperation("Insert session for ${session.activity_name}", true)
                } catch (e: Exception) {
                    Logger.logDatabaseOperation("Insert session for ${session.activity_name}", false, e)
                    Logger.e(Logger.TAG_TIMER, "Failed to save session to database", e)
                }
            }
        } ?: run {
            Logger.w(Logger.TAG_TIMER, "Attempted to save null session")
        }
    }

    fun stopActivity() {
        timer?.cancel()
        updateSessionProgress()
        // Mark as early completion when user manually finishes activity
        currentSession = currentSession?.copy(completion_type = CompletionType.EARLY)
        saveCurrentSession()
    }

    private fun handleViewModelCleared() {
        timer?.cancel()
        // Save current progress when ViewModel is cleared (e.g., user navigates away)
        if (!_isActivityComplete.value) {
            updateSessionProgress()
            saveCurrentSession()
        }
    }

    override fun onCleared() {
        super.onCleared()
        handleViewModelCleared()
    }

    private fun playSound(resId: Int) {
        try {
            Logger.logAudioEvent("Attempting to play sound", resId)
            val mediaPlayer = MediaPlayer.create(context, resId)
            
            if (mediaPlayer == null) {
                Logger.logAudioEvent("Failed to create MediaPlayer - resource not found", resId, 
                    IOException("MediaPlayer.create returned null for resource $resId"))
                return
            }
            
            mediaPlayer.setOnCompletionListener { mp ->
                try {
                    mp.release()
                    Logger.logAudioEvent("Sound playback completed and released", resId)
                } catch (e: Exception) {
                    Logger.logAudioEvent("Error releasing MediaPlayer", resId, e)
                }
            }
            
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Logger.logAudioEvent("MediaPlayer error: what=$what, extra=$extra", resId, 
                    RuntimeException("MediaPlayer error"))
                try {
                    mp.release()
                } catch (e: Exception) {
                    Logger.e(Logger.TAG_AUDIO, "Error releasing MediaPlayer after error", e)
                }
                true // Error handled
            }
            
            mediaPlayer.start()
            Logger.logAudioEvent("Sound playback started", resId)
            
        } catch (e: SecurityException) {
            Logger.logAudioEvent("Security exception playing sound", resId, e)
        } catch (e: IllegalStateException) {
            Logger.logAudioEvent("IllegalState exception playing sound", resId, e)
        } catch (e: Exception) {
            Logger.logAudioEvent("Unexpected error playing sound", resId, e)
        }
    }
}
