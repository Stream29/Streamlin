package io.github.stream29.streamlin.serialize.template

import io.github.stream29.streamlin.serialize.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder

@ExperimentalSerializationApi
abstract class EncoderTemplate : Encoder {
    abstract fun encodeAny(value: Any)

    override fun encodeBoolean(value: Boolean) = encodeAny(value)

    override fun encodeByte(value: Byte) = encodeAny(value)

    override fun encodeChar(value: Char) = encodeAny(value)

    override fun encodeDouble(value: Double) = encodeAny(value)

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        encodeString(enumDescriptor.getElementName(index))

    override fun encodeFloat(value: Float) = encodeAny(value)

    override fun encodeInline(descriptor: SerialDescriptor) = this

    override fun encodeInt(value: Int) = encodeAny(value)

    override fun encodeLong(value: Long) = encodeAny(value)

    override fun encodeNull() {
        // Do nothing.
    }

    override fun encodeShort(value: Short) = encodeAny(value)

    override fun encodeString(value: String) = encodeAny(value)
}

@ExperimentalSerializationApi
abstract class CompositeEncoderTemplate: CompositeEncoder {
    abstract fun encodeElement(descriptor: SerialDescriptor, index: Int, value: Any)

    override fun endStructure(descriptor: SerialDescriptor) {
        // Do nothing.
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value != null) {
            encodeSerializableElement(descriptor, index, serializer, value)
        }
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        TODO()
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodeElement(descriptor, index, value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeElement(descriptor, index, value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encodeElement(descriptor, index, value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodeElement(descriptor, index, value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodeElement(descriptor, index, value)
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodeElement(descriptor, index, value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodeElement(descriptor, index, value)
    }


    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeElement(descriptor, index, value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodeElement(descriptor, index, value)
    }
}