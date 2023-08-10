package com.cheparity.kernel.core.context

import com.cheparity.kernel.core.annotation.Component
import com.cheparity.kernel.core.annotation.ComponentScan
import com.cheparity.kernel.core.annotation.Configuration
import com.cheparity.kernel.core.annotation.Import
import com.cheparity.kernel.core.io.resource.ClassPathResource
import com.cheparity.kernel.core.io.resource.ResourceResolver
import com.cheparity.kernel.core.uitls.digAnnotation
import com.cheparity.kernel.core.uitls.isConfiguration
import com.cheparity.kernel.core.uitls.scanFactoryMethods
import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KClass

class AnnotationApplicationContext(private val configClass: Class<*>) {

    private val pkgToScan: Set<String> =
        configClass.getAnnotation(ComponentScan::class.java)?.value?.toSet() ?: setOf(configClass.`package`.name)

    private val importPkgToScan: Set<KClass<*>>? =
        configClass.getAnnotation(Import::class.java)?.value?.toSet()

    private var beans = HashMap<String, BeanDefinition>()

    private var classNameSet = HashSet<String>()

    init {
        scanForClasses()
        createBeanDefinitions()

        with(HashSet<String>()) {
            instantiateBeans(this)
        }

    }

    private fun instantiateBeans(beansRCreating: HashSet<String>) {
        beans.filter { it.value.isConfiguration() }.toSortedMap().forEach { (beanName, beanDef) ->
            beanDef.createBeanAsEarlySingleton(beansRCreating)
        }
        beans.filter { it.value.instance == null }.toSortedMap().forEach { (beanName, beanDef) ->
            beanDef.createBeanAsEarlySingleton(beansRCreating)
        }
    }


    /**
     * 1. 任何实例都得从构造方法谈起。对于@Bean注解标注的bean（也就是工厂下的Bean），它们的构造方法就是工厂方法。所以我们第一步就是**获取构造方法**。
     * 2. 获得了构造方法之后，我们要获取构造方法的参数。此时会出现两种情况：
     *     1. 参数标注了@Autowired，如 `@Autowired value2 = myBean2`。我们此时应该递归调用 `createBeanAsEarlySingleton`。
     *     2. 参数标注了@Value，若没有标记则按照默认标注了@Value处理，如 `@value value1 = "abc"` 或者 `value1 = "abc"` 。我们要用PropertyResolver解析属性，把属性值交给构造方法的参数。
     */
    private fun BeanDefinition.createBeanAsEarlySingleton(beansRCreating: HashSet<String>) {
        if (!beansRCreating.add(this.name)) throw RuntimeException("Circular dependency on bean ${this.name}")
        val constructor = this.constructor ?: this.factoryMethod!!
        val parameters = constructor.parameters
        parameters.forEach { parameter ->
            
        }
    }


    private fun scanForClasses() {
        pkgToScan.forEach { pkg ->
            val classPathResource = ClassPathResource(pkg.replace(".", "/"))
            val resolver = ResourceResolver(classPathResource.path)
            pkgToScan.forEach { _ ->
                classNameSet.addAll(resolver.scan { resource ->
                    if (resource.name.endsWith(".class")) resource.name.removeSuffix(".class") else null
                })
            }
        }
        //continue to search packages if there is an @Import annotation
        importPkgToScan?.forEach { importClass ->
            val importContext = AnnotationApplicationContext(importClass.java)
            importContext.scanForClasses()
//            beanFactory.putAll(importContext.beanFactory)
            classNameSet.addAll(importContext.classNameSet)
        }

    }


    private fun createBeanDefinitions() {
        classNameSet.forEach { className ->
            val clazz = Class.forName(className)
            val component = clazz.digAnnotation(Component::class.java)
            if (component != null) {
                val bd = BeanDefinition(clazz)
                if (beans.containsKey(bd.name)) throw RuntimeException("Bean name duplicated ${bd.name}")
                beans[bd.name] = bd
                //continue to judge if the class is annotated with `@Configuration`.
                val configAnno = clazz.digAnnotation(Configuration::class.java)
                if (configAnno != null) {
                    scanFactoryMethods(bd.name, clazz, beans)
                }
            }
        }
    }


    fun getBeanDefinition(name: String): Any = beans[name] ?: throw RuntimeException("Bean not found")
    fun getBeanDefinition(type: KClass<*>): BeanDefinition? = when {
        getBeanDefinitions(type).isEmpty() -> null
        getBeanDefinitions(type).size == 1 -> getBeanDefinitions(type)[0]
        else -> getBeanDefinitions(type).filter { it.primary }.takeIf { it.size == 1 }?.get(0)
            ?: throw RuntimeException("More than one bean marked as primary")
    }

    fun getBeanDefinitions(type: KClass<*>): List<BeanDefinition> =
        beans.values.filter { it::class == type }


    @TestOnly
    fun peekBeanFactory() {
        println("[beanFactory]")
        beans.forEach { (t, u) ->
            println("$t -> $u")
        }
    }

    @TestOnly
    fun peekClassNameSet() {
        println("[classNameSet]")
        classNameSet.forEach(::println)
    }

}