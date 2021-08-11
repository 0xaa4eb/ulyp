package com.ulyp.ui.code.util

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.ulyp.ui.code.SourceCode

class MethodLineNumberFinder(private val sourceCode: SourceCode) {

    fun getLine(methodName: String, defaultValue: Int): Int {

        val parsed : CompilationUnit = StaticJavaParser.parse(sourceCode.code)

        val voidVisitorAdapter = object : VoidVisitorAdapter<MutableList<MethodDeclaration>>() {
                override fun visit(method: MethodDeclaration, collector: MutableList<MethodDeclaration>) {
                    super.visit(method, collector)
                    if (methodName == method.name.asString() && method.range.isPresent) {
                        collector.add(method)
                    }
                }
        }
        val methods : MutableList<MethodDeclaration> = ArrayList()
        voidVisitorAdapter.visit(parsed, methods)
        return if (methods.isEmpty()) {
            defaultValue
        } else {
            methods[0].range.get().begin.line
        }
    }
}