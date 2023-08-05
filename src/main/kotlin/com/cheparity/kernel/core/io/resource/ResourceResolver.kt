package com.cheparity.kernel.core.io.resource

import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class ResourceResolver(private val basePackage: String) {
    companion object {
        private fun URI.decode(): String = URLDecoder.decode(this.toString(), StandardCharsets.UTF_8)
        private fun String.toPathString(): String = this.replace(".", File.separator)
        private fun String.removePrefix(prefix: String): String = this.substring(prefix.length)

        private fun String.removePrefix(): String = when {
            this.startsWith("file:") -> this.removePrefix("file:/")
            this.startsWith("jar:/") -> this.removePrefix("jar:/")
            else -> this
        }

    }

    private val classLoader = Thread.currentThread().contextClassLoader

    /**
     * Scan all the files in the base package. To customize the return list, use the [action] parameter.
     * If no argument passed, you must declare the type of the list explicitly.
     *
     * @param action The action to be performed on each file.
     *
     * @return A list of the results of the [action] performed on each file. Default is [Resource].
     */
    fun <R> scan(action: ((Resource) -> R?)? = null): List<R> = mutableListOf<R>().apply {
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
            val pkgAbsolutePath = uri.decode().removePrefix()
            val root: Path = when {
                uri.toString().startsWith("file:") -> FileSystems.getDefault().getPath(pkgAbsolutePath)
                uri.toString().startsWith("jar:") -> Paths.get(uri)
                else -> throw IllegalArgumentException("Unsupported protocol: ${uri.scheme}")
            }

            Files.walk(root).filter { it.toFile().isFile }.forEach { file ->
                val resource = Resource(
                    path = file.toString(),
                    name = file.fileName.toString()
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
