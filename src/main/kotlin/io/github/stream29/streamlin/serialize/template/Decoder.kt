package io.github.stream29.streamlin.serialize.template

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder

/**
 * A template for implementing [Decoder].
 *
 * This class provides default implementations for all `decodeXxx` method
 * and redirect to method [decodePrimitive] to decode primitive values.
 * You should override [decodePrimitive] to provide actual decoding logic.
 *
 * Also, you should override [beginStructure] to provide a new instance of [CompositeDecoder]
 * and [decodeNotNullMark] to decode nullable values.
 */
@ExperimentalSerializationApi
abstract class DecoderTemplate: Decoder {

    /**
     * Decodes a primitive value.
     * Corresponding kind is in [PrimitiveKind].
     */
    abstract fun decodePrimitive(): Any

    override fun decodeBoolean(): Boolean =
        decodePrimitive() as? Boolean
            ?: throw SerializationException("Expected Boolean")

    override fun decodeByte(): Byte =
        decodePrimitive() as? Byte
            ?: throw SerializationException("Expected Byte")

    override fun decodeChar(): Char =
        decodePrimitive() as? Char
            ?: throw SerializationException("Expected Char")

    override fun decodeDouble(): Double =
        decodePrimitive() as? Double
            ?: throw SerializationException("Expected Double")

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val found = decodePrimitive().toString()
        return enumDescriptor.findByName(found) ?: throw SerializationException("Enum with name $found not found")
    }

    override fun decodeFloat(): Float =
        decodePrimitive() as? Float
            ?: throw SerializationException("Expected Float")

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    override fun decodeInt(): Int =
        decodePrimitive() as? Int
            ?: throw SerializationException("Expected Int")

    override fun decodeLong(): Long =
        decodePrimitive() as? Long
            ?: throw SerializationException("Expected Long")

    override fun decodeNull(): Nothing? = null

    override fun decodeShort(): Short =
        decodePrimitive() as? Short
            ?: throw SerializationException("Expected Short")

    override fun decodeString(): String =
        decodePrimitive() as? String
            ?: throw SerializationException("Expected String")
}

/**
 * A template for implementing [CompositeDecoder].
 *
 * This class provides default implementations for all `decodeXxxElement` method
 * and redirect to method [decodePrimitiveElement] to decode primitive values.
 * You should override [decodePrimitiveElement] to provide actual decoding logic.
 */
@ExperimentalSerializationApi
abstract class CompositeDecoderTemplate : CompositeDecoder {

    /**
     * Decodes a primitive value from the underlying input.
     * The resulting value is associated with the [descriptor] element at the given [index].
     * The element at the given index should have [PrimitiveKind] kind.
     */
    abstract fun decodePrimitiveElement(descriptor: SerialDescriptor, index: Int) : Any

    override fun endStructure(descriptor: SerialDescriptor) {
        // Do nothing
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        decodePrimitiveElement(descriptor, index) as? Boolean
            ?: throw SerializationException(
                "Expected Boolean at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        decodePrimitiveElement(descriptor, index) as? Byte
            ?: throw SerializationException(
                "Expected Byte at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decodePrimitiveElement(descriptor, index) as? Char
            ?: throw SerializationException(
                "Expected Char at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        decodePrimitiveElement(descriptor, index) as? Double
            ?: throw SerializationException(
                "Expected Double at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        decodePrimitiveElement(descriptor, index) as? Float
            ?: throw SerializationException(
                "Expected Float at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        decodePrimitiveElement(descriptor, index) as? Int
            ?: throw SerializationException(
                "Expected Int at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        decodePrimitiveElement(descriptor, index) as? Long
            ?: throw SerializationException(
                "Expected Long at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decodePrimitiveElement(descriptor, index) as? Short
            ?: throw SerializationException(
                "Expected Short at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        decodePrimitiveElement(descriptor, index) as? String
            ?: throw SerializationException(
                "Expected String at ${descriptor.getElementName(index)}:${descriptor.getElementDescriptor(index)}"
            )
}

@ExperimentalSerializationApi
internal fun SerialDescriptor.findByName(serialName: String): Int? {
    for (i in 0..<this.elementsCount) {
        if (getElementName(i) == serialName) {
            return i
        }
    }
    return null
}