package io.github.stream29.streamlin

/**
 * Casts the receiver to the specified type.
 *
 * @return The receiver cast to type T.
 * @throws ClassCastException if the receiver is not of type T.
 */
public inline fun <reified T> Any?.cast() = this as T

/**
 * Safely casts the receiver to the specified type.
 *
 * @return The receiver cast to type T, or null if the receiver is not of type T.
 */
public inline fun <reified T> Any?.safeCast() = this as? T
