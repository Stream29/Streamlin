import io.github.stream29.streamlin.cast
import io.github.stream29.streamlin.filter
import io.github.stream29.streamlin.safeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class AddToStdlibTest {
    @Test
    fun testFilter() {
        // Test filter on non-null value
        val value = "test"
        assertEquals("test", value.filter { length > 3 })
        assertNull(value.filter { length > 4 })
        
        // Test filter on nullable value
        val nullableValue: String? = "test"
        assertEquals("test", nullableValue.filter { length > 3 })
        assertNull(nullableValue.filter { length > 4 })
        
        // Test filter on null value
        val nullValue: String? = null
        assertNull(nullValue.filter { length > 0 })
    }
    
    @Test
    fun testCast() {
        // Test successful cast
        val any: Any = "test"
        val string: String = any.cast()
        assertEquals("test", string)
        
        // Test failed cast
        val number: Any = 123
        assertFailsWith<ClassCastException> {
            number.cast<String>()
        }
    }
    
    @Test
    fun testSafeCast() {
        // Test successful safe cast
        val any: Any = "test"
        val string: String? = any.safeCast()
        assertEquals("test", string)
        
        // Test failed safe cast
        val number: Any = 123
        val failedCast: String? = number.safeCast()
        assertNull(failedCast)
    }
}