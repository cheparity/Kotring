package com.cheparity.kernel.core.io.property

import org.jetbrains.annotations.TestOnly
import java.time.*
import java.util.*
import kotlin.reflect.KClass


class PropertyResolver(props: Properties? = null) {


    private var properties: Map<String, String> = HashMap()
    private var converters = HashMap<KClass<*>, ((String) -> Any)>()

    init {
        System.getenv().forEach { (key, value) ->
            (properties as HashMap)[key] = value
        }
        props?.run {
            stringPropertyNames().forEach { key ->
                (properties as HashMap)[key] = props.getProperty(key)
            }
        }
        //converters init
        converters[Integer::class] = { it.toInt() }
        converters[Int::class] = { it.toInt() }
        converters[Long::class] = { it.toLong() }
        converters[Float::class] = { it.toFloat() }
        converters[Double::class] = { it.toDouble() }
        converters[Boolean::class] = { it.toBoolean() }
        converters[String::class] = { it }
        converters[Char::class] = { it[0] }
        converters[Short::class] = { it.toShort() }
        converters[Byte::class] = { it.toByte() }
        converters[LocalDate::class] = { LocalDate.parse(it) }
        converters[Date::class] = { Date(it.toLong()) }
        converters[LocalTime::class] = { LocalTime.parse(it) }
        converters[LocalDateTime::class] = { LocalDateTime.parse(it) }
        converters[ZonedDateTime::class] = { ZonedDateTime.parse(it) }
        converters[Duration::class] = { Duration.parse(it) }
        converters[ZoneId::class] = { ZoneId.of(it) }
    }

    /**
     * Handle get request like:
     * - getProperty("app.title")
     * - getProperty("${app.title}")
     * - getProperty("${app.title:Summer}")
     *
     * @param key Property's key, through which can get property's value.
     *
     * @return Returns the value, null if not found
     */
    fun getProperty(key: String): String? = when {
        key.startsWith("\$") -> {
            val propertyExpr = PropertyExpr.parse(key)
            getProperty(propertyExpr.key) ?: propertyExpr.defaultValue
        }

        else -> properties[key]
    }

    /**
     * Convert the value to the specified type.
     * @param clazz The type to be converted to.
     * @param value The value to be converted.
     * @param customizedConverter The customized converter, if not null, will be used instead of the default one.
     * @return The converted value.
     * @exception IllegalArgumentException If no converter found for the specified type. Please call [registerConverter]
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> convert(clazz: KClass<T>, value: String, customizedConverter: ((String) -> Any)? = null): T = when {
        customizedConverter != null -> customizedConverter(value) as T
        converters.containsKey(clazz) -> converters[clazz]!!(value) as T
        else -> throw IllegalArgumentException(
            "No converter found for class $clazz, please call " +
                    "[registerConverter]"
        )
    }


    fun registerConverter(clazz: KClass<*>, converter: (String) -> Any) {
        converters[clazz] = converter
    }

    @TestOnly
    fun peekProperties() {
        properties.forEach { println("[properties] $it") }
    }
}

