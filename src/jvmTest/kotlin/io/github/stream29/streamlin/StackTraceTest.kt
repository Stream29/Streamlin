package io.github.stream29.streamlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StackTraceTest {

    @Test
    fun testContextStackTrace() {
        val stackTrace = contextStackTrace
        assertTrue(stackTrace.isNotEmpty(), "Stack trace should not be empty")

        // The first element should be this test method
        val firstElement = stackTrace.first()
        assertEquals("io.github.stream29.streamlin.StackTraceTest", firstElement.className)
        assertEquals("testContextStackTrace", firstElement.methodName)
    }

    @Test
    fun testContextFileName() {
        val fileName = contextFileName
        assertNotNull(fileName, "File name should not be null")
        assertEquals("StackTraceTest.kt", fileName)
    }

    @Test
    fun testContextClassName() {
        val className = contextClassName
        assertEquals("io.github.stream29.streamlin.StackTraceTest", className)
    }

    @Test
    fun testContextMethodName() {
        val methodName = contextMethodName
        assertEquals("testContextMethodName", methodName)
    }

    @Test
    fun testContextLineNumber() {
        val lineNumber = contextLineNumber
        assertNotNull(lineNumber, "Line number should not be null")
        assertTrue(lineNumber > 0, "Line number should be positive")
    }

    @Test
    fun testNestedCall() {
        // Test that the stack trace correctly identifies the caller when called from a nested function
        fun nestedFunction(): String {
            return contextMethodName
        }

        assertEquals("testNestedCall\$nestedFunction", nestedFunction())
    }

    @Test
    fun testGetStackTraceFiltered() {
        val stackTrace = getStackTraceFiltered()
        assertTrue(stackTrace.isNotEmpty(), "Filtered stack trace should not be empty")

        // Verify that no elements from StackTraceKt are included
        for (element in stackTrace) {
            assertTrue(
                element.className != "io.github.stream29.streamlin.StackTraceKt",
                "Filtered stack trace should not contain elements from StackTraceKt"
            )
        }
    }
}