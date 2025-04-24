import io.github.stream29.streamlin.cast
import io.github.stream29.streamlin.safeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AddToStdlibTest {

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