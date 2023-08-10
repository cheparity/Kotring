package testReflect

import com.cheparity.kernel.core.annotation.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class Context(private val clazz: KClass<*>) {

    val anno = clazz.findAnnotation<Component>()

}