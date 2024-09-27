import io.github.stream29.streamlin.prettyPrintln
import io.github.stream29.streamlin.serialize.transform.StructureValue
import io.github.stream29.streamlin.serialize.transform.Transformer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalSerializationApi
class TransformTest {
    @Test
    fun polymorphicAndInlineTest() {
        val value = Transformer { encodeNull = false }.encodeToValue<TestTag>(TestData())
        prettyPrintln(value)
        val testData = Transformer.decodeFromValue<TestTag>(value)
        assertEquals(TestData("Stream", TestInline(12)), testData)
        prettyPrintln(testData)
    }

    @Test
    fun genericSafetyTest() {
        val value = Transformer.encodeToValue(TestGeneric(12))
        prettyPrintln(value)
        assertThrows<SerializationException>("Expected Long") { Transformer.decodeFromValue<TestGeneric<Long>>(value) }
        val testGeneric = Transformer.decodeFromValue<TestGeneric<Int>>(value)
        prettyPrintln(testGeneric)
    }

    @Test
    fun defaultTest() {
        assertEquals(Transformer { encodeDefault = false }.encodeToValue(TestDefault()), StructureValue())
        val value = Transformer.encodeToValue(TestDefault())
        prettyPrintln(value)
        val testDefault = Transformer.decodeFromValue<TestDefault>(value)
        prettyPrintln(testDefault)
        assertEquals(TestDefault("Stream", 12), testDefault)
    }

    @Test
    fun nullableTest() {
        val value = Transformer { encodeNull = true }.encodeToValue(TestNullable())
        prettyPrintln(value)
        val testNullable = Transformer.decodeFromValue<TestNullable>(value)
        prettyPrintln(testNullable)
        assertEquals(TestNullable(null, null), testNullable)
    }

    @Test
    fun notEncodeNullableTest() {
        val value = Transformer { encodeNull = false }.encodeToValue(TestNullable())
        prettyPrintln(value)
        assertEquals(StructureValue(), value)
    }

    @Test
    fun transformTest() {
        val value = Transformer.encodeToValue(TestDefault())
        prettyPrintln(value)
        val transformed = Transformer.decodeFromValue<TestTransform>(value)
        assertEquals(TestTransform(), transformed)
        prettyPrintln(transformed)
    }

    @Test
    fun fromMapTest() {
        val value = Transformer.encodeAny(
            mapOf(
                "name" to "Stream",
                "age" to mapOf(1 to 2, 3 to 4)
            )
        )
        prettyPrintln(value)
        val decoded = Transformer.decodeFromValue<TestStructure>(value)
        assertEquals(TestStructure(), decoded)
        prettyPrintln(decoded)
    }

    @Test
    fun toMapTest() {
        val value = Transformer.encodeToValue(TestStructure())
        prettyPrintln(value)
        val map = Transformer.decodeToMap(value as StructureValue)
        prettyPrintln(map)
        compareRecursive(mapOf(
            "name" to "Stream",
            "age" to mapOf(1 to 2, 3 to 4)
        ), map)
    }

    @Test
    fun fromListTest() {
        val list = listOf(
            "Stream",
            mapOf(1 to 2, 3 to 4)
        )
        val value = Transformer.encodeAny(list)
        prettyPrintln(value)
        val decoded = Transformer.decodeToMap(value)
        assertEquals(mapOf(
            0 to "Stream",
            1 to mapOf(1 to 2, 3 to 4)
        ), decoded)
        prettyPrintln(decoded)
    }

    @Test
    fun toListTest() {
        val value = TestTransform()
        val encoded = Transformer.encodeToValue(value)
        prettyPrintln(encoded)
        val decoded = Transformer.decodeFromValue<List<String>>(encoded)
        prettyPrintln(decoded)
        assertEquals(listOf("Stream"), decoded)
    }

    @Test
    fun fromAnyTest() {
        val encoded = Transformer.encodeAny(TestStructure())
        prettyPrintln(encoded)
        val decoded = Transformer.decodeFromValue<TestStructure>(encoded)
        assertEquals(TestStructure(), decoded)
        prettyPrintln(decoded)
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