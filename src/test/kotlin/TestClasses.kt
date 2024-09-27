import kotlinx.serialization.Serializable

@Serializable
data class TestStructure(
    val name: String = "Stream",
    val age: Map<Int,Int> = mapOf(1 to 2, 3 to 4)
)

@Serializable
data class TestDefault(
    val name: String = "Stream",
    val age: Int = 12
)

@Serializable
data class TestTransform(
    val name: String = "Stream"
)

@Serializable
data class TestNullable(
    val name: String? = null,
    val age: Int? = null
)

@Serializable
sealed interface TestTag

@Serializable
@JvmInline
value class TestInline(val value: Int = 5)

@Serializable
data class TestData(
    val name: String? = "Stream",
    val age: TestInline = TestInline(12)
) : TestTag

@Serializable
data class TestGeneric<T>(
    val property: T
)