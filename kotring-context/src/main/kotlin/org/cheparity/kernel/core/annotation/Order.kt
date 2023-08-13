package org.cheparity.kernel.core.annotation

@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Order(
    val value: Int,
)
