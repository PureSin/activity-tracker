package com.example.kelvinma.activitytracker.util

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DatabaseExporterTest {

    private lateinit var context: Context
    private lateinit var exporter: DatabaseExporter
    private lateinit var testDbFile: File

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        exporter = DatabaseExporter(context)
        
        // Create a test database file
        testDbFile = context.getDatabasePath("activity_tracker_database")
        testDbFile.parentFile?.mkdirs()
        
        // Create a simple test database
        val db = SQLiteDatabase.openOrCreateDatabase(testDbFile, null)
        db.execSQL("CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT)")
        db.execSQL("INSERT INTO test_table (name) VALUES ('test_data')")
        db.close()
    }

    @After
    fun cleanup() {
        // Clean up test files
        testDbFile.delete()
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        exportDir.deleteRecursively()
    }

    @Test
    fun exportDatabaseViaEmail_withValidEmail_returnsValidExportResult() {
        val testEmail = "test@example.com"
        
        val exportResult = exporter.exportDatabaseViaEmail(testEmail)
        
        assertNotNull("ExportResult should not be null", exportResult)
        
        val intent = exportResult!!.intent
        assertEquals("Intent action should be ACTION_SEND", Intent.ACTION_SEND, intent.action)
        assertEquals("Intent type should be sqlite3", "application/vnd.sqlite3", intent.type)
        
        val emailArray = intent.getStringArrayExtra(Intent.EXTRA_EMAIL)
        assertNotNull("Email array should not be null", emailArray)
        assertEquals("Email array should have one recipient", 1, emailArray!!.size)
        assertEquals("Email should match provided address", testEmail, emailArray[0])
        
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        assertNotNull("Subject should not be null", subject)
        assertTrue("Subject should contain app name", subject!!.contains("Activity Tracker Data Export"))
        
        val body = intent.getStringExtra(Intent.EXTRA_TEXT)
        assertNotNull("Email body should not be null", body)
        assertTrue("Body should contain export info", body!!.contains("Activity Tracker Data Export"))
        assertTrue("Body should mention database tables", body.contains("activity_sessions"))
        assertTrue("Body should contain file size", body.contains("File Size:"))
        
        val streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)
        assertNotNull("Stream URI should not be null", streamUri)
        
        // Test file size information
        assertTrue("File size should be greater than 0", exportResult.fileSizeBytes > 0)
        assertNotNull("Formatted file size should not be null", exportResult.getFormattedFileSize())
        assertTrue("File name should contain export pattern", exportResult.fileName.contains("activity_tracker_export"))
    }

    @Test
    fun exportDatabaseViaEmail_withNonExistentDatabase_returnsNull() {
        // Delete the test database
        testDbFile.delete()
        
        val exportResult = exporter.exportDatabaseViaEmail("test@example.com")
        
        assertNull("ExportResult should be null when database doesn't exist", exportResult)
    }

    @Test
    fun exportDatabaseViaEmail_createsExportFile() {
        val testEmail = "test@example.com"
        
        val exportResult = exporter.exportDatabaseViaEmail(testEmail)
        
        assertNotNull("ExportResult should not be null", exportResult)
        
        // Check that export directory was created
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        assertTrue("Export directory should be created", exportDir.exists())
        
        // Check that export file was created
        val exportFiles = exportDir.listFiles { file -> file.name.endsWith(".db") }
        assertNotNull("Export files array should not be null", exportFiles)
        assertTrue("At least one export file should be created", exportFiles!!.isNotEmpty())
        
        val exportFile = exportFiles[0]
        assertTrue("Export file should exist", exportFile.exists())
        assertTrue("Export file should not be empty", exportFile.length() > 0)
        assertTrue("Export file should have correct naming pattern", exportFile.name.contains("activity_tracker_export"))
    }
}