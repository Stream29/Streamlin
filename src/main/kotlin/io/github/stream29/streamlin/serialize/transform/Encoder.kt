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
sealed interface ValueContainer {
    val value: Value
}

@ExperimentalSerializationApi
class AnyEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    val config: TransformEncodeConfig = TransformEncodeConfig()
) : EncoderTemplate() {
    val record = Record()
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoderTemplate {
        val compositeEncoder = when (descriptor.kind) {
            StructureKind.CLASS -> ::StructureEncoder
            StructureKind.MAP -> ::MapEncoder
            PolymorphicKind.SEALED -> ::StructureEncoder
            StructureKind.LIST -> ::StructureEncoder
            StructureKind.OBJECT -> ::StructureEncoder
            PolymorphicKind.OPEN -> TODO()
            SerialKind.ENUM -> TODO()
            is PrimitiveKind -> throw SerializationException("PrimitiveKind is not structure")
            SerialKind.CONTEXTUAL -> TODO()
        }(serializersModule, config)
        record.component.add(compositeEncoder.value)
        return compositeEncoder
    }

    override fun encodePrimitive(value: Any) {
        record.component.add(PrimitiveValue(value))
    }

    override fun encodeNull() {
        if (config.encodeNull)
            record.component.add(NullValue)
    }
}

@ExperimentalSerializationApi
class StructureEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    val config: TransformEncodeConfig = TransformEncodeConfig()
) : CompositeEncoderTemplate(), ValueContainer {
    override val value = StructureValue()

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean =
        config.encodeDefault

    override fun encodePrimitiveElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Any
    ) {
        this.value.component.add(
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
            AnyEncoder(serializersModule, config).also { serializer.serialize(it, value) }.record.component[0]
        this.value.component.add(
            when (encodedValue) {
                is PrimitiveValue -> PrimitiveProperty(
                    descriptor.getElementName(index),
                    encodedValue.value
                )

                is StructureValue -> StructureProperty(
                    descriptor.getElementName(index),
                    encodedValue
                )

                is NullValue -> NullProperty(
                    descriptor.getElementName(index)
                )
            }
        )
    }

    override fun encodeNull(descriptor: SerialDescriptor, index: Int) {
        if (config.encodeNull)
            value.component.add(NullProperty(descriptor.getElementName(index)))
    }
}

@ExperimentalSerializationApi
class MapEncoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    val config: TransformEncodeConfig = TransformEncodeConfig()
) : CompositeEncoderTemplate(), ValueContainer {
    override val value = StructureValue()
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
            this.value.component.add(
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
        val encodedValue =
            AnyEncoder(serializersModule, config).also { serializer.serialize(it, value) }.record.component[0]
        this.value.component.add(
            when (encodedValue) {
                is PrimitiveValue -> PrimitiveProperty(
                    current!!,
                    encodedValue.value
                )

                is StructureValue -> StructureProperty(
                    current!!,
                    encodedValue
                )

                is NullValue -> NullProperty(
                    current!!
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

    override fun encodeNull(descriptor: SerialDescriptor, index: Int) {
        if (config.encodeNull)
            value.component.add(NullProperty(descriptor.getElementName(index)))
    }
}