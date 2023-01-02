package com.ulyp.ui.looknfeel

import com.ulyp.ui.elements.controls.ErrorModalView
import com.ulyp.ui.elements.misc.ExceptionAsTextView
import javafx.scene.Scene
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Component
class FontSizeChanger(private val applicationContext: ApplicationContext) {

    companion object {
        private const val STYLE_PREFIX = "call-tree-font-style"
    }

    fun refresh(scene: Scene, fontSettings: FontSettings) {
        try {

            val path = Files.createTempFile(STYLE_PREFIX, null)
            path.toFile().deleteOnExit()
            Files.write(
                    path,
                    """
                    .root {
                        -fx-font-size: ${fontSettings.systemFontSize}em;
                    }
                    .ulyp-call-tree {
                        -fx-font-family: ${fontSettings.recordingTreeFontName};
                        -fx-font-size: ${fontSettings.recordingTreeFontSize}em;
                    }
                    .ulyp-call-tree-call-node {
                        -fx-min-height: -1;
                        -fx-max-height: -1;
                    }
                    .ulyp-tree-view {
                        -fx-fixed-cell-size: ${fontSettings.recordingTreeFontSize * 1.7}em;
                    }
                    .ulyp-call-tree-identity-hash-code {
                        -fx-font-size: ${fontSettings.recordingTreeFontSize * 0.8}em;
                    }
                    """.toByteArray(StandardCharsets.UTF_8),
                    StandardOpenOption.WRITE
            )
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
}