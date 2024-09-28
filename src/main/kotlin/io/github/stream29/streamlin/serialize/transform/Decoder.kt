package io.github.stream29.streamlin.serialize.transform

import io.github.stream29.streamlin.serialize.template.CompositeDecoderTemplate
import io.github.stream29.streamlin.serialize.template.DecoderTemplate
import io.github.stream29.streamlin.serialize.template.findByName
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * A decoder that decodes [Value] objects to serializable objects.
 *
 * @param serializersModule The serializers module to use for decoding.
 * @param record The [Value] object that the decoder decodes from.
 */
@ExperimentalSerializationApi
class AnyDecoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    override val record: Value
) : DecoderTemplate(), ValueContainer {
    override fun decodeNotNullMark(): Boolean =
        !record.isNullValue()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val structureValue = record as StructureValue
        return when (descriptor.kind) {
            StructureKind.LIST -> ::ListDecoder
            StructureKind.MAP -> ::MapDecoder
            is PolymorphicKind -> ::TypeTaggedDecoder
            else -> ::StructureDecoder
        }(serializersModule, structureValue)
    }

    override fun decodePrimitive(): Any =
        record.let { it as? PrimitiveValue? }
            ?.value
            ?.let { return it }
            ?: throw SerializationException("Expected a primitive value, but found $record")

}

/**
 * A decoder that decodes [StructureValue] objects to serializable objects.
 *
 * @param serializersModule The serializers module to use for decoding.
 * @param record The [StructureValue] object that the decoder decodes from.
 */
@ExperimentalSerializationApi
open class StructureDecoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    final override val record: StructureValue
) : CompositeDecoderTemplate(), ValueContainer {

    protected open val iterator: Iterator<Property> = record.iterator()

    protected lateinit var currentProperty: Property

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext()) {
            currentProperty = iterator.next()
            val currentSerialName = currentProperty.key.value.toString()
            return descriptor.findByName(currentSerialName) ?: decodeElementIndex(descriptor)
        } else {
            return CompositeDecoder.DECODE_DONE
        }
    }

    override fun decodePrimitiveElement(descriptor: SerialDescriptor, index: Int): Any {
        val found = currentProperty as? PrimitiveProperty
            ?: throw MissingFieldException(currentProperty.key.toString(), descriptor.serialName)
        return found.value.value
            ?: throw SerializationException("Expected $descriptor, but found null at index $index")
    }

    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? =
        if (currentProperty.isNullProperty()) null
        else decodeSerializableElement(descriptor, index, deserializer, previousValue)

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ) = deserializer.deserialize(decodeInlineElement(descriptor, index))

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int) =
        AnyDecoder(
            serializersModule = serializersModule,
            record = currentProperty.value
        )
}

/**
 * A decoder that decodes [StructureValue] objects as [PolymorphicKind].
 * It reads the type tag by the first [PrimitiveProperty] and decodes the object based on the type tag.
 *
 * @param serializersModule The serializers module to use for decoding.
 * @param record The [StructureValue] object that the decoder decodes from.
 */
@ExperimentalSerializationApi
class TypeTaggedDecoder(
    serializersModule: SerializersModule = EmptySerializersModule(),
    record: StructureValue
) : StructureDecoder(serializersModule, record) {
    private var count = 0
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        when (count) {
            0 -> {
                currentProperty = iterator.next()
                return count++
            }

            1 -> return count++
            else -> return CompositeDecoder.DECODE_DONE
        }
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): AnyDecoder {
        return AnyDecoder(serializersModule, StructureValue(iterator.asSequence().toMutableList()))
    }
}

/**
 * A decoder that decodes [StructureValue] objects as [StructureKind.LIST].
 *
 * @param serializersModule The serializers module to use for decoding.
 * @param record The [StructureValue] object that the decoder decodes from.
 */
@ExperimentalSerializationApi
open class ListDecoder(
    serializersModule: SerializersModule = EmptySerializersModule(),
    record: StructureValue
) : StructureDecoder(serializersModule, record) {
    private var count = 0
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext()) {
            currentProperty = iterator.next()
            return count++
        } else {
            return CompositeDecoder.DECODE_DONE
        }
    }
}

/**
 * A decoder that decodes [StructureValue] objects as [StructureKind.MAP].
 *
 * @param serializersModule The serializers module to use for decoding.
 * @param record The [StructureValue] object that the decoder decodes from.
 */
@ExperimentalSerializationApi
class MapDecoder(
    serializersModule: SerializersModule = EmptySerializersModule(),
    record: StructureValue
) : ListDecoder(serializersModule, record) {
    var count = 0
    override val iterator = sequence {
        record.forEach {
            yield(Property(count, it.key))
            count++
            yield(Property(count, it.value))
            count++
        }
    }.iterator()
}