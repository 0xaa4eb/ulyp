package com.ulyp.ui;

import com.google.common.base.Preconditions;
import javafx.application.Platform;
import org.springframework.stereotype.Component;

@Component
public class RenderSettings {

    private boolean showTypes = false;

    public boolean showTypes() {
        Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread");
        return showTypes;
    }

    public RenderSettings setShowTypes(boolean showTypes) {
        Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread");
        this.showTypes = showTypes;
        return this;
    }
}
