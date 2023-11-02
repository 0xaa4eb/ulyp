package com.ulyp.ui.reader

import com.ulyp.core.util.FileUtil
import com.ulyp.storage.Filter
import com.ulyp.storage.tree.Index
import com.ulyp.storage.ReaderSettings
import com.ulyp.storage.RecordingDataReader
import com.ulyp.storage.impl.AsyncFileRecordingDataReader
import com.ulyp.storage.tree.InMemoryIndex
import com.ulyp.storage.tree.RocksdbIndex
import com.ulyp.storage.util.RocksdbChecker
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Component
class RecordingReaderRegistry(private val filterRegistry: FilterRegistry) {

    private val readers = mutableSetOf<RecordingDataReader>()

    @Synchronized
    fun newReader(file: File): RecordingDataReader {
        val rocksdbAvailable = RocksdbChecker.checkRocksdbAvailable()
        val readerDirectory = Files.createTempDirectory("ulyp.Reader")
        val index: Index = if (rocksdbAvailable.isAvailable) {
            RocksdbIndex(readerDirectory)
        } else {
            InMemoryIndex()
        }

        val storageFilter = filterRegistry.filter?.toStorageFilter() ?: Filter.defaultFilter()

        val recordingDataReader =
            AsyncFileRecordingDataReader(
                ReaderSettings.builder()
                    .file(file)
                    .autoStartReading(false)
                    .indexSupplier { index }
                    .filter(storageFilter)
                    .build()
            )

        readers.add(recordingDataReader)
        CloseReaderOnExitHook.add(Pair(readerDirectory, recordingDataReader))

        return recordingDataReader
    }

    private object CloseReaderOnExitHook {
        private var readers = mutableListOf<Pair<Path, RecordingDataReader>>()

        init {
            Runtime.getRuntime().addShutdownHook(Thread { runHooks() })
        }

        @Synchronized
        fun add(readerEntry: Pair<Path, RecordingDataReader>) {
            readers.add(readerEntry)
        }

        @Synchronized
        fun runHooks() {
            readers.forEach {
                try {
                    it.second.close()
                } catch (e: Exception) {
                    // TODO setup logging
                } finally {
                    FileUtil.deleteDirectory(it.first)
                }
            }
        }
    }
}