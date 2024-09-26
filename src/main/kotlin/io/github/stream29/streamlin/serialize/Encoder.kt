package io.github.stream29.streamlin.serialize

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
class AnyEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : Encoder {
    val record = Record()
    override fun beginStructure(descriptor: SerialDescriptor) =
        when (descriptor.kind) {
            StructureKind.CLASS -> StructureEncoder().also { record.component.add(it.record) }
            StructureKind.MAP -> MapEncoder().also { record.component.add(it.record) }
            else -> throw NotImplementedError("Unsupported descriptor kind: ${descriptor.kind}")
        }

    override fun encodeBoolean(value: Boolean) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeByte(value: Byte) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeChar(value: Char) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeDouble(value: Double) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        record.component.add(PrimitiveValue(index))
    }

    override fun encodeFloat(value: Float) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return this
    }

    override fun encodeInt(value: Int) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeLong(value: Long) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeNull() {
        // Do nothing.
    }

    override fun encodeShort(value: Short) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeString(value: String) {
        record.component.add(PrimitiveValue(value))
    }
}

@ExperimentalSerializationApi
class StructureEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeEncoder {
    val record = StructureValue()

    override fun endStructure(descriptor: SerialDescriptor) {
        // Do nothing.
    }

    private fun encodeElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Any
    ) {
        record.component.add(
            PrimitiveProperty(
                descriptor.getElementName(index),
                value
            )
        )
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

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val encoder = AnyEncoder()
        serializer.serialize(encoder, value)
        println(descriptor)
        record.component.add(
            when {
                descriptor.isInline -> PrimitiveProperty(
                    descriptor.getElementName(index),
                    encoder.record.component[0]
                )

                else -> StructureProperty(
                    descriptor.getElementName(index),
                    encoder.record.component[0] as StructureValue
                )
            }
        )
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

@ExperimentalSerializationApi
class MapEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeEncoder {
    val record = StructureValue()
    private var current: String? = null

    override fun endStructure(descriptor: SerialDescriptor) {
        // Do nothing.
    }

    private fun encodeElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Any
    ) {
        if (current == null) {
            if (value !is String)
                throw SerializationException("Only map with string keys are supported")
            else
                current = value
        } else {
            record.component.add(
                PrimitiveProperty(
                    current as String,
                    value
                )
            )
        }
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        throw SerializationException("There shouldn't be nullable key or value in map")
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        if (current == null)
            if (value is String)
                current = value
            else
                throw SerializationException("Only map with string keys are supported")
        else {
            val encoder = AnyEncoder()
            serializer.serialize(encoder, value)
            val value = encoder.record.component[0]
            record.component.add(
                when(value) {
                    is PrimitiveValue -> PrimitiveProperty(
                        current!!,
                        value.value
                    )

                    is StructureValue -> StructureProperty(
                        current!!,
                        value
                    )
                }
            )
            current = null
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