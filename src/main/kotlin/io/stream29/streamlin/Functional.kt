package io.stream29.streamlin

/**
 * Filters a value based on a predicate.
 *
 * @param predicate A function that returns true if the value should be kept, false otherwise.
 * @return The value if the predicate returns true, null otherwise.
 */
inline fun <T : Any> T.filter(crossinline predicate: T.() -> Boolean) =
    if (predicate()) this else null

/**
 * Filters a value based on a predicate.
 *
 * @param predicate A function that returns true if the value should be kept, false otherwise.
 * @return The value if the predicate returns true, null otherwise.
 */
@JvmName("filterNullable")
inline fun <T : Any> T?.filter(crossinline predicate: T.() -> Boolean) =
    if (this != null && predicate()) this else null