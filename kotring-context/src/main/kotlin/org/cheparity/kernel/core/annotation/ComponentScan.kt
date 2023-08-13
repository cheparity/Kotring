package org.cheparity.kernel.core.annotation

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class ComponentScan(
    vararg val value: String = [],
)
