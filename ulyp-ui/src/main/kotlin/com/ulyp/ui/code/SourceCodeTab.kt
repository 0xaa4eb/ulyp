package com.ulyp.ui.code

import com.ulyp.ui.code.util.MethodLineNumberFinder
import com.ulyp.ui.settings.Settings
import javafx.embed.swing.SwingNode
import javafx.scene.control.Tab
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.awt.Font
import java.awt.event.KeyListener
import java.io.IOException
import javax.swing.SwingUtilities
import kotlin.math.max

@Component
open class SourceCodeTab(@Autowired private val settings: Settings) : Tab() {
    private val textScrollPane: RTextScrollPane
    private val textArea: RSyntaxTextArea = RSyntaxTextArea()

    private var stamp: Long = 0
    // TODO move to Settings
    private var font = 15

    init {
        textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVA
        textArea.isCodeFoldingEnabled = true
        textArea.text = ""
        textArea.isEditable = false
        setTheme(com.ulyp.ui.looknfeel.Theme.DARK.rsyntaxThemePath)
        textScrollPane = RTextScrollPane(textArea)
        val swingNode = SwingNode()
        swingNode.content = textScrollPane
        content = swingNode

        textArea.addKeyListener(
                object : KeyListener {
                    override fun keyTyped(e: java.awt.event.KeyEvent) {
                        if (e.keyChar == '=') {
                            SwingUtilities.invokeLater {
                                synchronized(this) {
                                    // TODO move to Settings
                                    font++
                                    textArea.font = Font(settings.recordingTreeFontName.value, Font.PLAIN, font)
                                }
                            }
                        }
                        if (e.keyChar == '-') {
                            SwingUtilities.invokeLater {
                                synchronized(this) {
                                    font--
                                    textArea.font = Font(settings.recordingTreeFontName.value, Font.PLAIN, font)
                                }
                            }
                        }
                    }

                    override fun keyPressed(e: java.awt.event.KeyEvent) {

                    }

                    override fun keyReleased(e: java.awt.event.KeyEvent) {

                    }
                }
        )
    }

    fun setText(code: SourceCode, methodNameToScrollTo: String?) {
        text = code.className
        SwingUtilities.invokeLater {
            synchronized(this) {
                val genertedStamp = ++stamp
                textArea.text = code.code
                SwingUtilities.invokeLater {
                    synchronized(this) {
                        if (stamp == genertedStamp) {
                            val newVerticalPos = max(
                                    ((MethodLineNumberFinder(code).getLine(
                                            methodNameToScrollTo!!,
                                            0
                                    ) - 10) * textScrollPane.verticalScrollBar
                                            .maximum * 1.0 / code.getLineCount()).toInt(),
                                    0
                            )
                            textScrollPane.verticalScrollBar.value = newVerticalPos
                        }
                    }
                }
            }
        }
    }

    fun setTheme(themePath: String) {
        SwingUtilities.invokeLater {
            synchronized(this) {
                val theme: Theme
                try {
                    theme = Theme.load(javaClass.getResourceAsStream(themePath))
                    theme.apply(textArea)
                } catch (e: IOException) {
                    throw RuntimeException("Could not load theme", e)
                }
            }
        }
    }
}