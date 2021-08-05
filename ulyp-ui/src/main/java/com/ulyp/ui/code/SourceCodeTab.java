package com.ulyp.ui.code;

import com.ulyp.ui.code.util.MethodLineNumberFinder;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Tab;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.io.IOException;

public class SourceCodeTab extends Tab {

    private final RTextScrollPane textScrollPane;
    private long stamp = 0;

    private final RSyntaxTextArea textArea;

    public SourceCodeTab() {

        textArea = new RSyntaxTextArea();

        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);

        textScrollPane = new RTextScrollPane(textArea);

        Theme theme;
        try {
            theme = Theme.load(getClass().getResourceAsStream("/rsyntax-dark.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            throw new RuntimeException("Could not load theme", e);
        }

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(textScrollPane);

        setContent(swingNode);
    }

    public void setText(SourceCode code, String methodNameToScrollTo) {
        setText(code.getClassName());

        SwingUtilities.invokeLater(
                () -> {

                    synchronized (this) {
                        long genertedStamp = ++this.stamp;
                        this.textArea.setText(code.getCode());

                        SwingUtilities.invokeLater(
                                () -> {
                                    synchronized (this) {
                                        if (this.stamp == genertedStamp) {
                                            int newVerticalPos = Math.max(
                                                    (int) ((new MethodLineNumberFinder(code).getLine(methodNameToScrollTo, 0) - 10) * textScrollPane.getVerticalScrollBar().getMaximum() * 1.0 / code.getLineCount()),
                                                    0
                                            );
                                            this.textScrollPane.getVerticalScrollBar().setValue(newVerticalPos);
                                        }
                                    }
                                }
                        );
                    }
                }
        );
    }
}
