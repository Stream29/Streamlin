package io.github.stream29.streamlin.serialize

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.Any
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@ExperimentalSerializationApi
class DataClassDecoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : AbstractDecoder() {
    private var depth = 0
    private val elementIndexStack = mutableListOf(0)
    private var currentElementIndex: Int
        get() = elementIndexStack.last()
        set(value) {
            elementIndexStack[depth - 1] = value
        }
    private val descriptorStack = mutableListOf<SerialDescriptor>()
    private val currentDescriptor: SerialDescriptor
        get() = descriptorStack.last()
    private val currentElementName
        get() = currentDescriptor.getElementName(currentElementIndex)
    private val currentElementSerialName
        get() = currentDescriptor.getElementDescriptor(currentElementIndex).serialName

    override fun decodeString() = "Test"
    override fun decodeInt() = 2

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        testPrintln("decodeElementIndex: $descriptor")
        return if (currentElementIndex < descriptor.elementsCount) {
            currentElementIndex++
            currentElementIndex - 1
        } else CompositeDecoder.DECODE_DONE
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        descriptorStack.add(descriptor)
        elementIndexStack.add(0)
        depth++
        testPrintln("beginStructure: $descriptor")
        indent++
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        depth--
        descriptorStack.removeLast()
        elementIndexStack.removeLast()
        indent--
        testPrintln("endStructure: $descriptor")
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        testPrintln("decodeCollectionSize: $descriptor")
        return 3
    }

    override fun decodeSequentially() = false
}

@ExperimentalSerializationApi
class DataClassEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : AbstractEncoder() {
    private var depth = 0
    private val elementIndexStack = mutableListOf<Int>()
    private var currentElementIndex: Int
        get() = elementIndexStack.last()
        set(value) {
            elementIndexStack[depth - 1] = value
        }
    private val descriptorStack = mutableListOf<SerialDescriptor>()
    private val currentDescriptor: SerialDescriptor
        get() = descriptorStack.last()
    private val currentElementName
        get() = currentDescriptor.getElementName(currentElementIndex)
    private val currentElementSerialName
        get() = currentDescriptor.getElementDescriptor(currentElementIndex).serialName

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        descriptorStack.add(descriptor)
        elementIndexStack.add(0)
        depth++
        testPrintln("beginStructure: $descriptor")
        testPrintln("${descriptor.kind}")
        indent++
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        descriptorStack.removeLast()
        elementIndexStack.removeLast()
        depth--
        indent--
        testPrintln("endStructure: $descriptor")
    }

    override fun encodeValue(value: Any) {
        testPrintln("encodeValue: $value,currentElementIndex: $currentElementIndex, currentElementName: $currentElementName, currentElementSerialName: $currentElementSerialName")
        currentElementIndex++
    }
}

var indent = 0
fun testPrintln(text: String) {
    print("  ".repeat(indent))
    println(text)
}

@Serializable
sealed interface Tag

@Serializable
@SerialName("Test")
data class Test(val name: String = "Stream", val age: String = "114514") : Tag

@Serializable
data class Test2(
    val test1: Test,
    val test2: Test,
)

inline fun <reified T> T.encodeWith(encoder: Encoder) =
    serializer(typeOf<T>()).serialize(encoder, this)

inline fun <reified T> decodeWith(decoder: Decoder) =
    serializer(typeOf<T>()).deserialize(decoder)

@ExperimentalSerializationApi
fun main() {
    val testList = mapOf("test1" to Test("test1"), "test2" to Test("test2"))
    val encoder = AnyEncoder()
    testList.encodeWith(encoder)
    println(encoder.record)
    val decoder = AnyDecoder(encoder.record)
    println(decodeWith<Test2>(decoder))
    println(decodeWith<Map<String, Test>>(decoder))
    println(decodeWith<Map<String, Map<String, String>>>(decoder))
}