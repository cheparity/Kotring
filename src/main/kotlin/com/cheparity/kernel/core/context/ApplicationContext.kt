package com.cheparity.kernel.core.context

interface ApplicationContext {

    @Throws(RuntimeException::class)
    fun getBean(name: String): Any
}