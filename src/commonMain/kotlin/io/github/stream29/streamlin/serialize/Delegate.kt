package io.github.stream29.streamlin.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.getValue

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

public inline fun <Original, reified Delegate> DelegatingSerializer(
    noinline fromDelegate: (Delegate) -> Original,
    noinline toDelegate: (Original) -> Delegate,
): DelegatingSerializer<Original, Delegate> =
    DelegatingSerializer(serializer<Delegate>(), fromDelegate, toDelegate)