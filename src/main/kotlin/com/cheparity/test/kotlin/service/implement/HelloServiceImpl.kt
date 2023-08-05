package com.cheparity.test.kotlin.service.implement

import com.cheparity.kernel.core.annotation.Component
import com.cheparity.test.kotlin.service.HelloService


@Component
class HelloServiceImpl: HelloService {

    override fun sayHello() {
        println("Hello, world!")
    }


}