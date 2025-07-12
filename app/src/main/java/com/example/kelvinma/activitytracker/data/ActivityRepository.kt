package com.example.kelvinma.activitytracker.data

import android.content.Context
import com.charleskorn.kaml.Yaml
import com.example.kelvinma.activitytracker.util.Logger
import com.example.kelvinma.activitytracker.util.Result
import com.example.kelvinma.activitytracker.util.safeCall
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import java.io.FileNotFoundException
import java.io.IOException

class ActivityRepository(private val context: Context) {

    /**
     * Loads all activities from YAML files in the assets directory.
     * Returns a list of successfully parsed activities, skipping any that fail to parse.
     */
    fun getActivities(): List<Activity> {
        Logger.d(Logger.TAG_REPOSITORY, "Loading activities from assets directory")
        
        return safeCall {
            val assetManager = context.assets
            assetManager.list("")?.filter { it.endsWith(".yaml") } ?: emptyList()
        }.onError { throwable ->
            Logger.e(Logger.TAG_REPOSITORY, "Failed to list asset files", throwable)
        }.getOrDefault(emptyList()).mapNotNull { fileName ->
            loadActivityFromFile(fileName).getOrNull()
        }.also { activities ->
            Logger.i(Logger.TAG_REPOSITORY, "Successfully loaded ${activities.size} activities")
        }
    }
    
    /**
     * Loads a single activity from a YAML file with comprehensive error handling.
     */
    private fun loadActivityFromFile(fileName: String): Result<Activity> {
        Logger.d(Logger.TAG_REPOSITORY, "Loading activity from file: $fileName")
        
        return safeCall {
            val assetManager = context.assets
            
            // Read YAML file content
            val yamlString = assetManager.open(fileName).bufferedReader().use { reader ->
                reader.readText()
            }
            
            if (yamlString.isBlank()) {
                throw IllegalArgumentException("YAML file is empty: $fileName")
            }
            
            // Parse YAML to Activity object
            val activity = Yaml.default.decodeFromString<Activity>(yamlString)
            
            // Validate parsed activity
            validateActivity(activity, fileName)
            
            Logger.logYamlParsing(fileName, true)
            activity
            
        }.onError { throwable ->
            when (throwable) {
                is FileNotFoundException -> {
                    Logger.e(Logger.TAG_YAML, "YAML file not found: $fileName", throwable)
                }
                is IOException -> {
                    Logger.e(Logger.TAG_YAML, "IO error reading YAML file: $fileName", throwable)
                }
                is SerializationException -> {
                    Logger.e(Logger.TAG_YAML, "Invalid YAML format in file: $fileName", throwable)
                }
                is IllegalArgumentException -> {
                    Logger.e(Logger.TAG_YAML, "Invalid activity data in file: $fileName", throwable)
                }
                else -> {
                    Logger.e(Logger.TAG_YAML, "Unexpected error parsing YAML file: $fileName", throwable)
                }
            }
            Logger.logYamlParsing(fileName, false, throwable)
        }
    }
    
    /**
     * Validates an activity object to ensure it has valid data.
     */
    private fun validateActivity(activity: Activity, fileName: String) {
        if (activity.name.isBlank()) {
            throw IllegalArgumentException("Activity name cannot be blank in file: $fileName")
        }
        
        if (activity.intervals.isEmpty()) {
            throw IllegalArgumentException("Activity must have at least one interval in file: $fileName")
        }
        
        activity.intervals.forEachIndexed { index, interval ->
            if (interval.name.isNullOrBlank()) {
                throw IllegalArgumentException("Interval ${index + 1} name cannot be blank in file: $fileName")
            }
            
            if (interval.duration <= 0) {
                throw IllegalArgumentException("Interval '${interval.name}' duration must be positive in file: $fileName")
            }
            
            // Validate duration unit
            val validUnits = setOf("seconds", "minutes", "hours")
            if (interval.duration_unit.lowercase() !in validUnits) {
                throw IllegalArgumentException("Invalid duration unit '${interval.duration_unit}' for interval '${interval.name}' in file: $fileName")
            }
            
            // Validate rest duration if present
            interval.rest_duration?.let { restDuration ->
                if (restDuration < 0) {
                    throw IllegalArgumentException("Rest duration cannot be negative for interval '${interval.name}' in file: $fileName")
                }
                
                interval.rest_duration_unit?.let { restUnit ->
                    if (restUnit.lowercase() !in validUnits) {
                        throw IllegalArgumentException("Invalid rest duration unit '$restUnit' for interval '${interval.name}' in file: $fileName")
                    }
                }
            }
        }
        
        Logger.d(Logger.TAG_REPOSITORY, "Activity validation passed for: ${activity.name} from file: $fileName")
    }
    
    /**
     * Gets a single activity by name.
     * Returns null if not found or if there's an error loading activities.
     */
    fun getActivityByName(name: String): Activity? {
        Logger.d(Logger.TAG_REPOSITORY, "Looking for activity: $name")
        
        return getActivities().find { it.name == name }.also { activity ->
            if (activity != null) {
                Logger.d(Logger.TAG_REPOSITORY, "Found activity: $name")
            } else {
                Logger.w(Logger.TAG_REPOSITORY, "Activity not found: $name")
            }
        }
    }
}

