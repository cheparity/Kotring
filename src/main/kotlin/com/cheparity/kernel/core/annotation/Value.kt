package com.cheparity.kernel.core.annotation

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
annotation class Value(
    val value: String,
)
