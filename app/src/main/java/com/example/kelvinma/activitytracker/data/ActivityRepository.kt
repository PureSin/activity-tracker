package com.example.kelvinma.activitytracker.data

import android.content.Context
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

class ActivityRepository(private val context: Context) {

    fun getActivities(): List<Activity> {
        val assetManager = context.assets
        val yamlFiles = assetManager.list("")?.filter { it.endsWith(".yaml") } ?: emptyList()
        return yamlFiles.map { fileName ->
            val yamlString = assetManager.open(fileName).bufferedReader().use { it.readText() }
            Yaml.default.decodeFromString<Activity>(yamlString)
        }
    }
}

