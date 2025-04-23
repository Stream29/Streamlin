package io.github.stream29.streamlin

/**
 * Represents an uninitialized value in an AutoUpdateProperty.
 * Used internally to indicate that a property has not been initialized yet.
 */
public data object UninitializedValue

/**
 * Defines the update mode for AutoUpdateProperty.
 */
public enum class AutoUpdateMode {
    /**
     * Lazy mode: Values are updated only when accessed.
     */
    LAZY,

    /**
     * Propagate mode: Updates are propagated to all dependent properties immediately.
     */
    PROPAGATE
}

/**
 * Creates a new uninitialized AutoUpdatePropertyRoot.
 *
 * @param T The type of the property value.
 * @param sync Whether the property should be thread-safe.
 * @param mode The update mode of the property.
 * @return A new uninitialized AutoUpdatePropertyRoot.
 */
public expect fun <T> AutoUpdatePropertyRoot(
    sync: Boolean = true,
    mode: AutoUpdateMode = AutoUpdateMode.PROPAGATE,
): AutoUpdatePropertyRoot<T>

/**
 * Creates a new initialized AutoUpdatePropertyRoot with the given initial value.
 *
 * @param T The type of the property value.
 * @param sync Whether the property should be thread-safe.
 * @param mode The update mode of the property.
 * @param initValue The initial value of the property.
 * @return A new initialized AutoUpdatePropertyRoot.
 */
public expect fun <T> AutoUpdatePropertyRoot(
    sync: Boolean = true,
    mode: AutoUpdateMode = AutoUpdateMode.PROPAGATE,
    initValue: T,
): AutoUpdatePropertyRoot<T>

/**
 * Creates a proxy for this AutoUpdatePropertyRoot.
 * The proxy allows working with a different type while maintaining the update functionality.
 *
 * @param Root The type of the original property value.
 * @param Proxy The type of the proxy property value.
 * @param rootToProxy A function that converts from the root type to the proxy type.
 * @param proxyToRoot A function that converts from the proxy type to the root type.
 * @return A new AutoUpdatePropertyRoot that proxies this property.
 */
public fun <Root, Proxy> AutoUpdatePropertyRoot<Root>.proxied(
    rootToProxy: (Root) -> Proxy,
    proxyToRoot: (Proxy) -> Root,
): AutoUpdatePropertyRoot<Proxy> =
    ProxiedAutoUpdatePropertyRoot(this, rootToProxy, proxyToRoot)

/**
 * Operator function that allows using AutoUpdateProperty as a property delegate for read-only properties.
 *
 * @param T The type of the property value.
 * @param thisRef The object containing the delegated property.
 * @param property The metadata for the delegated property.
 * @return The current value of the property.
 */
public operator fun <T> AutoUpdateProperty<T>.getValue(thisRef: Any?, property: Any?) = get()

/**
 * Operator function that allows using AutoUpdatePropertyRoot as a property delegate for read-write properties.
 *
 * @param T The type of the property value.
 * @param thisRef The object containing the delegated property.
 * @param property The metadata for the delegated property.
 * @param value The new value to set.
 */
public operator fun <T> AutoUpdatePropertyRoot<T>.setValue(thisRef: Any?, property: Any?, value: T) = set(value)

/**
 * Interface for a property that automatically updates its value when its dependencies change.
 *
 * @param T The type of the property value.
 */
public interface AutoUpdateProperty<T> {
    /**
     * Creates a subproperty that depends on this property.
     * When this property changes, the subproperty will be updated accordingly.
     *
     * @param transform A function that transforms the value of this property to the value of the subproperty.
     * @return A new AutoUpdateProperty that depends on this property.
     */
    public fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R>

    /**
     * Gets the current value of the property.
     *
     * @return The current value of the property.
     * @throws IllegalStateException if the property is not initialized.
     */
    public fun get(): T
}

/**
 * Interface for a root property that can be set directly.
 * Changes to the root property will propagate to all dependent properties.
 *
 * @param T The type of the property value.
 */
public interface AutoUpdatePropertyRoot<T> : AutoUpdateProperty<T> {
    /**
     * Sets the value of the property.
     * This will trigger updates to all dependent properties.
     *
     * @param value The new value of the property.
     */
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
