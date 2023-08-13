package org.cheparity.kernel.core.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Import(
    vararg val value: KClass<*> = [],
)
