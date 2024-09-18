package io.stream29.streamlin

inline fun <T : Any> T.filter(crossinline predicate: T.() -> Boolean) =
    if (predicate()) this else null

@JvmName("filterNullable")
inline fun <T : Any> T?.filter(crossinline predicate: T.() -> Boolean) =
    if (this != null && predicate()) this else null