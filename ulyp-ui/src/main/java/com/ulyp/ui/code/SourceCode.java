package com.ulyp.ui.code;

public class SourceCode {

    private final String className;
    private final String code;

    public SourceCode(String className, String code) {
        this.className = className;
        this.code = code;
    }

    public String getClassName() {
        return className;
    }

    public String getCode() {
        return code;
    }

    public SourceCode prependToSource(String text) {
        return new SourceCode(this.className, text + this.code);
    }

    public int getLineCount() {
        return code.split("\n").length;
    }

    @Override
    public String toString() {
        return "SourceCode{" +
                "className='" + className + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
