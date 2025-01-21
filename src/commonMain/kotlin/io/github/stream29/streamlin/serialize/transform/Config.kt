package io.github.stream29.streamlin.serialize.transform

/**
 * The configuration to use for encoding and decoding.
 *
 * @property encodeDefault Whether to encode default values.
 * @property encodeNull Whether to encode null values.
 */
public data class TransformConfiguration(
    var encodeDefault: Boolean = true,
    var encodeNull: Boolean = false
)