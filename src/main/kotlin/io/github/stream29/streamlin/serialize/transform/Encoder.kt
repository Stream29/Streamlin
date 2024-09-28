package io.github.stream29.streamlin.serialize.transform

import io.github.stream29.streamlin.serialize.template.CompositeEncoderTemplate
import io.github.stream29.streamlin.serialize.template.EncoderTemplate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.SerializersModule

/**
 * An encoder that encodes serializable objects to a [Value] object.
 *
 * @param serializersModule The serializers module to use for encoding.
 * @param config The configuration to use for encoding.
 *
 * @property record The [Value] object that the encoder encodes to.
 */
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
            is PolymorphicKind -> ::TypeTaggedEncoder
            is StructureKind.MAP -> ::MapEncoder
            else -> ::StructureEncoder
        }(serializersModule, config)
        _record = compositeEncoder.record
        return compositeEncoder
    }

    override fun encodePrimitive(value: Any) {
        _record = PrimitiveValue(value)
    }

    override fun encodeNull() {
        _record = PrimitiveValue(null)
    }
}

/**
 * A composite encoder that encodes structures to a [StructureValue] object.
 *
 * @param serializersModule The serializers module to use for encoding.
 * @param config The configuration to use for encoding.
 *
 * @property record The [StructureValue] object that the encoder encodes to.
 */
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
                PrimitiveValue(descriptor.getElementName(index)),
                PrimitiveValue(value)
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
        this.record.add(Property(descriptor.getElementName(index),encodedValue))
    }

    override fun encodeNull(descriptor: SerialDescriptor, index: Int) {
        if (config.encodeNull)
            record.add(PrimitiveProperty.of(descriptor.getElementName(index),null))
    }
}

/**
 * A composite encoder that encodes maps to a [StructureValue] object.
 * It treats the elements as key-value pairs and encodes them as a [StructureValue].
 *
 * @param serializersModule The serializers module to use for encoding.
 * @param config The configuration to use for encoding.
 */
@ExperimentalSerializationApi
class MapEncoder(
    serializersModule: SerializersModule,
    config: TransformConfiguration
) : StructureEncoder(serializersModule, config) {
    override val record = StructureValue(ToMapList())
}

/**
 * A list that writes like a list but reads like a map.
 * When writing, it receives a [Property] and treats it as a map key or value.
 * Every 2 properties, the first is used as the key and the second as the value, consisting the result property.
 * Then it inserts the property into the list.
 *
 * @param container The underlying list to store properties.
 */
private class ToMapList(
    private val container: MutableList<Property> = mutableListOf()
) : MutableList<Property> by container {
    var key: Any? = null
    override fun add(element: Property): Boolean {
        if (key == null) {
            if (element is PrimitiveProperty) key = element.value.value
            else throw SerializationException("Key must be a primitive type")
        } else {
            container.add(Property(key,element.value))
            key = null
        }
        return true
    }

    override fun toString(): String {
        return container.toString()
    }
}

/**
 * A composite encoder that encodes serializable polymorphic objects to a [StructureValue] object.
 * It uses the type tag to record the type of the object to be encoded.
 * The type tag is encoded as a [PrimitiveProperty] with the key "type", as the first property of the [record].
 *
 * @param serializersModule The serializers module to use for encoding.
 * @param config The configuration to use for encoding.
 */
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