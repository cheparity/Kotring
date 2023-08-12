package com.cheparity.kernel.core.io.property

import org.jetbrains.annotations.TestOnly
import java.time.*
import java.util.*
import kotlin.reflect.KType
import kotlin.reflect.full.createType


class PropertyResolver(props: Properties? = null) {

    companion object {
        private var converters = HashMap<KType, ((String) -> Any)>()

        /**
         * Convert the value to the specified type.
         * @param clazz The type to be converted to.
         * @param customizedConverter The customized converter, if not null, will be used instead of the default one.
         * @return The converted value.
         * @exception IllegalArgumentException If no converter found for the specified type. Please call [registerConverter]
         */
        @Suppress("UNCHECKED_CAST")
        fun String?.convertTo(type: KType, customizedConverter: ((String) -> Any)? = null): Any? = when {
            customizedConverter != null -> this?.let { customizedConverter(it) }
            converters.containsKey(type) -> this?.let { converters[type]!!(it) }
            else -> throw IllegalArgumentException(
                "No converter found for type $type, please call " +
                        "[registerConverter]"
            )
        }
    }


    private var properties: Map<String, String> = HashMap()


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
        converters[Integer::class.createType()] = { it.toInt() }
        converters[Int::class.createType()] = { it.toInt() }
        converters[Long::class.createType()] = { it.toLong() }
        converters[Float::class.createType()] = { it.toFloat() }
        converters[Double::class.createType()] = { it.toDouble() }
        converters[Boolean::class.createType()] = { it.toBoolean() }
        converters[String::class.createType()] = { it }
        converters[Char::class.createType()] = { it[0] }
        converters[Short::class.createType()] = { it.toShort() }
        converters[Byte::class.createType()] = { it.toByte() }
        converters[LocalDate::class.createType()] = { LocalDate.parse(it) }
        converters[Date::class.createType()] = { Date(it.toLong()) }
        converters[LocalTime::class.createType()] = { LocalTime.parse(it) }
        converters[LocalDateTime::class.createType()] = { LocalDateTime.parse(it) }
        converters[ZonedDateTime::class.createType()] = { ZonedDateTime.parse(it) }
        converters[Duration::class.createType()] = { Duration.parse(it) }
        converters[ZoneId::class.createType()] = { ZoneId.of(it) }
    }

    /**
     * Handle get request like:
     * - getProperty("app.title")
     * - getProperty("#{app.title}")
     * - getProperty("#{app.title:Summer}")
     *
     * @param key Property's key, through which can get property's value.
     *
     * @return Returns the value, null if not found
     */
    fun getProperty(key: String, type: KType? = null): String? {
        val prop = when {
            key.startsWith("#") -> {
                val propertyExpr = PropertyExpr.parse(key)
                getProperty(propertyExpr.key) ?: propertyExpr.defaultValue
            }

            else -> properties[key]
        }
        return prop
    }


    fun registerConverter(type: KType, converter: (String) -> Any) {
        converters[type] = converter
    }

    @TestOnly
    fun peekProperties() {
        properties.forEach { println("[properties] $it") }
    }
}

