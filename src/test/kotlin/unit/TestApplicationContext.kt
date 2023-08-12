package unit

import com.cheparity.app.config.MainConfigWithParam
import com.cheparity.kernel.core.annotation.Autowired
import com.cheparity.kernel.core.context.AnnotationApplicationContext
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

class TestApplicationContext {

    @Test
    fun testInstance() {
        val context = AnnotationApplicationContext(MainConfigWithParam::class)
        val bean = context.getBean("MainConfigWithParam") as MainConfigWithParam
        assert(bean.myBean2 != null)
        assert(bean.windir == "C:\\Windows")
        assert(bean.tstDefaultValue == "defaultValue")
    }

    @Test
    fun testBeanInstance() {
        val context = AnnotationApplicationContext(MainConfigWithParam::class)
        println("----------")
        context.peekBeanDefinitions()
    }

    @Test
    fun trial() {
        val prop = MainConfigWithParam::class.declaredMemberProperties
            .find { it.name == "myBean2" }
            .also { println(it) }
        prop?.hasAnnotation<Autowired>().also { println(it) }
    }

    @Test
    fun testBeanDefinitions() {
        val context = AnnotationApplicationContext(MainConfigWithParam::class)
        context.peekClassNameSet()
        println("-------------------------------------")
        context.peekBeanDefinitions()
        println("-------------------------------------")
    }
}