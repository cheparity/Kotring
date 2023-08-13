package org.cheparity.kernel.core.io.resource

data class ClassPathResource(override val path: String, override val name: String = path) : Resource {
    override val classLoader: ClassLoader =
        Thread.currentThread().contextClassLoader ?: ClassPathResource::class.java.classLoader
        ?: ClassLoader.getSystemClassLoader()
}