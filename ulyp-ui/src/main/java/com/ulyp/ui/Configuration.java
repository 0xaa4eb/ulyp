package com.ulyp.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@org.springframework.context.annotation.Configuration
@ComponentScan(value = "com.ulyp.ui")
public class Configuration {

    @Bean
    public PrimaryViewController viewController() {
        return new PrimaryViewController();
    }

    @Bean
    public ProcessTabPane processTabPane() {
        ProcessTabPane tabPane = new ProcessTabPane();

        // TODO move somewhere
        tabPane.setPrefHeight(408.0);
        tabPane.setPrefWidth(354.0);

        return tabPane;
    }
}
