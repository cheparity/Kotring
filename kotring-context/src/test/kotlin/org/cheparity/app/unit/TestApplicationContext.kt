package org.cheparity.app.unit

import org.cheparity.app.config.MainConfigWithParam
import org.cheparity.kernel.core.annotation.Autowired
import org.cheparity.kernel.core.context.AnnotationApplicationContext
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

class TestApplicationContext {

    @Test
    fun testInstance() {
        val context = AnnotationApplicationContext(MainConfigWithParam::class)
        val bean = context.getBean("MainConfigWithParam") as MainConfigWithParam
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