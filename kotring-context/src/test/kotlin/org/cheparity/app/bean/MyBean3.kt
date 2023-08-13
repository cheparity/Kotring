package org.cheparity.app.bean

import org.cheparity.kernel.core.annotation.Autowired
import org.cheparity.kernel.core.annotation.Component

@Component
class MyBean3(
    @Autowired myBean1: MyBean1,
    @Autowired myBean2: MyBean2,
)