package com.ulyp.ui.code.find

import java.io.File
import java.nio.file.Path

class GradleJarFile protected constructor(path: File) : JarFile(path) {
    companion object {
        protected fun isGradleJarFile(path: Path): Boolean {
            return path.toString().contains(".gradle")
        }
    }
}