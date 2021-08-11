package com.ulyp.ui

import com.google.common.base.Preconditions
import javafx.application.Platform
import org.springframework.stereotype.Component

@Component
class RenderSettings {

    private var showTypes = false

    fun showTypes(): Boolean {
        Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
        return showTypes
    }

    fun setShowTypes(showTypes: Boolean): RenderSettings {
        Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
        this.showTypes = showTypes
        return this
    }
}