package io.github.stream29.streamlin

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.modules.SerializersModule

/**
 * Storage an instance with its [KType].
 * Because `Streamlin` already make [KType] [Serializable], this class is
 * also [Serializable].
 *
 * This class allows you to store a set of [Serializable] types together
 * without creating a common interface for them and add them to [SerializersModule]
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = WithKTypeDeserializer::class)
public data class WithKType(
    @Serializable(with = KTypeSerializer::class)
    val type: KType,
    @Contextual
    val value: Any?,
)

/**
 * Creates a [WithKType] instance with the specified [value] and [KType].
 *
 * @param value The value to be stored.
 * @param T The type of the value.
 * @return A [WithKType] instance with the specified [value] and [KType].
 */
@OptIn(ExperimentalSerializationApi::class)
public inline fun <reified T> WithKType(value: T): WithKType =
    WithKType(typeOf<T>(), value)

/**
 * A [KSerializer] for [KType].
 *
 * This serializer delegates the serialization and deserialization to [SKType], where `s` means [Serializable]
 */
public object KTypeSerializer :
    KSerializer<KType> by DelegatingSerializer<KType, SKType>(
        fromDelegate = { it.deserialize() },
        toDelegate = { it.serializable() }
    )

internal object WithKTypeDeserializer : KSerializer<WithKType> {
    private val generatedSerializer = WithKType.generatedSerializer()
    override val descriptor: SerialDescriptor get() = generatedSerializer.descriptor
    override fun serialize(
        encoder: Encoder,
        value: WithKType
    ) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(
                descriptor = descriptor,
                index = 0,
                serializer = KTypeSerializer,
                value = value.type
            )
            encodeSerializableElement(
                descriptor = generatedSerializer.descriptor,
                index = 1,
                serializer = serializer(value.type),
                value = value.value,
            )
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): WithKType {
        return decoder.decodeStructure(descriptor) {
            check(decodeElementIndex(descriptor) == 0) { "No type discriminator found" }
            val type = decodeSerializableElement(
                descriptor = descriptor.getElementDescriptor(0),
                index = 0,
                deserializer = KTypeSerializer
            )
            val deserializer = serializer(type)
            check(decodeElementIndex(descriptor) == 1) { "Unexpected end of input after type discriminator" }
            @Suppress("UNCHECKED_CAST")
            WithKType(
                type,
                decodeSerializableElement(
                    descriptor = deserializer.descriptor,
                    index = 1,
                    deserializer = deserializer,
                )
            )
        }
    }
}
