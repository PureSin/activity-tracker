package com.example.kelvinma.activitytracker.ui.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kelvinma.activitytracker.data.ActivitySessionDao

class AnalyticsViewModelFactory(
    private val activitySessionDao: ActivitySessionDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(activitySessionDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}