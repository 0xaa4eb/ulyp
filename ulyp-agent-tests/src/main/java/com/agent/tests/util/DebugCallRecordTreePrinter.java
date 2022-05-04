package com.agent.tests.util;

import com.ulyp.storage.CallRecord;

import java.util.List;

public class DebugCallRecordTreePrinter {

    private static final int MAX_CALLS_PER_LEVEL_PRINTED = 10;
    private static final int MAX_DEPTH_PRINTED = 3;

    public static String printTree(CallRecord root) {
        StringBuilder builder = new StringBuilder();
        printTreeImpl(root, builder, 0);
        return builder.toString();
    }

    private static void printTreeImpl(CallRecord node, StringBuilder builder, int depth) {
        if (depth > MAX_DEPTH_PRINTED) {
            return;
        }

        List<CallRecord> children = node.getChildren();
        int maxSize = Math.min(children.size(), MAX_CALLS_PER_LEVEL_PRINTED);
        StringBuilder depthPadding = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            depthPadding.append("  ");
        }

        for (int i = 0; i < maxSize; i++) {
            CallRecord child = children.get(i);
            builder.append(depthPadding);
            printCallRecordContent(child, builder);
            builder.append("\n");
            printTreeImpl(child, builder, depth + 1);
        }

        if (children.size() > maxSize) {
            builder.append(depthPadding);
            builder.append("...");
            builder.append("\n");
        }
    }

    private static void printCallRecordContent(CallRecord node, StringBuilder builder) {

        builder.append(node.getReturnValue())
                .append(" ")
                .append(node.getCallee())
                .append(".")
                .append(node.getMethod().getName())
                .append("(")
                .append(node.getArgs())
                .append(")");

    }
}
