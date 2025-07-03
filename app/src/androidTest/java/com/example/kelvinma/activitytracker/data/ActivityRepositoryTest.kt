package com.example.kelvinma.activitytracker.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityRepositoryTest {

    private lateinit var repository: ActivityRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = ActivityRepository(context)
    }

    @Test
    fun getActivities_loadsFromYaml() {
        val activities = repository.getActivities()

        assertEquals(2, activities.size)

        val workoutActivity = activities.find { it.name == "7 Min Workout" }
        assertTrue(workoutActivity != null)
        assertEquals(2, workoutActivity?.intervals?.size)
        assertEquals("Jumping Jacks", workoutActivity?.intervals?.get(0)?.name)
        assertEquals(30, workoutActivity?.intervals?.get(0)?.duration)
        assertEquals("seconds", workoutActivity?.intervals?.get(0)?.duration_unit)
        assertEquals(5, workoutActivity?.intervals?.get(0)?.rest_duration)
        assertEquals("seconds", workoutActivity?.intervals?.get(0)?.rest_duration_unit)
        assertEquals("Wall Sit", workoutActivity?.intervals?.get(1)?.name)
        assertEquals(45, workoutActivity?.intervals?.get(1)?.duration)
        assertEquals("seconds", workoutActivity?.intervals?.get(1)?.duration_unit)


        val studyActivity = activities.find { it.name == "Study Session" }
        assertTrue(studyActivity != null)
        assertEquals(1, studyActivity?.intervals?.size)
        assertEquals("Pomodoro", studyActivity?.intervals?.get(0)?.name)
        assertEquals(25, studyActivity?.intervals?.get(0)?.duration)
        assertEquals("minutes", studyActivity?.intervals?.get(0)?.duration_unit)
        assertEquals(5, studyActivity?.intervals?.get(0)?.rest_duration)
        assertEquals("minutes", studyActivity?.intervals?.get(0)?.rest_duration_unit)
    }
}
