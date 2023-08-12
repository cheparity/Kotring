package com.cheparity.kernel.core.context

import com.cheparity.kernel.core.annotation.*
import com.cheparity.kernel.core.io.property.PropertyResolver
import com.cheparity.kernel.core.io.property.PropertyResolver.Companion.convertTo
import com.cheparity.kernel.core.io.resource.ClassPathResource
import com.cheparity.kernel.core.io.resource.ResourceResolver
import com.cheparity.kernel.core.uitls.*
import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class AnnotationApplicationContext(configClass: KClass<*>) {

    private val pkgToScan: Set<String> =
        configClass.findAnnotation<ComponentScan>()?.value?.toSet() ?: setOf(configClass.java.`package`.name)

    private val importPkgToScan: Set<KClass<*>>? = configClass.findAnnotation<Import>()?.value?.toSet()

    private var beans = HashMap<String, BeanDefinition>()

    private var classNameSet = HashSet<String>()

    init {
        scanForClasses()
        createBeanDefinitions()
        with(HashSet<String>()) {
            instantiateBeans(this)
        }
        beans.forEach { (_, bd) ->
            bd.injectProperties()
        }
    }

    private fun instantiateBeans(beansRCreating: HashSet<String>) {
        //first instantiate configBean
        beans.filter { it.value.asConfigBean }.toSortedMap().forEach { (_, beanDef) ->
            //Avoid instantiate twice
            if (!beanDef.initialized()) beanDef.createBeanAsEarlySingleton(beansRCreating)
        }
        //next instantiate other beans
        beans.filter { !it.value.initialized() }.toSortedMap().forEach { (_, beanDef) ->
            if (!beanDef.initialized()) beanDef.createBeanAsEarlySingleton(beansRCreating)
        }
    }

    private fun BeanDefinition.injectProperties() {
        //handle @Value
        this.clazz.memberProperties.filter { it.hasAnnotation<Value>() }.onEach { it.isAccessible = true }.onEach {
            val value = it.findAnnotation<Value>()!!.value
            val kType = it.returnType
            val v = PropertyResolver().getProperty(value)?.convertTo(kType)
            (it as KMutableProperty<*>).setter.call(this.instance, v)
        }
        //handle @Autowired
        this.clazz.memberProperties.filter { it.hasAnnotation<Autowired>() }.onEach {
            it.isAccessible = true
        }.onEach {
            val autowired = it.findAnnotation<Autowired>()!!
            val name = autowired.name
            val required = autowired.value
            val type = it.returnType.classifier as KClass<*>
            val bean2inj = if (name.isNotEmpty()) getBeanDefinition(name, type) else getBeanDefinition(type)
            if (bean2inj == null && required) {
                throw RuntimeException(
                    "No bean of [$type] found in [${this.clazz}].\nTips: Turn " + "@Autowired(false) on [$type] to avoid this exception"
                )
            }
            (it as KMutableProperty<*>).setter.call(
                this.instance, bean2inj?.instance ?: bean2inj?.createBeanAsEarlySingleton(
                    HashSet()
                )
            )
        }
    }


    /**
     * 1. 任何实例都得从构造方法谈起。对于@Bean注解标注的bean（也就是工厂下的Bean），它们的构造方法就是工厂方法。所以我们第一步就是**获取构造方法**。
     * 2. 获得了构造方法之后，我们要获取构造方法的参数。此时会出现两种情况：
     *     1. 参数标注了@Autowired，如 `@Autowired value2 = myBean2`。我们此时应该递归调用 `createBeanAsEarlySingleton`。
     *     2. 参数标注了@Value，若没有标记则按照默认标注了@Value处理，如 `@value value1 = "abc"` 或者 `value1 = "abc"` 。我们要用PropertyResolver解析属性，把属性值交给构造方法的参数。
     */
    private fun BeanDefinition.createBeanAsEarlySingleton(beansRCreating: HashSet<String>): Any {
        if (!beansRCreating.add(this.name)) throw RuntimeException(
            "Circular dependency occurs when creating bean ${this.name}"
        )
        val constructor = this.constructor ?: this.factoryFunc!! as KFunction<*>
        val parameters = constructor.parameters
        //Legality check
        parameters.onEach { param ->
            val autowired = param.hasAnnotation<Autowired>()
            val value = param.hasAnnotation<Value>()
            //ConfigBean not allowed @Autowired
            if (asConfigBean && autowired) throw RuntimeException(
                "ConfigBean ${this.name} cannot be annotated with @Autowired"
            )
            //should have one of @Value and @Autowired
            if ((value && autowired) || (!value && !autowired)) throw RuntimeException(
                "$param should be annotated with ONLY one of [@Value] and [@Autowired]"
            )
        }

        /**
         * Instantiate beans.
         * 1. get args required
         * 2. pass the args to constructor KFunction
         * 3. create an instance of the bean and set it to beanDefinition
         */

        val args = arrayOfNulls<Any?>(parameters.size)
        for ((index, param) in parameters.withIndex()) {
            val valueAnno = param.findAnnotation<Value>()
            val autowiredAnno = param.findAnnotation<Autowired>()
            when {
                valueAnno != null -> args[index] = PropertyResolver().getProperty(valueAnno.value).convertTo(param.type)
                autowiredAnno != null -> {
                    /**
                     * 在使用@Autowired时，首先在容器中查询对应类型的bean
                     * - 如果查询结果刚好为一个，就将该bean装配给@Autowired指定的数据
                     * - 如果查询的结果不止一个，那么@Autowired会根据名称来查找。
                     * - 如果查询的结果为空，那么会抛出异常。解决方法时，使用required=false
                     */
                    val name = autowiredAnno.name
                    val required = autowiredAnno.value
                    val type = param.type.classifier as KClass<*>
                    val dependsOnBeanDef: BeanDefinition? = if (name.isEmpty()) getBeanDefinition(type)
                    else getBeanDefinition(name, type)
                    if (dependsOnBeanDef == null && required) {
                        throw RuntimeException(
                            "No bean of [$type] found in [${this.clazz}].\nTips: Turn " + "@Autowired(false) on [$type] to avoid this exception"
                        )
                    }
                    args[index] = if (dependsOnBeanDef != null && !dependsOnBeanDef.asConfigBean) {
                        dependsOnBeanDef.instance ?: dependsOnBeanDef.createBeanAsEarlySingleton(beansRCreating)
                    } else null
                }
            }
        }
        //create instance by constructor with args
        val instance = constructor.call(*args).also { instance ->
            this.instance = instance
            beansRCreating.remove(this.name)
            instance ?: throw RuntimeException("Failed to create bean in $this at AnnotationApplicationContext")
        }
        println("Begin to instantiate $instance")
        return instance!!
    }

    fun getBean(name: String): Any = beans[name]?.instance ?: throw RuntimeException("No bean named $name found")

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
            val importContext = AnnotationApplicationContext(importClass)
            importContext.scanForClasses()
            classNameSet.addAll(importContext.classNameSet)
        }

    }

    private fun createBeanDefinitions() {
        classNameSet.forEach { className ->

            val clazz = Class.forName(className).kotlin
            val component = clazz.java.digAnnotation(Component::class.java)
            if (component != null) {
                val bd = BeanDefinition(
                    clazz = clazz,
                    name = clazz.takeBeanName(clazz.java.digAnnotation(Component::class.java)!!),
                    order = clazz.getOrder(),
                    constructor = clazz.getSuitableConstructor(),
                    primary = clazz.isPrimary(),
                )
                if (beans.containsKey(bd.name)) throw RuntimeException("Bean name duplicated ${bd.name}")
                beans[bd.name] = bd
                //continue to judge if the class is annotated with `@Configuration`.
                val configAnno = clazz.java.digAnnotation(Configuration::class.java)
                if (configAnno != null) {
                    scanFactoryMethods(bd.name, clazz, beans)
                }
            }
        }
    }

    private fun getBeanDefinition(type: KClass<*>): BeanDefinition? = when {
        getBeanDefinitions(type).isEmpty() -> null
        getBeanDefinitions(type).size == 1 -> getBeanDefinitions(type)[0]
        else -> getBeanDefinitions(type).filter { it.primary }.takeIf { it.size == 1 }?.get(0)
            ?: throw RuntimeException("More than one bean marked as primary")
    }

    private fun getBeanDefinition(name: String, type: KClass<*>): BeanDefinition? {
        val beanDef = getBeanDefinition(type)
        if (beans[name] != beanDef) throw RuntimeException("Bean $name isn't corresponding to $type")
        return beanDef
    }

    private fun getBeanDefinitions(type: KClass<*>): List<BeanDefinition> = beans.values.filter { it.clazz == type }


    @TestOnly
    fun peekBeanDefinitions() {
        println("[beanDefinitions]")
        beans.forEach { (t, u) ->
            print("[name:$t, configBean:${u.asConfigBean}, factoryBean:${u.asFactoryBean}, instantiated:${u.initialized()}] ")
            println("$u")
        }
    }

    @TestOnly
    fun peekClassNameSet() {
        println("classNameSet")
        classNameSet.forEach(::println)
    }

}

