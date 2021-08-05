package com.ulyp.ui.code.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.ulyp.ui.code.SourceCode;

import java.util.ArrayList;
import java.util.List;

public class MethodLineNumberFinder {

    private final SourceCode sourceCode;

    public MethodLineNumberFinder(SourceCode sourceCode) {
        this.sourceCode = sourceCode;
    }

    public int getLine(String methodName, int defaultValue) {
        CompilationUnit parsed = StaticJavaParser.parse(sourceCode.getCode());

        VoidVisitorAdapter<List<MethodDeclaration>> voidVisitorAdapter = new VoidVisitorAdapter<List<MethodDeclaration>>() {
            public void visit(MethodDeclaration method, List<MethodDeclaration> collector) {
                super.visit(method, collector);

                if (methodName.equals(method.getName().asString()) && method.getRange().isPresent()) {
                    collector.add(method);
                }
            }
        };

        List<MethodDeclaration> methods = new ArrayList<>();

        voidVisitorAdapter.visit(parsed, methods);

        if (methods.isEmpty()) {
            return defaultValue;
        } else {
            //noinspection OptionalGetWithoutIsPresent
            return methods.get(0).getRange().get().begin.line;
        }
    }
}
