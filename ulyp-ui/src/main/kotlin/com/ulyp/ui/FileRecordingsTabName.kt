package com.ulyp.ui

import com.ulyp.transport.ProcessInfo
import java.io.File

class FileRecordingsTabName(private val file: File, private val processInfo: ProcessInfo) {
    override fun toString(): String {
        return String.format("%s: %s", file.absolutePath, processInfo.mainClassName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileRecordingsTabName

        if (file != other.file) return false
        if (processInfo != other.processInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + processInfo.hashCode()
        return result
    }


}