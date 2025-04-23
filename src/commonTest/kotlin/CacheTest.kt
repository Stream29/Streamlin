import io.github.stream29.streamlin.cacheWith
import io.github.stream29.streamlin.globalCached
import io.github.stream29.streamlin.lazy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CacheTest {
    @Test
    fun testCacheWith() {
        // Test cacheWith for non-null values
        val computationCount = mutableMapOf<Int, Int>()
        val cacheMap = mutableMapOf<Int, Int>()

        val cachedFunction = cacheWith(cacheMap) { param ->
            // Count how many times each parameter is computed
            computationCount[param] = (computationCount[param] ?: 0) + 1
            param * 2
        }

        // First call should compute
        assertEquals(2, cachedFunction(1))
        assertEquals(1, computationCount[1])

        // Second call should use cache
        assertEquals(2, cachedFunction(1))
        assertEquals(1, computationCount[1]) // Still 1, not recomputed

        // Different parameter should compute
        assertEquals(4, cachedFunction(2))
        assertEquals(1, computationCount[2])

        // Verify cache contents
        assertEquals(2, cacheMap[1])
        assertEquals(4, cacheMap[2])
    }

    @Test
    fun testCacheWithNullable() {
        // Test cacheWith for nullable values
        val computationCount = mutableMapOf<Int, Int>()
        val cacheMap = mutableMapOf<Int, String?>()

        val cachedFunction = cacheWith(cacheMap) { param ->
            // Count how many times each parameter is computed
            computationCount[param] = (computationCount[param] ?: 0) + 1

            // Return null for even numbers, string for odd
            if (param % 2 == 0) null else "Value $param"
        }

        // Test with value that returns non-null
        assertEquals("Value 1", cachedFunction(1))
        assertEquals(1, computationCount[1])

        // Second call should use cache
        assertEquals("Value 1", cachedFunction(1))
        assertEquals(1, computationCount[1]) // Still 1, not recomputed

        // Test with value that returns null
        assertNull(cachedFunction(2))
        assertEquals(1, computationCount[2])

        // Second call with null should use cache
        assertNull(cachedFunction(2))
        assertEquals(1, computationCount[2]) // Still 1, not recomputed

        // Verify cache contents
        assertEquals("Value 1", cacheMap[1])
        assertNull(cacheMap[2])
        assertTrue(cacheMap.containsKey(2)) // Null is cached
    }

    @Test
    fun testGlobalCached() {
        // Test class for globalCached
        class TestClass(val id: Int) {
            var computationCount = 0

            // Non-null property
            val cachedValue by globalCached {
                computationCount++
                "Value $id"
            }
        }

        val instance1 = TestClass(1)
        val instance2 = TestClass(2)

        // First access should compute
        assertEquals("Value 1", instance1.cachedValue)
        assertEquals(1, instance1.computationCount)

        // Second access should use cache
        assertEquals("Value 1", instance1.cachedValue)
        assertEquals(1, instance1.computationCount) // Still 1, not recomputed

        // Different instance should compute its own value
        assertEquals("Value 2", instance2.cachedValue)
        assertEquals(1, instance2.computationCount)
    }

    @Test
    fun testGlobalCachedNullable() {
        // Test class for globalCached with nullable values
        class TestClass(val id: Int) {
            var computationCount = 0

            // Non-null property that returns different values
            val cachedValue by globalCached {
                computationCount++
                "Value $id"
            }
        }

        val instance1 = TestClass(1)
        val instance2 = TestClass(2)

        // First access should compute
        assertEquals("Value 1", instance1.cachedValue)
        assertEquals(1, instance1.computationCount)

        // Second access should use cache
        assertEquals("Value 1", instance1.cachedValue)
        assertEquals(1, instance1.computationCount) // Still 1, not recomputed

        // Different instance should not compute its own value
        assertEquals("Value 2", instance2.cachedValue)
        assertEquals(1, instance2.computationCount)
    }

    // Test class for lazy
    private class TestClassForLazy(val id: Int) {
        var computationCount = 0

        // Extension property using lazy
        val cachedValue by lazy(cacheMap) {
            computationCount++
            "Value $id"
        }

        companion object {
            val cacheMap = mutableMapOf<TestClassForLazy, String>()
        }
    }

    @Test
    fun testLazy() {
        val instance1 = TestClassForLazy(1)
        val instance2 = TestClassForLazy(2)

        // First access should compute
        assertEquals("Value 1", instance1.cachedValue)
        assertEquals(1, instance1.computationCount)

        // Second access should use cache
        assertEquals("Value 1", instance1.cachedValue)
        assertEquals(1, instance1.computationCount) // Still 1, not recomputed

        // Different instance should compute its own value
        assertEquals("Value 2", instance2.cachedValue)
        assertEquals(1, instance2.computationCount)

        // Verify cache contents
        assertEquals("Value 1", TestClassForLazy.cacheMap[instance1])
        assertEquals("Value 2", TestClassForLazy.cacheMap[instance2])
    }

    // Test class for lazy with nullable values
    private class TestClassForLazyNullable(val id: Int) {
        var computationCount = 0

        // Extension property using lazy - returns null for even IDs
        val cachedNullableValue by lazy(cacheMap) {
            computationCount++
            if (id % 2 == 0) null else "Value $id"
        }

        companion object {
            val cacheMap = mutableMapOf<TestClassForLazyNullable, String?>()
        }
    }

    @Test
    fun testLazyNullable() {
        val instance1 = TestClassForLazyNullable(1) // Will have non-null value
        val instance2 = TestClassForLazyNullable(2) // Will have null value

        // Test with instance that returns non-null
        assertEquals("Value 1", instance1.cachedNullableValue)
        assertEquals(1, instance1.computationCount)

        // Second access should use cache
        assertEquals("Value 1", instance1.cachedNullableValue)
        assertEquals(1, instance1.computationCount) // Still 1, not recomputed

        // Test with instance that returns null
        assertNull(instance2.cachedNullableValue)
        assertEquals(1, instance2.computationCount)

        // Second access should use cache
        assertNull(instance2.cachedNullableValue)
        assertEquals(1, instance2.computationCount) // Still 1, not recomputed

        // Verify cache contents
        assertEquals("Value 1", TestClassForLazyNullable.cacheMap[instance1])
        assertNull(TestClassForLazyNullable.cacheMap[instance2])
        assertTrue(TestClassForLazyNullable.cacheMap.containsKey(instance2)) // Null is cached
    }
}
