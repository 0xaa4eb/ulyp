package com.ulyp.ui.code.find

import com.ulyp.core.exception.UlypException
import com.ulyp.ui.code.SourceCode
import com.ulyp.ui.util.file.TmpFile
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class ByteCode(private val className: String, private val bytecode: ByteArray) {

    fun decompile(): SourceCode {
        val classfile = TmpFile("$className.class")
        try {
            FileOutputStream(classfile.path.toFile()).use { fileOutputStream -> fileOutputStream.write(bytecode) }
        } catch (e: Exception) {
            // TODO show warning
            e.printStackTrace()
        }
        ConsoleDecompiler.main(
            arrayOf(
                classfile.path.toAbsolutePath().toString(),
                classfile.path.parent.toAbsolutePath().toString()
            )
        )
        val output = Paths.get(classfile.path.parent.toAbsolutePath().toString(), "$className.java")
        return try {
            SourceCode(className, Files.readAllLines(output).stream().collect(Collectors.joining("\n")))
        } catch (e: IOException) {
            throw UlypException(e)
        } finally {
            output.toFile().delete()
        }
    }
}