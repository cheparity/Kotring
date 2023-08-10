package com.cheparity.test.kotlin


class MainApplication

infix fun String.subPrefix(substring: String): String {
    val index = this.lastIndexOf(substring)
    return if (index != -1) this.substring(index + 1, this.length) else this
}


fun main() {
    println("abc" subPrefix "a")
}



