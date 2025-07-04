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

    private var timer: CountDownTimer? = null
    private var hadPauses = false
    private var startTime = 0L

    init {
        startTimer()
    }

    private fun startTimer() {
        playSound(R.raw.interval_start)
        val interval = activity.intervals[_currentIntervalIndex.value]
        val durationMillis = interval.duration * 1000L // Assuming seconds for now
        startTime = System.currentTimeMillis()

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
        if (_currentIntervalIndex.value < activity.intervals.size - 1) {
            _currentIntervalIndex.value++
            startTimer()
        } else {
            // Activity finished
            playSound(R.raw.activity_complete)
            finishActivity()
        }
    }

    private fun finishActivity() {
        viewModelScope.launch {
            val session = ActivitySession(
                activity_name = activity.name,
                start_timestamp = startTime,
                end_timestamp = System.currentTimeMillis(),
                total_intervals_in_activity = activity.intervals.size,
                intervals_completed = _currentIntervalIndex.value + 1,
                overall_progress_percentage = 100f,
                had_pauses = hadPauses
            )
            activitySessionDao.insert(session)
        }
    }

    private fun playSound(resId: Int) {
        val mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
        }
        mediaPlayer?.start()
    }
}
