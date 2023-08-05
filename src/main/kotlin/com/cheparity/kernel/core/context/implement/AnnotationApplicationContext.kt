package com.cheparity.kernel.core.context.implement

import com.cheparity.kernel.core.annotation.Component
import com.cheparity.kernel.core.context.ApplicationContext
import java.io.File
import java.net.URLDecoder

/**
 * @param basePackage The package where the application starts scanning, shaped like *"com.example"*
 */
class AnnotationApplicationContext(private val basePackage: String) : ApplicationContext {
    companion object {
        private lateinit var rootPath: String
    }

    private var beanFactory: Map<Class<*>, Any> = HashMap()

    /**
     * The steps for scanning are as follows:
     * 1. Scan all classes in the specified package(this package and all sub-packages)
     * 2. Find all **classes** with @Component annotations
     * 3. Create an instance of the class and put it in the beanFactory
     *
     * Variables portrait:
     * - basePackage: com.cheparity
     * - reshapedBasePackage: com\cheparity
     * - decodedFilePath: /C:/Users/Niyuta/IdeaProjects/kotring/out/production/kotring/com\cheparity
     * - rootPath: C:/Users/Niyuta/IdeaProjects/kotring/out/production/kotring/
     */
    init {
        val reshapedBasePackage = basePackage.replace(".", File.separator)
        val absoluteUrls = Thread.currentThread().contextClassLoader.getResources(reshapedBasePackage)
        while (absoluteUrls.hasMoreElements()) {
            val decodedFilePath = URLDecoder.decode(absoluteUrls.nextElement().file, "UTF-8")
            rootPath = decodedFilePath.substring(1, decodedFilePath.length - basePackage.length)
            scanBeans(decodedFilePath)
        }
    }

    /**
     * Scan the beans in the specified file, and put them into beanFactory.
     * Usually used as the entry point for calling scanBeans(file:String)
     *
     * Variables portrait:
     * - filePath: com\cheparity\annotation\Bean.class
     * - classname: com.cheparity.annotation.Bean
     *
     * @param file The file to be scanned, is a **File**.
     *
     */
    private fun scanBeans(file: File) {
        if (file.isDirectory) {
            val children = file.listFiles()
            children?.forEach { scanBeans(it) }
        } else {
            val fileRelativePath = file.absolutePath.substring(rootPath.length)
            if (!fileRelativePath.contains(".class")) {
                return
            }
            val classname = fileRelativePath
                .replace(File.separator, ".")
                .replace(".class", "")
            val clazz = Class.forName(classname)
            clazz.getAnnotation(Component::class.java)?.let {
                val instance = clazz.getDeclaredConstructor().newInstance()
                if (clazz.genericInterfaces.isNotEmpty()) {
                    val interfaceClazz = clazz.genericInterfaces[0] as Class<*>
                    (beanFactory as HashMap)[interfaceClazz] = instance
                } else {
                    (beanFactory as HashMap)[clazz] = instance
                }
            }

        }
    }


    /**
     * Scan the beans in the specified file. Just simply call scanBeans(file: File).
     *
     * @param file The file to be scanned, is a String.
     */
    private fun scanBeans(file: String) {
        scanBeans(File(file)) //to ensure the file path is correct
    }


    override fun <T> getBeen(clazz: Class<T>): Any = beanFactory[clazz]!!
}