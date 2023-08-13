package org.cheparity.kernel.core.annotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Value(
    val value: String,
)
