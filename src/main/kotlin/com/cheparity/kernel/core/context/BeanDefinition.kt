package com.cheparity.kernel.core.context

import com.cheparity.kernel.core.annotation.Component
import com.cheparity.kernel.core.uitls.*
import java.lang.reflect.Constructor
import java.lang.reflect.Method


/**
 * Kotlin without @postConstruct and @preDestroy. It uses `init` and `destroy` instead.
 */
data class BeanDefinition(
    val name: String, //the key of bean definition
    val clazz: Class<*>, //the class of bean
    var instance: Any? = null, //the instance of bean, needs to be initialized later
    var constructor: Constructor<*>? = null, //the constructor of bean
    var factoryName: String = "", //the factory's name if the bean is created by a factory
    var factoryMethod: Method? = null, //if the bean is created by a factory method, this is the method, otherwise null
    var order: Int = 0, //the init order of bean
    var primary: Boolean = false, //if the bean is primary, specified by @Primary
) : Comparable<BeanDefinition> {
    /**
     * This is the simplest constructor. You just need to pass a [clazz] to it, and all other args are default.
     *
     * @param clazz The class of the bean.
     */
    constructor(clazz: Class<*>) : this(
        clazz = clazz,
        name = clazz.takeBeanName(clazz.digAnnotation(Component::class.java)!!),
        order = clazz.getOrder(),
        constructor = clazz.getSuitableConstructor(),
        primary = clazz.isPrimary(),
    )

    override fun compareTo(other: BeanDefinition): Int {
        return this.order - other.order
    }

}