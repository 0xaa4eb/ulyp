package com.ulyp.ui.code.find

import com.ulyp.ui.code.SourceCode
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Predicate
import java.util.stream.Collectors

class SourceCodeFinder(classpath: List<String?>) {

    private val jars: MutableList<JarFile>
    private val decompilingExecutorService = Executors.newFixedThreadPool(2)

    fun find(javaClassName: String?): CompletableFuture<SourceCode> {
        for (jar in jars) {
            val sourceCode = jar.findSourceByClassName(javaClassName!!)
            if (sourceCode != null) {
                return CompletableFuture.completedFuture(sourceCode)
            }
        }
        val result = CompletableFuture<SourceCode>()
        decompilingExecutorService.execute {
            for (jar in jars) {
                val byteCode = jar.findByteCodeByClassName(javaClassName!!)
                if (byteCode != null) {
                    result.complete(
                        byteCode.decompile()
                            .prependToSource(String.format("// Decompiled from: %s \n", jar.absolutePath))
                    )
                }
            }
            result.complete(SourceCode("", ""))
        }
        return result
    }

    init {
        jars = classpath.stream()
            .filter { x: String? -> File(x).exists() }
            .filter { x: String? -> File(x).name.endsWith(".jar") }
            .filter { x: String? -> !File(x).isDirectory }
            .map { x: String? ->
                try {
                    return@map JarFile(Paths.get(x).toFile())
                } catch (e: Exception) {
                    return@map null
                }
            }
            .filter(Predicate { obj: JarFile? -> Objects.nonNull(obj) })
            .collect(Collectors.toList())
        val sourcesJars: MutableList<JarFile> = ArrayList()
        for (jarFile in jars) {
            try {
                val sourcesJar = jarFile.deriveSourcesJar()
                if (sourcesJar != null) {
                    sourcesJars.add(sourcesJar)
                }
            } catch (e: Exception) {
                println("Could not open derive sources jar for $jarFile")
            }
        }
        jars.addAll(0, sourcesJars)
    }
}