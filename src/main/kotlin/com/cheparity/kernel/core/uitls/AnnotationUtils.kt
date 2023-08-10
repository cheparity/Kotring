package com.cheparity.kernel.core.uitls

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
 * **Unsupported yet! Packages and file facades are not yet supported in Kotlin reflection.**
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
 *
 */
fun <A : Annotation> KClass<*>.digAnnotation(
    annoClass: KClass<A>,
    visited: MutableSet<KClass<*>> = mutableSetOf(),
): A? {
    if (!visited.add(this)) {
        return null
    }

    val directAnnotation = this.java.getAnnotation(annoClass.java)
    if (directAnnotation != null) {
        return this.java.getAnnotation(annoClass.java)
    }

    for (annotation in this.annotations) {
        val indirectAnnotation = annotation.annotationClass.digAnnotation(annoClass, visited)
        if (indirectAnnotation != null) {
            return indirectAnnotation
        }
    }

    return null
}