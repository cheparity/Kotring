package org.cheparity.app.config

import org.cheparity.app.bean.MyBean2
import org.cheparity.kernel.core.annotation.Autowired
import org.cheparity.kernel.core.annotation.ComponentScan
import org.cheparity.kernel.core.annotation.Configuration
import org.cheparity.kernel.core.annotation.Value

@Configuration
@ComponentScan("org.cheparity.app")
class MainConfigWithParam {
    @Autowired
    lateinit var myBean2: MyBean2

    @Value("#{windir}")
    lateinit var windir: String

    @Value("#{key:defaultValue}")
    lateinit var tstDefaultValue: String
}