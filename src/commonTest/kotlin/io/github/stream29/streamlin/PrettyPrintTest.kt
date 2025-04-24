package io.github.stream29.streamlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrettyPrintTest {
    @Test
    fun testToPrettyFormat() {
        // Test with a simple JSON-like string
        val input = """{"name":"John","age":30,"address":{"street":"Main St","city":"New York"}}"""
        val expected = """{
  "name":"John",
  "age":30,
  "address":{
    "street":"Main St",
    "city":"New York"
  }
}"""
        val result = input.toPrettyFormat()
        assertEquals(expected, result)
        
        // Test with nested arrays
        val inputWithArrays = """{"name":"John","hobbies":["reading","swimming",{"type":"sports","activities":["football","basketball"]}]}"""
        val resultWithArrays = inputWithArrays.toPrettyFormat()
        
        // Check that the result has proper indentation
        assertTrue(resultWithArrays.contains("  \"hobbies\":["))
        assertTrue(resultWithArrays.contains("    \"reading\""))
        assertTrue(resultWithArrays.contains("      \"type\":\"sports\""))
        assertTrue(resultWithArrays.contains("      \"activities\":["))
        assertTrue(resultWithArrays.contains("        \"football\""))
    }
    
    @Test
    fun testPrettyPrint() {
        // Since prettyPrint outputs to console, we can only test that it doesn't throw exceptions
        val input = """{"name":"John","age":30}"""
        prettyPrint(input) // Should not throw
        
        // Test with an object
        data class Person(val name: String, val age: Int)
        val person = Person("John", 30)
        prettyPrint(person) // Should not throw
    }
    
    @Test
    fun testPrettyPrintln() {
        // Since prettyPrintln outputs to console, we can only test that it doesn't throw exceptions
        val input = """{"name":"John","age":30}"""
        prettyPrintln(input) // Should not throw
        
        // Test with an object
        data class Person(val name: String, val age: Int)
        val person = Person("John", 30)
        prettyPrintln(person) // Should not throw
    }
}