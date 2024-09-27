package io.github.stream29.streamlin.serialize.transform

import io.github.stream29.streamlin.serialize.template.CompositeEncoderTemplate
import io.github.stream29.streamlin.serialize.template.EncoderTemplate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
class AnyEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : EncoderTemplate() {
    val record = Record()
    override fun beginStructure(descriptor: SerialDescriptor) =
        when (descriptor.kind) {
            StructureKind.CLASS -> StructureEncoder().also { record.component.add(it.record) }
            StructureKind.MAP -> MapEncoder().also { record.component.add(it.record) }
            PolymorphicKind.SEALED -> StructureEncoder().also { record.component.add(it.record) }
            else -> throw NotImplementedError("Unsupported descriptor kind: ${descriptor.kind}")
        }

    override fun encodePrimitive(value: Any) {
        record.component.add(PrimitiveValue(value))
    }
}

@ExperimentalSerializationApi
class StructureEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeEncoderTemplate() {
    val record = StructureValue()

    override fun encodePrimitiveElement(
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

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val encodedValue = AnyEncoder().also { serializer.serialize(it, value) }.record.component[0]
        record.component.add(
            when (encodedValue) {
                is PrimitiveValue -> PrimitiveProperty(
                    descriptor.getElementName(index),
                    encodedValue.value
                )

                is StructureValue -> StructureProperty(
                    descriptor.getElementName(index),
                    encodedValue
                )
            }
        )
    }
}

@ExperimentalSerializationApi
class MapEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeEncoderTemplate() {
    val record = StructureValue()
    private var current: String? = null

    override fun encodePrimitiveElement(
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

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        if (current == null) {
            if (value is String) {
                current = value
                return
            } else {
                throw SerializationException("Only map with string keys are supported")
            }
        }
        val encodedValue = AnyEncoder().also { serializer.serialize(it, value) }.record.component[0]
        record.component.add(
            when (encodedValue) {
                is PrimitiveValue -> PrimitiveProperty(
                    current!!,
                    encodedValue.value
                )

                is StructureValue -> StructureProperty(
                    current!!,
                    encodedValue
                )
            }
        )
        current = null

    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        throw SerializationException("There shouldn't be nullable key or value in map")
    }
}