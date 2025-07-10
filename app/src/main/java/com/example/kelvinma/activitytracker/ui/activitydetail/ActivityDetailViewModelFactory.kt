package com.example.kelvinma.activitytracker.ui.activitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kelvinma.activitytracker.data.ActivitySessionDao

class ActivityDetailViewModelFactory(
    private val activitySessionDao: ActivitySessionDao,
    private val activityName: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityDetailViewModel::class.java)) {
            return ActivityDetailViewModel(activitySessionDao, activityName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}