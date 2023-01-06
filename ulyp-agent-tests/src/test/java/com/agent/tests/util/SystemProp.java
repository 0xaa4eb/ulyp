package com.agent.tests.util;

import lombok.Builder;

@Builder
public class SystemProp {

    private final String key;
    private final String value;

    private SystemProp(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String toJavaCmdLineProp() {
        return "-D" + key + (value != null ? ("=" + value) : "");
    }
}
