package io.stream29.streamlin

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule

/**
 * Deserialize a [T] object from a function.
 *
 * @param mapper A function that contains the logic to retrieve the value.
 * @return Result<T> for deserialized object or error.
 */
inline fun <reified T : Any> fromFunctionResult(noinline mapper: (String) -> String?) =
    runCatching { fromFunction<T>(mapper) }

/**
 * Deserialize a [T] object from a function.
 *
 * @param mapper A function that contains the logic to retrieve the value.
 * @return A [T] object.
 */
inline fun <reified T> fromFunction(noinline mapper: (String) -> String?) =
    serializer<T>().deserialize(SimpleFunctionDecoder(mapper))

/**
 * A decoder that deserializes data from a function.
 *
 * @param mapper A function that contains the logic to retrieve the value.
 * @param delimiter The delimiter used to separate elements in the path.
 */
@OptIn(ExperimentalSerializationApi::class)
class SimpleFunctionDecoder(
    private val mapper: (String) -> String?,
    private val delimiter: String = "."
) : AbstractDecoder() {
    override val serializersModule = EmptySerializersModule()
    private var depth = -1

    private val elementCountStack = mutableListOf<Int>()
    private var currentElementCount: Int
        get() = elementCountStack.last()
        set(value) {
            elementCountStack[depth] = value
        }

    private val descriptorStack = mutableListOf<SerialDescriptor>()
    private val currentDescriptor: SerialDescriptor
        get() = descriptorStack.last()

    private val currentPrefix = mutableListOf<String>()

    private val currentElementName
        get() = currentDescriptor.getElementName(currentElementCount)
    private val currentElementSerialName
        get() = currentDescriptor.getElementDescriptor(currentElementCount).serialName
    private val currentElementPath
        get() = currentPrefix.asSequence().plus(currentElementName).joinToString(delimiter)

    override fun decodeString(): String {
        return mapper(currentElementPath).also { currentElementCount++ }
            ?: throw MissingFieldException(currentElementPath, currentElementSerialName)
    }

    override fun decodeInt() =
        decodeString().toInt()

    override fun decodeBoolean() =
        decodeString().toBoolean()

    override fun decodeFloat() =
        decodeString().toFloat()

    override fun decodeDouble() =
        decodeString().toDouble()

    override fun decodeLong() =
        decodeString().toLong()

    override fun decodeShort() =
        decodeString().toShort()

    override fun decodeByte() =
        decodeString().toByte()

    override fun decodeChar() =
        decodeString().single()

    override fun decodeEnum(enumDescriptor: SerialDescriptor) =
        enumDescriptor.getElementIndex(decodeString())

    override fun decodeValue() = throw SerializationException("Not supported type: $currentElementSerialName")

    override fun decodeNotNullMark() = mapper(currentElementPath) != null

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while(true) {
            println(currentElementCount)
            if (currentElementCount >= descriptor.elementsCount)
                return CompositeDecoder.DECODE_DONE
            if(!currentDescriptor.isElementOptional(currentElementCount) || decodeNotNullMark())
                break
            currentElementCount++
        }
        return currentElementCount
    }

    override fun decodeSequentially() = false

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (depth >= 0) //root node corresponds to no prefix
            currentPrefix.add(currentElementName)
        descriptorStack.add(descriptor)
        elementCountStack.add(0)
        depth++
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        depth--
        descriptorStack.removeLast()
        elementCountStack.removeLast()
        if (depth >= 0) {
            currentPrefix.removeLast()
            currentElementCount++
        }
    }
}