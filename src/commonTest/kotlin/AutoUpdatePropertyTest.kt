import io.github.stream29.streamlin.AutoUpdateMode
import io.github.stream29.streamlin.AutoUpdatePropertyRoot
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
        assertEquals(1, root)
        assertEquals(2, sub1)
        assertEquals(4, sub2)
        root = 2
        assertEquals(2, root)
        assertEquals(4, sub1)
        assertEquals(8, sub2)
    }

    @Test
    fun testAll() {
        testAutoUpdate(AutoUpdatePropertyRoot(sync = false, mode = AutoUpdateMode.PROPAGATE))
        testAutoUpdate(AutoUpdatePropertyRoot(sync = false, mode = AutoUpdateMode.LAZY))
    }
}