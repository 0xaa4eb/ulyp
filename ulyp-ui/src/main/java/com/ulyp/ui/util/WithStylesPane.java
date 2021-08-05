package com.ulyp.ui.util;

import javafx.scene.layout.Pane;

import java.util.Arrays;

public class WithStylesPane<T extends Pane> {

    private final T pane;

    public WithStylesPane(T pane, CssClass... classes) {
        pane.getChildren().forEach(child -> Arrays.stream(classes).forEach(style -> child.getStyleClass().addAll(style.getCssClasses())));

        this.pane = pane;
    }

    public T get() {
        return pane;
    }
}
