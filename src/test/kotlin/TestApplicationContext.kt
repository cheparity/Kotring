import com.cheparity.kernel.core.context.AnnotationApplicationContext
import com.cheparity.test.kotlin.MainApplication
import com.cheparity.test.kotlin.config.ImportConfig
import com.cheparity.test.kotlin.config.MainConfig
import com.cheparity.test.kotlin.config.MainConfigWithParam
import org.junit.jupiter.api.Test
import kotlin.reflect.full.primaryConstructor

class TestApplicationContext {
    private val context = AnnotationApplicationContext(MainConfigWithParam::class)

    @Test
    fun constructorTest() {
        val constructor = MainConfigWithParam::class.primaryConstructor
        println(constructor?.name)
        constructor?.parameters?.forEach(::println)
    }

    @Test
    fun createBeanAsEarlySingletonTest() {
//        context
    }

    @Test
    fun testBeanFactory() {

//        context.peekBeanFactory()
//        val bean = context.getBean(HelloDao::class.java)
//        bean.sayHello()
    }

    @Test
    fun testCreateBeanDefinitions() {
//        val forName = Class.forName("com.cheparity.test.kotlin.MainApplicationKt")
//        println(forName)
        AnnotationApplicationContext(MainConfig::class)
    }

    @Test
    fun testContextWithAnnoValue() {
//        println(MainConfig::class.findAnnotation<ComponentScan>()?.value?.toList())
        AnnotationApplicationContext(MainConfig::class)

    }

    @Test
    fun testContextWithoutAnnoValue() {
        AnnotationApplicationContext(MainApplication::class)
    }

    @Test
    fun testBeanDefinitions() {
        val context = AnnotationApplicationContext(ImportConfig::class)
        context.peekClassNameSet()
        println("-------------------------------------")
        context.peekBeanDefinitions()
        println("-------------------------------------")
    }
}