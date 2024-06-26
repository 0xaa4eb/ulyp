package com.ulyp.ui

import com.ulyp.core.util.Preconditions
import com.ulyp.ui.settings.RecordedCallWeightType
import com.ulyp.ui.settings.Settings
import javafx.application.Platform
import org.springframework.stereotype.Component

@Component
class RenderSettings(val settings: Settings) {

    var showTypes = false
        get() {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            return field
        }
        set(value) {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            field = value
        }
    var showTimestamps = false
        get() {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            return field
        }
        set(value) {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            field = value
        }
    var recordedCallWeightType: RecordedCallWeightType = settings.getRecordedCallWeightType()
        get() {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            return field
        }
        set(value) {
            Preconditions.checkState(Platform.isFxApplicationThread(), "Not in FX application thread")
            field = value
        }
}