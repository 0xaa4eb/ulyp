package com.ulyp.ui.util

object ClassNameUtils {
    @JvmStatic
    fun toSimpleName(name: String): String {
        val lastDot = name.lastIndexOf('.')
        return if (lastDot > 0) {
            name.substring(lastDot + 1)
        } else {
            name
        }
    }
}