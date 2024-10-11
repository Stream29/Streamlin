package io.github.stream29.streamlin

import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A global cached property.
 *
 * @param provider A function that provides the value of the property.
 * @return A [ReadOnlyProperty] that caches the value of the property.
 */
inline fun <T, V : Any> globalCached(crossinline provider: T.() -> V) =
    object : ReadOnlyProperty<T, V> {
        private var value: V? = null
        override fun getValue(thisRef: T, property: KProperty<*>): V =
            value ?: provider(thisRef).also { value = it }
    }

/**
 * A global cached property that can return null.
 *
 * @param provider A function that provides the value of the property.
 * @return A [ReadOnlyProperty] that caches the value of the property.
 */
@JvmName("globalCachedNullable")
inline fun <T, V : Any> globalCached(crossinline provider: T.() -> V?) =
    object : ReadOnlyProperty<T, V?> {
        private var value: V? = null
        private var initialized = false
        override fun getValue(thisRef: T, property: KProperty<*>): V? =
            if (initialized) value!!
            else provider(thisRef).also { value = it; initialized = true }
    }

/**
 * Creates a new function that caches the result of the original function.
 *
 * @param cacheMap A [MutableMap] that caches the result of the function.
 * @param func A function that contains the logic to compute the value.
 * @return A function that caches the result of the function.
 */
inline fun <T, R : Any> cacheWith(cacheMap: MutableMap<T, R>, crossinline func: (T) -> R): ((T) -> R) =
    { arg: T -> cacheMap.getOrPut(arg) { func(arg) } }

/**
 * Creates a new function that caches the result of the original function.
 * The result can be null. And a null value will be cached.
 *
 * @param cacheMap A [MutableMap] that caches the result of the function.
 * @param func A function that contains the logic to compute the value.
 * @return A function that caches the result of the function.
 */
@JvmName("cacheWithNullable")
inline fun <T, R : Any> cacheWith(cacheMap: MutableMap<T, R?>, crossinline func: (T) -> R?): ((T) -> R?) =
    { arg: T ->
        if (cacheMap.contains(arg)) cacheMap[arg]
        else func(arg).also { cacheMap[arg] = it }
    }

/**
 * A cached extension lazy property that performs like a member lazy property.
 *
 * @param cacheMap A mutable map that stores the cached values.
 * @param provider A function that provides the value of the property.
 * @return A [ReadOnlyProperty] that provides the cached value of the property.
 */
inline fun <T, V : Any> lazy(cacheMap: MutableMap<T, V>, crossinline provider: T.() -> V) =
    cacheWith(cacheMap) { provider(it) }.let {
        ReadOnlyProperty<T, V> { thisRef, _ -> it(thisRef) }
    }

/**
 * A cached extension lazy property that performs like a member lazy property.
 * The result can be null. And a null value will be cached.
 *
 * @param cacheMap A mutable map that stores the cached values.
 * @param provider A function that provides the value of the property.
 * @return A [ReadOnlyProperty] that provides the cached value of the property.
 */
@JvmName("lazyNullable")
inline fun <T, V : Any> lazy(cacheMap: MutableMap<T, V?>, crossinline provider: T.() -> V?) =
    cacheWith(cacheMap) { provider(it) }.let {
        ReadOnlyProperty<T, V?> { thisRef, _ -> it(thisRef) }
    }