package com.ulyp.ui.util.file

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class TmpFile(name: String) {
    val path: Path = Paths.get(System.getProperty("java.io.tmpdir"), name)

    init {
        File(name).deleteOnExit()
    }
}