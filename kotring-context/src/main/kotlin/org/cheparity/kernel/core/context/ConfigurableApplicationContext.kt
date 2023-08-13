package org.cheparity.kernel.core.context

import kotlin.reflect.KClass


interface ConfigurableApplicationContext : ApplicationContext {
    fun getBeanDefinitions(type: KClass<*>): List<BeanDefinition?>?

    fun getBeanDefinition(type: KClass<*>): BeanDefinition?

    fun getBeanDefinition(name: String): BeanDefinition?

    fun getBeanDefinition(name: String, requiredType: KClass<*>): BeanDefinition?

    fun BeanDefinition.createBeanAsEarlySingleton(): Any?
}