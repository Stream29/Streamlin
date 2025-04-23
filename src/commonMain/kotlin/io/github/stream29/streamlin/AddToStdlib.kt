package io.github.stream29.streamlin

import kotlin.jvm.JvmName

/**
 * Filters a value based on a predicate.
 *
 * @param predicate A function that returns true if the value should be kept, false otherwise.
 * @return The value if the predicate returns true, null otherwise.
 */
public inline fun <T : Any> T.filter(crossinline predicate: T.() -> Boolean) =
    if (predicate()) this else null

/**
 * Filters a value based on a predicate.
 *
 * @param predicate A function that returns true if the value should be kept, false otherwise.
 * @return The value if the predicate returns true, null otherwise.
 */
@JvmName("filterNullable")
public inline fun <T : Any> T?.filter(crossinline predicate: T.() -> Boolean) =
    if (this != null && predicate()) this else null

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
