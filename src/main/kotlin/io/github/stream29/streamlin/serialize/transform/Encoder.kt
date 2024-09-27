package io.github.stream29.streamlin.serialize.transform

import io.github.stream29.streamlin.serialize.template.CompositeEncoderTemplate
import io.github.stream29.streamlin.serialize.template.EncoderTemplate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
class AnyEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    val config: TransformEncodeConfig = TransformEncodeConfig()
) : EncoderTemplate(), ValueContainer {
    private var _record: Value? = null
    override val record
        get() = _record ?: throw SerializationException("No value has been encoded yet")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoderTemplate {
        val compositeEncoder = StructureEncoder(serializersModule, config)
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
class StructureEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    private val config: TransformEncodeConfig = TransformEncodeConfig()
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
        val encodedValue =
            AnyEncoder(serializersModule, config).also { serializer.serialize(it, value) }.record
        val elementName = descriptor.getElementName(index)
        this.record.add(
            when (encodedValue) {
                is PrimitiveValue -> PrimitiveProperty(elementName, encodedValue.value)
                is StructureValue -> StructureProperty(elementName, encodedValue)
                is NullValue -> NullProperty(elementName)
            }
        )
    }

    override fun encodeNull(descriptor: SerialDescriptor, index: Int) {
        if (config.encodeNull)
            record.add(NullProperty(descriptor.getElementName(index)))
    }
}