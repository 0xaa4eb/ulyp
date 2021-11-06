package com.ulyp.ui.code.find

import com.ulyp.ui.code.SourceCode
import com.ulyp.ui.util.StreamDrainer
import java.io.File
import java.io.IOException

open class JarFile(private val file: File) {
    private var jarFile: java.util.jar.JarFile

    companion object {
        private const val CLASS_FILE_EXTENSION = ".class"
        private const val CLASS_SOURCE_FILE_EXTENSION = ".java"
    }

    init {
        try {
            jarFile = java.util.jar.JarFile(file.absolutePath)
        } catch (e: IOException) {
            throw RuntimeException("Could not find jar file: $file", e)
        }
    }

    /**
     * For maven/gradle jar files it's possible to locate jar file with source code
     * @return jar file with source if possible to locate or null otherwise
     */
    fun deriveSourcesJar(): JarFile? {
        val libFolder = file.toPath().parent.parent
        for (p in libFolder.toFile().listFiles()) {
            if (p.isDirectory) {
                for (jarFile in p.listFiles()) {
                    if (jarFile.absolutePath.contains("-sources.jar")) {
                        return JarFile(jarFile)
                    }
                }
            }
        }
        return null
    }

    fun findSourceByClassName(className: String): SourceCode? {
        val zipEntry = jarFile.getEntry(className.replace('.', '/') + CLASS_SOURCE_FILE_EXTENSION)
        if (zipEntry == null) {
            return null
        } else {
            try {
                jarFile.getInputStream(zipEntry).use { inputStream ->
                    return SourceCode(
                        className,
                        String(StreamDrainer.DEFAULT.drain(inputStream))
                    )
                }
            } catch (e: IOException) {
                throw RuntimeException("Could not read jar file: " + e.message, e)
            }
        }
    }

    fun findByteCodeByClassName(className: String): ByteCode? {
        val zipEntry = jarFile.getEntry(className.replace('.', '/') + CLASS_FILE_EXTENSION)
        if (zipEntry == null) {
            return null
        } else {
            try {
                jarFile.getInputStream(zipEntry)
                    .use { inputStream -> return ByteCode(className, StreamDrainer.DEFAULT.drain(inputStream)) }
            } catch (e: IOException) {
                throw RuntimeException("Could not read jar file: " + e.message, e)
            }
        }
    }

    val absolutePath: String
        get() = file.absolutePath
}