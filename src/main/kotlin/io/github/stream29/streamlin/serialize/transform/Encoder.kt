package io.github.stream29.streamlin.serialize.transform

import io.github.stream29.streamlin.serialize.template.CompositeEncoderTemplate
import io.github.stream29.streamlin.serialize.template.EncoderTemplate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
class AnyEncoder(
    override val serializersModule: SerializersModule,
    private val config: TransformConfiguration
) : EncoderTemplate(), ValueContainer {
    private var _record: Value? = null
    override val record
        get() = _record ?: throw SerializationException("No value has been encoded yet")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoderTemplate {
        val compositeEncoder = when (descriptor.kind) {
            is PolymorphicKind -> TypeTaggedEncoder(serializersModule, config)
            else -> StructureEncoder(serializersModule, config)
        }
        _record = compositeEncoder.record
        return compositeEncoder
    }

    override fun encodePrimitive(value: Any) {
        _record = PrimitiveValue(value)
    }

    override fun encodeNull() {
        _record = NullValue
    }
}

@ExperimentalSerializationApi
open class StructureEncoder(
    final override val serializersModule: SerializersModule,
    private val config: TransformConfiguration
) : CompositeEncoderTemplate(), ValueContainer {
    override val record = StructureValue()

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean =
        config.encodeDefault

    override fun encodePrimitiveElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Any
    ) {
        this.record.add(
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
        val encodedValue = AnyEncoder(serializersModule, config).also { serializer.serialize(it, value) }.record
        this.record.add(encodedValue.named(descriptor.getElementName(index)))
    }

    override fun encodeNull(descriptor: SerialDescriptor, index: Int) {
        if (config.encodeNull)
            record.add(NullProperty(descriptor.getElementName(index)))
    }
}

@ExperimentalSerializationApi
class TypeTaggedEncoder(
    override val serializersModule: SerializersModule,
    private val config: TransformConfiguration
) : CompositeEncoderTemplate(), ValueContainer {
    private val nestedEncoder = StructureEncoder(serializersModule, config)

    override val record by nestedEncoder::record

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean =
        config.encodeDefault

    override fun encodePrimitiveElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Any
    ) = nestedEncoder.encodePrimitiveElement(descriptor, index, value)

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val encodedValues =
            AnyEncoder(serializersModule, config)
            .also { serializer.serialize(it, value) }
            .record
            .let { it as StructureValue }.component
        this.record.addAll(encodedValues)
    }

    override fun encodeNull(descriptor: SerialDescriptor, index: Int) = nestedEncoder.encodeNull(descriptor, index)
}