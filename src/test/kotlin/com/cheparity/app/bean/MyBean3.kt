package com.cheparity.app.bean

import com.cheparity.kernel.core.annotation.Autowired
import com.cheparity.kernel.core.annotation.Component

@Component
class MyBean3(
    @Autowired myBean1: MyBean1,
    @Autowired myBean2: MyBean2,
)