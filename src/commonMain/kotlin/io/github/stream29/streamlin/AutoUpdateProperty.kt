package io.github.stream29.streamlin

public data object UninitializedValue

public enum class AutoUpdateMode {
    LAZY,
    PROPAGATE
}

public expect fun <T> AutoUpdatePropertyRoot(
    sync: Boolean = true,
    mode: AutoUpdateMode = AutoUpdateMode.PROPAGATE,
): AutoUpdatePropertyRoot<T>

public expect fun <T> AutoUpdatePropertyRoot(
    sync: Boolean = true,
    mode: AutoUpdateMode = AutoUpdateMode.PROPAGATE,
    initValue: T,
): AutoUpdatePropertyRoot<T>

public fun <Root, Proxy> AutoUpdatePropertyRoot<Root>.proxied(
    rootToProxy: (Root) -> Proxy,
    proxyToRoot: (Proxy) -> Root,
): AutoUpdatePropertyRoot<Proxy> =
    ProxiedAutoUpdatePropertyRoot(this, rootToProxy, proxyToRoot)

public operator fun <T> AutoUpdateProperty<T>.getValue(thisRef: Any?, property: Any?) = get()

public operator fun <T> AutoUpdatePropertyRoot<T>.setValue(thisRef: Any?, property: Any?, value: T) = set(value)

public interface AutoUpdateProperty<T> {
    public fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R>
    public fun get(): T
}

public interface AutoUpdatePropertyRoot<T> : AutoUpdateProperty<T> {
    public fun set(value: T)
}

internal class ProxiedAutoUpdatePropertyRoot<Root, Proxy>(
    val root: AutoUpdatePropertyRoot<Root>,
    val rootToProxy: (Root) -> Proxy,
    val proxyToRoot: (Proxy) -> Root,
) : AutoUpdateProperty<Proxy> by root.subproperty(rootToProxy),
    AutoUpdatePropertyRoot<Proxy> {
    override fun set(value: Proxy) {
        root.set(proxyToRoot(value))
    }
}

internal class UnsafeLazyAutoUpdatePropertyRoot<T>(
    private var value: Any? = UninitializedValue,
) : AutoUpdatePropertyRoot<T> {
    internal var version: Int = 0
        private set

    override fun set(value: T) {
        this.value = value
        version++
    }

    override fun get(): T {
        if (value === UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R> =
        UnsafeLazyAutoUpdatePropertyNode(this, transform)
}

internal class UnsafeLazyAutoUpdatePropertyNode<T, V>(
    private val root: UnsafeLazyAutoUpdatePropertyRoot<T>,
    private val transform: (T) -> V,
) : AutoUpdateProperty<V> {
    private var version: Int = -1
    private var value: V? = null

    override fun get(): V {
        if (root.version != version) {
            value = transform(root.getValue(null, null))
            version = root.version
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    override fun <R> subproperty(transform: (V) -> R): AutoUpdateProperty<R> =
        UnsafeLazyAutoUpdatePropertyNode(root) { transform(this.transform(it)) }
}

internal class UnsafePropagateAutoUpdatePropertyRoot<T>(
    private var value: Any? = UninitializedValue
) : AutoUpdatePropertyRoot<T> {
    private val subpropertyList = mutableListOf<UnsafePropagateAutoUpdatePropertyNode<T, *>>()

    override fun set(value: T) {
        this.value = value
        subpropertyList.forEach { it.value = it.transform(value) }
    }

    override fun get(): T {
        if (value == UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R> =
        UnsafePropagateAutoUpdatePropertyNode(
            this,
            transform,
            @Suppress("UNCHECKED_CAST")
            if (value === UninitializedValue) UninitializedValue else transform(value as T)
        ).also { subpropertyList.add(it) }
}

internal class UnsafePropagateAutoUpdatePropertyNode<T, V>(
    private val root: UnsafePropagateAutoUpdatePropertyRoot<T>,
    internal val transform: (T) -> V,
    internal var value: Any? = UninitializedValue
) : AutoUpdateProperty<V> {
    override fun get(): V {
        if (value === UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    override fun <R> subproperty(transform: (V) -> R): AutoUpdateProperty<R> =
        root.subproperty { transform(this.transform(it)) }
}