package io.github.stream29.streamlin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.getValue

/**
 * A serializer that delegates serialization and deserialization to another serializer.
 * This is useful when you want to serialize a type using a different representation.
 *
 * @param Original The original type to be serialized/deserialized.
 * @param Delegate The delegate type used for actual serialization/deserialization.
 * @property delegate The serializer for the delegate type.
 * @property fromDelegate A function that converts from the delegate type to the original type.
 * @property toDelegate A function that converts from the original type to the delegate type.
 */
public class DelegatingSerializer<Original, Delegate>(
    private val delegate: KSerializer<Delegate>,
    private val fromDelegate: (Delegate) -> Original,
    private val toDelegate: (Original) -> Delegate,
) : KSerializer<Original> {
    override val descriptor: SerialDescriptor by delegate::descriptor
    override fun serialize(encoder: Encoder, value: Original): Unit =
        delegate.serialize(encoder, toDelegate(value))

    override fun deserialize(decoder: Decoder): Original =
        fromDelegate(delegate.deserialize(decoder))
}

/**
 * Creates a DelegatingSerializer using a reified delegate type.
 * This factory function automatically creates the serializer for the delegate type.
 *
 * @param Original The original type to be serialized/deserialized.
 * @param Delegate The delegate type used for actual serialization/deserialization.
 * @param fromDelegate A function that converts from the delegate type to the original type.
 * @param toDelegate A function that converts from the original type to the delegate type.
 * @return A new DelegatingSerializer that uses the specified conversion functions.
 */
public inline fun <Original, reified Delegate> DelegatingSerializer(
    noinline fromDelegate: (Delegate) -> Original,
    noinline toDelegate: (Original) -> Delegate,
): DelegatingSerializer<Original, Delegate> =
    DelegatingSerializer(serializer<Delegate>(), fromDelegate, toDelegate)
