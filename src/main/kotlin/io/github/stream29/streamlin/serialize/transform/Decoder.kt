package io.github.stream29.streamlin.serialize.transform

import io.github.stream29.streamlin.serialize.template.CompositeDecoderTemplate
import io.github.stream29.streamlin.serialize.template.DecoderTemplate
import io.github.stream29.streamlin.serialize.template.findByName
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
class AnyDecoder(
    val record: Record,
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : DecoderTemplate() {
    override fun decodeNotNullMark(): Boolean =
        record.component.isNotEmpty()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val structureValue = record.component[0] as StructureValue
        return when (descriptor.kind) {
            is StructureKind.CLASS -> StructureDecoder(structureValue)
            is StructureKind.MAP -> MapDecoder(structureValue)
            else -> throw NotImplementedError("Unsupported descriptor kind: ${descriptor.kind}")
        }
    }

    override fun decodePrimitive(): Any =
        record.component
            .firstOrNull { it is PrimitiveValue }
            .let { it as PrimitiveValue? }
            ?.value
            ?.let { return it }
            ?: Unit

}


@ExperimentalSerializationApi
class StructureDecoder(
    val record: StructureValue,
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeDecoderTemplate() {

    private var iterator = record.component.iterator()

    private lateinit var currentProperty: Property

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
    ): T? {
        val found = currentProperty as? StructureProperty
        return found?.let {
            deserializer.deserialize(AnyDecoder(Record(mutableListOf(found.value))))
        }
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        return when (val currentProperty = this.currentProperty) {
            is PrimitiveProperty -> {
                deserializer.deserialize(
                    AnyDecoder(
                        Record(
                            mutableListOf(
                                PrimitiveValue(
                                    currentProperty.value
                                )
                            )
                        )
                    )
                )
            }

            is StructureProperty -> {
                deserializer.deserialize(
                    AnyDecoder(
                        Record(
                            mutableListOf(
                                currentProperty.value
                            )
                        )
                    )
                )
            }
        }
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO()
    }
}

@ExperimentalSerializationApi
class MapDecoder(
    val record: StructureValue,
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeDecoderTemplate() {
    private var iterator = record.component.iterator()

    private lateinit var currentProperty: Property

    private var keyOrValue: Boolean = true

    private var count = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (!iterator.hasNext() && keyOrValue) CompositeDecoder.DECODE_DONE
        else count

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
    ): T? {
        val found = currentProperty as? StructureProperty
        return found?.let {
            deserializer.deserialize(AnyDecoder(Record(mutableListOf(found.value))))
        }
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        if (keyOrValue) {
            currentProperty = iterator.next()
            keyOrValue = false
            count++
            return currentProperty.key as T
        } else {
            keyOrValue = true
            val currentProperty = this.currentProperty
            count++
            return when (currentProperty) {
                is PrimitiveProperty -> {
                    deserializer.deserialize(
                        AnyDecoder(
                            Record(
                                mutableListOf(
                                    PrimitiveValue(
                                        currentProperty.value
                                    )
                                )
                            )
                        )
                    )
                }

                is StructureProperty -> {
                    deserializer.deserialize(
                        AnyDecoder(
                            Record(
                                mutableListOf(
                                    currentProperty.value
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO()
    }

    override fun decodeSequentially(): Boolean = false
}