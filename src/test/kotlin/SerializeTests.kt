import io.github.stream29.streamlin.serialize.transform.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

@ExperimentalSerializationApi
fun main() {
    val testList = TestGeneric(12)
    val encoder = AnyEncoder(
        config = TransformEncodeConfig(
            encodeNull = true,
            encodeDefault = true
        )
    )
    testList.encodeWith<TestGeneric<Int>>(encoder)
    prettyPrintln(encoder.record)
    val decoder = AnyDecoder(record = encoder.record)
    prettyPrintln(decodeWith<TestGeneric<Int>>(decoder))
}

@Serializable
open class Tag

@Serializable
@JvmInline
value class TestInline(val value: Int = 5)

@Serializable
data class TestData(
    val name: String? = "Stream",
//    @Contextual
    val age: TestGeneric<Int> = TestGeneric<Int>(12)
) : Tag()

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

inline fun <reified T> T.encodeWith(encoder: Encoder) =
    serializer(typeOf<T>()).serialize(encoder, this)

inline fun <reified T> decodeWith(decoder: Decoder) =
    serializer(typeOf<T>()).deserialize(decoder)
