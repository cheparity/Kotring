package com.cheparity.test.kotlin.config

import com.cheparity.kernel.core.annotation.Autowired
import com.cheparity.kernel.core.annotation.ComponentScan
import com.cheparity.kernel.core.annotation.Configuration
import com.cheparity.test.kotlin.bean.MyBean2

@Configuration
@ComponentScan("com.cheparity.test.kotlin")
class MainConfigWithParam(
    val value1: String = "abc", //TODO To figure out if the value will appear in both param and field
) {
    @Autowired
    lateinit var myBean2: MyBean2
}