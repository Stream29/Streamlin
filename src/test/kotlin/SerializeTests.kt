import io.github.stream29.streamlin.serialize.function.fromFunction
import io.github.stream29.streamlin.serialize.transform.AnyDecoder
import io.github.stream29.streamlin.serialize.transform.AnyEncoder
import io.github.stream29.streamlin.serialize.transform.TransformConfiguration
import io.github.stream29.streamlin.serialize.transform.Transformer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalSerializationApi
class SerializeTests {
    @Test
    fun fromFunctionTest() {
        val map = mapOf(
            "name" to "Stream",
            "age.property" to "12"
        )
        val testData = fromFunction<TestData> { map[it] }
        assertEquals(TestData("Stream", TestInline(12)), testData)
        prettyPrintln(testData)
    }

    @Test
    fun polymorphicTest() {
        val value = Transformer.encodeToValue<Tag>(TestData())
        prettyPrintln(value)
        val testData = Transformer.decodeFromValue<Tag>(value)
        assertEquals(TestData("Stream", TestInline(12)), testData)
        prettyPrintln(testData)
    }

}

@Serializable
sealed interface Tag

@Serializable
@JvmInline
value class TestInline(val value: Int = 5)

@Serializable
data class TestData(
    val name: String? = "Stream",
    val age: TestInline = TestInline(12)
) : Tag

@Serializable
data class TestGeneric<T>(
    val property: T
)

fun prettyPrintln(any: Any?) = prettyPrintln(any.toString())
fun prettyPrintln(text: String) {
    var indent = 0
    text.asSequence().forEach {
        when (it) {
            '{', '[', '(' -> {
                println(it)
                indent += 2
                print(" ".repeat(indent))
            }

            '}', ']', ')' -> {
                indent -= 2
                print("\n" + " ".repeat(indent))
                print(it)
            }

            ',' -> {
                println(it)
                print(" ".repeat(indent - 1))
            }

            else -> print(it)
        }
    }
    println()
}
