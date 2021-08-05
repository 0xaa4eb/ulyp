package com.ulyp.ui;

import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.ObjectRepresentation;
import com.ulyp.ui.renderers.RenderedObject;
import com.ulyp.ui.util.ClassNameUtils;
import com.ulyp.ui.util.CssClass;
import com.ulyp.ui.util.TextBuilder;
import com.ulyp.ui.util.WithStylesPane;
import javafx.scene.Node;
import javafx.scene.text.TextFlow;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ulyp.ui.util.CssClass.*;

public class RenderedCallRecord extends TextFlow {

    public RenderedCallRecord(CallRecord node, RenderSettings renderSettings) {
        List<Node> text = new ArrayList<>();

        text.addAll(renderReturnValue(node, renderSettings));
        text.addAll(renderMethodName(node, renderSettings));
        text.addAll(renderArguments(node, renderSettings));

        getChildren().addAll(text);
    }

    private static TextBuilder text() {
        return new TextBuilder();
    }

    private static List<Node> renderReturnValue(CallRecord node, RenderSettings renderSettings) {
        if (!node.isVoidMethod() || node.hasThrown()) {

            List<Node> output = new ArrayList<>();

            RenderedObject renderedObject = new WithStylesPane<>(RenderedObject.of(node.getReturnValue(), renderSettings), CALL_TREE_RETURN_VALUE).get();

            if (node.hasThrown()) {
                renderedObject = new WithStylesPane<>(renderedObject, CALL_TREE_THROWN).get();
            }

            output.add(renderedObject);
            output.add(text().text(" : ").style(CALL_TREE_PLAIN_TEXT).build());
            return output;
        } else {
            return Collections.emptyList();
        }
    }

    private static List<Node> renderArguments(CallRecord node, RenderSettings renderSettings) {
        boolean hasParameterNames = !node.getParameterNames().isEmpty() && node.getParameterNames().stream().noneMatch(name -> name.startsWith("arg"));

        List<Node> output = new ArrayList<>();
        output.add(text().text("(").style(CALL_TREE_PLAIN_TEXT).build());

        for (int i = 0; i < node.getArgs().size(); i++) {
            ObjectRepresentation argValue = node.getArgs().get(i);

            if (hasParameterNames) {
                output.add(text().text(node.getParameterNames().get(i)).style(CALL_TREE_ARG_NAME).build());
                output.add(text().text(": ").style(CALL_TREE_PLAIN_TEXT).build());
            }

            output.add(RenderedObject.of(argValue, renderSettings));

            if (i < node.getArgs().size() - 1) {
                output.add(text().text(", ").style(CALL_TREE_PLAIN_TEXT).build());
            }
        }

        output.add(text().text(")").style(CALL_TREE_PLAIN_TEXT).build());
        return output;
    }

    @NotNull
    private static List<Node> renderMethodName(CallRecord node, RenderSettings renderSettings) {
        List<Node> result = new ArrayList<>();

        if (node.isStatic() || node.isConstructor()) {
            result.add(text().text(ClassNameUtils.toSimpleName(node.getClassName())).style(CALL_TREE_METHOD_NAME).build());
        } else {
            RenderedObject callee = RenderedObject.of(node.getCallee(), renderSettings);
            callee.getChildren().forEach(child -> child.getStyleClass().addAll(CALL_TREE_CALLEE.getCssClasses()));
            result.add(callee);
        }
        result.add(text().text(".").style(CssClass.CALL_TREE_METHOD_NAME).build());

        TextBuilder methodNameBuilder = text().text(node.getMethodName()).style(CssClass.CALL_TREE_METHOD_NAME);
        if (node.isStatic()) {
            methodNameBuilder = methodNameBuilder.style(CssClass.CALL_TREE_METHOD_NAME);
        }
        result.add(methodNameBuilder.build());
        return result;
    }
}
