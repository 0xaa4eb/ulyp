package com.ulyp.ui.code

class SourceCode(val className: String, val code: String) {
    fun prependToSource(text: String): SourceCode {
        return SourceCode(className, text + this.code)
    }

    fun getLineCount(): Int {
        return code.split("\n".toRegex()).toTypedArray().size
    }

    override fun toString(): String {
        return "SourceCode{" +
                "className='" + className + '\'' +
                ", code='" + code + '\'' +
                '}'
    }
}