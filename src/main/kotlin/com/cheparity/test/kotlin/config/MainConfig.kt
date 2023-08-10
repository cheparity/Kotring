package com.cheparity.test.kotlin.config

import com.cheparity.kernel.core.annotation.Bean
import com.cheparity.kernel.core.annotation.ComponentScan
import com.cheparity.kernel.core.annotation.Configuration
import com.cheparity.test.kotlin.bean.MyBean1

@Configuration
@ComponentScan("com.cheparity.test.kotlin")
class MainConfig {

    @Bean
    fun myBean1() = MyBean1()
}