package com.cheparity.kernel.core.annotation

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER
)
@MustBeDocumented
annotation class Autowired(
    val value: Boolean = true,
    val name: String = "",
)