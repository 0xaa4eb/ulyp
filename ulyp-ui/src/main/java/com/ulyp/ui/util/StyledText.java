package com.ulyp.ui.util;

import javafx.scene.text.Text;

public class StyledText {

    public static Text of(String content, CssClass style) {
        Text text = new Text(content);
        text.getStyleClass().addAll(style.getCssClasses());
        return text;
    }
}
