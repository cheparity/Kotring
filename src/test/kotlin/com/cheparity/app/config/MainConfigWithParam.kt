package com.cheparity.app.config

import com.cheparity.app.bean.MyBean2
import com.cheparity.kernel.core.annotation.Autowired
import com.cheparity.kernel.core.annotation.ComponentScan
import com.cheparity.kernel.core.annotation.Configuration
import com.cheparity.kernel.core.annotation.Value

@Configuration
@ComponentScan("com.cheparity.app")
class MainConfigWithParam {
    @Autowired
    lateinit var myBean2: MyBean2

    @Value("#{windir}")
    lateinit var windir: String

    @Value("#{key:defaultValue}")
    lateinit var tstDefaultValue: String
}