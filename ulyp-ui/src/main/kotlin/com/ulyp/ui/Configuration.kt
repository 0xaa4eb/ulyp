package com.ulyp.ui

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(value = ["com.ulyp.ui"])
open class Configuration {
    @Bean
    open fun viewController(): PrimaryViewController {
        return PrimaryViewController()
    }

    @Bean
    open fun processTabPane(): ProcessTabPane {
        val tabPane = ProcessTabPane()

        tabPane.prefHeight = 408.0
        tabPane.prefWidth = 354.0
        return tabPane
    }
}