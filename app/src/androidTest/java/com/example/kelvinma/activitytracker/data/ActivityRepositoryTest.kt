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

        assertTrue("Should have at least 2 activities", activities.size >= 2)

        // Just verify that activities are loaded and have basic structure
        activities.forEach { activity ->
            assertTrue("Activity should have a name", activity.name.isNotBlank())
            assertTrue("Activity should have intervals", activity.intervals.isNotEmpty())
            
            activity.intervals.forEach { interval ->
                assertTrue("Interval should have a name", !interval.name.isNullOrBlank())
                assertTrue("Interval should have positive duration", interval.duration > 0)
                assertTrue("Interval should have valid duration unit", !interval.duration_unit.isNullOrBlank())
            }
        }
    }
}
