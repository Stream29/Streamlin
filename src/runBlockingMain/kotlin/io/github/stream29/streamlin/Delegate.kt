package io.github.stream29.streamlin

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

public actual fun <T> AutoUpdatePropertyRoot(
    sync: Boolean,
    mode: AutoUpdateMode
): AutoUpdatePropertyRoot<T> = when (mode) {
    AutoUpdateMode.LAZY ->
        if (sync) SyncLazyAutoUpdatePropertyRoot()
        else UnsafeLazyAutoUpdatePropertyRoot()

    AutoUpdateMode.PROPAGATE ->
        if (sync) SyncPropagateAutoUpdatePropertyRoot()
        else UnsafePropagateAutoUpdatePropertyRoot()
}

public actual fun <T> AutoUpdatePropertyRoot(
    sync: Boolean,
    mode: AutoUpdateMode,
    initValue: T
): AutoUpdatePropertyRoot<T> = when (mode) {
    AutoUpdateMode.LAZY ->
        if (sync) SyncLazyAutoUpdatePropertyRoot(initValue)
        else UnsafeLazyAutoUpdatePropertyRoot(initValue)

    AutoUpdateMode.PROPAGATE ->
        if (sync) SyncPropagateAutoUpdatePropertyRoot(initValue)
        else UnsafePropagateAutoUpdatePropertyRoot(initValue)
}

internal class SyncLazyAutoUpdatePropertyRoot<T>(
    @Volatile
    private var value: Any? = UninitializedValue,
) : AutoUpdatePropertyRoot<T> {
    internal val version = atomic(0)

    override operator fun setValue(thisRef: Any?, property: Any?, value: T): Unit {
        this.value = value
        version.incrementAndGet()
    }

    override operator fun getValue(thisRef: Any?, property: Any?): T {
        if (value === UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R> =
        SyncLazyAutoUpdatePropertyNode(this, transform)
}

internal class SyncLazyAutoUpdatePropertyNode<T, V>(
    private val root: SyncLazyAutoUpdatePropertyRoot<T>,
    private val transform: (T) -> V,
) : AutoUpdateProperty<V> {
    @Volatile
    private var version = -1

    @Volatile
    private var value: V? = null
    private val mutex = Mutex()

    override operator fun getValue(thisRef: Any?, property: Any?): V {
        runBlocking {
            mutex.withLock {
                if (root.version.value != version) {
                    value = transform(root.getValue(null, null))
                    version = root.version.value
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    override fun <R> subproperty(transform: (V) -> R): AutoUpdateProperty<R> =
        SyncLazyAutoUpdatePropertyNode(root) { transform(this.transform(it)) }
}

internal class SyncPropagateAutoUpdatePropertyRoot<T>(
    @Volatile
    private var value: Any? = UninitializedValue
) : AutoUpdatePropertyRoot<T> {
    private val subpropertyList = mutableListOf<SyncPropagateAutoUpdatePropertyNode<T, *>>()
    private val mutex = Mutex()

    override operator fun setValue(thisRef: Any?, property: Any?, value: T): Unit {
        runBlocking {
            mutex.withLock {
                this@SyncPropagateAutoUpdatePropertyRoot.value = value
                subpropertyList.forEach { it.value = it.transform(value) }
            }
        }
    }

    override operator fun getValue(thisRef: Any?, property: Any?): T {
        if (value == UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R> = runBlocking {
        mutex.withLock {
            SyncPropagateAutoUpdatePropertyNode(this@SyncPropagateAutoUpdatePropertyRoot, transform)
                .also { subpropertyList.add(it) }
        }
    }

}

internal class SyncPropagateAutoUpdatePropertyNode<T, V>(
    private val root: SyncPropagateAutoUpdatePropertyRoot<T>,
    internal val transform: (T) -> V,
) : AutoUpdateProperty<V> {
    @Volatile
    internal var value: Any? = UninitializedValue

    override operator fun getValue(thisRef: Any?, property: Any?): V {
        if (value === UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    override fun <R> subproperty(transform: (V) -> R): AutoUpdateProperty<R> =
        root.subproperty { transform(this.transform(it)) }
}