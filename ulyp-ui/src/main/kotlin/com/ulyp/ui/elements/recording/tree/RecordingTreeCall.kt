package com.ulyp.ui.elements.recording.tree

import com.ulyp.storage.CallRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.elements.recording.objects.RecordedObject.Companion.of
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.TextBuilder
import com.ulyp.ui.util.WithStylesPane
import javafx.scene.Node
import javafx.scene.text.TextFlow
import java.util.function.Consumer

class RecordingTreeCall(node: CallRecord, renderSettings: RenderSettings) : TextFlow() {

    companion object {
        private fun text(): TextBuilder {
            return TextBuilder()
        }

        private fun renderReturnValue(node: CallRecord, renderSettings: RenderSettings): List<Node> {
            return if (node.method.returnsSomething() || node.hasThrown()) {
                val output: MutableList<Node> = ArrayList()
                var returnValue = WithStylesPane(
                        of(node.returnValue, renderSettings),
                        CssClass.CALL_TREE_ALL_CSS,
                        CssClass.CALL_TREE_RETURN_VALUE_CSS
                ).get()
                if (node.hasThrown()) {
                    returnValue = WithStylesPane(
                        returnValue,
                        CssClass.CALL_TREE_ALL_CSS,
                        CssClass.CALL_TREE_THROWN_CSS
                    ).get()
                    output.add(
                        text().text("\uD83D\uDDF2")
                            .style(CssClass.CALL_TREE_ALL_CSS)
                            .style(CssClass.CALL_TREE_THROWN_CSS)
                            .build()
                    )
                }
                output.add(returnValue)
                output.add(
                        text().text(" : ")
                                .style(CssClass.CALL_TREE_ALL_CSS)
                                .style(CssClass.CALL_TREE_NODE_SEPARATOR_CSS)
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
                    .style(CssClass.CALL_TREE_ALL_CSS)
                    .style(CssClass.CALL_TREE_NODE_SEPARATOR_CSS)
                    .build()
            )
            for (i in node.args.indices) {
                val argValue = node.args[i]
                output.add(of(argValue, renderSettings))
                if (i < node.args.size - 1) {
                    output.add(text().text(", ")
                            .style(CssClass.CALL_TREE_ALL_CSS)
                            .style(CssClass.CALL_TREE_NODE_SEPARATOR_CSS)
                            .build())
                }
            }
            output.add(text().text(")")
                    .style(CssClass.CALL_TREE_ALL_CSS)
                    .style(CssClass.CALL_TREE_NODE_SEPARATOR_CSS)
                    .build())
            return output
        }

        private fun renderCallee(node: CallRecord, renderSettings: RenderSettings): List<Node> {
            val result: MutableList<Node> = ArrayList()

            if (node.method.isStatic || node.method.isConstructor) {
                result.add(
                        text().text(toSimpleName(node.method.declaringType.name))
                                .style(CssClass.CALL_TREE_ALL_CSS)
                                .style(CssClass.CALL_TREE_METHOD_NAME_CSS)
                                .build()
                )
            } else {
                val callee = of(node.callee, renderSettings)
                callee.children.forEach(
                        Consumer { child: Node ->
                            child.styleClass.addAll(CssClass.CALL_TREE_ALL_CSS.cssClasses)
                            child.styleClass.addAll(CssClass.CALL_TREE_CALLEE_CSS.cssClasses)
                        }
                )
                result.add(callee)
            }

            result.add(
                    text().text(".")
                            .style(CssClass.CALL_TREE_ALL_CSS)
                            .style(CssClass.CALL_TREE_METHOD_NAME_CSS)
                            .build()
            )

            var methodNameBuilder = text().text(node.method.name)
                    .style(CssClass.CALL_TREE_ALL_CSS)
                    .style(CssClass.CALL_TREE_METHOD_NAME_CSS)

            if (node.method.isStatic) {
                methodNameBuilder = methodNameBuilder
                        .style(CssClass.CALL_TREE_ALL_CSS)
                        .style(CssClass.CALL_TREE_METHOD_NAME_CSS)
            }
            result.add(methodNameBuilder.build())
            return result
        }
    }

    init {
        val text: MutableList<Node> = ArrayList()
        text.addAll(renderReturnValue(node, renderSettings))
        text.addAll(renderCallee(node, renderSettings))
        text.addAll(renderArguments(node, renderSettings))
        children.addAll(text)
    }
}