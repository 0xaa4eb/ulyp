package com.ulyp.ui.elements.recording.tree

import com.ulyp.core.util.Duration
import com.ulyp.storage.tree.CallRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.elements.recording.objects.RenderedObject.Companion.of
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.TextBuilder
import com.ulyp.ui.util.WithStylesPane
import javafx.scene.Node
import javafx.scene.text.TextFlow
import java.util.function.Consumer

class RecordedCallView(callRecord: CallRecord, renderSettings: RenderSettings) : TextFlow() {

    init {
        val text: MutableList<Node> = ArrayList()
        if (renderSettings.showTimestamps) {
            text.addAll(renderTimestamp(callRecord))
        }
        text.addAll(renderReturnValue(callRecord, renderSettings))
        text.addAll(renderCallee(callRecord, renderSettings))
        text.addAll(renderArguments(callRecord, renderSettings))
        children.addAll(text)
    }

    private fun text(): TextBuilder {
        return TextBuilder()
    }

    private fun renderReturnValue(node: CallRecord, renderSettings: RenderSettings): List<Node> {
        return if (node.method.returnsSomething() || node.hasThrown()) {
            val output: MutableList<Node> = ArrayList()
            var returnValue = WithStylesPane(
                    of(node.returnValue, renderSettings),
                    Style.CALL_TREE,
                    Style.CALL_TREE_RETURN_VALUE
            ).get()
            if (node.hasThrown()) {
                returnValue = WithStylesPane(
                        returnValue,
                        Style.CALL_TREE,
                        Style.CALL_TREE_THROWN
                ).get()
                output.add(
                        text().text("\uD83D\uDDF2")
                                .style(Style.CALL_TREE)
                                .style(Style.CALL_TREE_THROWN)
                                .build()
                )
            }
            output.add(returnValue)
            output.add(
                    text().text(" : ")
                            .style(Style.CALL_TREE)
                            .style(Style.CALL_TREE_NODE_SEPARATOR)
                            .build()
            )
            output
        } else {
            emptyList()
        }
    }

    private fun renderArguments(node: CallRecord, renderSettings: RenderSettings): List<Node> {
        val output: MutableList<Node> = ArrayList()
        output.add(text().text("(")
                .style(Style.CALL_TREE)
                .style(Style.CALL_TREE_NODE_SEPARATOR)
                .build()
        )
        for (i in node.args.indices) {
            val argValue = node.args[i]
            output.add(of(argValue, renderSettings))
            if (i < node.args.size - 1) {
                output.add(text().text(", ")
                        .style(Style.CALL_TREE)
                        .style(Style.CALL_TREE_NODE_SEPARATOR)
                        .build())
            }
        }
        output.add(text().text(")")
                .style(Style.CALL_TREE)
                .style(Style.CALL_TREE_NODE_SEPARATOR)
                .build())
        return output
    }

    private fun renderCallee(node: CallRecord, renderSettings: RenderSettings): List<Node> {
        val result: MutableList<Node> = ArrayList()

        if (node.method.isStatic) {
            result.add(
                    text().text(
                            if (renderSettings.showTypes) {
                                node.method.declaringType.name
                            } else {
                                toSimpleName(node.method.declaringType.name)
                            })
                            .style(Style.CALL_TREE)
                            .style(Style.CALL_TREE_METHOD_NAME)
                            .build()
            )
        } else {
            val callee = of(node.callee, renderSettings)
            callee.children.forEach(
                    Consumer { child: Node ->
                        child.styleClass.addAll(Style.CALL_TREE.cssClasses)
                        child.styleClass.addAll(Style.CALL_TREE_CALLEE.cssClasses)
                    }
            )
            result.add(callee)
        }
        result.add(
                text().text(".")
                        .style(Style.CALL_TREE)
                        .style(Style.CALL_TREE_METHOD_NAME)
                        .build()
        )

        var methodNameBuilder = text().text(node.method.name)
                .style(Style.CALL_TREE)
                .style(Style.CALL_TREE_METHOD_NAME)

        if (node.method.isStatic) {
            methodNameBuilder = methodNameBuilder
                    .style(Style.CALL_TREE)
                    .style(Style.CALL_TREE_METHOD_NAME)
        }
        result.add(methodNameBuilder.build())
        return result
    }

    private fun renderTimestamp(node: CallRecord): List<Node> {
        val result: MutableList<Node> = ArrayList()
        result.add(text().text("${Duration(node.nanosDuration)} ")
                .style(Style.CALL_TREE_TIMESTAMP)
                .build())
        return result
    }
}