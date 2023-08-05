package com.cheparity.test.kotlin.dao

import com.cheparity.kernel.core.annotation.Component

@Component
class HelloDao {
    fun sayHello() {
        println("[HelloDao] Hello World!")
    }
}