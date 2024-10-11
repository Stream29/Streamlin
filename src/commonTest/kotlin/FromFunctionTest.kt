import io.github.stream29.streamlin.prettyPrintln
import io.github.stream29.streamlin.serialize.function.fromFunction
import kotlin.test.Test
import kotlin.test.assertEquals

class FromFunctionTest {
    @Test
    fun fromMapTest() {
        val map = mapOf(
            "name" to "Stream",
            "age.property" to "12"
        )
        val testData = fromFunction<TestData> { map[it] }
        assertEquals(TestData("Stream", TestInline(12)), testData)
        prettyPrintln(testData)
    }

    @Test
    fun defaultTest() {
        val map = mapOf(
            "name" to "Stream",
        )
        val testDefault = fromFunction<TestDefault> { map[it] }
        prettyPrintln(testDefault)
        assertEquals(TestDefault("Stream", 12), testDefault)
    }

    @Test
    fun nullableTest() {
        val map = mapOf(
            "name" to null,
        )
        val testNullable = fromFunction<TestNullable> { map[it] }
        prettyPrintln(testNullable)
        assertEquals(TestNullable(null, null), testNullable)
    }
}