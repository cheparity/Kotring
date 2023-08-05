package com.cheparity.kernel.core.io.property

internal data class PropertyExpr(val key: String, val defaultValue: String? = null) {

    companion object {

        /**
         * Parse the expression to a [PropertyExpr] object.
         *
         * @param expr The expression to be parsed. Shaped like ${key:defaultValue} or ${key}
         */
        fun parse(expr: String): PropertyExpr {
            assert(expr.startsWith("\${") && expr.endsWith("}"))
            return when (val n = expr.indexOf(":")) {
                -1 -> PropertyExpr(expr.substring(2, expr.length - 1))
                else -> PropertyExpr(expr.substring(2, n), expr.substring(n + 1, expr.length - 1))
            }
        }
    }

}
