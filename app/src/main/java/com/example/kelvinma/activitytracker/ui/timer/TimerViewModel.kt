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

    private var timer: CountDownTimer? = null
    private var hadPauses = false
    private var startTime = 0L
    private var currentSession: ActivitySession? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
            createInitialSession()
        }
        
        playSound(R.raw.interval_start)
        val interval = activity.intervals[_currentIntervalIndex.value]
        val durationMillis = interval.duration * 1000L // Assuming seconds for now

        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timerValue.value = millisUntilFinished
                if (millisUntilFinished / 1000 <= 3) {
                    playSound(R.raw.progress_beep)
                }
            }

            override fun onFinish() {
                playSound(R.raw.interval_end)
                nextInterval()
            }
        }.start()
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
        timer?.cancel()
        _isPaused.value = true
        hadPauses = true
    }

    fun resumeTimer() {
        _isPaused.value = false
        val remainingTime = _timerValue.value
        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timerValue.value = millisUntilFinished
                if (millisUntilFinished / 1000 <= 3) {
                    playSound(R.raw.progress_beep)
                }
            }

            override fun onFinish() {
                playSound(R.raw.interval_end)
                nextInterval()
            }
        }.start()
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
                    activitySessionDao.insert(session)
                } catch (e: Exception) {
                    // Handle database error if needed
                }
            }
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
        val mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
        }
        mediaPlayer?.start()
    }
}
