package io.github.stream29.streamlin.serialize.function

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.Any

/**
 * Deserialize a [T] object from a function.
 *
 * @param mapper A function that contains the logic to retrieve the value.
 * @return Result<T> for deserialized object or error.
 */
public inline fun <reified T : Any> fromFunctionResult(noinline mapper: (String) -> String?): Result<T> =
    runCatching { fromFunction<T>(mapper) }

/**
 * Deserialize a [T] object from a function.
 *
 * @param mapper A function that contains the logic to retrieve the value.
 * @return A [T] object.
 */
public inline fun <reified T> fromFunction(noinline mapper: (String) -> String?): T =
    serializer<T>().deserialize(SimpleFunctionDecoder(mapper))

/**
 * A decoder that deserializes data from a function.
 *
 * @param mapper A function that contains the logic to retrieve the value.
 * @param delimiter The delimiter used to separate elements in the path.
 */
@OptIn(ExperimentalSerializationApi::class)
public class SimpleFunctionDecoder(
    private val mapper: (String) -> String?,
    private val delimiter: String = "."
) : AbstractDecoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()
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

    override fun decodeInt(): Int =
        decodeString().toInt()

    override fun decodeBoolean(): Boolean =
        decodeString().toBoolean()

    override fun decodeFloat(): Float =
        decodeString().toFloat()

    override fun decodeDouble(): Double =
        decodeString().toDouble()

    override fun decodeLong(): Long =
        decodeString().toLong()

    override fun decodeShort(): Short =
        decodeString().toShort()

    override fun decodeByte(): Byte =
        decodeString().toByte()

    override fun decodeChar(): Char =
        decodeString().single()

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        enumDescriptor.getElementIndex(decodeString())

    override fun decodeValue(): Nothing = throw SerializationException("Not supported type: $currentElementSerialName")

    override fun decodeNotNullMark(): Boolean = mapper(currentElementPath) != null

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while(true) {
            if (currentElementCount >= descriptor.elementsCount)
                return CompositeDecoder.DECODE_DONE
            if(!currentDescriptor.isElementOptional(currentElementCount) || decodeNotNullMark())
                break
            currentElementCount++
        }
        return currentElementCount
    }

    override fun decodeSequentially(): Boolean = false

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