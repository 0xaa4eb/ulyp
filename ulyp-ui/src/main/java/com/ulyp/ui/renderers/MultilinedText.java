package com.ulyp.ui.renderers;

import javafx.scene.text.Text;

public class MultilinedText extends Text {

    public MultilinedText(String value) {
        super(trimText(value));
    }

    static String trimText(String text) {
        if (text.length() < 100) {
            return text;
        }
        StringBuilder output = new StringBuilder(text.length() + 10);
        for (int i = 0; i < text.length(); i++) {
            if (i % 100 == 0) {
                output.append("\n");
            }
            output.append(text.charAt(i));
        }
        return output.toString();
    }
}
