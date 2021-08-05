package com.ulyp.ui.util;


import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextBuilder {

    private String text;
    private List<String> clazzes = new ArrayList<>();

    public TextBuilder text(String text) {
        this.text = text;
        return this;
    }

    public TextBuilder style(String clazz) {
        this.clazzes.add(clazz);
        return this;
    }

    public TextBuilder style(CssClass clazz) {
        this.clazzes.addAll(clazz.getCssClasses());
        return this;
    }

    public Text build() {
        Text te = new Text(text);
        clazzes.forEach(cl -> te.getStyleClass().add(cl));
        return te;
    }
}
