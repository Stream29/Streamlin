import io.github.stream29.streamlin.DelegatingSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DelegatingSerializerTest {
    @Serializable(with = PointSerializer::class)
    data class Point(val x: Int, val y: Int)

    @Serializable
    data class PointDto(val coords: String)

    object PointSerializer : KSerializer<Point> by DelegatingSerializer(
        fromDelegate = { it: PointDto -> it.coords.split(",").map { it.toInt() }.let { Point(it[0], it[1]) } },
        toDelegate = { PointDto("${it.x},${it.y}") }
    )

    @Test
    fun testSerializerDescriptor() {
        val delegateDescriptor = kotlinx.serialization.serializer<PointDto>().descriptor
        assertEquals(delegateDescriptor, PointSerializer.descriptor)
    }

    @Test
    fun testPointJsonSerialization() {
        val json = Json { prettyPrint = false }
        val point = Point(10, 20)
        val jsonString = json.encodeToString(point)
        val expectedJson = """{"coords":"10,20"}"""
        assertEquals(expectedJson, jsonString)
        val deserializedPoint = json.decodeFromString<Point>(jsonString)
        assertEquals(point, deserializedPoint)
    }
}
