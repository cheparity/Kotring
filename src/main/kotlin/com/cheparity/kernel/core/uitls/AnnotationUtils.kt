package com.cheparity.kernel.core.uitls

import com.cheparity.kernel.core.context.BeanDefinition
import kotlin.reflect.KClass

/**
 *
 * Find the annotation on the target class **recursively**
 *
 * Example:
 * ```kotlin
 * var componentAnno = UserDao::class.digAnnotation(Component::class)
 * ```
 * @return Returns the annotation if found, null otherwise.
 *
 * @param annoClass The annotation class to find.
 *
 */
fun <A : Annotation> Class<*>.digAnnotation(
    annoClass: Class<A>,
    visited: MutableSet<Class<*>> = mutableSetOf(),
): A? {
    if (visited.contains(this)) {
        return null
    }

    visited.add(this)

    val directAnnotation = this.getAnnotation(annoClass)
    if (directAnnotation != null) {
        return directAnnotation
    }

    for (annotation in this.annotations) {
        val indirectAnnotation = annotation.annotationClass.java.digAnnotation(annoClass, visited)
        if (indirectAnnotation != null) {
            return indirectAnnotation
        }
    }

    return null
}


/**
 * To determine whether the target class is annotated with the specified annotation
 *
 * @return Returns true if the target class is annotated with the specified annotation, false otherwise.
 *
 * @param A The annotation class to find.
 */
inline fun <reified A : Annotation> KClass<*>.isAnnotatedWith(): Boolean =
    this::class.java.digAnnotation(A::class.java) != null

fun BeanDefinition.isConfiguration(): Boolean =
    this.clazz.digAnnotation(com.cheparity.kernel.core.annotation.Configuration::class.java) != null