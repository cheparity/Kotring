package com.cheparity.kernel.core.uitls

import com.cheparity.kernel.core.annotation.Bean
import com.cheparity.kernel.core.annotation.Component
import com.cheparity.kernel.core.annotation.Order
import com.cheparity.kernel.core.annotation.Primary
import com.cheparity.kernel.core.context.BeanDefinition
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

fun Method.takeBeanName(anno: Bean): String =
    anno.value.takeUnless { it.isEmpty() } ?: this.name


fun Class<*>.takeBeanName(anno: Component): String =
    anno.value.takeUnless { it.isEmpty() } ?: this.simpleName

/**
 * Get the only PUBLIC constructor, null then the only PRIVATE constructor.
 *
 * @throws RuntimeException Throws exception if there's more than one PUBLIC or PRIVATE constructor.
 */
fun Class<*>.getSuitableConstructor(): Constructor<*> =
    if (this.constructors.isNotEmpty()) {
        if (this.constructors.size != 1) throw RuntimeException(
            "More than one PUBLIC constructors found in " +
                    "${this::class}"
        )
        else this.constructors[0]
    } else if (this.declaredConstructors.isNotEmpty()) {
        if (this.declaredConstructors.size != 1) throw RuntimeException(
            "More than one PRIVATE constructors found in " +
                    "${this::class}"
        )
        else this.declaredConstructors[0]
    } else {
        throw RuntimeException("No constructors found in ${this::class}")
    }

fun KClass<*>.getSuitableConstructor(): KFunction<Any>? = this.primaryConstructor

fun Class<*>.getOrder(): Int = this.getAnnotation(Order::class.java)?.value ?: 0

fun Class<*>.isPrimary(): Boolean = this.getAnnotation(Primary::class.java) != null

/**
 * To get the method annotated with [annoClass] in this class.
 *
 * Example:
 *
 * ```kotlin
 * var method = UserDao::class.getAnnotationMethod(PostConstruct::class.java)
 * ```
 *
 * @param annoClass The annotation class to find.
 */
fun <A : Annotation> Class<*>.getAnnotationMethod(annoClass: Class<A>): Method? {
    val methods = this.declaredMethods.filter { it.isAnnotationPresent(annoClass) }
    if (methods.size > 1) throw RuntimeException(
        "More than one method annotated with ${annoClass.simpleName} found in " +
                "${this::class}"
    )
    if (methods[0].parameterCount != 0) throw RuntimeException(
        "Method annotated with ${annoClass.simpleName} in ${this::class} " +
                "should not have any parameters"
    )
    return methods[0]
}

/**
 * Scan for the methods annotated with `@Bean` annotation under the class annotated with `@Configuration`. Add the
 * methods to the [beanFactory]
 */
fun scanFactoryMethods(factoryBeanName: String, factoryClass: Class<*>, beanFactory: HashMap<String, BeanDefinition>) {
    factoryClass.declaredMethods.filter {
        it.isAnnotationPresent(Bean::class.java)
    }.forEach {
        val beanAnno = it.getAnnotation(Bean::class.java)
        val returnType = it.returnType
        if (beanFactory.containsKey(it.takeBeanName(beanAnno)))
            throw java.lang.RuntimeException("Bean duplicates on METHOD ${it.takeBeanName(beanAnno)} of $factoryBeanName")
        beanFactory[it.takeBeanName(beanAnno)] = BeanDefinition(
            clazz = returnType,
            name = it.takeBeanName(beanAnno),
            factoryMethod = it,
            factoryName = factoryBeanName,
            order = it.getAnnotation(Order::class.java)?.value ?: 0,
            primary = it.getAnnotation(Primary::class.java) != null
        )
    }
}