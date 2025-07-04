package com.example.kelvinma.activitytracker.data

import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val name: String,
    val intervals: List<Interval>
)

@Serializable
data class Interval(
    val name: String? = null,
    val duration: Int,
    val duration_unit: String,
    val rest_duration: Int? = null,
    val rest_duration_unit: String? = null
)
