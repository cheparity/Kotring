package org.cheparity.kernel.core.annotation


@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Bean(
    val value: String = "",
)
