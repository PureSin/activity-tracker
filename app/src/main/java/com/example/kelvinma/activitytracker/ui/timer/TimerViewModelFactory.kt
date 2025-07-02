package com.example.kelvinma.activitytracker.ui.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivitySessionDao

class TimerViewModelFactory(
    private val activity: Activity,
    private val activitySessionDao: ActivitySessionDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(activity, activitySessionDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
