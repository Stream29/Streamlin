package io.github.stream29.streamlin

import kotlinx.serialization.Serializable
import kotlin.arrayOf
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializableAnyTest {

    @Serializable
    data class TestSerializable(
        val name: String = "hello world",
        val age: Int = 123,
        val address: String = "somewhere"
    )

    @Serializable
    data class GenericContainer<T>(val value: T)

    @Test
    fun testBasicTypes() {
        testWith(42)
        testWith("Hello, World!")
        testWith(true)
        testWith(3.14159)
        testWith(9223372036854775807L)
    }

    @Test
    fun testRegularClass() {
        testWith(TestSerializable())
    }

    @Test
    fun testCollectionTypes() {
        testWith(listOf(1, 2, 3, 4, 5))
        testWith(mapOf("one" to 1, "two" to 2, "three" to 3))
        testWith(setOf("apple", "banana", "cherry"))
        testWith(arrayOf(1, 2, 3, 4, 5).toList())
        testWith(listOf(mapOf("a" to 1, "b" to 2), mapOf("c" to 3, "d" to 4)))
    }

    @Test
    fun testGenericTypes() {
        val list = listOf(WithKType(1), WithKType("h"))
        val encodeToString = json.encodeToString(list)
        println(encodeToString)
        val decodeFromString = json.decodeFromString<List<WithKType>>(encodeToString)
        prettyPrintln(decodeFromString)
        testWith(GenericContainer(42))
        testWith(GenericContainer("Hello"))
        testWith(GenericContainer(TestSerializable()))
        testWith(GenericContainer(listOf(1, 2, 3)))
        testWith(GenericContainer(GenericContainer("Nested")))
    }

    /**
     * Helper method to test serialization and deserialization of a value with type information
     */
    private inline fun <reified T> testWith(value: T) {
        val serialized = json.encodeToString(WithKType(value))
        val deserialized = json.decodeFromString<WithKType>(serialized)
        assertEquals(value, deserialized.value, "Deserialized value should match the original")
    }
}
