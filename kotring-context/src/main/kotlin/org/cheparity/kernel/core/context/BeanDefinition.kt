package org.cheparity.kernel.core.context

import org.cheparity.kernel.core.annotation.Configuration
import org.cheparity.kernel.core.uitls.digAnnotation
import kotlin.reflect.KClass
import kotlin.reflect.KFunction


/**
 * A bean's definition. Also: Kotlin doesn't support `@postConstruct` and `@preDestroy`. It uses `init` and `destroy`
 * instead.
 *
 * @param name The key of bean definition.
 * @param clazz The class of bean. Also: no need to be a KType cause no need to use generics.
 * @param instance The instance of bean, needs to be initialized later.
 * @param factoryName If the bean's created by a config class, [factoryName] is the config class' bean name. Null if
 * not.
 * @param factoryFunc If the bean's created by a config class, [factoryFunc] stands for the fun to create it. Null if
 * not.
 * @param order Init order of the bean. Default 0.
 * @param primary Verify if the bean is primary, specified by [@Primary]. Default false.
 */
data class BeanDefinition(
    val name: String,
    val clazz: KClass<*>,
    var instance: Any? = null,
    var constructor: KFunction<*>? = null, //The constructor function. Null if the bean is annotated with [@Configuration]
    var factoryName: String? = null,
    var factoryFunc: Function<*>? = null,
    var order: Int = 0,
    var primary: Boolean = false,
) : Comparable<BeanDefinition> {

    override fun compareTo(other: BeanDefinition): Int {
        return this.order - other.order
    }

    val asConfigBean: Boolean = this.clazz.java.digAnnotation(Configuration::class.java) != null

    val asFactoryBean: Boolean = this.factoryFunc != null

    val asNormalBean: Boolean = !asConfigBean && !asFactoryBean

    fun initialized(): Boolean = this.instance != null


}