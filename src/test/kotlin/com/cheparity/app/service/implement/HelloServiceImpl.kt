package com.cheparity.app.service.implement

import com.cheparity.app.service.HelloService
import com.cheparity.kernel.core.annotation.Component


@Component("helloService")
class HelloServiceImpl : HelloService {

    override fun sayHello() {
        println("Hello, world!")
    }


}