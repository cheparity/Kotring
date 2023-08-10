package testReflect

import com.cheparity.kernel.core.annotation.Component
import kotlin.reflect.full.findAnnotation

@Component
class Clazz


fun main() {
    println(Clazz::class.findAnnotation<Component>())
    println(Context(Clazz::class).anno)
}