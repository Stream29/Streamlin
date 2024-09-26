package io.github.stream29.streamlin.serialize

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
) : Decoder {
    override fun decodeNotNullMark(): Boolean =
        record.component.isNotEmpty()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val structureValue = record.component[0] as StructureValue
        return when (descriptor.kind) {
            is StructureKind.CLASS -> StructureDecoder(structureValue)
            is StructureKind.MAP -> MapDecoder(structureValue)
            else -> throw NotImplementedError("Unsupported descriptor kind: $descriptor")
        }
    }

    private inline fun <reified T : Any> decodeValue(): T {
        val found = record.component.firstOrNull { it is PrimitiveValue && it.value is T } as PrimitiveValue?
        found?.let { return found.value as T }
            ?: throw MissingFieldException(T::class.simpleName!!, T::class.simpleName!!)
    }

    override fun decodeBoolean(): Boolean = decodeValue()

    override fun decodeByte(): Byte = decodeValue()

    override fun decodeChar(): Char = decodeValue()

    override fun decodeDouble(): Double = decodeValue()

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeValue()

    override fun decodeFloat(): Float = decodeValue()

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    override fun decodeInt(): Int = decodeValue()

    override fun decodeLong(): Long = decodeValue()

    override fun decodeNull(): Nothing? = null

    override fun decodeShort(): Short = decodeValue()

    override fun decodeString(): String = decodeValue()
}


@ExperimentalSerializationApi
class StructureDecoder(
    val record: StructureValue,
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeDecoder {

    private var iterator = record.component.iterator()

    private lateinit var currentProperty: Property

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext()) {
            currentProperty = iterator.next()
            return descriptor.findIndex(currentProperty.key) ?: decodeElementIndex(descriptor)
        } else {
            return CompositeDecoder.DECODE_DONE
        }
    }

    private inline fun <reified T : Any> decodeElement(descriptor: SerialDescriptor, index: Int): T {
        val found = currentProperty as? PrimitiveProperty
            ?: throw MissingFieldException(currentProperty.key, descriptor.serialName)
        return found.value as T
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

    override fun endStructure(descriptor: SerialDescriptor) {
        // Do nothing
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        decodeElement(descriptor, index)

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        decodeElement(descriptor, index)

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decodeElement(descriptor, index)

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        decodeElement(descriptor, index)

    override fun decodeSequentially(): Boolean = false

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        decodeElement(descriptor, index)

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        decodeElement(descriptor, index)

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        decodeElement(descriptor, index)

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decodeElement(descriptor, index)

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        decodeElement(descriptor, index)
}

@ExperimentalSerializationApi
class MapDecoder(
    val record: StructureValue,
    override val serializersModule: SerializersModule = EmptySerializersModule()
) : CompositeDecoder {
    private var iterator = record.component.iterator()

    private lateinit var currentProperty: Property

    private var keyOrValue: Boolean = true

    private var count = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (!iterator.hasNext() && keyOrValue) CompositeDecoder.DECODE_DONE
        else count

    }

    private inline fun <reified T : Any> decodeElement(descriptor: SerialDescriptor, index: Int): T {
        val found = currentProperty as? PrimitiveProperty
            ?: throw MissingFieldException(currentProperty.key, descriptor.serialName)
        return found.value as T
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

    override fun endStructure(descriptor: SerialDescriptor) {
        // Do nothing
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        decodeElement(descriptor, index)

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        decodeElement(descriptor, index)

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decodeElement(descriptor, index)

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        decodeElement(descriptor, index)

    override fun decodeSequentially(): Boolean = false

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        decodeElement(descriptor, index)

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        decodeElement(descriptor, index)

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        decodeElement(descriptor, index)

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decodeElement(descriptor, index)

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        decodeElement(descriptor, index)
}

@ExperimentalSerializationApi
private fun SerialDescriptor.findIndex(serialName: String): Int? {
    for (i in 0..<this.elementsCount) {
        if (getElementName(i) == serialName) {
            return i
        }
    }
    return null
}