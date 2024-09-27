import io.github.stream29.streamlin.serialize.transform.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

@ExperimentalSerializationApi
fun main() {
    val testList = Test(name = null)
    val encoder = AnyEncoder()
    testList.encodeWith(encoder)
    println(encoder.record)
    val decoder = AnyDecoder(encoder.record)
    println(decodeWith<Test>(decoder))
}

@Serializable
sealed interface Tag

@Serializable
@SerialName("Test")
data class Test(
    val name: String? = "Stream",
    val age: TestEnum = TestEnum.A
) : Tag

@Serializable
enum class TestEnum {
    A, B, C
}

inline fun <reified T> T.encodeWith(encoder: Encoder) =
    serializer(typeOf<T>()).serialize(encoder, this)

inline fun <reified T> decodeWith(decoder: Decoder) =
    serializer(typeOf<T>()).deserialize(decoder)
