package com.example.kelvinma.activitytracker.data

import android.content.Context
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

class ActivityRepository(private val context: Context) {

    fun getActivities(): Activities {
        val yamlString = context.assets.open("activities.yaml").bufferedReader().use { it.readText() }
        return Yaml.default.decodeFromString(yamlString)
    }
}
