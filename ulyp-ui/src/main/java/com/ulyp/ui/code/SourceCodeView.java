package com.ulyp.ui.code;

import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;

@Component
public class SourceCodeView extends TabPane {

    private final SourceCodeTab mainTab;

    public SourceCodeView() {

        this.mainTab = new SourceCodeTab();
        this.getTabs().add(mainTab);
    }

    public void setText(SourceCode code, String methodNameToScrollTo) {
        this.mainTab.setText(code, methodNameToScrollTo);
    }
}
