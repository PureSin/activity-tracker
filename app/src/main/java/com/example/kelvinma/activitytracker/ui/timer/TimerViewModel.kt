package com.example.kelvinma.activitytracker.ui.timer

import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivitySession
import com.example.kelvinma.activitytracker.data.ActivitySessionDao
import com.example.kelvinma.activitytracker.data.CompletionType
import com.example.kelvinma.activitytracker.data.Interval
import com.example.kelvinma.activitytracker.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private val _isRestPeriod = MutableStateFlow(false)
    val isRestPeriod: StateFlow<Boolean> = _isRestPeriod

    private var timer: CountDownTimer? = null
    private var hadPauses = false
    private var startTime = 0L
    private var currentSession: ActivitySession? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingSpeechText: String? = null
    private var vibrator: Vibrator? = null

    init {
        Logger.i(Logger.TAG_TIMER, "Initializing timer for activity: ${activity.name}")
        initializeVibrator()
        initializeTextToSpeech()
        // startTimer() will be called after TTS initialization
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
            val isRest = _isRestPeriod.value
            
            if (isRest) {
                startRestPeriod(interval, intervalIndex)
            } else {
                startActivityInterval(interval, intervalIndex)
            }
            
        } catch (e: Exception) {
            Logger.e(Logger.TAG_TIMER, "Error starting timer", e)
            // Attempt to continue with next interval if possible
            if (_currentIntervalIndex.value < activity.intervals.size - 1) {
                _currentIntervalIndex.value++
                startTimer()
            }
        }
    }
    
    private fun startActivityInterval(interval: Interval, intervalIndex: Int) {
        Logger.logTimerEvent("Starting interval", "index: $intervalIndex, name: ${interval.name}")
        
        // Provide haptic feedback when interval starts
        performHapticFeedback()
        
        interval.name?.let { name ->
            speakIntervalName(name)
        }
        
        // Convert duration to milliseconds based on unit
        val durationMillis = convertToMilliseconds(interval.duration, interval.duration_unit)
        
        timer?.cancel() // Cancel any existing timer
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timerValue.value = millisUntilFinished
            }

            override fun onFinish() {
                Logger.logTimerEvent("Interval completed", "index: $intervalIndex")
                // Provide haptic feedback when interval completes
                performHapticFeedback()
                finishInterval()
            }
        }.start()
    }

    private fun startRestPeriod(interval: Interval, intervalIndex: Int) {
        Logger.logTimerEvent("Starting rest period", "index: $intervalIndex, duration: ${interval.rest_duration} ${interval.rest_duration_unit}")
        
        // Convert rest duration to milliseconds
        val restDurationMillis = convertToMilliseconds(interval.rest_duration ?: 0, interval.rest_duration_unit ?: "seconds")
        
        if (restDurationMillis > 0) {
            timer?.cancel()
            timer = object : CountDownTimer(restDurationMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timerValue.value = millisUntilFinished
                }

                override fun onFinish() {
                    Logger.logTimerEvent("Rest period completed", "index: $intervalIndex")
                    // Provide haptic feedback when rest period completes
                    performHapticFeedback()
                    finishRestPeriod()
                }
            }.start()
        } else {
            // No rest duration, move directly to next interval
            finishRestPeriod()
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
            val isRest = _isRestPeriod.value
            Logger.logTimerEvent("Timer resumed", "remaining: ${remainingTime}ms, isRest: $isRest")
            
            timer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timerValue.value = millisUntilFinished
                }

                override fun onFinish() {
                    Logger.logTimerEvent("Timer completed after resume", "index: ${_currentIntervalIndex.value}, isRest: $isRest")
                    // Provide haptic feedback when timer completes after resume
                    performHapticFeedback()
                    if (isRest) {
                        finishRestPeriod()
                    } else {
                        finishInterval()
                    }
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
        if (_isRestPeriod.value) {
            finishRestPeriod()
        } else {
            finishInterval()
        }
    }

    private fun finishInterval() {
        val currentIndex = _currentIntervalIndex.value
        if (currentIndex >= activity.intervals.size) {
            // Already completed all intervals
            return
        }
        
        val currentInterval = activity.intervals[currentIndex]
        val hasRestDuration = (currentInterval.rest_duration ?: 0) > 0
        
        if (hasRestDuration && currentIndex < activity.intervals.size - 1) {
            // Start rest period (but don't advance interval index yet)
            _isRestPeriod.value = true
            startTimer()
        } else {
            // No rest or this is the last interval, move to next interval
            nextInterval()
        }
    }
    
    private fun finishRestPeriod() {
        _isRestPeriod.value = false
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
        cleanupTextToSpeech()
    }

    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        Logger.i(Logger.TAG_AUDIO, "Vibrator initialized")
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Logger.w(Logger.TAG_AUDIO, "Language not supported for TTS, falling back to English")
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                }
                isTtsInitialized = true
                Logger.i(Logger.TAG_AUDIO, "TextToSpeech initialized successfully")
                
                // Now that TTS is ready, start the timer
                startTimer()
                
                // Speak any pending text
                pendingSpeechText?.let { text ->
                    speakIntervalName(text)
                    pendingSpeechText = null
                }
            } else {
                Logger.e(Logger.TAG_AUDIO, "TextToSpeech initialization failed")
                isTtsInitialized = false
                // Still start the timer even if TTS fails
                startTimer()
            }
        }
    }

    private fun speakIntervalName(intervalName: String) {
        if (isTtsInitialized && textToSpeech != null) {
            try {
                textToSpeech?.speak(intervalName, TextToSpeech.QUEUE_FLUSH, null, "interval_name")
                Logger.i(Logger.TAG_AUDIO, "Speaking interval name: $intervalName")
            } catch (e: Exception) {
                Logger.e(Logger.TAG_AUDIO, "Error speaking interval name: $intervalName", e)
            }
        } else {
            // Queue the speech for when TTS is ready
            pendingSpeechText = intervalName
            Logger.i(Logger.TAG_AUDIO, "TextToSpeech not ready yet, queuing speech for: $intervalName")
        }
    }

    private fun performHapticFeedback() {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Use VibrationEffect for API 26+
                    val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    } else {
                        VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    vib.vibrate(effect)
                } else {
                    // Fallback for older devices
                    @Suppress("DEPRECATION")
                    vib.vibrate(100)
                }
                Logger.i(Logger.TAG_AUDIO, "Haptic feedback performed")
            }
        } catch (e: Exception) {
            Logger.e(Logger.TAG_AUDIO, "Error performing haptic feedback", e)
        }
    }

    private fun cleanupTextToSpeech() {
        textToSpeech?.let {
            if (it.isSpeaking) {
                it.stop()
            }
            it.shutdown()
            Logger.i(Logger.TAG_AUDIO, "TextToSpeech cleaned up")
        }
        textToSpeech = null
        isTtsInitialized = false
    }

}
