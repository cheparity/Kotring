package com.cheparity

import com.cheparity.kernel.core.io.resource.Resource
import com.cheparity.kernel.core.io.resource.ResourceResolver


class MainApplication

fun main() {
    val classList = ResourceResolver("com.cheparity.test.kotlin").scan<Resource>()
    classList.forEach { println(it) }
}