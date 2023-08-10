package com.cheparity.kernel.core.io.resource

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

/**
 *
 * @param basePackage: package name shaped like com.cheparity.test.kotlin
 */
class ResourceResolver(private val basePackage: String) {
    companion object {
        private fun URI.decode(): String = URLDecoder.decode(this.toString(), StandardCharsets.UTF_8)
        private fun String.toPathString(): String = this.replace(".", "/")
        private fun String.removePrefix(prefix: String): String = this.substring(prefix.length)

        private fun String.removePrefix(): String = when {
            this.startsWith("file:") -> this.removePrefix("file:/")
            this.startsWith("jar:/") -> this.removePrefix("jar:/")
            else -> this
        }

        private fun String.toName(): String =
            this.removePrefix("/")
                .removePrefix("\\")
                .replace("\\", ".")
                .replace("/", ".")

        infix fun String.subSuffix(substring: String): String {
            val index = this.indexOf(substring)
            return if (index != -1) this.substring(0, index) else this
        }

        infix fun String.subPrefix(substring: String): String {
            val contains = this.contains(substring)
            return if (contains) this.substring(substring.length, this.length) else this
        }

    }

    private val classLoader = Thread.currentThread().contextClassLoader

    /**
     * Scan all the files in the base package and its sub packages. To customize the return list, use the [action]
     * parameter.
     * If no argument passed, you must declare the type of the list explicitly.
     *
     * @param action The action to be performed on each file.
     *
     * @return A list of the results of the [action] performed on each file. Default is [ClassPathResource].
     */
    fun <R> scan(action: ((ClassPathResource) -> R?)? = null): List<R> = mutableListOf<R>().apply {
        val pkgRelativePath = basePackage.toPathString()

        val urls = classLoader.getResources(pkgRelativePath)
        /**
         * Why we use 'getResources' instead of 'getResource'?
         * In some cases, there can be multiple resource URLs for a given package path. This can occur when using
         * multiple class loaders or in certain runtime environments, such as multiple class loaders, modular environments,
         * or dynamic loading.
         */
        while (urls.hasMoreElements()) {
            val uri: URI = urls.nextElement().toURI()
            val absolutePath = uri.decode().removePrefix()
            val sysRootPath = absolutePath subSuffix pkgRelativePath
            val root: Path = when {
                uri.toString().startsWith("file:") -> FileSystems.getDefault().getPath(absolutePath)
                uri.toString().startsWith("jar:") -> Paths.get(uri)
                else -> throw IllegalArgumentException("Unsupported protocol: ${uri.scheme}")
            }

            Files.walk(root).filter { it.toFile().isFile }.forEach { file ->
                val resource = ClassPathResource(
                    path = file.toString(),
                    name = (file.pathString.toName() subPrefix sysRootPath.toName())
                )
                /**
                 * if (action != null) then add(action(resource)
                 * else add(resource)
                 */
                @Suppress("UNCHECKED_CAST")
                action?.let { it(resource)?.let { it1 -> this.add(it1) } } ?: this.add(resource as R)
            }

        }
    }
}


