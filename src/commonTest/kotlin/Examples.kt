import io.github.stream29.streamlin.*
import io.github.stream29.streamlin.serialize.function.fromFunction
import io.github.stream29.streamlin.serialize.transform.Transformer
import io.github.stream29.streamlin.serialize.transform.fromMap
import io.github.stream29.streamlin.serialize.transform.fromSerializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.test.Test

// For cached function
val cacheMap = mutableMapOf<Int, Int>()
val cachedFunction = cacheWith(cacheMap) {
    println("Computing with param $it...")
    // Doing some heavy computation...
    it * 2
}

// For cached extension properties
class Example(val name: String)

val lazyMap = mutableMapOf<Example, Int>()
val Example.nameLength by lazy(lazyMap) { name.length }
val Example.nameOfFirstAccessedInstance by globalCached { name }

// For pretty print and transformer
data class PrettyPrint(
    val name: String = "Stream",
    val age: Int = 114514,
    val address: String = "Stream's home",
    val embedded: Embedded = Embedded(),
)

@Serializable
data class Embedded(
    val property1: String = "1",
    val property2: String = "2",
)

// For fromFunction decoder
@Serializable
data class Config(
    val name: String = "Stream",
    val age: Int = 114514,
    val doubleProperty: Double,
    val embedded: Embedded = Embedded(),
) : Tag

@Serializable
@SerialName("less")
data class LessConfig(
    val name: String,
    val age: Int,
) : Tag

@Serializable
data class MoreConfig(
    val name: String = "Stream",
    val age: Int = 114514,
    val doubleProperty: Double,
    val embedded: Embedded = Embedded(),
    val more: Boolean = true,
)

@Serializable
data class NullableConfig(
    val name: String = "Stream",
    val nullableProperty: Int?
)

@Serializable
sealed interface Tag

@Serializable
@JvmInline
value class Holder<T>(val value: T)

class Examples {
    @Test
    fun cachedFunctionExample() {
        // Only compute once for 1
        cachedFunction(1)
        // Only compute once for 2
        cachedFunction(2)
        // No computation for 1 again
        cachedFunction(1)
        // No computation for 2 again
        cachedFunction(2)
    }

    @Test
    fun cachedExtensionPropertiesExample() {
        val example1 = Example("Stream")
        val example2 = Example("Stream29")

        println(example1.nameLength) // "Stream".length = 6
        println(example2.nameLength) // "Stream29".length = 8

        println(example1.nameOfFirstAccessedInstance) // "Stream"
        println(example2.nameOfFirstAccessedInstance) // "Stream"
    }

    @Test
    fun prettyPrintExample() {
        val example = PrettyPrint()
        prettyPrintln(example)
        /*
        will print:
        PrettyPrint(
          name=Stream,
          age=114514,
          address=Stream's home,
          embedded=Embedded(
            property1=1,
            property2=2
          )
        )
         */
    }

    @Test
    fun filterExample() {
        val example = Example("Stream")
        example.filter { name.startsWith("str", ignoreCase = true) } // example
        example.filter { name.startsWith("str", ignoreCase = false) } // null
    }

    @Test
    fun fromFunctionExample() {
        val map = mapOf(
            "name" to "Stream",
            "age" to "114514",
            "doubleProperty" to "2.0",
            "embedded.property1" to "1",
            "embedded.property2" to "2",
        )
        val config = fromFunction<Config>(map::get) // Config("Stream", 114514, 2.0, Embedded("1", "2"))
    }

    @Test
    fun transformerExample() {
        val map = mapOf(
            "name" to "Stream",
            "age" to 114514,
            "doubleProperty" to 2.0,
            "nullableProperty" to null,
        )
        // Map can be transformed to serializable object
        // Commonly, fromSerializable can be used for a map with known type parameter (e.g. Map<String, String>)
        // If the type parameter of map is not clear or not serializable (e.g. Map<String, Any>), use fromMap instead
        val lessConfig = with(Transformer) { fromMap(map).toSerializable<LessConfig>() }
        // LessConfig(name=Stream, age=114514)

        // Default values works smoothly
        val defaultValueConfig = with(Transformer) { fromMap(map).toSerializable<Config>() }
        // Config(name=Stream, age=114514, doubleProperty=2.0, embedded=Embedded(property1=1, property2=2))

        // Nullable values works smoothly
        val nullableConfig = with(Transformer) { fromMap(map).toSerializable<NullableConfig>() }
        // NullableConfig(name=Stream, nullableProperty=null)

        // Serializable objects can be transformed to map
        // The returned type of toMap is Map<*,*> because the type of the object is not known
        // But you can get the type of elements of the returned map at runtime by `in`
        val transformedMap = with(Transformer) { fromSerializable(defaultValueConfig).toMap() }
        // {name=Stream, age=114514, doubleProperty=2.0, embedded={property1=1, property2=2}}

        // List can also be deserialized, but indexes (or key of map, name of property) are not preserved
        // Commonly, toSerializable can be used for a list with known type parameter (e.g. List<String>)
        // If its type parameter is not clear or not serializable (e.g. List<Any>), use toMap().values instead
        val listMap = mapOf(1 to "hello", 3 to "world")
        val listFromMap = with(Transformer) { fromMap(listMap).toSerializable<List<String>>() }
        // [hello, world]
        val listFromObject = with(Transformer) {
            fromSerializable(Embedded("hello", "world")).toSerializable<List<String>>()
        }
        // [hello, world]

        // List can also be serialized, using indexes as keys
        val list = listOf("hello", "world")
        val mapFromList = with(Transformer) { fromSerializable(list).toMap() }
        // {0=hello, 1=world}

        // With "type" property, a sealed class or interface can be deserialized polymorphically
        // Default name of a type is its qualified name
        // @SerialName can be used to simplify the name
        val polymorphicMap = mapOf(
            "type" to "less",
            "name" to "Stream",
            "age" to 114514
        )
        val polymorphicConfig = with(Transformer) { fromMap(polymorphicMap).toSerializable<Tag>() }
        // LessConfig(name=Stream, age=114514)

        // Inline classes and generic classes are supported smoothly
        val inlineAndGenericDeserialized = with(Transformer) { fromSerializable(1).toSerializable<Holder<Int>>() }
        // Holder(value=1)

        // You can decide whether to encode null and default values
        val myTransformer = Transformer {
            encodeNull = false
            encodeDefault = false
        }
        val encodedMap = with(myTransformer) {
            fromSerializable(NullableConfig(nullableProperty = null)).toMap()
        }
        // {} (blank map)

        // When working with generics, type safety is guaranteed
        runCatching {
            with(Transformer) {
                fromSerializable(Holder("hello")).toSerializable<Holder<Int>>()
            }
        }.onFailure {
            val message = it.message
            // Expected Int
        }
    }
}