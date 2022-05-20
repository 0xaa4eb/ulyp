package com.ulyp.ui.looknfeel

import com.ulyp.ui.elements.controls.ErrorPopup
import com.ulyp.ui.elements.misc.ExceptionAsText
import javafx.scene.Scene
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Component
class FontSizeChanger(private val applicationContext: ApplicationContext) {
    private var currentFontSize = 1.0
    private val fontChooser = FontNameResolver()

    fun upscale(scene: Scene) {
        currentFontSize += 0.05
        refreshFont(scene, currentFontSize)
    }

    fun downscale(scene: Scene) {
        currentFontSize -= 0.05
        refreshFont(scene, currentFontSize)
    }

    private fun refreshFont(scene: Scene, font: Double) {
        try {

            val path = Files.createTempFile(STYLE_PREFIX, null)
            path.toFile().deleteOnExit()
            Files.write(
                path,
                """
                .ulyp-ctt {
                    -fx-font-family: ${fontChooser.getFontName()};
                    -fx-font-size: ${font}em;
                }
                .ulyp-ctt-identity-hash-code {
                    -fx-font-size: ${font * 0.8}em;
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
                    ErrorPopup::class.java,
                    "Could not change font size: " + e.message,
                    ExceptionAsText(e)
            )
            errorPopup.show()
        }
    }

    companion object {
        private const val STYLE_PREFIX = "ctt-font-style"
    }
}