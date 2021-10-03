package com.ulyp.ui.code

import com.ulyp.ui.code.util.MethodLineNumberFinder
import javafx.embed.swing.SwingNode
import javafx.scene.control.Tab
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import org.springframework.stereotype.Component
import java.io.IOException
import javax.swing.SwingUtilities

@Component
class SourceCodeTab : Tab() {
    private val textScrollPane: RTextScrollPane
    private var stamp: Long = 0
    private val textArea: RSyntaxTextArea = RSyntaxTextArea()

    init {
        textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVA
        textArea.isCodeFoldingEnabled = true
        textArea.text = ""
        setTheme(com.ulyp.ui.looknfeel.Theme.DARK.rsyntaxThemePath)
        textScrollPane = RTextScrollPane(textArea)
        val swingNode = SwingNode()
        swingNode.content = textScrollPane
        content = swingNode
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
                            val newVerticalPos = Math.max(
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
        val theme: Theme
        try {
            theme = Theme.load(javaClass.getResourceAsStream(themePath))
            theme.apply(textArea)
        } catch (e: IOException) {
            throw RuntimeException("Could not load theme", e)
        }
    }
}