package io.stream29.streamlin

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <T, V : Any> globalCached(crossinline provider: T.() -> V) =
    object : ReadOnlyProperty<T, V> {
        private var value: V? = null
        override fun getValue(thisRef: T, property: KProperty<*>): V =
            value ?: provider(thisRef).also { value = it }
    }

@JvmName("globalCachedNullable")
inline fun <T, V : Any> globalCached(crossinline provider: T.() -> V?) =
    object : ReadOnlyProperty<T, V?> {
        private var value: V? = null
        private var initialized = false
        override fun getValue(thisRef: T, property: KProperty<*>): V? =
            if (initialized) value!!
            else provider(thisRef).also { value = it; initialized = true }
    }

inline fun <T, R : Any> cacheWith(cacheMap: MutableMap<T, R>, crossinline func: (T) -> R): ((T) -> R) =
    { arg: T -> cacheMap.getOrPut(arg) { func(arg) } }

@JvmName("cacheWithNullable")
inline fun <T, R : Any> cacheWith(cacheMap: MutableMap<T, R?>, crossinline func: (T) -> R?): ((T) -> R?) =
    { arg: T ->
        if (cacheMap.contains(arg)) cacheMap[arg]
        else func(arg).also { cacheMap[arg] = it }
    }

inline fun <T, V : Any> lazy(cacheMap: MutableMap<T, V>, crossinline provider: T.() -> V) =
    cacheWith(cacheMap) { provider(it) }.let {
        ReadOnlyProperty<T, V> { thisRef, _ -> it(thisRef) }
    }

@JvmName("lazyNullable")
inline fun <T, V : Any> lazy(cacheMap: MutableMap<T, V?>, crossinline provider: T.() -> V?) =
    cacheWith(cacheMap) { provider(it) }.let {
        ReadOnlyProperty<T, V?> { thisRef, _ -> it(thisRef) }
    }