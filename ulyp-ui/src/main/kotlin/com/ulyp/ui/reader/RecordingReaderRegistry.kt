package com.ulyp.ui.reader

import com.ulyp.core.util.FileUtil
import com.ulyp.storage.tree.Filter
import com.ulyp.storage.reader.RecordingDataReader
import com.ulyp.storage.reader.FileRecordingDataReaderBuilder
import com.ulyp.storage.tree.CallRecordTree
import com.ulyp.storage.tree.CallRecordTreeBuilder
import com.ulyp.storage.tree.InMemoryIndex
import com.ulyp.storage.tree.Index
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
    fun newCallRecordTree(file: File): CallRecordTree? {
        val recordingDataReader = FileRecordingDataReaderBuilder(file).build()
        val processMetadata = recordingDataReader.processMetadata ?: return null

        val rocksdbAvailable = RocksdbChecker.checkRocksdbAvailable()
        val readerDirectory = Files.createTempDirectory("ulyp.Reader")
        val index: Index = if (rocksdbAvailable.value()) {
            RocksdbIndex(readerDirectory)
        } else {
            InMemoryIndex()
        }

        val storageFilter = filterRegistry.filter?.toStorageFilter() ?: Filter.defaultFilter()

        readers.add(recordingDataReader)

        val callRecordTree = CallRecordTreeBuilder(recordingDataReader)
            .setReadInfinitely(true)
            .setIndexSupplier { index }
            .build()
        CloseReaderOnExitHook.add(Pair(readerDirectory, callRecordTree))

        return callRecordTree
    }

    private object CloseReaderOnExitHook {
        private var readers = mutableListOf<Pair<Path, CallRecordTree>>()

        init {
            Runtime.getRuntime().addShutdownHook(Thread { runHooks() })
        }

        @Synchronized
        fun add(readerEntry: Pair<Path, CallRecordTree>) {
            readers.add(readerEntry)
        }

        @Synchronized
        fun runHooks() {
            readers.forEach {
                try {
                    it.second.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    FileUtil.deleteDirectory(it.first)
                }
            }
        }
    }
}