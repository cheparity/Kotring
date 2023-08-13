package org.cheparity.kernel.core.annotation


@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Component(
    val value: String = "",
)
