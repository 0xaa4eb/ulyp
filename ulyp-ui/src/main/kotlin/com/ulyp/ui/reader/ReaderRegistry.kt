package com.ulyp.ui.reader

import com.ulyp.core.util.FileUtil
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
import java.util.concurrent.ConcurrentHashMap

@Component
class ReaderRegistry(private val filterRegistry: FilterRegistry) {

    private val readersMap = ConcurrentHashMap<Path, RecordingDataReader>()

    @Synchronized
    fun newCallRecordTree(file: File): CallRecordTree? {
        val recordingDataReader = FileRecordingDataReaderBuilder(file).build()

        val rocksdbAvailable = RocksdbChecker.checkRocksdbAvailable()
        val readerDirectory = Files.createTempDirectory("ulyp.Reader")
        val index: Index = if (rocksdbAvailable.value()) {
            RocksdbIndex(readerDirectory)
        } else {
            InMemoryIndex()
        }

        readersMap[file.toPath().toAbsolutePath()] = recordingDataReader

        val callRecordTree = CallRecordTreeBuilder(recordingDataReader)
            .setReadInfinitely(false)
            .setIndexSupplier { index }
            .build()
        CloseReaderOnExitHook.add(Pair(readerDirectory, callRecordTree))

        return callRecordTree
    }

    fun getByFile(file: File): RecordingDataReader? {
        return readersMap[file.toPath().toAbsolutePath()]
    }

    fun dispose(callTree: CallRecordTree) {
        CloseReaderOnExitHook.remove(callTree)
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
        fun remove(callTree: CallRecordTree) {
            readers.removeIf { it.second == callTree}
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