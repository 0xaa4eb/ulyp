package com.ulyp.ui.code

import javafx.scene.control.TabPane
import org.springframework.stereotype.Component

@Component
class SourceCodeView : TabPane() {

    private val mainTab: SourceCodeTab = SourceCodeTab()

    fun setText(code: SourceCode?, methodNameToScrollTo: String?) {
        mainTab.setText(code!!, methodNameToScrollTo)
    }

    init {
        tabs.add(mainTab)
    }
}