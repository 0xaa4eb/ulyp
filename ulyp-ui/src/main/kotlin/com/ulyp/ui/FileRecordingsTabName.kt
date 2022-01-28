package com.ulyp.ui

import com.ulyp.core.ProcessMetadata
import java.io.File

class FileRecordingsTabName(private val file: File, private val processMetadata: ProcessMetadata) {
    override fun toString(): String {
        return String.format("%s: %s", file.absolutePath, processMetadata.mainClassName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileRecordingsTabName

        if (file != other.file) return false
        if (processMetadata != other.processMetadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + processMetadata.hashCode()
        return result
    }


}