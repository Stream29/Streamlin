package io.github.stream29.streamlin.serialize.template

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

/**
 * A template for implementing [Encoder].
 *
 * This class provides default implementations for all `encodeXxx` method
 * and redirect to method [encodePrimitive] to encode primitive values.
 * You should override [encodePrimitive] to provide actual encoding logic.
 *
 * Also, you should override [beginStructure] to provide a new instance of [CompositeEncoder]
 */
@OptIn(ExperimentalSerializationApi::class)
public abstract class EncoderTemplate : Encoder {

    /**
     * Encodes a primitive value.
     * Corresponding kind is in [PrimitiveKind].
     */
    public abstract fun encodePrimitive(value: Any)

    override fun encodeBoolean(value: Boolean): Unit = encodePrimitive(value)

    override fun encodeByte(value: Byte): Unit = encodePrimitive(value)

    override fun encodeChar(value: Char): Unit = encodePrimitive(value)

    override fun encodeDouble(value: Double): Unit = encodePrimitive(value)

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int): Unit =
        encodeString(enumDescriptor.getElementName(index))

    override fun encodeFloat(value: Float): Unit = encodePrimitive(value)

    override fun encodeInline(descriptor: SerialDescriptor): EncoderTemplate = this

    override fun encodeInt(value: Int): Unit = encodePrimitive(value)

    override fun encodeLong(value: Long): Unit = encodePrimitive(value)

    override fun encodeShort(value: Short): Unit = encodePrimitive(value)

    override fun encodeString(value: String): Unit = encodePrimitive(value)
}

/**
 * A template for implementing [CompositeEncoder].
 *
 * This class provides default implementations for all `encodeXxxElement` method
 * and redirect to method [encodePrimitiveElement] to encode primitive values.
 * You should override [encodePrimitiveElement] to provide actual encoding logic.
 *
 * Also, you should override [encodeSerializableElement] to provide encoding for embedded serializable objects.
 */
@OptIn(ExperimentalSerializationApi::class)
public abstract class CompositeEncoderTemplate : CompositeEncoder {

    /**
     * Encodes [value] associated with an element
     * at the given [index] in [serial descriptor][descriptor].
     * The element at the given [index] should have [PrimitiveKind] kind.
     */
    public abstract fun encodePrimitiveElement(descriptor: SerialDescriptor, index: Int, value: Any)

    /**
     * Called when calling [encodeNullableSerializableElement] with a `null` value.
     */
    public open fun encodeNull(descriptor: SerialDescriptor, index: Int) {
        // Do nothing.
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // Do nothing.
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value != null) encodeSerializableElement(descriptor, index, serializer, value)
        else encodeNull(descriptor, index)
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        object : EncoderTemplate() {
            override val serializersModule: SerializersModule
                get() = this@CompositeEncoderTemplate.serializersModule

            override fun encodePrimitive(value: Any) {
                encodePrimitiveElement(descriptor, index, value)
            }

            override fun encodeNull() {
                encodeNull(descriptor, index)
            }

            override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
                throw SerializationException("Inline element can't have nested structure")
            }
        }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodePrimitiveElement(descriptor, index, value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodePrimitiveElement(descriptor, index, value)
    }
}