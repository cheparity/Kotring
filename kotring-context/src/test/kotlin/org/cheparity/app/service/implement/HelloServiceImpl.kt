package org.cheparity.app.service.implement

import org.cheparity.app.service.HelloService
import org.cheparity.kernel.core.annotation.Component


@Component("helloService")
class HelloServiceImpl : HelloService {

    override fun sayHello() {
        println("Hello, world!")
    }


}