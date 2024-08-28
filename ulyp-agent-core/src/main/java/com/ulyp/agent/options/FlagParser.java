package com.ulyp.agent.options;

public class FlagParser implements Parser<Boolean> {

    @Override
    public Boolean parse(String text) {
        if (text != null) {
            if (text.trim().isEmpty()) {
                return true;
            } else {
                return Boolean.parseBoolean(text.trim());
            }
        }
        return false;
    }
}
