package com.ulyp.ui

import com.ulyp.storage.CallRecord
import com.ulyp.ui.renderers.RenderedObject.Companion.of
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
            return if (!node.isVoidMethod || node.hasThrown()) {
                val output: MutableList<Node> = ArrayList()
                var renderedObject = WithStylesPane(
                        of(node.returnValue, renderSettings),
                        CssClass.CALL_TREE_ALL,
                        CssClass.CALL_TREE_RETURN_VALUE
                ).get()
                if (node.hasThrown()) {
                    renderedObject = WithStylesPane(
                            renderedObject,
                            CssClass.CALL_TREE_ALL,
                            CssClass.CALL_TREE_THROWN
                    ).get()
                }
                output.add(renderedObject)
                output.add(
                        text().text(" : ")
                                .style(CssClass.CALL_TREE_ALL)
                                .style(CssClass.CALL_TREE_NODE_SEPARATOR)
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
                    .style(CssClass.CALL_TREE_ALL)
                    .style(CssClass.CALL_TREE_NODE_SEPARATOR)
                    .build()
            )
            for (i in node.args.indices) {
                val argValue = node.args[i]
                output.add(of(argValue, renderSettings))
                if (i < node.args.size - 1) {
                    output.add(text().text(", ")
                            .style(CssClass.CALL_TREE_ALL)
                            .style(CssClass.CALL_TREE_NODE_SEPARATOR)
                            .build())
                }
            }
            output.add(text().text(")")
                    .style(CssClass.CALL_TREE_ALL)
                    .style(CssClass.CALL_TREE_NODE_SEPARATOR)
                    .build())
            return output
        }

        private fun renderCallee(node: CallRecord, renderSettings: RenderSettings): List<Node> {
            val result: MutableList<Node> = ArrayList()

            if (node.isStatic || node.isConstructor) {
                result.add(
                        text().text(toSimpleName(node.className))
                                .style(CssClass.CALL_TREE_ALL)
                                .style(CssClass.CALL_TREE_METHOD_NAME)
                                .build()
                )
            } else {
                val callee = of(node.callee, renderSettings)
                callee.children.forEach(
                        Consumer { child: Node ->
                            child.styleClass.addAll(CssClass.CALL_TREE_ALL.cssClasses)
                            child.styleClass.addAll(CssClass.CALL_TREE_CALLEE.cssClasses)
                        }
                )
                result.add(callee)
            }

            result.add(
                    text().text(".")
                            .style(CssClass.CALL_TREE_ALL)
                            .style(CssClass.CALL_TREE_METHOD_NAME)
                            .build()
            )

            var methodNameBuilder = text().text(node.methodName)
                    .style(CssClass.CALL_TREE_ALL)
                    .style(CssClass.CALL_TREE_METHOD_NAME)

            if (node.isStatic) {
                methodNameBuilder = methodNameBuilder
                        .style(CssClass.CALL_TREE_ALL)
                        .style(CssClass.CALL_TREE_METHOD_NAME)
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