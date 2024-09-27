package io.github.stream29.streamlin.serialize.transform

import io.github.stream29.streamlin.serialize.template.CompositeDecoderTemplate
import io.github.stream29.streamlin.serialize.template.DecoderTemplate
import io.github.stream29.streamlin.serialize.template.findByName
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
class AnyDecoder(
    private val record: MutableList<Value> = mutableListOf(),
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : DecoderTemplate() {
    override fun decodeNotNullMark(): Boolean =
        record.isNotEmpty() && record[0] !is NullValue

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val structureValue = record[0] as StructureValue
        return when (descriptor.kind) {
            StructureKind.MAP, StructureKind.LIST -> ::ListDecoder
            else -> ::StructureDecoder
        }(structureValue, serializersModule)
    }

    override fun decodePrimitive(): Any =
        record.firstOrNull { it is PrimitiveValue }
            .let { it as PrimitiveValue? }
            ?.value
            ?.let { return it }
            ?: Unit

}


@ExperimentalSerializationApi
open class StructureDecoder(
    record: StructureValue,
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeDecoderTemplate() {

    protected var iterator = record.component.iterator()

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
        return found.value
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
    ): T {
        return deserializer.deserialize(
            AnyDecoder(
                mutableListOf(
                    when (val currentProperty = this.currentProperty) {
                        is PrimitiveProperty -> PrimitiveValue(currentProperty.value)

                        is StructureProperty -> currentProperty.value

                        is NullProperty -> NullValue
                    }
                )
            )
        )
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO()
    }
}

@ExperimentalSerializationApi
class ListDecoder(
    record: StructureValue,
    serializersModule: SerializersModule = EmptySerializersModule()
) : StructureDecoder(record, serializersModule) {
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext()) {
            currentProperty = iterator.next()
            return currentProperty.key.toInt()
        } else {
            return CompositeDecoder.DECODE_DONE
        }
    }
}