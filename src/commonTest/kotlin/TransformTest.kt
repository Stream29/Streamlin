import io.github.stream29.streamlin.prettyPrintln
import io.github.stream29.streamlin.serialize.transform.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalSerializationApi
class TransformTest {
    @Test
    fun polymorphicAndInlineTest() {
        val value = Transformer { encodeNull = false }.fromSerializable<TestTag>(TestData())
        prettyPrintln(value)
        val testData = Transformer.toSerializable<TestTag>(value)
        assertEquals(TestData("Stream", TestInline(12)), testData)
        prettyPrintln(testData)
    }

    @Test
    fun genericSafetyTest() {
        val value = Transformer.fromSerializable(TestGeneric(12))
        prettyPrintln(value)
        try {
            Transformer.toSerializable<TestGeneric<Long>>(value)
        } catch (e: SerializationException) {
            assertEquals("Expected Long", e.message)
        }
        val testGeneric = Transformer.toSerializable<TestGeneric<Int>>(value)
        prettyPrintln(testGeneric)
    }

    @Test
    fun defaultTest() {
        assertEquals(Transformer { encodeDefault = false }.fromSerializable(TestDefault()), StructureValue())
        val value = Transformer.fromSerializable(TestDefault())
        prettyPrintln(value)
        val testDefault = Transformer.toSerializable<TestDefault>(value)
        prettyPrintln(testDefault)
        assertEquals(TestDefault("Stream", 12), testDefault)
    }

    @Test
    fun nullableTest() {
        val value = Transformer { encodeNull = true }.fromSerializable(TestNullable())
        prettyPrintln(value)
        val testNullable = Transformer.toSerializable<TestNullable>(value)
        prettyPrintln(testNullable)
        assertEquals(TestNullable(null, null), testNullable)
    }

    @Test
    fun notEncodeNullableTest() {
        val value = Transformer { encodeNull = false }.fromSerializable(TestNullable())
        prettyPrintln(value)
        assertEquals(StructureValue(), value)
    }

    @Test
    fun transformTest() {
        val value = Transformer.fromSerializable(TestDefault())
        prettyPrintln(value)
        val transformed = Transformer.toSerializable<TestTransform>(value)
        assertEquals(TestTransform(), transformed)
        prettyPrintln(transformed)
    }

    @Test
    fun fromMapTest() {
        val value = Transformer.fromMap(
            mapOf(
                "name" to "Stream",
                "age" to TestInline(12)
            )
        )
        prettyPrintln(value)
        val decoded = Transformer.toSerializable<TestGenericHolder>(value)
        assertEquals(TestGenericHolder(), decoded)
        prettyPrintln(decoded)
    }

    @Test
    fun toMapTest() {
        val value = Transformer.fromSerializable(TestStructure())
        prettyPrintln(value)
        val map = Transformer.toMap(value as StructureValue)
        prettyPrintln(map)
        compareRecursive(
            mapOf(
                "name" to "Stream",
                "age" to mapOf(1 to 2, 3 to 4)
            ), map
        )
    }

    @Test
    fun fromListTest() {
        val list = listOf(
            "Stream",
            mapOf(1 to 2, 3 to 4)
        )
        val value = Transformer.fromList(list)
        prettyPrintln(value)
        val decoded = Transformer.toMap(value)
        assertEquals(
            mapOf(
                0 to "Stream",
                1 to mapOf(1 to 2, 3 to 4)
            ), decoded
        )
        prettyPrintln(decoded)
    }

    @Test
    fun toListTest() {
        val value = TestTransform()
        val encoded = Transformer.fromSerializable(value)
        prettyPrintln(encoded)
        val decoded = Transformer.toSerializable<List<String>>(encoded)
        prettyPrintln(decoded)
        assertEquals(listOf("Stream"), decoded)
    }
}

fun compareRecursive(a: Map<*, *>, b: Map<*, *>) {
    for ((key, value) in a) {
        if (!b.contains(key))
            throw AssertionError("Key $key not found in b")
        if (value is Map<*, *>)
            compareRecursive(value, b[key] as Map<*, *>)
        else
            assertEquals(value, b[key])
    }
}