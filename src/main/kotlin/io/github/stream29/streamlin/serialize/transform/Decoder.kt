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

@ExperimentalSerializationApi
class AnyDecoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    override val record: Value
) : DecoderTemplate(), ValueContainer {
    override fun decodeNotNullMark(): Boolean =
        record !is NullValue

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val structureValue = record as StructureValue
        return when (descriptor.kind) {
            StructureKind.MAP, StructureKind.LIST -> ::ListDecoder
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


@ExperimentalSerializationApi
open class StructureDecoder(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    final override val record: StructureValue
) : CompositeDecoderTemplate(), ValueContainer {

    protected var iterator: Iterator<Property> = record.iterator()

    protected lateinit var currentProperty: Property

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext()) {
            currentProperty = iterator.next()
            return descriptor.findByName(currentProperty.key) ?: decodeElementIndex(descriptor)
        } else {
            return CompositeDecoder.DECODE_DONE
        }
    }

    override fun decodePrimitiveElement(descriptor: SerialDescriptor, index: Int): Any {
        val found = currentProperty as? PrimitiveProperty
            ?: throw MissingFieldException(currentProperty.key, descriptor.serialName)
        return found.value.value
    }

    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? =
        if (currentProperty is NullProperty) null
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

@ExperimentalSerializationApi
class ListDecoder(
    serializersModule: SerializersModule = EmptySerializersModule(),
    record: StructureValue
) : StructureDecoder(serializersModule, record) {
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext()) {
            currentProperty = iterator.next()
            return currentProperty.key.toInt()
        } else {
            return CompositeDecoder.DECODE_DONE
        }
    }
}