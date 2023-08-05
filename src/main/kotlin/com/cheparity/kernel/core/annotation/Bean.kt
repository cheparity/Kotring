package com.cheparity.kernel.core.annotation


@Target(AnnotationTarget.FUNCTION) //Bean是加在function上的注解
//@Retention(AnnotationRetention.RUNTIME) //运行时生效，这个其实可以不写，是默认的
annotation class Bean
