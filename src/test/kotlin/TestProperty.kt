import com.cheparity.kernel.core.io.property.PropertyResolver
import java.io.InputStream
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestProperty {

    @Test
    fun testProperty() {
        val properties = Properties().apply { load(InputStream.nullInputStream()) }
        val propertyResolver = PropertyResolver(properties)
        propertyResolver.peekProperties()
        assertEquals(propertyResolver.getProperty("windir"), "C:\\Windows")
        assertEquals(propertyResolver.getProperty("\${windir}"), "C:\\Windows")
        assertEquals(propertyResolver.getProperty("\${key:defaultValue}"), "defaultValue")
    }
}