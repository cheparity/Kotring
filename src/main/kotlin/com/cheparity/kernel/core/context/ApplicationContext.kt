package com.cheparity.kernel.core.context

interface ApplicationContext {

    fun <T> getBeen(clazz: Class<T>): Any
}