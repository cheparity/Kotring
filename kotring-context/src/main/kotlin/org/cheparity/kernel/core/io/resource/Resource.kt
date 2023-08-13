package org.cheparity.kernel.core.io.resource

interface Resource {
    val path: String
    val name: String
    val classLoader: ClassLoader
}