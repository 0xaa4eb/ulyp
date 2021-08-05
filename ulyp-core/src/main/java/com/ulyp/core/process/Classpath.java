package com.ulyp.core.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Classpath {

    private final List<String> value;

    public Classpath() {
        this.value = Arrays.asList(
                Pattern.compile(System.getProperty("path.separator"), Pattern.LITERAL).split(System.getProperty("java.class.path"))
        );
    }

    private Classpath(List<String> value) {
        this.value = value;
    }

    public Classpath add(String file) {
        List<String> copy = new ArrayList<>(this.value);
        copy.add(file);
        return new Classpath(copy);
    }

    public List<String> toList() {
        return value;
    }
}
