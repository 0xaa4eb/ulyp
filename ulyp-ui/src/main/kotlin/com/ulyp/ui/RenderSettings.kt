package com.ulyp.ui

import com.google.common.base.Preconditions
import javafx.application.Platform
import org.springframework.stereotype.Component

@Component
class RenderSettings {

    var showTypes = false
        get() {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            return field
        }
        set(value) {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            field = value
        }
}