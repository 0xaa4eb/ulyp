package com.ulyp.agent.options;

import lombok.Getter;

public class AgentOption<T> implements Option<T> {

    private final String prop;
    private final Parser<T> parser;
    @Getter
    private final String description;

    private volatile boolean initiated;
    private volatile T value;

    public AgentOption(String prop, Parser<T> parser, String description) {
        this.prop = prop;
        this.parser = parser;
        this.description = description;
    }

    @Override
    public T get() {
        if (initiated) {
            return value;
        }

        synchronized (this) {
            if (initiated) {
                return value;
            }

            String textValue = System.getProperty(prop);
            if (textValue == null) {
                value = null;
            } else {
                try {
                    value = parser.parse(textValue.trim());
                } catch (Exception e) {
                    throw new OptionInvalidException("Failed to parse option " + prop + " value '" + textValue + "'. " +
                            "Please see the description for usage and correct values: " + description, e);
                }
            }
            initiated = true;
            return value;
        }
    }

    @Override
    public String toString() {
        return "Option '" + prop + "', value: " + get();
    }
}
