package com.ulyp.ui.font

import javafx.scene.Scene
import javafx.scene.text.Font
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import javax.annotation.PostConstruct

@Component
class FontSizeChanger {
    private var currentFontSize = 14
    private var fontFamily: String? = null

    @PostConstruct
    fun init() {
        fontFamily = getFontFamily()
    }

    private fun getFontFamily(): String {
        val families = Font.getFamilies()
        if (families.contains("Monaco")) {
            return "Monaco"
        }
        if (families.contains("Consolas")) {
            return "Consolas"
        }
        return if (families.contains("Monospaced")) {
            "Monospaced"
        } else Font.getDefault().family
    }

    fun upscale(scene: Scene) {
        val font = ++currentFontSize
        refreshFont(scene, font)
    }

    fun downscale(scene: Scene) {
        val font = --currentFontSize
        refreshFont(scene, font)
    }

    private fun refreshFont(scene: Scene, font: Int) {
        try {
            val path = Files.createTempFile(STYLE_PREFIX, null)
            path.toFile().deleteOnExit()
            Files.write(
                path,
                """.ulyp-ctt {
    -fx-font-family: $fontFamily;
    -fx-font-size: ${font}px;
}""".toByteArray(StandardCharsets.UTF_8),
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
            e.printStackTrace()
        }
    }

    companion object {
        private const val STYLE_PREFIX = "ulyp-ctt-font-style"
    }
}