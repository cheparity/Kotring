package com.cheparity.kernel.core.annotation

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Autowired(
    val value: Boolean = true,
    val name: String = "",
)