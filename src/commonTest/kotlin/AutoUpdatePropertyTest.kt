import io.github.stream29.streamlin.AutoUpdateMode
import io.github.stream29.streamlin.AutoUpdatePropertyRoot
import io.github.stream29.streamlin.getValue
import io.github.stream29.streamlin.proxied
import io.github.stream29.streamlin.setValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AutoUpdatePropertyTest {
    fun testAutoUpdate(rootProperty: AutoUpdatePropertyRoot<Int>) {
        val subProperty1 = rootProperty.subproperty { it * 2 }
        val subProperty2 = subProperty1.subproperty { it * 2 }
        var root by rootProperty
        val sub1 by subProperty1
        val sub2 by subProperty2
        assertFails { println(root) }
        assertFails { println(sub1) }
        assertFails { println(sub2) }
        root = 1
        val subProperty3 = subProperty2.subproperty { it * 2 }
        val sub3 by subProperty3
        assertEquals(1, root)
        assertEquals(2, sub1)
        assertEquals(4, sub2)
        assertEquals(8, sub3)
        root = 2
        assertEquals(2, root)
        assertEquals(4, sub1)
        assertEquals(8, sub2)
        assertEquals(16, sub3)

        // Test proxied property
        val proxiedProperty = rootProperty.proxied(
            rootToProxy = { it * 10 },
            proxyToRoot = { it / 10 }
        )
        var proxiedRoot by proxiedProperty

        // Test that the proxied property reflects the current root value
        assertEquals(20, proxiedRoot)

        // Test that changing the proxied property updates the root property
        proxiedRoot = 30
        assertEquals(3, root)
        assertEquals(6, sub1)
        assertEquals(12, sub2)
        assertEquals(24, sub3)
        assertEquals(30, proxiedRoot)

        // Test that changing the root property updates the proxied property
        root = 4
        assertEquals(4, root)
        assertEquals(8, sub1)
        assertEquals(16, sub2)
        assertEquals(32, sub3)
        assertEquals(40, proxiedRoot)
    }

    @Test
    fun testAll() {
        testAutoUpdate(AutoUpdatePropertyRoot(sync = false, mode = AutoUpdateMode.PROPAGATE))
        testAutoUpdate(AutoUpdatePropertyRoot(sync = false, mode = AutoUpdateMode.LAZY))
    }
}
