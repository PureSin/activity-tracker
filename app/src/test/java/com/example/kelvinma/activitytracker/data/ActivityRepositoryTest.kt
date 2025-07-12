package com.example.kelvinma.activitytracker.data

import android.content.Context
import android.content.res.AssetManager
import com.charleskorn.kaml.Yaml
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException

class ActivityRepositoryTest {

    private val mockContext = mock<Context>()
    private val mockAssetManager = mock<AssetManager>()
    private lateinit var repository: ActivityRepository

    @Before
    fun setup() {
        whenever(mockContext.assets).thenReturn(mockAssetManager)
        repository = ActivityRepository(mockContext)
    }

    @Test
    fun testGetActivities_validYamlFiles_returnsActivities() {
        // Arrange
        val yamlFiles = arrayOf("workout.yaml", "study.yaml", "meditation.yaml")
        val validYaml = """
            name: Test Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        
        yamlFiles.forEach { fileName ->
            whenever(mockAssetManager.open(fileName))
                .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))
        }

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(3, activities.size)
        activities.forEach { activity ->
            assertEquals("Test Activity", activity.name)
            assertEquals(1, activity.intervals.size)
            assertEquals("Test Interval", activity.intervals[0].name)
            assertEquals(30, activity.intervals[0].duration)
            assertEquals("seconds", activity.intervals[0].duration_unit)
        }
    }

    @Test
    fun testGetActivities_emptyAssetDirectory_returnsEmptyList() {
        // Arrange
        whenever(mockAssetManager.list("")).thenReturn(arrayOf())

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(0, activities.size)
    }

    @Test
    fun testGetActivities_nullAssetList_returnsEmptyList() {
        // Arrange
        whenever(mockAssetManager.list("")).thenReturn(null)

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(0, activities.size)
    }

    @Test
    fun testGetActivities_malformedYaml_skipsInvalidFiles() {
        // Arrange
        val yamlFiles = arrayOf("valid.yaml", "invalid.yaml")
        val validYaml = """
            name: Valid Activity
            intervals:
              - name: Valid Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()
        
        val invalidYaml = """
            name: Invalid Activity
            intervals:
              - name: Invalid Interval
                duration: -5
                duration_unit: invalid_unit
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("valid.yaml"))
            .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))
        whenever(mockAssetManager.open("invalid.yaml"))
            .thenReturn(ByteArrayInputStream(invalidYaml.toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(1, activities.size)
        assertEquals("Valid Activity", activities[0].name)
    }

    @Test
    fun testGetActivities_fileNotFound_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("existing.yaml", "missing.yaml")
        val validYaml = """
            name: Existing Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("existing.yaml"))
            .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))
        whenever(mockAssetManager.open("missing.yaml"))
            .thenThrow(FileNotFoundException("File not found"))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(1, activities.size)
        assertEquals("Existing Activity", activities[0].name)
    }

    @Test
    fun testGetActivities_ioException_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("good.yaml", "corrupted.yaml")
        val validYaml = """
            name: Good Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("good.yaml"))
            .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))
        whenever(mockAssetManager.open("corrupted.yaml"))
            .thenThrow(IOException("IO error"))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(1, activities.size)
        assertEquals("Good Activity", activities[0].name)
    }

    @Test
    fun testGetActivities_emptyYamlFile_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("valid.yaml", "empty.yaml")
        val validYaml = """
            name: Valid Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("valid.yaml"))
            .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))
        whenever(mockAssetManager.open("empty.yaml"))
            .thenReturn(ByteArrayInputStream("".toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(1, activities.size)
        assertEquals("Valid Activity", activities[0].name)
    }

    @Test
    fun testGetActivityByName_existingActivity_returnsActivity() {
        // Arrange
        val yamlFiles = arrayOf("test.yaml")
        val validYaml = """
            name: Test Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("test.yaml"))
            .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))

        // Act
        val activity = repository.getActivityByName("Test Activity")

        // Assert
        assertNotNull(activity)
        assertEquals("Test Activity", activity?.name)
    }

    @Test
    fun testGetActivityByName_nonExistingActivity_returnsNull() {
        // Arrange
        val yamlFiles = arrayOf("test.yaml")
        val validYaml = """
            name: Test Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("test.yaml"))
            .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))

        // Act
        val activity = repository.getActivityByName("Non-existing Activity")

        // Assert
        assertNull(activity)
    }

    @Test
    fun testValidation_blankActivityName_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("blank_name.yaml")
        val invalidYaml = """
            name: ""
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("blank_name.yaml"))
            .thenReturn(ByteArrayInputStream(invalidYaml.toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(0, activities.size)
    }

    @Test
    fun testValidation_emptyIntervals_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("no_intervals.yaml")
        val invalidYaml = """
            name: Test Activity
            intervals: []
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("no_intervals.yaml"))
            .thenReturn(ByteArrayInputStream(invalidYaml.toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(0, activities.size)
    }

    @Test
    fun testValidation_negativeDuration_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("negative_duration.yaml")
        val invalidYaml = """
            name: Test Activity
            intervals:
              - name: Invalid Interval
                duration: -10
                duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("negative_duration.yaml"))
            .thenReturn(ByteArrayInputStream(invalidYaml.toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(0, activities.size)
    }

    @Test
    fun testValidation_invalidDurationUnit_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("invalid_unit.yaml")
        val invalidYaml = """
            name: Test Activity
            intervals:
              - name: Invalid Interval
                duration: 30
                duration_unit: invalid_unit
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("invalid_unit.yaml"))
            .thenReturn(ByteArrayInputStream(invalidYaml.toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(0, activities.size)
    }

    @Test
    fun testValidation_validRestDuration_includesFile() {
        // Arrange
        val yamlFiles = arrayOf("with_rest.yaml")
        val validYaml = """
            name: Test Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
                rest_duration: 10
                rest_duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("with_rest.yaml"))
            .thenReturn(ByteArrayInputStream(validYaml.toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(1, activities.size)
        assertEquals(10, activities[0].intervals[0].rest_duration)
    }

    @Test
    fun testValidation_negativeRestDuration_skipsFile() {
        // Arrange
        val yamlFiles = arrayOf("negative_rest.yaml")
        val invalidYaml = """
            name: Test Activity
            intervals:
              - name: Test Interval
                duration: 30
                duration_unit: seconds
                rest_duration: -5
                rest_duration_unit: seconds
        """.trimIndent()

        whenever(mockAssetManager.list("")).thenReturn(yamlFiles)
        whenever(mockAssetManager.open("negative_rest.yaml"))
            .thenReturn(ByteArrayInputStream(invalidYaml.toByteArray()))

        // Act
        val activities = repository.getActivities()

        // Assert
        assertEquals(0, activities.size)
    }
}