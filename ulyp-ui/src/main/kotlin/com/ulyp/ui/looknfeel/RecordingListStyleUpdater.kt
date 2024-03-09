package com.ulyp.ui.looknfeel

import com.ulyp.ui.elements.controls.ErrorModalView
import com.ulyp.ui.elements.misc.ExceptionAsTextView
import com.ulyp.ui.settings.Settings
import javafx.scene.Scene
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Component
class RecordingListStyleUpdater(private val applicationContext: ApplicationContext) {

    companion object {
        private const val STYLE_PREFIX = "recording-list-stylesheet"
    }

    private fun buildStyleSheet(settings: Settings): String {
        return """
        .ulyp-recording-list-item {
            -fx-padding: ${settings.recordingListSpacing.get()}px;
        }
        """
    }

    fun update(scene: Scene, settings: Settings) {
        try {

            val path = Files.createTempFile(STYLE_PREFIX, null)
            path.toFile().deleteOnExit()
            Files.write(path, buildStyleSheet(settings).toByteArray(StandardCharsets.UTF_8), StandardOpenOption.WRITE)
            var index: Int? = null
            for (i in scene.stylesheets.indices) {
                if (scene.stylesheets[i].contains(STYLE_PREFIX)) {
                    index = i
                    break
                }
            }
            if (index != null) {
                scene.stylesheets[index] = path.toFile().toURI().toString()
            } else {
                scene.stylesheets.add(path.toFile().toURI().toString())
            }
        } catch (e: IOException) {

            val errorPopup = applicationContext.getBean(
                    ErrorModalView::class.java,
                    "Could not change font size: " + e.message,
                    ExceptionAsTextView(e)
            )
            errorPopup.show()
        }
    }

    fun update(scenes: List<Scene>, settings: Settings) {
        scenes.forEach { update(it, settings) }
    }
}