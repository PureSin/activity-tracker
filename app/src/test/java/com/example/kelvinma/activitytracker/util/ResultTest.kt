package com.example.kelvinma.activitytracker.util

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ResultTest {

    @Test
    fun testSuccess_creation() {
        val result = Result.Success("test data")
        
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals("test data", result.getOrNull())
        assertEquals("test data", result.getOrDefault("default"))
        assertEquals("test data", result.getOrThrow())
    }

    @Test
    fun testError_creation() {
        val exception = RuntimeException("Test error")
        val result: Result<String> = Result.Error(exception, "Custom message")
        
        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertNull(result.getOrNull())
        assertEquals("default", result.getOrDefault("default"))
        assertEquals("Custom message", (result as Result.Error).message)
    }

    @Test(expected = RuntimeException::class)
    fun testError_getOrThrow_throwsException() {
        val exception = RuntimeException("Test error")
        val result: Result<String> = Result.Error(exception)
        
        result.getOrThrow()
    }

    @Test
    fun testSuccess_onSuccess_callsAction() {
        var actionCalled = false
        val result = Result.Success("test")
        
        result.onSuccess { 
            actionCalled = true
            assertEquals("test", it)
        }
        
        assertTrue(actionCalled)
    }

    @Test
    fun testError_onSuccess_doesNotCallAction() {
        var actionCalled = false
        val result: Result<String> = Result.Error(RuntimeException())
        
        result.onSuccess { actionCalled = true }
        
        assertFalse(actionCalled)
    }

    @Test
    fun testSuccess_onError_doesNotCallAction() {
        var actionCalled = false
        val result = Result.Success("test")
        
        result.onError { actionCalled = true }
        
        assertFalse(actionCalled)
    }

    @Test
    fun testError_onError_callsAction() {
        var actionCalled = false
        val exception = RuntimeException("Test error")
        val result: Result<String> = Result.Error(exception)
        
        result.onError { 
            actionCalled = true
            assertEquals(exception, it)
        }
        
        assertTrue(actionCalled)
    }

    @Test
    fun testSuccess_map_transformsValue() {
        val result = Result.Success(5)
        val mapped = result.map { it * 2 }
        
        assertTrue(mapped.isSuccess)
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun testError_map_preservesError() {
        val exception = RuntimeException("Test error")
        val result: Result<Int> = Result.Error(exception)
        val mapped = result.map { "transformed" }
        
        assertTrue(mapped.isError)
        assertNull(mapped.getOrNull())
    }

    @Test
    fun testSuccess_map_exceptionInTransform_returnsError() {
        val result = Result.Success("test")
        val mapped = result.map<String> { throw RuntimeException("Transform error") }
        
        assertTrue(mapped.isError)
        assertNull(mapped.getOrNull())
    }

    @Test
    fun testSafeCall_success() {
        val result = safeCall { "success" }
        
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun testSafeCall_exception() {
        val result = safeCall { throw RuntimeException("Test error") }
        
        assertTrue(result.isError)
        assertNull(result.getOrNull())
    }

    @Test
    fun testSafeSuspendCall_success() = runTest {
        val result = safeSuspendCall { "success" }
        
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun testSafeSuspendCall_exception() = runTest {
        val result = safeSuspendCall { throw RuntimeException("Test error") }
        
        assertTrue(result.isError)
        assertNull(result.getOrNull())
    }

    @Test
    fun testChaining_successToSuccess() {
        val result = safeCall { 5 }
            .map { it * 2 }
            .map { it.toString() }
        
        assertTrue(result.isSuccess)
        assertEquals("10", result.getOrNull())
    }

    @Test
    fun testChaining_successToError() {
        val result = safeCall { 5 }
            .map { it * 2 }
            .map<String> { throw RuntimeException("Transform error") }
        
        assertTrue(result.isError)
        assertNull(result.getOrNull())
    }

    @Test
    fun testChaining_errorPreservation() {
        val originalException = RuntimeException("Original error")
        val result: Result<String> = Result.Error(originalException)
        val mapped = result
            .map { "transformed" }
            .map { "transformed again" }
        
        assertTrue(mapped.isError)
        assertNull(mapped.getOrNull())
    }
}