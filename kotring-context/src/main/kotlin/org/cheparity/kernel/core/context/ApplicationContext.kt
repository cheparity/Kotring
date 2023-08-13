package org.cheparity.kernel.core.context

import kotlin.reflect.KClass

interface ApplicationContext : AutoCloseable {

    fun containsBean(name: String): Boolean

    @Throws(RuntimeException::class)
    fun <T> getBean(name: String): T

    @Throws(RuntimeException::class)
    fun <T> getBean(name: String, requiredType: KClass<*>): T

    @Throws(RuntimeException::class)
    fun <T> getBean(requiredType: KClass<*>): T
    fun <T> getBeans(requiredType: KClass<*>): List<T>

    override fun close()
}