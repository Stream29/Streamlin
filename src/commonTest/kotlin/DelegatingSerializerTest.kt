import io.github.stream29.streamlin.DelegatingSerializer
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class DelegatingSerializerTest {
    // Original data class that we want to serialize
    data class Point(val x: Int, val y: Int)
    
    // Delegate data class that will be used for actual serialization
    @Serializable
    data class PointDto(val coords: String) {
        companion object {
            fun fromPoint(point: Point): PointDto = PointDto("${point.x},${point.y}")
            fun toPoint(dto: PointDto): Point {
                val (x, y) = dto.coords.split(",").map { it.toInt() }
                return Point(x, y)
            }
        }
    }
    
    // Create a serializer for Point using DelegatingSerializer
    private val pointSerializer = DelegatingSerializer(
        fromDelegate = PointDto.Companion::toPoint,
        toDelegate = PointDto.Companion::fromPoint
    )
    
    @Test
    fun testDelegatingSerializerFunctions() {
        // Test the conversion functions directly
        val point = Point(10, 20)
        val dto = PointDto.fromPoint(point)
        
        // Verify the conversion to DTO works correctly
        assertEquals("10,20", dto.coords)
        
        // Verify the conversion back to Point works correctly
        val roundTrip = PointDto.toPoint(dto)
        assertEquals(point, roundTrip)
    }
    
    @Test
    fun testSerializerDescriptor() {
        // Verify that the descriptor is delegated correctly
        val delegateDescriptor = kotlinx.serialization.serializer<PointDto>().descriptor
        assertEquals(delegateDescriptor, pointSerializer.descriptor)
    }
    
    // Test with a more complex example using nested objects
    data class ComplexObject(val name: String, val point: Point)
    
    @Serializable
    data class ComplexObjectDto(val name: String, val pointCoords: String) {
        companion object {
            fun fromComplexObject(obj: ComplexObject): ComplexObjectDto = 
                ComplexObjectDto(obj.name, "${obj.point.x},${obj.point.y}")
                
            fun toComplexObject(dto: ComplexObjectDto): ComplexObject {
                val (x, y) = dto.pointCoords.split(",").map { it.toInt() }
                return ComplexObject(dto.name, Point(x, y))
            }
        }
    }
    
    // Create a serializer for ComplexObject using DelegatingSerializer
    private val complexObjectSerializer = DelegatingSerializer(
        fromDelegate = ComplexObjectDto.Companion::toComplexObject,
        toDelegate = ComplexObjectDto.Companion::fromComplexObject
    )
    
    @Test
    fun testComplexDelegatingSerializerFunctions() {
        // Test the conversion functions directly
        val complexObject = ComplexObject("Test", Point(70, 80))
        val dto = ComplexObjectDto.fromComplexObject(complexObject)
        
        // Verify the conversion to DTO works correctly
        assertEquals("Test", dto.name)
        assertEquals("70,80", dto.pointCoords)
        
        // Verify the conversion back to ComplexObject works correctly
        val roundTrip = ComplexObjectDto.toComplexObject(dto)
        assertEquals(complexObject, roundTrip)
    }
    
    @Test
    fun testComplexSerializerDescriptor() {
        // Verify that the descriptor is delegated correctly
        val delegateDescriptor = kotlinx.serialization.serializer<ComplexObjectDto>().descriptor
        assertEquals(delegateDescriptor, complexObjectSerializer.descriptor)
    }
}